package com.openenergi.flex.message;

import static org.junit.Assert.*;

import java.io.IOException;
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
		Signal<SignalPointItem> s = new Signal<SignalPointItem>();
		s.addEntity("l1");
		s.setTimestamp(12345L);
		s.setType("something");

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

		Signal<SignalPointItem> m = null;
		try {
			m = (Signal<SignalPointItem>) Message.deserialize(s);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Failed to deserialize message");
		}
		assertEquals(m.getEntities().get(0), "l1");
			assertEquals(m.getTopic(), "signals");
			assertEquals((Long) 12345L, m.getTimestamp());
			assertEquals(m.getType(), "something");
			ZonedDateTime testDateTime = ZonedDateTime.of(2016,5,27, 15, 0, 0, 0, ZoneOffset.UTC);
			assertEquals(testDateTime.toLocalDate(), m.getItem(0).getStart().toLocalDate());

			assertEquals(m.getItem(0).getValue(), (Double)1.23);

	}
	
}
