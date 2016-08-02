package com.openenergi.flex.message;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;


import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;


public class SignalTest {
	

	@Test
	public void testSerializeEvent() {
		Signal<SignalPointItem> s = (Signal<SignalPointItem>) new Signal<SignalPointItem>()
					.addEntity("l1")
					.setTimestamp(12345L)
					.setType("something");

		s.addItem(new SignalPointItem(ZonedDateTime.of(LocalDateTime.of(2016,12,27, 0, 0, 0, 0), ZoneOffset.UTC), 1.23));
		
		try {
            JSONAssert.assertEquals("{\"topic\": \"signals\", \"entities\":[\"l1\"],\"items\":[{\"start_at\":\"2016-12-27T00:00:00Z\",\"value\":1.23}],\"timestamp\":12345,\"type\":\"something\"}", s.toString(), true);

        } catch (JSONException e1) {
			fail("Failed to serialize: " + e1.getMessage());
		}
	}

	@Test
	public void testDeserializeSignal() {
		String s = "{\"topic\": \"signals\", \"entities\":[\"l1\"],\"items\":[{\"start_at\":\"2016-05-27T15:00:00Z\",\"value\":1.23}],\"timestamp\":12345,\"type\":\"something\"}";

			Signal<SignalPointItem> m = (Signal<SignalPointItem>) Message.deserialize(s);
			assertEquals(m.entities.get(0), "l1");
			assertEquals(m.topic, "signals");
			assertEquals((Long) 12345L, m.timestamp);
			assertEquals(m.type, "something");
			ZonedDateTime testDateTime = ZonedDateTime.of(2016,5,27, 15, 0, 0, 0, ZoneOffset.UTC);
			assertEquals(m.getItem(0).getStart(), testDateTime);

			assertEquals(m.getItem(0).getValue(), (Double)1.23);

	}
	
}
