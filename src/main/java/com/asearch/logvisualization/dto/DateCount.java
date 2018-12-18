package com.asearch.logvisualization.dto;

import lombok.Data;

@Data
public class DateCount {
	private String date;
	private long count;
	
	public DateCount(String date, long count) {
		this.date = date;
		this.count = count;
	}
}