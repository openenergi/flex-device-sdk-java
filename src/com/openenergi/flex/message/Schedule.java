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

import java.util.ArrayList;
import java.util.List;

/**
 * A schedule is a collection of intervals that define the (string) value of a variable over a period of time.
 * For more details see the documentation <a href="https://github.com/openenergi/flex-device-sdk-java/blob/master/Messages.md">here</a>.
 * @author mbironneau
 *
 */
public class Schedule extends Message {
	
	String topic = "schedules";
	
	private List<ScheduleItem> schedule;
	
	public Schedule(){
		this.schedule = new ArrayList<ScheduleItem>();
	}
	
	/**
	 * Adds an item to the schedule. A 
	 * @param item
	 * @return
	 */
	public Schedule addItem(ScheduleItem item){
		this.schedule.add(item);
		return this;
	}
}
