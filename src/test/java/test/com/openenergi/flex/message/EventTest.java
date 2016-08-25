package com.openenergi.flex.message;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


public class EventTest {
	

	@Test
	public void testSerializeEvent() {
		Event e = (Event) new Event()
					.setValue("value")
					.setLevel(Event.Level.DEBUG)
					.setEntity("l1")
					.setTimestamp(12345L)
					.setType("something");
		
		try {
			//System.out.println(e.toString());
			JSONAssert.assertEquals("{\"topic\": \"events\", \"entity\": \"l1\",  \"timestamp\": 12345,  \"type\": \"something\", \"value\": \"value\", \"level\": 0}", e.toString(), true);
		} catch (JSONException e1) {
			fail("Failed to serialize: " + e1.getMessage());
		}
	}

	@Test
	public void testDeserializeEvent() {
		String s = "{\"topic\": \"events\", \"entity\": \"l1\",  \"timestamp\": 12345,  \"type\": \"something\", \"value\": \"value\", \"level\": 0}";
		try {
			Event m = (Event) Message.deserialize(s);
			assertEquals(m.getEntity(), "l1");
			assertEquals(m.getTopic(), "events");
			assertEquals((Long) 12345L, m.getTimestamp());
			assertEquals(m.getType(), "something");
			if (m instanceof Event){
				assertEquals("value", ((Event)m).getValue());
				assertEquals(Event.Level.DEBUG, ((Event)m).getLevel());
			} else {
				fail("not an event");
			}
			
		} catch (IOException e1) {
			fail("Failed to deserialize: " + e1.getMessage());
		}
	}
	
}
