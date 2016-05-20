# Message Format Specification

*Version: 1.0.0*

last modified on 2016-05-13

### Versioning

The message format specification follows the conventions of semantic versioning. Namely, 

* The revision is incremented after non-normative mistakes in the documentation are corrected or clarifications made 
* The minor version is incremented after normative backwards-compatible changes are made
* The major version is incremented after normative backwards-incompatible changes are made

For example, a implementation written against version 1.2.3 will continue to work against version 1.2.4 and version 1.3.4, but not version 2.0.0. 

## Intended Audience

This page is intended for system integrators who want to send Dynamic Demand data to Open Energi and/or receive portfolio management signals from Open Energi.

## Scope

This document specifies the format of messages sent *from* field devices to the Open Energi cloud (Flex) and the format of messages sent *to* field devices from Flex. All communication between Flex and field devices follow these formats.

This document also outlines the communication protocols that can be used to send these messages.

## Table of Contents

1. [Security Requirements](#sec)
2. [Serialization](#ser)
3. [Protocol Requirements](#prot)
	1. [AMQP 1.0](#prot-amqp)
	2. [MQTT v3](#prot-mqtt)
	3. [HTTP/1](#prot-http)
4. [Validation and Clock Synchronization](#valid)
5. [Entities](#ent)
6. [Device-to-Cloud Message Types](#mes)
	1. [Readings](#mes-readings)
	2. [Events](#mes-events)
	3. [Schedules](#mes-schedules)
7. [Cloud-to-Device Message Types](#sig)
	1. [Portfolio Management Signals](#sig-basic)
	2. [Schedule-based signals](#sig-schedule)
## <a name="sec"></a>Security Requirements

All connections to Flex must be secured by SSL/TLS.  Supported cipher suites as of April 12, 2016 are listed [here](https://blogs.msdn.microsoft.com/azuresecurity/2016/04/12/azure-cipher-suite-change-removes-rc4-support/). 

## <a name="ser"></a>Serialization

All messages are sent in UTF-8 encoded JSON. A single message is a JSON object. Multiple messages may be sent in batches as a JSON array:


`[{message1}, {message2}, ...]`


## <a name="prot"></a>Protocol Requirements

Messages can be sent via AMQP 1.0, MQTT v3 and HTTP/1. AMQP is also available over WebSockets. Flex uses the Microsoft Azure IoT Hub service as a message broker. Developer documentation for this service can be found [here](https://azure.microsoft.com/en-gb/documentation/articles/iot-hub-devguide/). 


### <a name="prot-amqp"></a>AMQP 1.0

Authentication is done using SASL PLAIN tokens. See [Microsoft's documentation](https://azure.microsoft.com/en-gb/documentation/articles/iot-hub-devguide/) for more details.

A Device Id and SAS token will be provided by Open Energi on request. A unique Device Id and SAS token are required for each logical device. 

The SAS token will be valid for one year.

### <a name="prot-mqtt"></a>MQTT v3

For more information, see [Microsoft's documentation](https://azure.microsoft.com/en-gb/documentation/articles/iot-hub-mqtt-support/) on MQTT support. In particular, note that QoS 2 is not currently supported.

A Device Id and SAS token will be provided by Open Energi on request. A unique Device Id and SAS token are required for each logical device. 

The SAS token will be valid for one year.

Note that messages should be published to the topic `devices/{device_id}/messages/events/`. 

### <a name="prot-http"></a>HTTP/1

See [Microsoft's documentation](https://msdn.microsoft.com/en-gb/library/mt590784.aspx) for more details.

A Device Id and SAS token will be provided by Open Energi on request. A unique Device Id and SAS token are required for each logical device. 

The SAS token will be valid for one year.

Open Energi needs to be able to determine the connectivity of a device at any given time. As HTTP/1 is not a bidirectional protocol it is therefore necessary for the device to perform the following activity (heartbeat) at least once per minute:

* Send a “connected” event with level 0. The value field will be ignored (see below for details)

## <a name="valid"></a>Validation

Several validation rules are applied to readings coming from devices. In most cases, invalid messages will be preserved so they can later be fixed or manually dropped. In the following circumstances, the data cannot be stored and will be dropped immediately during ingestion:

* The message payload is not valid JSON
* A mandatory field is missing or null in the message (see below for details). Open Energi will be notified of such errors and will attempt to notify the implementer.
* A message field value is of the wrong type (see below for details). Open Energi will be notified of such errors and will attempt to notify the implementer.
* The device’s SAS token has expired. When this occurs the connection will be in a bad state and this will be observable to the implementer through protocol-level errors.

Cases in which the data may not be valid but will be stored for later remediation:

* Device attempting to send messages for an entity not associated with the device 
* Data not passing business logic layer validation, such as declared availability being too high
* Data that is too old (see below)


### Clock Synchronization

Open Energi will nominate an NTP server. The implementer must use this server to keep the device clock synchronized at all times when the device has outbound connectivity. The polling interval must not be greater than 10 minutes. 

```
Something about 3G latency
```

## <a name="ent"></a>Entities

All messages are associated with an **entity**. An entity is a logical, stateful thing on whose state metrics are defined and recorded. Certain state transitions may also be recorded as events. Examples of entities are physical assets, meters or sensors.

The list of entities associated to a device and their meaning will be agreed between Open Energi and the integrator beforehand.

## <a name="mes"></a>Message Types

### <a name="mes-readings"></a>Readings

A reading is an instantaneous measurement of a metric associated with an entity. The value of the reading is assumed to hold between the timestamp of the message and the next received timestamp of a readings message of the same type (known as a change-of-value or step-after series). Examples are instantaneous power consumption of an asset or grid frequency recorded from a meter.

Due to the uncertainties associated with clock drift readings that are too old may not be included in Dynamic Demand aggregations (see appendix).

*In the event of conflicting readings (eg. same entity/type/timestamp but different value), the last write wins. If there are conflicting readings in the same batch (for protocols that support message batching), the one ultimately used by the system, if any, is non-deterministic.*

#### Fields

<table>
    <tr>
        <th>Field</th>
        <th>Type</th>
        <th>Description</th>
        <th>Example</th>
    </tr>
    <tr>
        <td>topic</td>
		<td>String. The value should always be "readings"</td>
		<td>Topic to identify the message as a reading</td>
		<td>readings</td>
    </tr>
    <tr>
        <td>entity</td>
		<td>String (max length 10), not null, case-insensitive</td>
		<td>Unique entity code associated with reading</td>
		<td>l1332</td>
    </tr>
    <tr>
        <td>type</td>
		<td>String (max length 64), not null, case-insensitive</td>
		<td>Type of reading</td>
		<td>availability-ffr-high</td>
    </tr>
    <tr>
        <td>timestamp</td>
		<td>integer, not null</td>
		<td>Time reading was recorded, milliseconds since epoch</td>
		<td>1462350193446</td>
    </tr>
    <tr>
        <td>value</td>
		<td>float, not null</td>
		<td>Value of reading</td>
		<td>12.2</td>
    </tr>
    <tr>
        <td>created_at</td>
		<td>String, optional, can be null</td>
		<td>ISO 8601 datetime at which reading was recorded</td>
		<td>2016-12-25T12:00:00.000Z</td>
    </tr>
</table>

*Example reading:*
    
    {
    	"topic": "readings"
    	"entity": "l1234",
    	"type": "power",
    	"timestamp": 1462350193446,
    	"value", 10.1,
    	"created_at": "2015-12-25T16:00:00.00Z"
    }

In order for Open Energi to be able to aggregate the flexible energy provided by the integrator, some special readings need to be provided regularly, and they need to satisfy the below requirements.

#### Power Consumption Readings
The type should be “power”. The value should be in kW. A new reading should be generated whenever the power consumption increases or decreases by more than 5% since its last sent value, or every 12 hours, whichever comes first. 

[Requirements for power metering?]

#### Availability Readings
The type should be one of the following:

* `availability-ffr-low`: The amount of power consumption that is available to be deferred within 2 seconds of `timestamp` and for up to 30 minutes
* `availability-ffr-high`: The amount of power consumption that is available to be brought forward within 2 seconds of `timestamp` and for up to 30 minutes

The value should be in kW.

A new reading should be generated whenever the availability increases or decreases by more than 5% since its last sent value, or every 12 hours, whichever comes first.

[Detailed example of availability calculation?]

#### Response Readings
The type should be one of the following:

* `response-ffr-high`: The amount of power consumption that is currently being brought forward
* `response-ffr-low`: The amount of power consumption that is currently being deferred

The value should be in kW.

A new reading should be generated whenever the response increases or decreases by more than 5% since its last sent value, or every 12 hours, whichever comes first.


#### Process Variables
Implementers are free to choose the type name to be meaningful to them (eg. temperature). The exception is control variable and setpoints, which have special meaning to the system. These should have types:

* `control-variable`: variable that is used to control the asset. Affects whether it is available for Dynamic Demand
* `setpoint`: Setpoint for the control variable
* `setpoint-high`: Upper edge of the setpoint band beyond which the asset will not be available for Dynamic Demand
* `setpoint-low`: Lower edge of the setpoint band beneath which the asset will not be available for Dynamic Demand


### <a name="mes-events"></a>Events

An event is a discrete, instantaneous record of an entity’s state transition. Examples are switch requests or alarm conditions.

**Fields**

<table>
    <tr>
        <th>Field</th>
        <th>Type</th>
        <th>Description</th>
        <th>Example</th>
    </tr>
    <tr>
        <td>topic</td>
		<td>String. The value should always be "events"</td>
		<td>Topic to identify the message as an event</td>
		<td>readings</td>
    </tr>
    <tr>
        <td>entity</td>
		<td>String (max length 10), not null, case-insensitive</td>
		<td>Unique entity code associated with reading</td>
		<td>l1332</td>
    </tr>
    <tr>
        <td>type</td>
		<td>String (max length 64), not null, case-insensitive</td>
		<td>Type of event</td>
		<td>state-of-charge-alert</td>
    </tr>
    <tr>
        <td>timestamp</td>
		<td>integer, not null</td>
		<td>Time event was recorded, milliseconds since epoch</td>
		<td>1462350193446</td>
    </tr>
	<tr>
		<td>level</td>
		<td>Integer, not null</td>
		<td>
			Prioritization/relevance of the event. Affects its retention policy and alerting services. Acceptable levels are:
			<ul>
				<li>0 - DEBUG</li>
				<li>1 - INFO</li>
				<li>2 - WARN</li>
				<li>3 - ERROR</li>
			</ul>
		</td>
		<td>3</td>
	</tr>
    <tr>
        <td>value</td>
		<td>String, optional, can be null</td>
		<td>Optional extra event content</td>
		<td>"State of charge below 10%"</td>
    </tr>
    <tr>
        <td>created_at</td>
		<td>String, optional, can be null</td>
		<td>ISO 8601 datetime at which reading was recorded</td>
		<td>2016-12-25T12:00:00.000Z</td>
    </tr>
</table>

*Example event message:*

    {
    	"topic": "events"
    	"entity": "l1234",
    	"type": "switch-ffr-start",
    	"timestamp": 1462350193446,
    	"value", "-1",
		"level": 1,
    	"created_at": "2015-12-25T16:00:00.00Z"
    }

Use cases for events are different than readings. Events can be used to trigger alerts or send debug information, whereas readings are associated with a continuous metric. 

#### Alerts

Events with `WARN`/`ERROR` level are interpreted as alerts (`ERROR` level is given higher priority). To resolve an alert, subsequently send another message of the same type with a lower level. 

#### Debugging Information

Debugging messages can be sent with `INFO`/`DEBUG` level. The type should have some meaning to the integrator.

#### Switch Requests

The following message properties should be used. The Level should be 1 (INFO):
* When a switch request begins, a message of type “switch-ffr-start” with value “1”/”-1” (high/low) should be sent. 
* When a switch request ends, a message of type “switch-ffr-end” should be sent (with no value).

#### Continuity Events

Continuity events can be used to repudiate data. In the event that an integrator wishes to repudiate data that has previously been sent, there are two options:

* Send correct data by sending readings with the same timestamps and different values
* Send a continuity event

The `value` of a continuity event is the number of seconds, ending at `timestamp`, that **all** readings should be considered invalid. For example, if `timestamp` corresponds to 1:00PM today and the `value` of the continuity event is `1800`, then all readings betweeen 12:30PM and 1:00PM today will be considered invalid by the system on receipt of the continuity event.

Integrators who issue frequent continuity events may see their Dynamic Demand revenue penalised as it affects Open Energi's ability to rely on the data they provide to supply grid balancing services.

### <a name="mes-schedules"></a>Schedules

A schedule is a collection of intervals that define the (string) value of a variable over a period of time. For example, the list of services that an entity is allowed to participate in can be sent via a schedule. The schedule can be repeating. Intervals are specified in [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601#Time_intervals) notation.


**Schedule message fields**

<table>
    <tr>
        <th>Field</th>
        <th>Type</th>
        <th>Description</th>
        <th>Example</th>
    </tr>
    <tr>
        <td>topic</td>
		<td>String. The value should always be "schedules"</td>
		<td>Topic to identify the message as a schedule</td>
		<td>schedules</td>
    </tr>
    <tr>
        <td>entity</td>
		<td>String (max length 10), not null, case-insensitive</td>
		<td>Unique entity code associated with schedule</td>
		<td>l1332</td>
    </tr>
    <tr>
        <td>type</td>
		<td>String (max length 64), not null, case-insensitive</td>
		<td>Type of schedule (name of variable the schedule is associated with)</td>
		<td>services</td>
    </tr>
    <tr>
        <td>schedule</td>
		<td>array of <strong>interval</strong></td>
		<td>Schedule specification</td>
		<td><em>See below</em></td>
    </tr>
</table>

**Interval fields**

<table>
    <tr>
        <th>Field</th>
        <th>Type</th>
        <th>Description</th>
        <th>Example</th>
    </tr>
    <tr>
        <td>span</td>
		<td>String, maybe null or omitted. ISO 8601 interval.</td>
		<td>The start and duration of the interval</td>
		<td>2016-W01-1T00:00:00/P1D</td>
    </tr>
    <tr>
        <td>repeat</td>
		<td>String, maybe null or omitted. ISO 8601 duration.</td>
		<td>The duration between repeats of the interval. Null values are interpreted as non-recurring.</td>
		<td>P1W</td>
    </tr>
    <tr>
        <td>value</td>
		<td>String (max length 512), not null</td>
		<td>Value of the schedule during this interval</td>
		<td>“[\“ffr\”, \“special Monday service\”]”</td>
    </tr>

</table>

In the event that multiple intervals overlap or conflict, the preceding value in the schedule array will take priority. For example, in the below message the last interval in the “schedule” array specifies the default value for “services”, whereas the first interval specifies a list of services that should be applied on Mondays beginning in the first week of 2016.

*Example of a schedule message*:

	{
		"topic": "schedules",
		"entity": "l1009",
		"type": "services",
		"schedule": [{
			"span": "2016-W01-1T00:00:00/P1D",
			"repeat": "P1W",
			"value": “[\“ffr\”, \“special Monday service\”]”
		}, {
			"span": null,
			"repeat": null,
			"value": ["ffr"]
		}]
	}

## <a name="sig"></a>Messages to Devices

To receive messages, devices must communicate via AMQP or MQTT protocols (see above for details). 

### MQTT Settings

See [Microsoft's documentation](https://azure.microsoft.com/en-gb/documentation/articles/iot-hub-mqtt-support/) for more details.
 
Namely, the device should subscribe using `devices/{device_id}/messages/devicebound/#` as the topic filter. This is subject to change.

### <a name="sig-basic"></a>Portfolio Management Signals

Portfolio management signals (i.e. DDv2) can be sent to devices that connect via MQTT or AMQP. A portfolio management signal is a request for a device to modify the state of one or more associated entities in the specified manner. It is a one-time request – for recurring requests see “Schedule signals”.

**Fields**

<table>
    <tr>
        <th>Field</th>
        <th>Type</th>
        <th>Description</th>
        <th>Example</th>
    </tr>
    <tr>
        <td>created_at</td>
		<td>String, not null, ISO 8601 datetime</td>
		<td>System time when signal was generated</td>
		<td>2016-12-25T12:00:00.000Z</td>
    </tr>
    <tr>
        <td>topic</td>
		<td>String. The value should always be "signals"</td>
		<td>Topic to identify the message as a Portfolio Management Signal</td>
		<td>schedules</td>
    </tr>
    <tr>
        <td>entities</td>
		<td>String (max length 10), not null, case-insensitive</td>
		<td>List of unique entity codes of entities targeted by signal</td>
		<td>l1332</td>
    </tr>
    <tr>
        <td>type</td>
		<td>String (max length 64), not null, case-insensitive</td>
		<td>The variable that is targeted by the signal</td>
		<td>oe-add</td>
    </tr>
    <tr>
        <td>signal</td>
		<td>array of <strong>signal points</strong></td>
		<td>Signal specification</td>
		<td><em>See below</em></td>
    </tr>
</table>

**Signal Point Fields**

<table>
    <tr>
        <th>Field</th>
        <th>Type</th>
        <th>Description</th>
        <th>Example</th>
    </tr>
    <tr>
        <td>start_at</td>
		<td>String, not null, ISO 8601 datetime</td>
		<td>Time at which the targeted variable should assume <code>value</code> </td>
		<td>2015-12-25T12:01:00Z</td>
    </tr>
    <tr>
        <td>value</td>
		<td>float, not null</td>
		<td>Value of reading</td>
		<td>0.2</td>
    </tr>

</table>

The last signal point in the signal should leave the entity in a "safe" state.

*Example of a portfolio management signal message*

    {
    	"topic": "signal",
		"generated_at": "2015-12-25T12:00:00Z",
    	"entities": ["l1234", "l4509"],
    	"type": "oe-add",
    	"signal": [{
    		"start_at": "2015-12-25T12:01:00Z",
    		"value": 0.1
    	}, {
    		"start_at": "2015-12-25T13:00:00Z",
    		"value": 0
    	}]
    
    }
    
### <a name="sig-schedule"></a>Schedule Signals

Schedule signals are used to signal more complex or recurring signals to the device, similar to the “schedule” messages above. For a full example of such a message see below. The topic should be `schedule-signal`. 

*Example schedule signal message:*

This message specifies that entities `l1234` and `l4509` should defer as much power consumption as possible during peak price periods of 16:00-18:00 on weekdays, starting from the first week of 2016, by setting `oe-add` variable to `-0.5` during these periods.

The last element in the `schedule` array specifies the default value fo `oe-add` outside peak price periods.

    {
    	"topic": "schedule-signal",
		"generated_at": "2015-12-25T12:00:00Z",
    	"entities": ["l1234", "l4509"],
    	"type": "oe-add",
    	"schedule": [{
    		"span": "2016-W01-1T16:00:00/P2H",
			"repeat": "P1W",
    		"value": -0.5
    	}, {
    		"span": "2016-W01-2T16:00:00/P2H",
			"repeat": "P1W",
    		"value": -0.5
    	},{
    		"span": "2016-W01-3T16:00:00/P2H",
			"repeat": "P1W",
    		"value": -0.5
    	},{
    		"span": "2016-W01-4T16:00:00/P2H",
			"repeat": "P1W",
    		"value": -0.5
    	},{
    		"span": "2016-W01-5T16:00:00/P2H",
			"repeat": "P1W",
    		"value": -0.5
    	},{
    		"span": null,
			"repeat": null,
    		"value": 0
    	}]
    
    }
    