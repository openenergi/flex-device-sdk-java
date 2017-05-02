# Message Format Specification

*Version: 2.0.0*

Last modified on 2017-05-02

### Versioning
*Prior to version 1.0, the specification is a draft that is subject to short-notice changes and alterations.*

The message format specification follows the conventions of semantic versioning starting with version 1.0. Namely, 

* The revision is incremented after non-normative corrections or clarifications
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

## <a name="failure"></a>Failure Modes

There are several types of reasons that a message can fail to deliver:

* Connection (link) failure - This will be visible to the application through network level errors. If using the RetryingClient from the OE SDK it will retry and the error will not be visible to the end application.
* Message bus rejects message - Either because the message is over 256Kb or the communication key is incorrect. This will be visible to the application through protocol-level errors
* Message bus rejects message - due to too many requests (or the server is busy). This will be visible to the application through protocol-level errors. If using the RetryingClient from the OE SDK it will retry and the error will not be visible to the end application.
* Message bus accepts message, but the message is invalid - The message will be stored and its invalidity will be recorded. These can be queried through the Device API.

If using one of the Open Energi SDKs or the Microsoft IoT Hub client, a list of protocol level errors can be found 
[here](https://azure.github.io/azure-iot-sdks/java/device/api_reference/com/microsoft/azure/iothub/IotHubStatusCode.html). The RetryingClient will retry in certain cases so some failures (eg. server busy) will not be visible to the end application.

If using the RetryingClient, message may be buffered in the event of retriable exceptions. In the event that the cause of the exception (eg network failure) lasts a long time, the size of the buffer may reach the capacity of the machine, which will inevitably lead to data loss and degradation of performance. Integrators should take care to size the device appropriately so that it can store 24H of FFR data.

### Clock Synchronization

Open Energi will nominate an NTP server. All devices that send Dynamic Demand data to the Open Energi cloud must ensure that their clocks drift no more than 100ms.

## <a name="ent"></a>Entities

All messages are associated with an **entity**. An entity is a logical, stateful thing on whose state metrics are defined and recorded. Certain state transitions may also be recorded as events. Examples of entities are physical assets, meters or sensors. 

A **device** is a type of entity that represents a physical or virtual gateway device on a site. In the tree of all entities, a device is a parent to all other physical entities on a site. Devices are special because they are associated with a **communication key** used to authenticate all communications via the API and ensure that integrators do not accidentally send data for entities for which they have no authority.

The list of entities associated to a device and their meaning will be agreed between Open Energi and the integrator beforehand.

## <a name="mes"></a>Message Types

### <a name="mes-readings"></a>Readings

A reading is an instantaneous measurement of a metric associated with an entity. The value of the reading is assumed to hold between the timestamp of the message and the next received timestamp of a readings message of the same type (known as a change-of-value or step-after series). Examples are instantaneous power consumption of an asset or grid frequency recorded from a meter.

Due to the uncertainties associated with clock drift readings that are too old may not be included in Dynamic Demand aggregations. Integrators should not in general submit data that is older than 24h.

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
The type should be `power`. The value should be in kW and rounded to the nearest 100W. A new reading should be generated whenever the power consumption increases or decreases by more than 5% since its last sent value, or every 12 hours, whichever comes first.

#### Availability Readings
The type should be one of the following:

* `availability-ffr-low`: (kW, the nearest 100W) The amount of power consumption that is available to be deferred within 2 seconds of `timestamp` and for up to 30 minutes
* `availability-ffr-high`: (kW, to the nearest 100W) The amount of power consumption that is available to be brought forward within 2 seconds of `timestamp` and for up to 30 minutes


A new reading should be generated whenever the availability increases or decreases by more than 5% since its last sent value, or every 12 hours, whichever comes first.

**Example Availability Calculations**

Given a load with two stages (on/off), and a maximum power rating of 10kW, the high availability would be 0kW whenever the load is on and 10kW whenever it is off (and could be turned on if required). The low availability would be 0kW whenever the load is off and 10kW whenever it is on (and could be turned off if required).

For a variable speed load with a maximum power rating of 10kW, the high availability would be the difference between 10kW and the current power consumption (if the power consumption can be increased). The low availability would be the difference between the current power consumption and 0kW (if the power consumption can be decreased).

For a frequency tracking load (such as a battery) the high availability is the response expected from the load at a system frequency of 50.5Hz. The low availability is the response expected from the load at a system frequency of 49.5Hz.

#### Response Readings
The type should be one of the following:

* `response-ffr-high`: (kW, to the nearest 100W) The amount of power consumption that is currently being brought forward
* `response-ffr-low`: (kW, to the nearest 100W) The amount of power consumption that is currently being deferred

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
		<td>Object, possibly null</td>
		<td>Value of the schedule during this interval</td>
		<td>[“ffr”, “special Monday service”]</td>
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
			"value": [“ffr”, “special Monday service”]
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

**Signal Fields**

Unlike above, where the `value` field is a number, the `items` field is an array of **batch points**.


    
<table>
    <tr>
        <th>Field</th>
        <th>Type</th>
        <th>Description</th>
        <th>Example</th>
    </tr>
	<tr>
        <td>timestamp</td>
		<td>Long, not null (ms since epoch)</td>
		<td>System time when signal was generated</td>
		<td>1413235133452</td>
    </tr>
    <tr>
        <td>topic</td>
		<td>String. The value should always be "signals"</td>
		<td>Topic to identify the message as a Portfolio Management Signal</td>
		<td>signals</td>
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
		<td>The identifying name of the group of targeted variables</td>
		<td>oe-vars</td>
    </tr>
    <tr>
        <td>items</td>
		<td>array of <strong>signal points</strong></td>
		<td>Signal specification</td>
		<td><em>See below</em></td>
    </tr>
</table>

**Signal Points**


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
        <td>values</td>
		<td>array of <strong>signal point items</strong>, not null</td>
		<td>Value of readings</td>
		<td>[{"variable": "oe-add", "value": 0.1}, {"variable": "oe-multiply", "value": "1.1"}]</td>
    </tr>

</table>

**Signal Point Item Fields**

<table>
    <tr>
        <th>Field</th>
        <th>Type</th>
        <th>Description</th>
        <th>Example</th>
    </tr>
    <tr>
        <td>variable</td>
		<td>String</td>
		<td>The name of the variable</td>
		<td>oe-add</td>
    </tr>
    <tr>
        <td>value</td>
		<td>float, not null</td>
		<td>Value of variable</td>
		<td>0.1</td>
    </tr>

</table>

The last signal point in the signal should leave the entity in a "safe" state.

*Example of a portfolio management signal message*


	{
		"topic": "signals",
		"generated_at": "2015-12-25T12:00:00Z",
		"entities": ["l1234", "l4509"],	
		"type": "oe-add",
		"items": [{
			"start_at": "2015-12-25T12:01:00Z",
			"values": [{
				"variable": "oe-add",
				"value": 0.1
			}, {
				"variable": "oe-multiply",
				"value": "1.1"
			}]
		}, {
			"start_at": "2015-12-25T13:00:00Z",
			"values": [{
				"variable": "oe-add",
				"value": 0
			}, {
				"variable": "oe-multiply",
				"value": "1"
			}]
		}]

	}


#### Open Energi Signal Types

There are two Signal types that have special meaning to Open Energi's control algorithm: `oe-add` and `oe-multiply`. They are used to manipulate Grid Frequency before it is used as an input to the algorithm (the Effective Frequency). 

* `oe-multiply-high`: This value makes the algorithm's response to the Grid Frequency more or less extreme by multiplying its deviation from 50Hz by the given amount. Its default value is 1. It only applies when the Grid Frequency is greater than 50Hz.
* `oe-multiply-low`: This value makes the algorithm's response to the Grid Frequency more or less extreme by multiplying its deviation from 50Hz by the given amount. Its default value is 1. it only applies when the Grid Frequency is less than 50Hz.
* `oe-add`: This value alters the algorithm's response to the Grid Frequency in an additive manner (see formulae below). Its default value is 0. 

**Calculation**

Given a Grid Frequency reading of *GF*, the Effective Frequency input of the control algorithm will be

* `0.5*(2*oeMultiplyHigh*(GF - 50)+oeAdd)+50` if `GF >= 50`
* `0.5*(2*oeMultiplyLow*(GF - 50)+oeAdd)+50` otherwise


### <a name="sig-schedule"></a>Schedule Signals

Schedule signals are used to signal more complex or recurring signals to the device, similar to the “schedule” messages above. For a full example of such a message see below. The topic should be `schedule-signals`. 

*Example schedule signal message:*

This message specifies that entities `l1234` and `l4509` should defer as much power consumption as possible during peak price periods of 16:00-18:00 on weekdays, starting from the first week of 2016, by setting `oe-add` variable to `-0.5` during these periods.

The last element in the `schedule` array specifies the default value fo `oe-add` outside peak price periods.

    {
    	"topic": "schedule-signals",
		"timestamp": 14100023938431,
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
    