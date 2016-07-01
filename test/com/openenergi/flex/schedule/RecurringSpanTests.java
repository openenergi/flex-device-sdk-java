package com.openenergi.flex.schedule;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.Test;


public class RecurringSpanTests {
	

	@Test
	public void testGetNextSpanNow() {
		try {
			LocalDateTime now = LocalDateTime.now();
			Span s = new Span(now, Duration.ofHours(1));
			RecurringSpan rs = new RecurringSpan(s, Duration.ofDays(1));
			Span s2 = rs.getNextSpan();
			assertTrue(s2.start.equals(now));
			assertTrue(s2.length.equals(Duration.ofHours(1)));
		} catch (IllegalArgumentException ex){
			fail("Failed testParse " + ex.getMessage());
		}
	}

	@Test
	public void testGetNextSpanNotNow() {
		try {
			LocalDateTime now = LocalDateTime.now().minusMinutes(2);
			Span s = new Span(now, Duration.ofMinutes(1));
			RecurringSpan rs = new RecurringSpan(s, Duration.ofMinutes(4));
			Span s2 = rs.getNextSpan();
			assertTrue(s2.start.equals(now.plusMinutes(2)));
			assertTrue(s2.length.equals(Duration.ofMinutes(1)));
		} catch (IllegalArgumentException ex){
			fail("Failed testParse " + ex.getMessage());
		}
	}
	
	@Test
	public void testGetNextSpanAdjacent(){
		try {
			LocalDateTime now = LocalDateTime.now().minusMinutes(2);
			Span s = new Span(now, Duration.ofMinutes(1));
			RecurringSpan rs = new RecurringSpan(s, Duration.ofMinutes(1));
			Span s2 = rs.getNextSpan();
			assertTrue(s2.start.equals(now.plusMinutes(2)));
			assertTrue(s2.length.equals(Duration.ofMinutes(1)));
		} catch (IllegalArgumentException ex){
			fail("Failed testParse " + ex.getMessage());
		}
	}
	

}
