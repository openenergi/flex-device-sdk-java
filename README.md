# Flex Device SDK for Java
![Travis Badge](https://travis-ci.org/openenergi/flex-device-sdk-java.svg?branch=master)


The Device SDK allows field devices to send Dynamic Demand data to the Open Energi cloud (Flex) and subscribe to Portfolio Management signals.

## Requirements

The SDK requires Java 8. If you need support for a lower version please contact us and we'll share some code that works for Java 4+ but doesn't support schedule messages.

## Installation

Download a JAR from the releases page. 

**Maven**

Starting with version 0.2 you will be able to download the JAR from Maven central.

```
	<dependency>
  		<groupId>com.openenergi.flex</groupId>
  		<artifactId>flex-device-sdk-java</artifactId>
  		<version>0.1.0</version>
  	</dependency>
```

## Usage

### Connecting to the Message Broker

Given OE's Hub URL, a Device Id and Device Key:

```java
import com.openenergi.flex.device.Client;

client = Client("<Hub URL>", "<Device Id>", "<Device Key>");	
client.connect(); 	
```

### Sending a Message

Refer to [Message Format Specification](https://github.com/openenergi/flex-device-sdk-java/blob/master/Messages.md) for details on the different message types.

**Reading**

```java
import com.openenergi.flex.message.Reading;


Reading msg = new Reading()
		.setType(Reading.Types.POWER)
		.setEntity("l1234")
		.setValue(13.4);

client.publish(msg);
```

Even though the example above uses a reading type from an enumeration, any string less than 64 characters in length can be passed into the `type` method. Note that types are not case-sensitive and will be lowercased during ingestion.

Note that even if the `publish` method returns successfully, the message is not guaranteed to have been delivered. To be certain you need to use a callback to correlate acknowledged messages from the broker with published messages from your device - see "Acknowledgement" below.

**Event**

```java
import com.openenergi.flex.message.Event;

Event msg = new Event()
		.setType("state-of-charge")
		.setLevel(Event.Levels.WARN)
		.setEntity("s12")
		.setValue("State of charge below 10% for the last 5 minutes");

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
ctx.setData(123); //Message Id meaningful to application - can be any object

client.onPublish((MessageContext ctx) -> System.out.println("Message with Id " + ctx.getData().toString() + " published!"));
```

### Overriding the Message Timestamp

By default, the `timestamp` field of the message will be set to the current system time when the message constructor (eg. `Reading()`) is invoked. You can override this:

```java
import java.time.LocalDateTime;

msg = Reading().setTimestamp(LocalDateTime.now());
```
    
### Setting the `created_at` field:

By default the `created_at` field is not populated. To set the `created_at` field using a DateTime:

```java
import java.time.ZonedDateTime;
import java.time.ZoneOffset;

msg = Event().setCreatedAt(ZonedDateTime.now(ZoneOffset.UTC));
```

## Receiving Messages

Devices are by default subscribed to Portfolio Management signals. These are further documented in *Message.md*. There is only one callback for point and schedule messages.

```java
import com.openenergi.flex.message.Signal;
import com.openenergi.flex.message.ScheduleSignal;

client.onSignal((Signal signal) -> System.out.println(signal));
```

More documentation coming soon on how to get the current value of a signal (or the time at which it will next change) - for now we suggest having a look at the javadoc for `Signal`. 

**Disabling message subscription**

If you do not want to subscribe to cloud-to-device messages, you should use the `disableSubscription()` method. It can later be re-enabled using `enableSubscription()`.

```java
import com.openenergi.flex.device.Client;
    
client = Client("<Hub URL", "<Device Id>", "<Device Key>");
   
client.disableSubscription();
connect();
```