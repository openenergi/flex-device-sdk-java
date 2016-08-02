package com.openenergi.flex.message;

import org.junit.Test;
import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
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
        LocalDateTime now = LocalDateTime.now();
        Long nowMillis = now.toInstant(ZoneOffset.UTC).toEpochMilli();
        CountDownLatch lock = new CountDownLatch(2);

        signal.addItem(new SignalPointItem(now, 1.));
        signal.addItem(new SignalPointItem(now.plusSeconds(1), 2.));
        signal.setType("oe-test");
        signal.entities = new ArrayList<String> ();
        signal.entities.add("L1234");

        Scheduler.accept(signal, signalCallbackItem -> {
            System.out.println(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli() - nowMillis); //check how good the scheduling is
            assertEquals("oe-test", signalCallbackItem.getType());
            assertEquals(1, signalCallbackItem.getEntities().size());
            assertEquals("L1234", signalCallbackItem.getEntities().get(0));
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

}
