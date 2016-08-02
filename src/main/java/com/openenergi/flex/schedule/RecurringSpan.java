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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

public class RecurringSpan {
	public Span span;
	public Duration repeat;
	
	public RecurringSpan(Span span, Duration repeat){
		this.span = span;
		this.repeat = repeat;
	}
	
	/**
	 * Parses the given span and repeat into a RecurringSpan.
	 * @param span The ISO-8601 formatted interval (eg. "2016-01-01/P1D")
	 * @param repeat The ISO-8601 formatted repeat (eg. "P7D")
	 * @throws IllegalArgumentException
	 */
	public RecurringSpan(String span, String repeat) throws IllegalArgumentException {
		this.span = new Span(span);
		
		try {
			this.repeat = Duration.parse(repeat);
		} catch (DateTimeParseException ex){
			throw new IllegalArgumentException("Invalid repeat value: " + ex.toString());
		}
	}
	
	/**
	 * Returns the next span that will contain the current time or exceed it.
	 * @return
	 */
	public Span getNextSpan(){
		ZonedDateTime now = ZonedDateTime.now();
		Span curr = new Span(this.span.start, this.span.length);
		while(!(curr.containsCurrentTime() || curr.start.isAfter(now))){
			curr.start = ZonedDateTime.from(curr.length.addTo(curr.start));
		}
		return curr;
	}
}
