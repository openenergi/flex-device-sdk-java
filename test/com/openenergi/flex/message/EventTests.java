package com.openenergi.flex.message;

import static org.junit.Assert.*;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;


public class EventTests {
	

	@Test
	public void testSerializeEvent() {
		Event e = (Event) new Event()
					.setValue("value")
					.setLevel(Event.Level.DEBUG)
					.setEntity("l1")
					.setTimestamp(12345L)
					.setType("something");
		
		try {
			System.out.println(e.toString());
			JSONAssert.assertEquals("{\"topic\": \"events\", \"entity\": \"l1\",  \"timestamp\": 12345,  \"type\": \"something\", \"value\": \"value\", \"level\": 0}", e.toString(), true);
		} catch (JSONException e1) {
			fail("Failed to serialize: " + e1.getMessage());
		}
	}
	
}
