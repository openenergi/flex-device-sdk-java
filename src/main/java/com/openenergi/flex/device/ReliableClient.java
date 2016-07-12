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
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
public class ReliableClient implements Client{
    private static AtomicLong backoffExpiration = new AtomicLong();
    private static AtomicLong retryPeriod = new AtomicLong(2000L);
    private static Long retryIncrement = 2000L;
    private static final Long retryMax = 60000L;
    Persister persister;
    Prioritizer prioritizer;
    Client client;

    private class BufferDrainer implements Runnable {
        private Persister persister;
        private Client client;
        private AtomicLong sleepUntil;

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
                }

            }
        }
    }

    public ReliableClient(String hubUrl, String deviceId, String deviceKey) {
        this.client = new BasicClient(hubUrl, deviceId, deviceKey);
        this.prioritizer = new FFRPrioritizer();
        this.persister = new MemoryPersister(10000);
        this.setPublishCallback();
        (new Thread(new BufferDrainer(this.persister, this.client))).run();
    }

    public ReliableClient(BasicClient client, Persister persister){
        this.client = client;
        this.persister = persister;
        this.prioritizer = new FFRPrioritizer();
    }

    public ReliableClient(BasicClient client, Persister persister, Prioritizer prioritizer){
        this.client = client;
        this.persister = persister;
        this.prioritizer = prioritizer;
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
                    this.persister.delete(token);
                    return;
                case OK:
                    //great
                    resetRetryInterval();
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
    private static void backOff(){
        if (ReliableClient.backoffExpiration.get() < System.currentTimeMillis()) {
            ReliableClient.backoffExpiration.set(System.currentTimeMillis() + ReliableClient.retryPeriod.get());
        }
        incrementRetryInterval();
    }

    /**
     * Increment the retry interval by the retry increment and cap this to the max retry interval.
     */
    private static void incrementRetryInterval(){
        Long currentInterval = ReliableClient.retryPeriod.get();
        if (currentInterval < ReliableClient.retryMax){
            currentInterval += ReliableClient.retryIncrement;
            if (currentInterval > ReliableClient.retryMax) currentInterval = ReliableClient.retryMax;
        }
        ReliableClient.retryPeriod.set(currentInterval);
    }

    /**
     * Reset the retry period to its minimum value (the retry increment).
     */
    private static void resetRetryInterval(){
        ReliableClient.retryPeriod.set(ReliableClient.retryIncrement);
    }





    @Override
    public void connect() throws IOException {
        this.client.connect();
    }

    @Override
    public void publish(Message msg) {
        Long token = -1L;

        try {
            token = this.persister.put(msg, this.prioritizer.score(msg), true);
        } catch (PersisterFullException e) {
            e.printStackTrace(); //TODO(mbironneau): log
        }

        if (System.currentTimeMillis() >= ReliableClient.backoffExpiration.get()) {
            //only publish the message if we are not backing off
            this.client.publish(msg, new MessageContext(token));
        }

    }

    @Override
    public void publish(Message msg, MessageContext ctx) {
        throw new NotImplementedException();
    }

    @Override
    public void onPublish(Consumer<MessageContext> ctx) {
        throw new NotImplementedException();
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
