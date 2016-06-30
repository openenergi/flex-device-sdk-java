package com.openenergi.flex.message;

import java.util.Date;

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
	 * Current value of item.
	 * @return Current value
	 */
	Float getValue();
	
	/**
	 * Date when the item will be valid (in the case of a schedule - when the item is next valid).
	 * @return Start date when value takes effect
	 */
	Date getStart();
}
