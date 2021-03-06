/*
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

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * An event is a discrete, instantaneous record of an entity's state transition. Examples are switch requests or alarm conditions.
 * This class contains a builder for events. For further documentation see <a href="https://github.com/openenergi/flex-device-sdk-java/blob/master/Messages.md">here</a>.
 * 
 * @author mbironneau
 *
 */
public class Event extends Message {
	public static class Builder {
		private Event event = new Event();

		public Builder atTime(Long timestamp){
			event.setTimestamp(timestamp);
			return this;
		}

		public Builder withType(Type type){
			event.setType(type.getValue());
			return this;
		}

		public Builder withCustomType(String type){
			event.setType(type);
			return this;
		}

		public Builder withEntity(String code){
			event.setEntity(code);
			return this;
		}

		public Builder withValue(String value){
			this.event.setValue(value);
			return this;
		}

		public Builder withLevel(Level level){
			this.event.setLevel(level);
			return this;
		}

		public Event build(){
			return this.event;
		}
	}
	
	
	/**
	 * Event levels are used to prioritize the message. They affect the retention policies and possible alerting. Event levels of WARN and ERROR
	 * should be used to indicate abnormal conditions. The resolution of an abnormal condition should be done by sending a subsequent Event of 
	 * the same type with a lower Level, for example following up an ERROR message with an INFO message of the same type.
	 */
	public enum Level {
		DEBUG(0), 
		INFO(1), 
		WARN(2), 
		ERROR(3);


		Integer value;
		
		Level(Integer value){
			this.value = value;
		}

		@JsonValue
		public Integer valueOf(){
			return this.value;
		}
	}
	
	public enum Type {
		/**
		 * This means that the asset should undertake to bring forward or defer its power consumption.
		 * This event will be triggered when the Grid Frequency crosses the Trigger Frequency of the asset.
		 */
		FFR_SWITCH_START("switch-ffr-start"), 
		
		/**
		 * This event means that the asset is no longer participating in FFR and may return to its normal operational
		 * control pattern (it is no longer required to bring forward or defer any power consumption).
		 */
		FFR_SWITCH_END("switch-ffr-end");
		@SuppressWarnings("unused")
		private String value;

		public String getValue(){
			return this.value;
		}
		
		private Type(String value){
			this.value = value;
		}
	}
	
	private Integer level;
	private String value;
 
	public Event() {
		this.setTopic("events");
	}

	public Level getLevel() {
		return Level.values()[this.level];
	}

	public Event setLevel(Level level) {
		this.level = level.valueOf();
		return this;
	}

	public String getValue() {
		return value;
	}

	public Event setValue(String value) {
		this.value = value;
		return this;
	}


	
	
}
