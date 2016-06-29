package com.openenergi.flex.schedule;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public class RecurringSpan {
	private Span span;
	private Duration repeat;
	
	public RecurringSpan(Span span, Duration repeat){
		this.span = span;
		this.repeat = repeat;
	}
	
	/**
	 * Parses the given span and repeat into a RecurringSpan.
	 * @param span The ISO-8601 formatted interval (eg. "2016-01-01/P1D")
	 * @param repeat The ISO-8601 formatted repeat (eg. "P7D")
	 * @throws IllegalArgumentException
	 */
	public RecurringSpan(String span, String repeat) throws IllegalArgumentException {
		this.span = new Span(span);
		
		try {
			this.repeat = Duration.parse(repeat);
		} catch (DateTimeParseException ex){
			throw new IllegalArgumentException("Invalid repeat value: " + ex.toString());
		}
	}
	
	/**
	 * Returns the next span that will contain the current time or exceed it.
	 * @return
	 */
	public Span getNextSpan(){
		LocalDateTime now = LocalDateTime.now();
		Span curr = new Span(this.span.start, this.span.length);
		while(!(curr.containsCurrentTime() || curr.start.isAfter(now))){
			curr.start = LocalDateTime.from(curr.length.addTo(curr.start));
		}
		return curr;
	}
}
