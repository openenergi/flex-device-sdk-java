package com.openenergi.flex.schedule;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.TimeZone;

public class Span {
	public LocalDateTime start;
	public Duration length;
	
	
	public Span(LocalDateTime start, Duration length){
		this.start = start;
		this.length = length;
	}
	
	/**
	 * Parses Span from string.
	 * @param str ISO8601-formatted string.
	 */
	public Span(String str) throws IllegalArgumentException {
		//Step 1: attempt to separate start and duration
		String[] parts = str.split("/");
		if (parts.length != 2){
			throw new IllegalArgumentException("Expected one occurence of \"/\"");
		}
		
		//Step 2: Parse start time
		try {
			this.start = LocalDateTime.parse(parts[0]);
		} catch (DateTimeParseException ex){
			throw new IllegalArgumentException("Invalid start date: " + ex.toString());
		}
		
		//Step 3: Parse the duration after the "/"
		try {
			this.length = Duration.parse(parts[1]);
		} catch (DateTimeParseException ex){
			throw new IllegalArgumentException("Invalid duration value: " + ex.toString());
		}
	}
	
	/**
	 * Formats the Span according to ISO8601 notation.
	 */
	public String toString(){
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		return df.format(this.start) + "/" + this.length.toString();
	}
	
	/**
	 * Returns the end time for the span (start + duration)
	 * @return end time
	 */
	public LocalDateTime getEndTime(){
		return LocalDateTime.from(this.length.addTo(this.start));
	}
	
	/**
	 * Determines whether this span contains the current time or not.
	 * @return True if the span contains the current time, False otherwise.
	 */
	public Boolean containsCurrentTime() {
		LocalDateTime now = LocalDateTime.now();
		if (this.start.isBefore(now) && this.getEndTime().isAfter(now)){
			return true;
		} else {
			return false;
		}
	}
	
	
}
