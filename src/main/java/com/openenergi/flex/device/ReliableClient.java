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
import com.openenergi.flex.persistence.Persister;

import java.io.IOException;
import java.util.function.Consumer;

/**
 *
 */
public class ReliableClient implements Client{
    Persister persister;
    Prioritizer prioritizer;
    BasicClient client;

    public ReliableClient(String hubUrl, String deviceId, String deviceKey) {
        this.client = new BasicClient(hubUrl, deviceId, deviceKey);
    }

    @Override
    public void connect() throws IOException {

    }

    @Override
    public void publish(Message msg) {

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
