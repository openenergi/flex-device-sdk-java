/*
 * Copyright (c) 2016 Open Energi. All rights reserved.
 *
 * MIT License Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the ""Software""),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED AS IS, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.openenergi.flex.device;

import com.openenergi.flex.message.Message;
import com.openenergi.flex.message.MessageContext;
import com.openenergi.flex.message.Signal;
import com.openenergi.flex.persistence.MemoryPersister;
import com.openenergi.flex.persistence.Persister;
import com.openenergi.flex.persistence.PersisterFullException;
import com.openenergi.flex.persistence.TokenizedObject;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * A client that implements buffering behavior, prioritizing the transmission of
 * certain messages (i.e. FFR-related) when connection is recovered.
 *
 * By default it buffers messages in memory during outages - this can be changed by
 * selecting a different persister in the constructor.
 */
public class RetryingClient implements Client{
    private static AtomicLong backoffExpiration = new AtomicLong();
    private static AtomicLong retryPeriod = new AtomicLong(2000L);
    private static Long retryIncrement = 2000L;
    private static final Long retryMax = 60000L;
    private BufferDrainer drainer;
    private Consumer<MessageContext> callback;

    Persister persister;
    Prioritizer prioritizer;
    Client client;

    private class BufferDrainer implements Runnable {
        private Persister persister;
        private Client client;
        private AtomicLong sleepUntil = new AtomicLong(0);

        public BufferDrainer(Persister persister, Client client){
            this.persister = persister;
            this.client = client;
        }

        public void setSleepUntil(Long until){
            this.sleepUntil.set(until);
        }

        public void run(){
            while (true){
                if (System.currentTimeMillis() < this.sleepUntil.get()){
                    try {
                        Thread.sleep(1000L);
                        continue;
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                try {
                    TokenizedObject to = this.persister.peekLock();
                    this.client.publish((Message)to.data, new MessageContext(to.token));
                } catch (NoSuchElementException ex){
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e){
                        return;
                    }
                }

            }
        }
    }

    public RetryingClient(String hubUrl, String deviceId, String deviceKey) {
        this(hubUrl, deviceId, deviceKey, 10000, BasicClient.Protocol.AMQPS);
    }

    public RetryingClient(String hubUrl, String deviceId, String deviceKey, BasicClient.Protocol protocol) {
        this(hubUrl, deviceId, deviceKey, 10000, protocol);
    }

    public RetryingClient(String hubUrl, String deviceId, String deviceKey, Integer bufferSize, BasicClient.Protocol protocol){
        this.client = new BasicClient(hubUrl, deviceId, deviceKey, protocol);
        this.prioritizer = new FFRPrioritizer();
        this.persister = new MemoryPersister(bufferSize);
        this.setPublishCallback();
        this.drainer = new BufferDrainer(this.persister, this.client);
        (new Thread(this.drainer)).start();
    }

    public RetryingClient(Client client, Persister persister){
        this(client, persister, new FFRPrioritizer());
    }

    public RetryingClient(Client client, Persister persister, Prioritizer prioritizer){
        this.client = client;
        this.persister = persister;
        this.prioritizer = prioritizer;
        this.setPublishCallback();
        this.drainer = new BufferDrainer(this.persister, this.client);
        (new Thread(this.drainer)).start();
    }

    private void setPublishCallback(){
        this.client.onPublish((MessageContext ctx) -> {
            Long token;
            token = (Long) ctx.getData();
            switch (ctx.getStatus()){
                case HUB_OR_DEVICE_ID_NOT_FOUND:
                case BAD_FORMAT:
                case MESSAGE_EXPIRED:
                case PRECONDITION_FAILED:
                case REQUEST_ENTITY_TOO_LARGE:
                case UNAUTHORIZED:
                    //not retriable
                    //TODO(mbironneau): log error and/or throw exception
                    resetRetryInterval();
                    if (this.callback != null) this.callback.accept(ctx);
                    this.persister.delete(token);
                    return;
                case OK:
                    //great
                    resetRetryInterval();
                    if (this.callback != null) this.callback.accept(ctx);
                    this.persister.delete(token);
                    return;
                case THROTTLED:
                case SERVER_BUSY:
                case INTERNAL_SERVER_ERROR:
                case TOO_MANY_DEVICES:
                    //retriable - release the message for retrying
                    backOff();
                    this.persister.release(token);
                    return;
                default:
                    //TODO(mbironneau): log
                    //we don't know what to do as this status
                    //code is unexpected. To prevent the persister
                    //from filling up if messages are going through,
                    //delete the message from the persister.
                    this.persister.delete(token);

            }
        });
    }

    /**
     * If there is no active backoff, create one and increment the retry interval.
     */
    private void backOff(){
        if (RetryingClient.backoffExpiration.get() < System.currentTimeMillis()) {
            RetryingClient.backoffExpiration.set(System.currentTimeMillis() + RetryingClient.retryPeriod.get());
        }
        incrementRetryInterval();
        this.drainer.setSleepUntil(RetryingClient.backoffExpiration.get());
    }

    /**
     * Increment the retry interval by the retry increment and cap this to the max retry interval.
     */
    private static void incrementRetryInterval(){
        Long currentInterval = RetryingClient.retryPeriod.get();
        if (currentInterval < RetryingClient.retryMax){
            currentInterval += RetryingClient.retryIncrement;
            if (currentInterval > RetryingClient.retryMax) currentInterval = RetryingClient.retryMax;
        }
        RetryingClient.retryPeriod.set(currentInterval);
    }

    /**
     * Reset the retry period to its minimum value (the retry increment).
     */
    private static void resetRetryInterval(){
        RetryingClient.retryPeriod.set(RetryingClient.retryIncrement);
    }





    @Override
    public void connect() throws IOException {
        this.client.connect();
    }

    /**
     * Disconnects from the IotHub. Idempotent.
     */
    @Override
    public void disconnect() {
       this.client.disconnect();
    }

    @Override
    public void publish(Message msg) {
        Long token = -1L;

        try {
            token = this.persister.put(msg, this.prioritizer.score(msg), true);
        } catch (PersisterFullException e) {
            e.printStackTrace(); //TODO(mbironneau): log
        }

        if (System.currentTimeMillis() >= RetryingClient.backoffExpiration.get()) {
            //only publish the message if we are not backing off
            this.client.publish(msg, new MessageContext(token));
        }

    }

    /**
     * Attempt to publish message and return token to persisted message. This token can
     * be used to retrieve the message from the client's Persister.
     * @param msg Message to publish
     * @return Token to persisted message.
     */
    public Long publishAndGetToken(Message msg){
        Long token = -1L;

        try {
            token = this.persister.put(msg, this.prioritizer.score(msg), true);
        } catch (PersisterFullException e) {
            e.printStackTrace(); //TODO(mbironneau): log
        }

        if (System.currentTimeMillis() >= RetryingClient.backoffExpiration.get()) {
            //only publish the message if we are not backing off
            this.client.publish(msg, new MessageContext(token));
        }

        return token;
    }

    @Override
    public void publish(Message msg, MessageContext ctx) {
        System.out.println("Not implemented RetryingClient.publish");
    }

    @Override
    public void onPublish(Consumer<MessageContext> callback) {
        this.callback = callback;
    }

    @Override
    public void onSignal(Consumer<Signal<?>> sig) {
        this.client.onSignal(sig);
    }

    @Override
    public void disableSubscription() {
        this.client.disableSubscription();
    }
}
