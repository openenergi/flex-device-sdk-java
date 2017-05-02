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
	public void testDeserializeBatchSignal() {
		String s = "{\n" +
				"\t\"generated_at\": \"2016-07-30T12:00:01.000Z\",\n" +
				"\t\"type\": \"oe-vars\",\n" +
				"\t\"topic\": \"signals\",\n" +
				"\t\"items\": [{\n" +
				"\t\t\"start_at\": \"2016-08-05T12:01:00Z\",\n" +
				"\t\t\"values\": [{\n" +
				"\t\t\t\"variable\": \"oe-add\",\n" +
				"\t\t\t\"value\": 1\n" +
				"\t\t}, {\n" +
				"\t\t\t\"variable\": \"oe-multiply-high\",\n" +
				"\t\t\t\"value\": 0\n" +
				"\t\t}, {\n" +
				"\t\t\t\"variable\": \"oe-multiply-low\",\n" +
				"\t\t\t\"value\": 0\n" +
				"\t\t}]\n" +
				"\t}],\n" +
				"\t\"entities\": [\"l1\"]\n" +
				"}";

		Signal<SignalBatchList> m = null;
		try {
			m = (Signal<SignalBatchList>) Message.deserialize(s);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Failed to deserialize message");
		}
		assertEquals(m.getEntities().get(0), "l1");
		assertEquals(m.getTopic(), "signals");
		assertEquals(m.getItems().size(), 1);
		assertEquals(m.getItem(0).getValues().size(), 3);
		assertEquals(m.getItem(0).getValues().get(0).getSubtype(), "oe-add");
		assertEquals(m.getItem(0).getValues().get(0).getValue(), (Double) 1.);
		assertEquals(m.getType(), "oe-vars");
		ZonedDateTime testDateTime = ZonedDateTime.of(2016,8,05, 12, 0, 1, 0, ZoneOffset.UTC);
		assertEquals(testDateTime.toLocalDate(), m.getItem(0).getStart().toLocalDate());

	}
	
}
