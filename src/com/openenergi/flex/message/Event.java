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

/**
 * An event is a discrete, instantaneous record of an entity’s state transition. Examples are switch requests or alarm conditions.
 * This class contains a builder for events. For further documentation see <a href="https://github.com/openenergi/flex-device-sdk-java/blob/master/Messages.md">here</a>.
 * 
 * @author mbironneau
 *
 */
public class Event extends Message {
	
	@SuppressWarnings("unused")
	private static String topic = "events";
	
	/**
	 * Event levels are used to prioritize the message. They affect the retention policies and possible alerting. Event levels of WARN and ERROR
	 * should be used to indicate abnormal conditions. The resolution of an abnormal condition should be done by sending a subsequent Event of 
	 * the same type with a lower Level, for example following up an ERROR message with an INFO message of the same type.
	 */
	public enum Level {
		DEBUG(0), INFO(1), WARN(2), ERROR(3);
		private int value;
		
		private Level(int value){
			this.value = value;
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
		private String value;
		
		private Type(String value){
			this.value = value;
		}
	}
	
	
	private String type;
	private Level level;
	private String value;
 
	public Event() {}

	public Level getLevel() {
		return level;
	}

	public Message setLevel(Level level) {
		this.level = level;
		return this;
	}

	public String getValue() {
		return value;
	}

	public Message setValue(String value) {
		this.value = value;
		return this;
	}


	
	
}
