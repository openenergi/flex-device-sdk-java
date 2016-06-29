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

import java.time.Duration;

import com.openenergi.flex.schedule.RecurringSpan;
import com.openenergi.flex.schedule.Span;

/**
 * This class assigns a value to a (possibly recurring) span of time. 
 * @author mbironneau
 *
 */
public class ScheduleItem extends Message {
	private String span;
	private String repeat;
	private String value;
	
	private RecurringSpan item;
	
	public ScheduleItem(RecurringSpan span, String value){
		this.item = span;
		this.value = value;
		this.format();
	}
	
	public ScheduleItem(){}
	
	public ScheduleItem(Span span, Duration repeat, String value){
		this.item = new RecurringSpan(span, repeat);
		this.value = value;
		this.format();
	}
	
	private void format(){
		if (this.item == null) return;
	
		if (this.item.span != null) {
			this.span = this.item.toString();
		} else {
			this.span = null;
		}
		
		if (this.item.repeat != null) {
			this.repeat = this.item.repeat.toString();
		} else {
			this.repeat = null;
		}	
	}
}
