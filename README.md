# Flex Device SDK for java

The Device SDK allows field devices to send Dynamic Demand data to the Open Energi cloud (Flex) and subscribe to Portfolio Management signals.

## Requirements

The SDK requires Java 8. If you need support for a lower version please contact us and we'll share some code that works for Java 4+ but doesn't support schedule messages.

## Installation

`pip install oe-flex-sdk`.

## Usage

### Connecting to the Message Broker

Given OE's Hub URL, a Device Id and SAS token:

```java
import com.openenergi.flex.device.Client;

public class HelloFlex {
	
	public static void main(string[] args) {
		client = Client("<Hub URL>", "<Device Id>", "<SAS Token>");
		
		client.connect(); 	
		
		if (!client.ping()) System.out.println("Failed to send 'connected' message to broker"); 
		
	}
	
} 

```

### Sending a Message

Refer to *Message.md* for details on the different message types.

**Reading**

```java
import com.openenergi.flex.message.Reading;

msg = Reading()
		.setType(Reading.POWER)
		.setEntity("l1234")
		.setValue(13.4);

msg_id = client.publish(msg);  # returns the message Id, which can be used to track acknowledgements
```

Even though the example above uses a reading type from an enumeration, any string less than 64 characters in length can be passed into the `type` method. Note that types are not case-sensitive and will be lowercased during ingestion.

Note that even if the `publish` method returns successfully, the message is not guaranteed to have been delivered. To be certain you need to use a callback to correlate acknowledged messages from the broker with published messages from your device - see "Acknowledgement" below.

**Event**

```java
import com.openenergi.flex.message.Event;

msg = Event()
		.setType("state-of-charge")
		.setLevel(Event.WARN)
		.setEntity("s12")
		.setValue("State of charge below 10% for the last 5 minutes");

msg_id = client.publish(msg);  # returns the message Id, which can be used to track acknowledgements
```

Note that although the above example uses a custom event type, the `EventType` class contains the FFR event types documented in *Message.md*.

Note also that even if the `publish` method returns successfully, the message is not guaranteed to have been delivered. To be certain you need to use a callback to correlate acknowledged messages from the broker with published messages from your device - see "Acknowledgement" below.

**Schedule**

*Note yet implemented - coming soon*

### Receiving Acknowledgements

To receive an acknowledgement when your message has been successfully received, use `onPublish()` method of the client to set a callback. 

```java
import com.openenergi.flex.message.Message;

client.onPublish((Message msg) -> System.out.println("Message with Id " + msg.id.toString() + " published!"));
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

Devices are by default subscribed to Portfolio Management signals. These are further documented in *Message.md*. There are two callbacks: one for signal messages and one for schedule signal messages.

```java
import com.openenergi.flex.message.Signal;
import com.openenergi.flex.message.ScheduleSignal;

client.onSignal((Signal signal) -> System.out.println(signal));
client.onScheduleSignal((ScheduleSignal signal) -> System.out.println(signal.schedule.getCurrentValue()));
```

**Disabling message subscription**

If you do not want to subscribe to cloud-to-device messages, you should use the `disableSubscription()` method. It can later be re-enalbed using `enableSubscription()`.

```java
import com.openenergi.flex.device.Client;
    
client = Client("<Hub URL", "<Device Id>", "<SAS Token>");
   
client.disableSubscription();
connect();
```