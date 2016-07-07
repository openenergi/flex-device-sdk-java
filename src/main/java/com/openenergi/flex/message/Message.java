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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonElement;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonPrimitive;
import java.time.ZoneId;
import java.time.Instant;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
	public String topic;
	public Long timestamp;
	public String entity;
	public String type;
	
	@SerializedName("created_at")
	private Date createdAt;
	
	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Message(){}
	
	public Message setType(String value){
		this.type = value;
		return this;
	}
	
	public Message setEntity(String value){
		this.entity = value;
		return this;
	}

	public String getEntity() {
		return entity;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public Message setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
		return this;
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
	public String toString(){
		if (this.timestamp == null){
			this.timestamp = System.currentTimeMillis();
		}
		Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
            @Override
            public JsonElement serialize(LocalDateTime localDateTime, Type type, JsonSerializationContext jsonSerializationContext) {
                Instant instant = localDateTime.atZone(ZoneOffset.UTC).toInstant();
                Date date = Date.from(instant);
                return new JsonPrimitive(date.getTime());
            }
        }).setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
		
		return gson.toJson(this);	
	}
	
	public static Object deserialize(String json) throws JsonParseException {
		Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
			@Override
			public LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
				Instant instant = Instant.ofEpochMilli(json.getAsJsonPrimitive().getAsLong());
				return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
			}
		}).create();
		
		JSONObject j;
		String topic;
		
		try {
			j = new JSONObject(json);
			topic = j.getString("topic");
		} catch (JSONException e){
			throw new JsonParseException(e.getMessage());
		}
		
		switch (topic){
		case "readings":
			return gson.fromJson(json, Reading.class);
		case "events":
			return gson.fromJson(json, Event.class);
		case "schedules":
			return gson.fromJson(json, Schedule.class);
		case "signals":
			return gson.fromJson(json, new TypeToken<Signal<SignalPointItem>>(){}.getType());
		case "schedule-signals":
			return gson.fromJson(json, new TypeToken<Signal<SignalScheduleItem>>(){}.getType());
		}
		return null;
		
	}
	
}
