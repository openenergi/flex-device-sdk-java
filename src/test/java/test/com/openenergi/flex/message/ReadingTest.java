package com.openenergi.flex.message;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


public class ReadingTest {
	

	@Test
	public void testSerializeReading() {
		Reading e = new Reading.Builder()
					.withValue(1.23)
					.withEntity("l1")
					.atTime(12345L)
					.withCustomType("something")
					.build();
		
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
			assertEquals(m.getEntity(), "l1");
			assertEquals(m.getTopic(), "readings");
			assertEquals((Long) 12345L, m.getTimestamp());
			assertEquals(m.getType(), "something");
			if (m instanceof Reading){
				assertEquals(((Reading)m).getValue(), (Double)1.23);
			} else {
				fail("not a reading");
			}
			
		} catch (IOException e1) {
			fail("Failed to deserialize: " + e1.getMessage());
		}
	}
	
}
