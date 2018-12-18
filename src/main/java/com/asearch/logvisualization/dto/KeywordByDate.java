package com.asearch.logvisualization.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class KeywordByDate {
	private String keyword;
	private List<DateCount> dateCount = new ArrayList<DateCount>();
}
