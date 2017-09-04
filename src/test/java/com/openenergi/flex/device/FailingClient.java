package com.openenergi.flex.device;

import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.openenergi.flex.message.Message;
import com.openenergi.flex.message.MessageContext;
import com.openenergi.flex.message.Signal;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * An asynchronous mock client that fails (when asked nicely). Message publication will be delayed
 * by a small random amount to test concurrency.
 *
 * Only IotHubStatusCode "OK" is taken to mean "published" - the rest are interpreted as various errors.
 */
public class FailingClient implements Client {
    private Consumer<MessageContext> msgCallback;
    private boolean silent = false;
    public List<Message> published = new ArrayList<Message>();

    @Override
    public void connect() throws IOException {}

    @Override
    public void disconnect(){}

    @Override
    public void publish(Message msg) {
        this.publish(msg, new MessageContext(1L));
    }

    @Override
    public void publish(Message msg, MessageContext ctx) {
        this.publishWithStatusCode(msg, ctx, IotHubStatusCode.OK);
    }

    /**
     * This means that no acknowledgement of message will ever be sent. Use to
     * mock scenarios where a non-standard return code is required by surrounding
     * with setPropagation() calls.
     */
    public void setPropagation(boolean propagate){
        this.silent = !propagate;
    }

    public void publishWithStatusCode(Message msg, IotHubStatusCode code){
        this.publishWithStatusCode(msg, new MessageContext(1L), code);
    }

    public void publishWithStatusCode(Message msg, MessageContext ctx, IotHubStatusCode code){
        Runnable publishTask = () -> {
            Random r = new Random();

            try {
                Thread.sleep(new Long((long)r.nextDouble()*1000L));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ctx.setStatus(code);

            if (code == IotHubStatusCode.OK) this.published.add(msg);

            if (!this.silent) this.msgCallback.accept(ctx);
        };

        publishTask.run();
    }

    @Override
    public void onPublish(Consumer<MessageContext> callback) {
        this.msgCallback = callback;
    }

    @Override
    public void onSignal(Consumer<Signal<?>> sig) {
        return;
    }

    @Override
    public void disableSubscription() {
        return;
    }
}
