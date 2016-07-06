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
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.SerializedName;


/**
 * 
 * A Signal is a cloud-to-device message that contains information on the future desired state of assets
 * connected to the device. 
 * 
 * Refer to the documentation <a href="https://github.com/openenergi/flex-device-sdk-java/blob/master/Messages.md">here</a> for more details.
 * 
 * @author mbironneau
 * @param <T>  The type of signal item (eg. NumericalItem or ScheduleItem)
 *
 */
public class Signal<T extends Schedulable> extends Message {
	
	@SerializedName("generated_at")
	Date generatedAt;
	
	List<String> entities;
	
	public enum Type {
		
		/**
		 * Amount added to Grid Frequency before it is input to RLTEC algorithm. Default is 0.
		 */
		OE_ADD("oe-add"),
		
		/**
		 * Amount that Grid Frequency is multiplied by before it is input to RLTEC algorithm. Default is 1.
		 */
		OE_MULTIPLY("oe-multiply");
		
		@SuppressWarnings("unused")
		private String value;
		
		private Type(String value){
			this.value = value;
		}
	}
	
	List<T> items;
	
	public Signal(){
		this.entities = new ArrayList<String>(); 
		this.items = new LinkedList<T>();
	}
	
	/**
	 * Adds an entity to the list of target entities.
	 * 
	 * @param entity The entity code of the target
	 * @return The signal
	 */
	public Signal<T> addEntity(String entity){
		this.entities.add(entity);
		return this;
	}
	
	/**
	 * Adds signal item to the list of items.
	 * @param item the item to add
	 * @return
	 */
	public Signal<T> addItem(T item){
		this.items.add(item);
		return this;
	}
	
	/**
	 * Returns the Signal item at the given index. 
	 * @param ix Zero-based index
	 * @return The item
	 */
	public T getItem(int ix){
		return this.items.get(ix);
	}
	
	/**
	 * Gets the current value of the signal. Assumes that the signal items are sorted in decreasing priority order (i.e. the last valid item should be applied).
	 * @return the value that the variable/parameter pointed to in type should be set to currently. 
	 */
	public Double getCurrentValue(){
		Date currentDate = new Date();
		ListIterator<T> li = this.items.listIterator(this.items.size());
		while (li.hasPrevious()){
			T item = li.previous();
			if (item.getStart().before(currentDate) && item.getValue() != null){
				return item.getValue();
			}
		};
		return null;
	}
	
	/**
	 * Gets the time at which the signal will next change value (call getCurrentValue() at that time to get the new value).
	 * @return the time at which the signal will next change value.
	 */
	public Date getNextChange(){
		Date currentDate = new Date();
		ListIterator<T> li = this.items.listIterator(this.items.size());
		while (li.hasPrevious()){
			T item = li.previous();
			if (item.getStart().after(currentDate)){
				return item.getStart();
			}
		};
		return null;
	}
	

}
