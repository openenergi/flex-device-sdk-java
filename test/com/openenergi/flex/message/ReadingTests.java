package com.openenergi.flex.message;

import static org.junit.Assert.*;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.google.gson.JsonSyntaxException;


public class ReadingTests {
	

	@Test
	public void testSerializeReading() {
		Reading e = (Reading) new Reading()
					.setValue(1.23)
					.setEntity("l1")
					.setTimestamp(12345L)
					.setType("something");
		
		try {
			//System.out.println(e.toString());
			JSONAssert.assertEquals("{\"topic\": \"readings\", \"entity\": \"l1\",  \"timestamp\": 12345,  \"type\": \"something\", \"value\": 1.23}", e.toString(), true);
		} catch (JSONException e1) {
			fail("Failed to serialize: " + e1.getMessage());
		}
	}

	@Test
	public void testDeserializeReading() {
		String s = "{\"topic\": \"readings\", \"entity\": \"l1\",  \"timestamp\": 12345,  \"type\": \"something\", \"value\": 1.23}";
		try {
			Reading m = (Reading) Message.deserialize(s);
			assertEquals(m.entity, "l1");
			assertEquals(m.topic, "readings");
			assertEquals((Long) 12345L, m.timestamp);
			assertEquals(m.type, "something");
			if (m instanceof Reading){
				assertEquals(((Reading)m).value, (Double)1.23);
			} else {
				fail("not an event");
			}
			
		} catch (JsonSyntaxException e1) {
			fail("Failed to deserialize: " + e1.getMessage());
		}
	}
	
}