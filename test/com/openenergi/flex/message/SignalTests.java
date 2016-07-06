package com.openenergi.flex.message;

import static org.junit.Assert.*;

import java.util.Date;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.google.gson.JsonSyntaxException;


public class SignalTests {
	

	/*@Test
	public void testSerializeEvent() {
		@SuppressWarnings("unchecked")
		Signal<SignalPointItem> s = (Signal<SignalPointItem>) new Signal<SignalPointItem>()
					.addEntity("l1");
					//.setTimestamp(12345L)
					//.setType("something");
		
		s.addItem(new SignalPointItem(new Date(2016,12,27), 1.23));
		
		try {
			System.out.println(s.toString());
			JSONAssert.assertEquals("{\"topic\": \"events\", \"entity\": \"l1\",  \"timestamp\": 12345,  \"type\": \"something\", \"value\": \"value\", \"level\": 0}", s.toString(), true);
		} catch (JSONException e1) {
			fail("Failed to serialize: " + e1.getMessage());
		}
	}*/
	
}
