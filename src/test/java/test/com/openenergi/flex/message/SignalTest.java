package com.openenergi.flex.message;

import static org.junit.Assert.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


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

		String output = null;
		try {
			output = s.toString();
			JSONAssert.assertEquals("{\"topic\": \"signals\", \"entities\":[\"l1\"],\"items\":[{\"start_at\":\"2016-12-27T00:00:00Z\",\"value\":1.23}],\"timestamp\":12345,\"type\":\"something\"}", output, true);
		} catch (Throwable t) {
			fail("Failed to serialize point: " + t.getMessage() + ". Output:\n" + output);
		}
	}

	@Test
	public void testSerializeBatchSignal() {
		Signal<SignalBatchList> s = new Signal<SignalBatchList>();
		s.addEntity("l1");
		s.setTimestamp(12345L);
		s.setType("example batch signals");
		List<SignalBatchListItem> items = new ArrayList<>();
		items.add(new SignalBatchListItem("subtype", 1.23));
		SignalBatchList list = new SignalBatchList(ZonedDateTime.of(LocalDateTime.of(2016,12,27, 0, 0, 0, 0), ZoneOffset.UTC), items);
		s.addItem(list);

		String output = null;
		try {
			output = s.toString();
			JSONAssert.assertEquals("{\"topic\": \"signals\", \"timestamp\":12345, \"type\":\"example batch signals\", \"entities\":[\"l1\"],\"items\":[{\"start_at\":\"2016-12-27T00:00:00Z\",\"values\":[{\"value\": 1.23,\"variable\":\"subtype\"}]}]}", output, true);
		} catch (Throwable t) {
			fail("Failed to serialize batch: " + t.getMessage() + ". Output:\n" + output);
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
		assertEquals(testDateTime.toLocalDate(), m.getItem(0).getStartAt().toLocalDate());

	}
	
}
