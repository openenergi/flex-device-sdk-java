package com.openenergi.flex.device;

import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.openenergi.flex.message.MessageContext;
import com.openenergi.flex.message.Reading;
import com.openenergi.flex.persistence.MemoryPersister;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;


public class ReliableClientTest {
    @Test
    public void testWithoutFailures() {
        MemoryPersister p = new MemoryPersister(10);
        RetryingClient client = new RetryingClient(new FailingClient(), p);
        Reading e = new Reading.Builder()
                .withValue(1.23)
                .withEntity("l1")
                .atTime(12345L)
                .withCustomType("something").build();
        AtomicInteger publishedCount = new AtomicInteger(2);
        client.onPublish((MessageContext ctx) -> {
            Integer published = publishedCount.decrementAndGet();
            if (published < 0) fail("Should not publish message more than once");
            if (published == 0) System.out.println("All published");
        });

        client.publish(e);
        client.publish(e);
        assertEquals(2L, p.counter.get()); //check that at some point, messages were persisted
    }

    @Test
    public void testWithRetry() {
        MemoryPersister p = new MemoryPersister(10);
        FailingClient mock = new FailingClient();
        RetryingClient client = new RetryingClient(mock, p);
        Reading e = new Reading.Builder()
                .withValue(1.23)
                .withEntity("l1")
                .atTime(12345L)
                .withCustomType("something").build();

        client.onPublish((MessageContext ctx) -> {
            //fail("Should not get called");
            if (ctx.getStatus() != IotHubStatusCode.OK) fail("Incorrect status code of second message");
            System.out.println("Message published after retry");
        });


        mock.setPropagation(false);
        Long token = client.publishAndGetToken(e); //will not trigger the callback as propagation is disabled


        try {
            Thread.sleep(1000L);

        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        assertNotNull(p.getByToken(token)); //check message has been persisted

        mock.setPropagation(true);
        mock.publishWithStatusCode(e, new MessageContext(token), IotHubStatusCode.SERVER_BUSY);
        try {
            Thread.sleep(2000L);

        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        assertEquals(1L, p.counter.get()); //double-check that at some point, messages were persisted
    }


}
