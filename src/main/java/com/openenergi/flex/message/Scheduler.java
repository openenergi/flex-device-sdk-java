package com.openenergi.flex.message;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * This class accepts a Schedulable and invokes a callback every time that the value changes with the targeted entity/ies,
 * the message type, and the new values.
 *
 * At the moment, it does *not* deal with persisting the schedulable, so in the event of a crash, the signal may NOT
 * be (completely) invoked. This is a to-do in the same category as persistence for the RetryingClient.
 *
 * Another work item is to coalesce signals for the same type/entities. That will be more work...
 */
public final class Scheduler {
    private static final Logger logger = LoggerFactory.getLogger(Scheduler.class);
    private final static ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(5, new ThreadPoolExecutor.CallerRunsPolicy());
    private final static ConcurrentHashMap<String, ZonedDateTime> latestSignals = new ConcurrentHashMap<String, ZonedDateTime>(1000);

    private static class ScheduleInvocation implements Runnable {
        private Consumer<SignalCallbackItem> callback;
        private Signal signal;
        private ScheduledExecutorService scheduler;
        private SignalElement nextInvocation;

        public ScheduleInvocation(Signal signal, ScheduledExecutorService scheduler, Consumer<SignalCallbackItem> callback){
            this.callback = callback;
            this.signal = signal;
            this.scheduler = scheduler;
        }

        @Override
        /**
         * Calls back with the current value of the signal and schedules the next invocation.
         */
        public void run() {
            logger.trace("Running invocation");
            SignalElement currentValues;
            if (this.nextInvocation == null){
                logger.trace("Next invocation is null - getting current value");
                currentValues = signal.getCurrentValues();
            } else {
                currentValues = this.nextInvocation;
            }

            if (currentValues != null){
                try {
                    signal.getEntities().forEach(entity -> {
                        ZonedDateTime latest = getLatestReceivedSignal((String) entity, signal.getType());

                        if (latest == null || !(latest.isAfter(signal.getGeneratedAt()))){
                            logger.trace("Executing invocation with start date " + currentValues.getStartAt().format(DateTimeFormatter.ISO_DATE_TIME));
                            currentValues.getValues().forEach((SignalBatchListItem sbi) -> {
                                String type;
                                //inherit the list item type from the signal's type for point/schedule signals.
                                if (sbi.getVariable()==null){
                                    type = signal.getType();
                                } else {
                                    type = sbi.getVariable();
                                }
                                callback.accept(new SignalCallbackItem((String) entity, type, sbi.getValue()));
                            });

                        }
                    });

                } catch (Exception e){
                    e.printStackTrace();
                }
            }
            scheduleNextRun();
        }

        private Long roundToTenthsOfASecond(Long millis){
            double m = Math.ceil(((double) millis)/10);
            return (long) m*10;
        }

        /**
         * Schedules the next invocation - or just returns if the signal has ended.
         */
        private void scheduleNextRun(){
            SignalElement nextChange = signal.getNextChange();
            logger.trace("Next invocation at " + nextChange.getStartAt().format(DateTimeFormatter.ISO_DATE_TIME));
            if (nextChange != null){
                try {
                    Long delay = roundToTenthsOfASecond(nextChange.getStartAt().toInstant().toEpochMilli() - ZonedDateTime.now().toInstant().toEpochMilli());
                    logger.trace("Milliseconds to next invocation " + delay.toString());
                    this.nextInvocation = nextChange;
                    scheduler.schedule(this, delay, TimeUnit.MILLISECONDS);
                } catch (RejectedExecutionException e){
                    //should never get reached as we are using CallerRunsPolicy
                    e.printStackTrace();
                }

            } else {
                //callback.accept(new SignalCallbackItem(null, SignalCallbackItem.END_OF_SIGNAL, null));
            }
        }
    }

    private static ZonedDateTime getLatestReceivedSignal(String entity, String type){
        return latestSignals.get(entity + "::" + type);
    }

    private static void setLatestReceivedSignal(String entity, String type, ZonedDateTime time){
        latestSignals.put(entity + "::" + type, time);
    }

    /**
     * Schedules the given signal for execution by invoking the callback whenever the signal changes with the new value.
     * Note that the scheduler will SORT the signal in start-time order before it begins!
     * @param signal The signal to schedule for execution.
     * @param callback The callback to call with the new value.
     */
    public static void accept(Signal signal, Consumer<SignalCallbackItem> callback) throws IllegalArgumentException {
        validate(signal);
        signal.sort();
        logger.trace("Scheduler accepted signal");
        SignalElement currentValue = signal.getCurrentValues();
        signal.getEntities().forEach(entity -> {
            ZonedDateTime latest = getLatestReceivedSignal((String) entity, signal.getType());
            if (latest == null || latest.isBefore(signal.getGeneratedAt())){
                setLatestReceivedSignal((String) entity, signal.getType(), signal.getGeneratedAt());
                logger.trace("Setting latest received cache for entity " + entity + " and type " + signal.getType() + " to " + signal.getGeneratedAt().format(DateTimeFormatter.ISO_DATE_TIME));
                if (currentValue != null && currentValue.getValues() != null){
                    logger.trace("Executing invocation with start time " + currentValue.getStartAt().format(DateTimeFormatter.ISO_DATE_TIME));
                    currentValue.getValues().forEach((SignalBatchListItem sbi) -> {
                        String type;
                        //inherit the list item type from the signal's type for point/schedule signals.
                        if (sbi.getVariable()==null){
                            type = signal.getType();
                        } else {
                            type = sbi.getVariable();
                        }
                        callback.accept(new SignalCallbackItem((String) entity, type, sbi.getValue()));
                    });
                } else {
                    logger.trace("Current signal value NULL");
                }
            }
        });


        if (signal.getNextChange() == null){
            logger.trace("End of signal - exiting");
            return;
        }
        logger.trace("Scheduling next invocation");
        ScheduleInvocation invocations = new ScheduleInvocation(signal, Scheduler.scheduler, callback);
        invocations.scheduleNextRun();
    }

    private static void validate(Signal signal) throws IllegalArgumentException {
        if (signal.getType() == null || signal.getEntities() == null || signal.getEntities().size() == 0 || signal.getGeneratedAt() == null){
            throw new IllegalArgumentException("Bad signal: Had null type, entities or generated_at");
        }
    }

}
