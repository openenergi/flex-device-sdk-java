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

import java.time.LocalDateTime;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents a single point in a Signal. The variable (type) of the Signal should take the value of the signal item 
 * after the local time exceeds startAt. For example, a signal for the variable OE_ADD with a single SignalItem that takes the value
 * of 0.5 in 30 minutes means that in 30 minutes, the RLTEC algorithm parameter OE ADD should be set to 0.5. 
 * @author mbironneau
 *
 */
public final class SignalPointItem implements Schedulable {
	@SerializedName("start_at")
	LocalDateTime startAt;
	Double value;
	public Double getValue() {
		return this.value;
	}
	public LocalDateTime getStart() {
		return this.startAt;
	}
	
	public SignalPointItem(){}
	
	public SignalPointItem(LocalDateTime start, Double value){
		this.startAt = start;
		this.value = value;
	}
}