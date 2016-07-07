package com.openenergi.flex.message;

import static org.junit.Assert.*;

import java.time.LocalDateTime;


import org.json.JSONException;
import org.junit.Test;
import org.junit.Ignore;
import org.skyscreamer.jsonassert.JSONAssert;


public class SignalTest {
	

	@Ignore
	@Test
	public void testSerializeEvent() {
		@SuppressWarnings("unchecked")
		Signal<SignalPointItem> s = (Signal<SignalPointItem>) new Signal<SignalPointItem>()
					.addEntity("l1")
					.setTimestamp(12345L)
					.setType("something");

		s.addItem(new SignalPointItem(LocalDateTime.of(2016,12,27, 0, 0, 0, 0), 1.23));
		
		try {
			JSONAssert.assertEquals("\"topic\": \"signals\", \"entities\":[\"l1\"],\"items\":[{\"start_at\":1482796800000,\"value\":1.23}],\"timestamp\":12345,\"type\":\"something\"}", s.toString(), true);
		} catch (JSONException e1) {
			fail("Failed to serialize: " + e1.getMessage());
		}
	}

	@Test
	public void testDeserializeSignal() {
		String s = "{\"topic\": \"signals\", \"entities\":[\"l1\"],\"items\":[{\"start_at\":1482796800000,\"value\":1.23}],\"timestamp\":12345,\"type\":\"something\"}";

			Signal<SignalPointItem> m = (Signal<SignalPointItem>) Message.deserialize(s);
			//System.out.println(m.toString());
			assertEquals(m.entities.get(0), "l1");
			assertEquals(m.topic, "signals");
			assertEquals((Long) 12345L, m.timestamp);
			assertEquals(m.type, "something");
			assertEquals(m.getItem(0).getStart(), LocalDateTime.of(2016,12,27, 0, 0, 0, 0));
			assertEquals(m.getItem(0).getValue(), (Double)1.23);

	}
	
}
