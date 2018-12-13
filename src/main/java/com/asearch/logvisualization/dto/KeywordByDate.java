package com.asearch.logvisualization.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class KeywordByDate {
	private String keyword;
	private List<DateCount> dateCount = new ArrayList<DateCount>();
}
