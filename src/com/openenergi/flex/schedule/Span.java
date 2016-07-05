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

package com.openenergi.flex.schedule;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Span {
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	
	public LocalDateTime start;
	public Duration length;
	
	
	public Span(LocalDateTime start, Duration length){
		this.start = start;
		this.length = length;
	}
	
	/**
	 * Parses Span from string.
	 * @param str ISO8601-formatted string.
	 */
	public Span(String str) throws IllegalArgumentException {
		//Step 1: attempt to separate start and duration
		String[] parts = str.split("/");
		if (parts.length != 2){
			throw new IllegalArgumentException("Expected one occurence of \"/\"");
		}
		
		//Step 2: Parse start time
		try {
			this.start = LocalDateTime.parse(parts[0], Span.formatter);
		} catch (DateTimeParseException ex){
			throw new IllegalArgumentException("Invalid start date: " + ex.toString());
		}
		
		//Step 3: Parse the duration after the "/"
		try {
			this.length = Duration.parse(parts[1]);
		} catch (DateTimeParseException ex){
			throw new IllegalArgumentException("Invalid duration value: " + ex.toString());
		}
	}
	
	/**
	 * Formats the Span according to ISO8601 notation.
	 */
	public String toString(){
		return this.start.format(Span.formatter) + "/" + this.length.toString();
	}
	
	/**
	 * Returns the end time for the span (start + duration)
	 * @return end time
	 */
	public LocalDateTime getEndTime(){
		return LocalDateTime.from(this.length.addTo(this.start));
	}
	
	/**
	 * Determines whether this span contains the current time or not. The left endpoint is inclusive whereas
	 * the right endpoint (end of interval) is not.
	 * @return True if the span contains the current time, False otherwise.
	 */
	public Boolean containsCurrentTime() {
		LocalDateTime now = LocalDateTime.now();
		if ((this.start.isBefore(now) || this.start.equals(now)) && this.getEndTime().isAfter(now)){
			return true;
		} else {
			return false;
		}
	}
	
	
}
