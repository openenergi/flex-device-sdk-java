/**
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

package com.openenergi.flex.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;

/**
 * This class consists of properties and methods shared by all Flex messages. In the abstract a Flex message just contains a timestamp, entity
 * and type. The timestamp is an integer representing the number of milliseconds since the epoch, when the message was generated. The entity 
 * is a string referencing the thing (device, sensor, asset...) to which the message is associated. The type identifies the message as being
 * a particular type of reading (eg. power consumption) or a specific alert. 
 * 
 * Refer to the documentation <a href="https://github.com/openenergi/flex-device-sdk-java/blob/master/Messages.md">here</a> for more details.
 */
public class Message
{
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String topic;
	private Long timestamp;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String entity;
	private String type;
	@JsonProperty("devicecode")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String deviceId;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String provenance;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Date createdAt;
	private static final ObjectMapper mapper = getMapper();
	private static final HashMap<String, TypeReference<?>> messageTypes = new HashMap<>();
	{
		messageTypes.put("readings", new TypeReference<Reading>(){});
		messageTypes.put("events", new TypeReference<Event>(){});
		messageTypes.put("schedules", new TypeReference<Schedule>(){});
		messageTypes.put("signals", new TypeReference<Signal<SignalBatchList>>(){});
		//Deprecated as of Message Format Spec 2.0.0.
		//messageTypes.put("batch-signals", new TypeReference<Signal<SignalBatchList>>(){});
		messageTypes.put("schedule-signals", new TypeReference<Signal<SignalScheduleItem>>(){});
	}

	public String getTopic()
	{
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public String getEntity() {
		return entity;
	}

	public void setEntity(String value){
		this.entity = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String value){
		this.type = value;
	}

	public Date getCreatedAt()
	{
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getProvenance() {
		return provenance;
	}

	public void setProvenance(String provenance) {
		this.provenance = provenance;
	}
	
	public void validate() throws InvalidMessageException {
		if (this.type.length() == 0) {
			throw new InvalidMessageException("Message type not defined");
		}
		if (this.entity.length() == 0){
			throw new InvalidMessageException("Entity not defined");
		}
	}
	
	@Override
	public String toString()
	{
		return getTopic() + ' ' + getType();
	}

	/**
	 * Serializes the message using JSON. If the timestamp field is not set it will be
	 * set to the current system time.
	 */
	public String serialise()
	{
		if (this.timestamp == null){
			this.timestamp = System.currentTimeMillis();
		}
		try {
			return mapper.writeValueAsString(this);
		} catch (JsonProcessingException e){
			throw new RuntimeException("Could not serialise message: " + e);
		}
	}
	
	public static Object deserialize(String json) throws IOException
	{
		JsonNode root = mapper.readTree(json);
		String topic = root.get("topic").asText();

		if (!messageTypes.containsKey(topic))
			throw new IOException("Unknown message topic - failed to deserialize: " + topic);

		return mapper.convertValue(root, messageTypes.get(topic));
	}

	/**
	 * Register a custom deserializer class for a given message topic (or override a built-in one).
	 * @param topic - the topic of messages that will be mapped to the custom deserializer.
	 * @param mappedType - a TypeReference to the class that the message will be mapped to
	 */
	public static void registerMessageMapper(String topic,  TypeReference<?> mappedType){
		messageTypes.put(topic, mappedType);
	}

	private static ObjectMapper getMapper()
	{
		ObjectMapper mapper = new ObjectMapper();

		// json names should be in snake case
		mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

		// handle serialisation of ZonedDateTime
		// causes ZonedDateTime to serialise as a timestamp string rather than props
		// like 1483228800.000000000
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		// converts timestamp to formatted string like 2017-01-01T00:00Z[UTC]
		mapper.registerModule(new JavaTimeModule());

		// following not permitted by niagara security manager
		mapper.disable(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS);
		mapper.disable(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS);
		mapper.disable(MapperFeature.USE_GETTERS_AS_SETTERS);
		mapper.disable(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS);

		return mapper;
	}
}
