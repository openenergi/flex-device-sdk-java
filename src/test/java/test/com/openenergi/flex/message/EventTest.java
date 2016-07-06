package com.openenergi.flex.message;

import static org.junit.Assert.*;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.google.gson.JsonSyntaxException;


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
			assertEquals(m.entity, "l1");
			assertEquals(m.topic, "events");
			assertEquals((Long) 12345L, m.timestamp);
			assertEquals(m.type, "something");
			if (m instanceof Event){
				assertEquals(((Event)m).value, "value");
				assertEquals(((Event)m).level, (Integer) 0);
			} else {
				fail("not an event");
			}
			
		} catch (JsonSyntaxException e1) {
			fail("Failed to deserialize: " + e1.getMessage());
		}
	}
	
}
