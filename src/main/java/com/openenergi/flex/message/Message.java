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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class consists of properties and methods shared by all Flex messages. In the abstract a Flex message just contains a timestamp, entity
 * and type. The timestamp is an integer representing the number of milliseconds since the epoch, when the message was generated. The entity 
 * is a string referencing the thing (device, sensor, asset...) to which the message is associated. The type identifies the message as being
 * a particular type of reading (eg. power consumption) or a specific alert. 
 * 
 * Refer to the documentation <a href="https://github.com/openenergi/flex-device-sdk-java/blob/master/Messages.md">here</a> for more details.
 */
public class Message {
	private String topic;
	private Long timestamp;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String entity;
	private String type;
	private static final ObjectMapper mapper = getMapper();

	private static ObjectMapper getMapper(){
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		ObjectMapper mapper = new ObjectMapper();
		mapper.setDateFormat(df);
		mapper.registerModule(new JavaTimeModule());
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

		return mapper;
	}

	@JsonProperty("devicecode")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String deviceId;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String provenance;

	
	@JsonProperty("created_at")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Date createdAt;
	
	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Message(){}
	
	public void setType(String value){
		this.type = value;
	}
	
	public void setEntity(String value){
		this.entity = value;
	}

	public String getEntity() {
		return entity;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public String getType() {
		return type;
	}
	
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
	
	public static Object deserialize(String json) throws IOException {
		String topic = "";
		String type = "";
		JsonNode root;
		root = mapper.readTree(json);
		topic = root.get("topic").asText();
		type = root.get("type").asText();


		switch (topic){
		case "readings":
			return mapper.convertValue(root, Reading.class);
		case "events":
			return mapper.convertValue(root, Event.class);
		case "schedules":
			return mapper.convertValue(root, Schedule.class);
		case "signals":
			if (type.equals("oe-vars")){
				return mapper.convertValue(root, new TypeReference<Signal<SignalBatchList>>() {});
			} else {
				return mapper.convertValue(root, new TypeReference<Signal<SignalPointItem>>() {});
			}

		case "schedule-signals":
			return mapper.convertValue(root, new TypeReference<Signal<SignalScheduleItem>>() {});
		}
		return null;
		
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
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
}
