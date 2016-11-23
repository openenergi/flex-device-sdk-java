package com.openenergi.flex.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * A Schedulable class represents an abstract point in a device-to-cloud Signal. For example, it may
 * consist of the time at which the point becomes valid and the value of the point (say, at 12.00 tomorrow
 * set the setpoint of the asset to 100.0 degrees C).
 * 
 * @author mbironneau
 *
 */
public interface Schedulable {
	
	/**
	 * Current values of item.
	 * @return Current value
	 */
	List<SignalBatchListItem> getValues();
	
	/**
	 * Date when the item will be valid (in the case of a schedule - when the item is next valid).
	 * @return Start date when value takes effect
	 */
	@JsonIgnore
	ZonedDateTime getStart();
}
