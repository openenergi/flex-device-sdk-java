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

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
public class Message {
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String topic;
	private Long timestamp;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String entity;
	private String type;
	private static final ObjectMapper mapper = getMapper();

	private static final HashMap<String, TypeReference<?>> messageTypes = new HashMap<String, TypeReference<?>>(){{
		put("readings", new TypeReference<Reading>(){});
		put("events", new TypeReference<Event>(){});
		put("schedules", new TypeReference<Schedule>(){});
		put("signals", new TypeReference<Signal<SignalBatchList>>(){});
		//Deprecated as of Message Format Spec 2.0.0.
		//put("batch-signals", new TypeReference<Signal<SignalBatchList>>(){});
		put("schedule-signals", new TypeReference<Signal<SignalScheduleItem>>(){});
	}};

	@JsonProperty("devicecode")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String deviceId;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String provenance;

	//@JsonProperty("created_at")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Date createdAt;
	
	public void validate() throws InvalidMessageException {
		if (this.type.length() == 0) {
			throw new InvalidMessageException("Message type not defined");
		}
		if (this.entity.length() == 0){
			throw new InvalidMessageException("Entity not defined");
		}
	};
	
	/**
	 * Serializes the message using JSON. If the timestamp field is not set it will be
	 * set to the current system time.
	 */
	public String toString() {
		if (this.timestamp == null){
			this.timestamp = System.currentTimeMillis();
		}
		try {
			return mapper.writeValueAsString(this);
		} catch (JsonProcessingException e){
			e.printStackTrace();
			return null;
		}
	}
	
	public static Object deserialize(String json) throws IOException, IllegalArgumentException {
		String topic;
		JsonNode root;
		root = mapper.readTree(json);
		topic = root.get("topic").asText();

		if (!messageTypes.containsKey(topic)){
			throw new IOException("Unknown message topic - failed to deserialize: " + topic);
		}
		TypeReference<?> deserializer = messageTypes.get(topic);
		return mapper.convertValue(root, deserializer);
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

	/**
	 * Register a custom deserializer class for a given message topic (or override a built-in one).
	 * @param topic - the topic of messages that will be mapped to the custom deserializer.
	 * @param mappedType - a TypeReference to the class that the message will be mapped to
	 */
	public static void registerMessageMapper(String topic,  TypeReference<?> mappedType){
		messageTypes.put(topic, mappedType);
	}

	private static ObjectMapper getMapper(){
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		ObjectMapper mapper = new ObjectMapper();
		mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
		mapper.setDateFormat(df);
		mapper.registerModule(new JavaTimeModule());
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		mapper.disable(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS);
		mapper.disable(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS);
		mapper.disable(MapperFeature.USE_GETTERS_AS_SETTERS);
		mapper.disable(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS);

		return mapper;
	}
}
