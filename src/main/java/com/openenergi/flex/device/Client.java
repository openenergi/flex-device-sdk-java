package com.openenergi.flex.device;

import com.openenergi.flex.message.Message;
import com.openenergi.flex.message.MessageContext;
import com.openenergi.flex.message.Signal;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Created by mbironneau on 08/07/2016.
 */
public interface Client {
    void connect() throws IOException;
    void disconnect();
    void publish(Message msg);
    void publish(Message msg, MessageContext ctx);
    void onPublish(Consumer<MessageContext> callback);
    void onSignal(Consumer<Signal<?>> callback);
    void disableSubscription();
}
