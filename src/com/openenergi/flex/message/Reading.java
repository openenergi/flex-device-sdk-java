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
 * A Reading is an instantaneous measurement of a metric associated with an entity (eg power consumption).
 * 
 * Refer to the documentation <a href="https://github.com/openenergi/flex-device-sdk-java/blob/master/Messages.md">here</a> for more details.
 * @author mbironneau
 *
 */
public class Reading extends Message{
	
	@SuppressWarnings("unused")
	private static String topic = "readings";
	
	/**
	 * This enum contains common reading types. Custom reading types can also be sent, though care should be taken
	 * to ensure that they not clash with the ones below.
	 * 
	 * @author mbironneau
	 *
	 */
	public enum Type {
		
		/**
		 * Instantaneous power consumption
		 */
		POWER("power"), 
		
		/**
		 * Instantaneous power that can be brought forward (increased) within 2 seconds, lasting for up to 30 minutes.
		 */
		AVAILABILITY_FFR_HIGH("availability-ffr-high"), 
		
		/**
		 * Instantaneous power that can be deferred (decreased) within 2 seconds, lasting for up to 30 minutes.
		 */
		AVAILABILITY_FFR_LOW("availability-ffr-low"),
		
		/**
		 * Instantaneous power that is currently being brought forward responding to a High FFR switch request.
		 */
		RESPONSE_FFR_HIGH("response-ffr-high"), 
		
		/**
		 * Instantaneous power that is currently being deferred responding to a Low FFR switch request.
		 */
		RESPONSE_FFR_LOW("response-ffr-low"),
		
		/**
		 * A process variable that controls a process. If it exits a particular control band the asset will not be available for DD.
		 */
		CONTROL_VARIABLE("control-variable"),
		
		/**
		 * The target value of the control variable.
		 */
		SETPOINT("setpoint"),
		
		/**
		 * The upper range of the control band of the control variable. If the value of the control variable exceeds this value the 
		 * asset will become unavailable for DD.
		 */
		SETPOINT_HIGH("setpoint-high"),
		
		/**
		 * The lower range of the control band of the control variable. If the value of the control variable goes below this value
		 * the asset will become unavailable for DD.
		 */
		SETPOINT_LOW("setpoint-low");
	
		private String value;
		
		private Type(String value){
			this.value = value;
		}
	
	}
	
	private Float value;

	public Float getValue() {
		return value;
	}

	public Reading setValue(Float value) {
		this.value = value;
		return this;
	}
	
	

}
