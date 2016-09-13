package com.openenergi.flex.message;

import com.openenergi.flex.schedule.Span;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


public class ScheduleTest {
	

	@Test
	public void testSerializeSchedule() {
		Schedule e = new Schedule.Builder()
					.withEntity("l1")
					.atTime(12345L)
					.withCustomType("something")
					.build();

		ScheduleItem item = new ScheduleItem(new Span(ZonedDateTime.of(LocalDateTime.of(2016, 12, 27, 23, 0), ZoneOffset.UTC), Duration.ofHours(1)), Duration.ofDays(7), "value");
		
		e.addItem(item);
		
		try {
			//System.out.println(e.toString());
			//FIXME(mbironneau) - 168H is not always equal to 7D with DST; not sure why this conversion is happening.
			JSONAssert.assertEquals("{\"topic\": \"schedules\", \"entity\": \"l1\",  \"timestamp\": 12345,  \"type\": \"something\", \"schedule\": [{\"span\": \"2016-12-27T23:00:00Z/PT1H\", \"repeat\": \"PT168H\", \"value\": \"value\"}]}", e.toString(), true);
		} catch (JSONException e1) {
			fail("Failed to serialize: " + e1.getMessage());
		}
	}

	@Test
	public void testDeserializeSchedule() {
		String s = "{\"topic\": \"schedules\", \"entity\": \"l1\",  \"timestamp\": 12345,  \"type\": \"something\", \"schedule\": [{\"span\": \"2016-12-27T23:00:00.000Z/PT1H\", \"repeat\": \"PT168H\", \"value\": \"value\"}]}";
		try {
			Schedule m = (Schedule) Message.deserialize(s);
			assertEquals(m.getEntity(), "l1");
			assertEquals(m.getTopic(), "schedules");
			assertEquals((Long) 12345L, m.getTimestamp());
			assertEquals(m.getType(), "something");
			if (m instanceof Schedule){
				ScheduleItem i = ((Schedule)m).getSchedule().get(0);
				assertEquals(i.getSpan(), "2016-12-27T23:00:00.000Z/PT1H");
				assertEquals(i.getRepeat(), "PT168H"); //FIXME(mbironneau) - 168H is not always equal to 7D with DST; not sure why this conversion is happening.
				assertEquals(i.getValue(), "value");
			} else {
				fail("not an schedule");
			}
			
		} catch (IOException e1) {
			fail("Failed to deserialize: " + e1.getMessage());
		}
	}
	
}
