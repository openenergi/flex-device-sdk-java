package com.openenergi.flex.message;

import org.junit.Test;
import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by mbironneau on 02/08/2016.
 */

public class SchedulerTest {
    static Double latestValue;
    //static Boolean ended = false;

    @Test
    public void testSchedulingSingle(){
        Signal<SignalPointItem> signal = new Signal<SignalPointItem>();
        ZonedDateTime now = ZonedDateTime.now();
        Long nowMillis = now.toInstant().toEpochMilli();
        CountDownLatch lock = new CountDownLatch(2);
        signal.setGeneratedAt(now);
        signal.addItem(new SignalPointItem(now, 1.));
        signal.addItem(new SignalPointItem(now.plusSeconds(1), 2.));
        signal.setType("oe-test");
        List<String> entities = new ArrayList<String> ();
        entities.add("L1234");
        signal.setEntities(entities);

        Scheduler.accept(signal, signalCallbackItem -> {
            System.out.println(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli() - nowMillis); //check how good the scheduling is
            assertEquals("oe-test", signalCallbackItem.getType());
            assertEquals("L1234", signalCallbackItem.getEntity());
            if (signalCallbackItem.getType() != SignalCallbackItem.END_OF_SIGNAL){
                latestValue = signalCallbackItem.getValue();
                System.out.println("VALUE: " + String.valueOf(latestValue));
            } else {
                //ended = true;
            }

            lock.countDown();
        });

        try {
            lock.await(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //assertTrue(ended);
        assertNotNull(latestValue);
        assertEquals(new Double(2), latestValue);

    }

    @Test
    public void testSchedulingBatch(){
        Signal<SignalBatchList> signal = new Signal<>();
        ZonedDateTime now = ZonedDateTime.now();
        Long nowMillis = now.toInstant().toEpochMilli();
        CountDownLatch lock = new CountDownLatch(2);
        signal.setGeneratedAt(now);
        List<SignalBatchListItem> items = new ArrayList<>();
        items.add(new SignalBatchListItem("oe-add", 0.5));
        SignalBatchList list = new SignalBatchList(now.plusSeconds(1), items);
        //list.setStartAt(now.plusSeconds(1));
        signal.addItem(list);
        signal.setType("oe-vars");
        List<String> entities = new ArrayList<String> ();
        entities.add("L1234");
        signal.setEntities(entities);

        Scheduler.accept(signal, signalCallbackItem -> {
            System.out.println(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli() - nowMillis); //check how good the scheduling is
            assertEquals("oe-add", signalCallbackItem.getType());
            assertEquals("L1234", signalCallbackItem.getEntity());
            assertEquals((Double) 0.5, signalCallbackItem.getValue());
            if (signalCallbackItem.getType() != SignalCallbackItem.END_OF_SIGNAL){
                latestValue = signalCallbackItem.getValue();
                System.out.println("VALUE: " + String.valueOf(latestValue));
            } else {
                //ended = true;
            }

            lock.countDown();
        });

        try {
            lock.await(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
