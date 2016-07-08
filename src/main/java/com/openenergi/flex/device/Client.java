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
    void publish(Message msg);
    void onPublish(Consumer<MessageContext> ctx);
    void onSignal(Consumer<Signal<?>> sig);
    void disableSubscription();
}
