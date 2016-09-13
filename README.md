# Flex Device SDK for Java
![Travis Badge](https://travis-ci.org/openenergi/flex-device-sdk-java.svg?branch=master)


The Device SDK allows field devices to send Dynamic Demand data to the Open Energi cloud (Flex) and subscribe to Portfolio Management signals.

## Requirements

The SDK requires Java 8.

## Getting Started

Download a JAR from the releases page. 

## Usage

### Clients

There are two different clients: the `BasicClient` and `RetryingClient`. The `BasicClient` client does not buffer messages - in the event of connection loss or server error, the messages will be lost unless there is application-level logic to do that. The `RetryingClient` can buffer and possibly persist messages so that they are not lost.

The usage of both clients is the same, but the `RetryingClient` has more options in the constructor:

* The `Persister` specifies which type of persistence to use for buffered messages. At the moment only in-memory persistence is implemented but we will implement on-disk persistence soon. If using the `MemoryPersister` the capacity can be specified, in maximum number of messages (default: 10000).
* The `Prioritizer` informs eviction policy for buffered messages and also which messages get sent first once connections get restored (the higher priority items get sent first). The default, `FFRPrioritizer`, gives higher priority to FFR-related messages (eg. availability) and newer messages.

*The RetryingClient does not implement message-context-based publishing at the moment as retry logic is handled within the client itself.*

### Connecting to the Message Broker

Given OE's Hub URL, a Device Id and Device Key:

```java
import com.openenergi.flex.device.BasicClient;

client = BasicClient("<Hub URL>", "<Device Id>", "<Device Key>");	
client.connect(); 	
```

### Sending a Message

Refer to [Message Format Specification](https://github.com/openenergi/flex-device-sdk-java/blob/master/Messages.md) for details on the different message types.

**Reading**

```java
import com.openenergi.flex.message.Reading;


Reading msg = new Reading.Builder()
		.withType(Reading.Types.POWER)
		.withEntity("l1234")
		.withValue(13.4)
		.build();

client.publish(msg);
```

Even though the example above uses a reading type from an enumeration, any string less than 64 characters in length can be passed into the `type` method. Note that types are not case-sensitive and will be lowercased during ingestion.

Note that even if the `publish` method returns successfully, the message is not guaranteed to have been delivered. To be certain you need to use a callback to correlate acknowledged messages from the broker with published messages from your device - see "Acknowledgement" below.

*If using the `RetryingClient`, the only exceptions you will receive are fatal ones - retriable exceptions will be retried.*

**Event**

```java
import com.openenergi.flex.message.Event;

Event msg = new Event.Builder()
		.withCustomType("state-of-charge")
		.withLevel(Event.Levels.WARN)
		.withEntity("s12")
		.withValue("State of charge below 10% for the last 5 minutes");

client.publish(msg);
```

Note that although the above example uses a custom event type, the `Event` class contains constants for the FFR event types documented in *Message.md*.

Note also that even if the `publish` method returns successfully, the message is not guaranteed to have been delivered. To be certain you need to use a callback to correlate acknowledged messages from the broker with published messages from your device - see "Acknowledgement" below.

**Schedule**

The messages above specify an instantaneous change of a metric. More generally, you may need to report that a value will change on a recurring, predictable basis. For example, you may want to withdraw an asset from service during certain periods.

A schedule allows you to achieve this. More documentation coming soon - for now we suggest looking at the javadoc for `Schedule`, `ScheduleItem`, `Span` and `RecurringSpan`.

### Receiving Acknowledgements

To receive an acknowledgement when your message has been successfully received, use `onPublish()` method of the client to set a callback. 

```java
import com.openenergi.flex.message.Message;
import com.openenergi.flex.message.MessageContext;

MessageContext ctx = new MessageContext();
ctx.setData(123); //Message context (eg. Id) meaningful to application - can be any object

client.onPublish((MessageContext ctx) -> System.out.println("Message with Id " + ctx.getData().toString() + " published!"));
```

### Overriding the Message Timestamp

By default, the `timestamp` field of the message will be set to the current system time when the message constructor (eg. `Reading()`) is invoked. You can override this:

```java
import java.time.LocalDateTime;

Reading msg = new Reading.Builder().atTime(System.currentTimeMillis()).build();
msg.setTimestamp(LocalDateTime.now()); //equivalent
```
    
### Setting the `created_at` field:

By default the `created_at` field is not populated. To set the `created_at` field using a DateTime:

```java
import java.time.ZonedDateTime;
import java.time.ZoneOffset;

Event msg = new Event();
msg.setCreatedAt(ZonedDateTime.now(ZoneOffset.UTC));
```

## Receiving Messages

Devices are by default subscribed to Portfolio Management signals. These are further documented in *Message.md*. There is only one callback for point and schedule messages.

```java
import com.openenergi.flex.message.Signal;
import com.openenergi.flex.message.ScheduleSignal;

client.onSignal((Signal signal) -> System.out.println(signal));
```

A Signal's current value can be obtained by calling `signal.getCurrentValue()`. For scheduling purposes, it is also possible to determine the time at which the signal will next change by calling `signal.getNextChange()`.

**Disabling message subscription**

If you do not want to subscribe to cloud-to-device messages, you should use the `disableSubscription()` method. It can later be re-enabled using `enableSubscription()`.

```java
import com.openenergi.flex.device.BasicClient;
    
client = Client("<Hub URL", "<Device Id>", "<Device Key>");
   
client.disableSubscription();
connect();
```

## Implementing Signal Scheduling

Cloud-to-device messages (Signals) are used to enact behavior from a gateway device such as modifying the effective Grid Frequency (eg. see message.md for details of the `oe-add` and `oe-multiply` variables). 

In order to process Signals correctly, it is necessary to implement the following behavior:

- Each Signal Item must be scheduled to take effect at the time specified by its `start_at` property
- If another Signal arrives for the same entity/variable type, it should take precedence over any previous signals if and only if its `generated_at` property is greater or equal to the others

To aid in implementing this behavior, the SDK comes with a singleton `Scheduler` class that has static methods to deal with any received signals. It will schedule the signal items for later execution and deal with overwriting signals with later ones according to the bullet points above. It will invoke a callback whenever a signal item should be executed, which leaves the implementer to glue the logic in to effect the change, eg. changing a parameter or writing to a register.

```java
client.onSignal((Signal signal) -> Scheduler.accept(signal, (SignalCallbackItem s) -> {
	System.out.println("Change variable " + s.getType() + " to value " + s.getValue() + " for entity " + s.getEntity());
}));

```
