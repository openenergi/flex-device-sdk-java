package com.openenergi.flex.message;

import java.util.Date;

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
