package com.asearch.logvisualization.dto;

import java.util.List;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class DateCount {
	private String date;
	private long count;
	
	public DateCount(String date, long count) {
		this.date = date;
		this.count = count;
	}
}
