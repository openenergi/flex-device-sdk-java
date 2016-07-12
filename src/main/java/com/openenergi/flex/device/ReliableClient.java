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

import java.io.IOException;
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
    Persister persister;
    Prioritizer prioritizer;
    BasicClient client;

    public ReliableClient(String hubUrl, String deviceId, String deviceKey) {
        this.client = new BasicClient(hubUrl, deviceId, deviceKey);
        this.prioritizer = new FFRPrioritizer();
        this.persister = new MemoryPersister(10000);
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
                    this.persister.delete(token);
                    return;
                case OK:
                    //great
                    this.persister.delete(token);
                    return;
                case THROTTLED:
                case SERVER_BUSY:
                case INTERNAL_SERVER_ERROR:
                case TOO_MANY_DEVICES:
                    //retriable - release the message for retrying
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
    public void onPublish(Consumer<MessageContext> ctx) {

    }

    @Override
    public void onSignal(Consumer<Signal<?>> sig) {

    }

    @Override
    public void disableSubscription() {

    }
}
