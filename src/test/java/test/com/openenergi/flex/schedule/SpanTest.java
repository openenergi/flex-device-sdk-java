package com.openenergi.flex.schedule;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.Test;


public class SpanTest {

	@Test
	public void testParseISO8601Nano() {
		try {
			Span s = new Span("2016-01-01T12:00:00.000Z/P1D");
			assertTrue(s.start.equals(ZonedDateTime.of(LocalDateTime.of(2016, 1, 1, 12, 0), ZoneOffset.UTC)));
			assertTrue(s.length.equals(Duration.ofDays(1)));
		} catch (IllegalArgumentException ex){
			ex.printStackTrace();
			fail("Failed testParse " + ex.getMessage());
		}
	}
	
	@Test
	public void testParseISO8601Nano2() {
		try {
			Span s = new Span("2016-08-27T23:00:00.000Z/PT1H");
			assertTrue(s.start.equals(ZonedDateTime.of(LocalDateTime.of(2016, 8, 27, 23, 0), ZoneOffset.UTC)));
			assertTrue(s.length.equals(Duration.ofHours(1)));
		} catch (IllegalArgumentException ex){
			fail("Failed testParse " + ex.getMessage());
		}
	}
	
	@Test
	public void testParseInvalid() {
		try {
			new Span("2016-08-27T23:00:00.000Z");
		} catch (IllegalArgumentException expected){
			return;
		}
		fail("Expected invalid span string to cause exception");
	}
	
	@Test
	public void testParseInvalid2() {
		try {
			new Span("2016-08-2723:00:00.000Z/P1D");
		} catch (IllegalArgumentException expected){
			return;
		}
		fail("Expected invalid datetime string to cause exception");
	}
	
	@Test
	public void testParseInvalid3() {
		try {
			new Span("2016-08-27T23:00:00.000Z/P1H");
		} catch (IllegalArgumentException expected){
			return;
		}
		fail("Expected invalid duration string to cause exception");
	}
	
	@Test
	public void testFormat(){
		String expected = "2016-08-27T23:00:00Z/PT1H";
		Span s = new Span(ZonedDateTime.of(LocalDateTime.of(2016, 8, 27, 23, 0), ZoneOffset.UTC), Duration.ofHours(1));
		assertEquals(s.toString(), expected);
	}
	
	@Test
	public void testEndTime(){
		ZonedDateTime expected = ZonedDateTime.of(LocalDateTime.of(2016, 8,27,13,0), ZoneOffset.UTC);
		Span s = new Span(ZonedDateTime.of(LocalDateTime.of(2016, 8, 27, 12, 0), ZoneOffset.UTC), Duration.ofHours(1));
		assertTrue(s.getEndTime().equals(expected));
	}

	@Test
	public void testContainsCurrentTime(){
		ZonedDateTime now = ZonedDateTime.now();
		Span s = new Span(now.minusMinutes(1), Duration.ofMinutes(2)); //unless the test takes more than a minute to run this will contain now
		assertTrue(s.containsCurrentTime());
	}
	
	@Test
	public void testDoesntContainCurrentTime(){
		ZonedDateTime now = ZonedDateTime.now();
		Span s = new Span(now.minusMinutes(2), Duration.ofMinutes(1));
		assertFalse(s.containsCurrentTime());
	}
}
