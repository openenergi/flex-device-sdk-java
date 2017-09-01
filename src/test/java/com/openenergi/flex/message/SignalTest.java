package com.openenergi.flex.message;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SignalTest {

	// All Signals are now BatchSignals so this is deprecated
	//@Test
	public void testSerializeSignalPoint() throws JSONException
	{
		Signal<SignalPointItem> s = new Signal<>();
		s.addEntity("l1");
		s.setTimestamp(12345L);
		s.setType("something");
		s.addItem(new SignalPointItem(ZonedDateTime.of(2016,12,27, 0, 0, 0, 0, ZoneId.of("UTC")), 1.23));

		String output = s.serialise();
		JSONAssert.assertEquals(output, "{\"topic\": \"signals\", \"entities\":[\"l1\"],\"items\":[{\"start_at\":\"2016-12-27T00:00:00Z\",\"value\":1.23}],\"timestamp\":12345,\"type\":\"something\"}", output, true);
	}

	@Test
	public void testSerializeSignalBatch() throws JSONException
	{
		Signal<SignalBatchList> s = new Signal<>();
		s.addEntity("l1");
		s.setTimestamp(12345L);
		s.setType("example batch signals");
		List<SignalBatchListItem> batchItems = new ArrayList<>();
		batchItems.add(new SignalBatchListItem("some-variable", 1.23));
		SignalBatchList batchList = new SignalBatchList();
		batchList.setStartAt(ZonedDateTime.of(2016,12,27, 0, 0, 0, 0, ZoneId.of("UTC")));
		batchList.setValues(batchItems);
		s.addItem(batchList);

		String output = s.serialise();
		JSONAssert.assertEquals(output, "{\"topic\": \"signals\", \"timestamp\":12345, \"type\":\"example batch signals\", \"entities\":[\"l1\"],\"items\":[{\"start_at\":\"2016-12-27T00:00:00Z\",\"values\":[{\"value\": 1.23,\"variable\":\"some-variable\"}]}]}", output, true);
	}

	// All Signals are now BatchSignals so this is deprecated
	//@Test
	public void testDeserializeSignalPoint() throws Exception
	{
		String s = "{\n" +
						"\t\"topic\": \"signals\"," +
						"\t\"entities\":[\"l1\"]," +
						"\t\"items\":[" +
						"\t{\n" +
						"\t\"start_at\":\"2016-12-27T00:00:00Z\"," +
						"\t\"value\":1.23" +
						"}]," +
						"\t\"timestamp\":12345," +
						"\t\"type\":\"something\"" +
						"}";

		Signal<SignalPointItem> signal = (Signal<SignalPointItem>)Message.deserialize(s);

		assertEquals(signal.getEntities().get(0), "l1");
		assertEquals(signal.getTopic(), "signals");
		assertEquals(signal.getItems().size(), 1);
		assertEquals(signal.getItem(0).getValues().size(), 1);
		assertEquals(signal.getItem(0).getValues().get(0).getValue(), 123, 0.1);
		assertEquals(signal.getType(), "something");
		ZonedDateTime testDateTime = ZonedDateTime.of(2016,12,27, 0, 0, 0, 0, ZoneId.of("UTC"));
		assertEquals(testDateTime.toLocalDate(), signal.getItem(0).getStartAt().toLocalDate());
	}

	@Test
	public void testDeserializeSignalBatch() throws IOException
	{
		String s = "{\n" +
				"\t\"generated_at\": \"2016-07-30T12:00:01.00Z\",\n" +
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

		Signal<SignalBatchList> m = (Signal<SignalBatchList>) Message.deserialize(s);

		assertEquals(m.getEntities().get(0), "l1");
		assertEquals(m.getTopic(), "signals");
		assertEquals(m.getItems().size(), 1);
		assertEquals(m.getItem(0).getValues().size(), 3);
		assertEquals(m.getItem(0).getValues().get(0).getVariable(), "oe-add");
		assertEquals(m.getItem(0).getValues().get(0).getValue(), 1.0, 0.1);
		assertEquals(m.getType(), "oe-vars");
		ZonedDateTime testDateTime = ZonedDateTime.of(2016,8,05, 12, 0, 1, 0, ZoneId.of("UTC"));
		assertEquals(testDateTime.toLocalDate(), m.getItem(0).getStartAt().toLocalDate());
	}
}
