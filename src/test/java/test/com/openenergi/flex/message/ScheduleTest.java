package com.openenergi.flex.message;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.google.gson.JsonSyntaxException;
import com.openenergi.flex.schedule.Span;


public class ScheduleTest {
	

	@Test
	public void testSerializeSchedule() {
		Schedule e = (Schedule) new Schedule()
					.setEntity("l1")
					.setTimestamp(12345L)
					.setType("something");

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
			assertEquals(m.entity, "l1");
			assertEquals(m.topic, "schedules");
			assertEquals((Long) 12345L, m.timestamp);
			assertEquals(m.type, "something");
			if (m instanceof Schedule){
				ScheduleItem i = ((Schedule)m).schedule.get(0);
				assertEquals(i.span, "2016-12-27T23:00:00.000Z/PT1H");
				assertEquals(i.repeat, "PT168H"); //FIXME(mbironneau) - 168H is not always equal to 7D with DST; not sure why this conversion is happening.
				assertEquals(i.value, "value");
			} else {
				fail("not an schedule");
			}
			
		} catch (JsonSyntaxException e1) {
			fail("Failed to deserialize: " + e1.getMessage());
		}
	}
	
}
