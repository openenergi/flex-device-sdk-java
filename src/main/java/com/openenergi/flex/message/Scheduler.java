package com.openenergi.flex.message;

import sun.nio.ch.ThreadPool;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * This class accepts a Schedulable and invokes a callback every time that the value changes with the targeted entity/ies,
 * the message type, and the new values.
 *
 * At the moment, it does *not* deal with persisting the schedulable, so in the event of a crash, the signal may NOT
 * be (completely) invoked. This is a to-do in the same category as persistence for the RetryingClient.
 */
public final class Scheduler {

    private final static ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(5, new ThreadPoolExecutor.CallerRunsPolicy());

    private static class ScheduleInvocation implements Runnable {
        private Consumer<SignalCallbackItem> callback;
        private Signal signal;
        private ScheduledExecutorService scheduler;

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
            Double currentValue = signal.getCurrentValue();
            scheduleNextRun();
            if (currentValue != null){
                try {
                    callback.accept(new SignalCallbackItem(signal.entities, signal.getType(), currentValue));
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

        }

        /**
         * Schedules the next invocation - or just returns if the signal has ended.
         */
        private void scheduleNextRun(){
            LocalDateTime nextChange = signal.getNextChange();
            if (nextChange != null){
                try {
                    Long delay = nextChange.toInstant(ZoneOffset.UTC).toEpochMilli() - LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
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

    /**
     * Schedules the given signal for execution by invoking the callback whenever the signal changes with the new value.
     * @param signal The signal to schedule for execution.
     * @param callback The callback to call with the new value.
     */
    public static void accept(Signal signal, Consumer<SignalCallbackItem> callback){


        //Step 1: Invoke the callback for the current value of the signal
        //assuming that the current date is greater than the start of the
        //first signal item
        Double currentValue = signal.getCurrentValue();

        if (currentValue != null){
            callback.accept(new SignalCallbackItem(signal.entities, signal.getType(), currentValue));
        }


        //Step 2: Schedule the next point. We're assuming that it won't be too close to the current
        //one as we are essentially scheduling them one by one, so any delay will compound.
        //A better way to do it would be to schedule multiple changes at once. This is another
        //item for future work.
        if (signal.getNextChange() == null){
            //callback.accept(new SignalCallbackItem(null, SignalCallbackItem.END_OF_SIGNAL, null));
            return;
        }

        ScheduleInvocation invocations = new ScheduleInvocation(signal, Scheduler.scheduler, callback);
        invocations.scheduleNextRun();
    }


}
