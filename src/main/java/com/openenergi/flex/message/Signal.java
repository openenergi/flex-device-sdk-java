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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

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
public class Signal<T extends Schedulable> extends Message
{
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	private ZonedDateTime generatedAt;
	private List<String> entities;
	private List<T> items;

	public Signal()
	{
		this.entities = new ArrayList<>();
		this.items = new LinkedList<T>();
		this.setTopic("signals");
	}

	public ZonedDateTime getGeneratedAt() {
		return generatedAt;
	}

	public void setGeneratedAt(ZonedDateTime generatedAt) {
		this.generatedAt = generatedAt;
	}

	public List<String> getEntities() {
		return entities;
	}

	public void setEntities(List<String> entities) {
		this.entities = entities;
	}

	public List<T> getItems(){
		return this.items;
	}

	public void setItems(List<T> items) {
		this.items = items;
	}

	/**
	 * Sorts the signal items by their start time. This is a stable sort, so if two items share a start time then
	 * they will not be re-ordered.
	 */
	public void sort(){
		this.items.sort(Comparator.comparing(a -> a.getStartAt()));
	}

	public void addEntity(String entity){
		this.entities.add(entity);
	}

	public void addItem(T item){
		this.items.add(item);
	}

	public T getItem(int ix){
		return this.items.get(ix);
	}
	
	/**
	 * Gets the current value of the signal. Assumes that the signal items are sorted in decreasing priority order (i.e. the last valid item should be applied).
	 * @return the value that the variable/parameter pointed to in type should be set to currently. 
	 */
	@JsonIgnore
	public SignalElement getCurrentValues(){
		ZonedDateTime currentDate = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		ListIterator<T> li = this.items.listIterator(this.items.size());
		while (li.hasPrevious()){
			T item = li.previous();
			if (currentDate.isAfter(item.getStartAt()) && item.getValues() != null){
				return new SignalElement(item.getStartAt(), item.getValues());
			}
		}
		return null;
	}
	
	/**
	 * Gets the time and values of the signal at next change
	 * @return the SignalElement representing the value of the signal at the next change point.
	 */
	@JsonIgnore
	public SignalElement getNextChange(){
		ZonedDateTime currentDate = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		ListIterator<T> li = this.items.listIterator();
		while (li.hasNext()){
			T item = li.next();
			if (item.getStartAt().isAfter(currentDate)){
				return new SignalElement(item.getStartAt(), item.getValues());
			}
		}
		return null;
	}

	public enum Type {
		/**
		 * Amount added to Grid Frequency before it is input to RLTEC algorithm. Default is 0.
		 */
		OE_ADD("oe-add"),

		/**
		 * Amount that Grid Frequency is multiplied by before it is input to RLTEC algorithm. Default is 1.
		 */
		OE_MULTIPLY("oe-multiply");

		Type(String value)
		{
			this.value = value;
		}

		private String value;
	}
}
