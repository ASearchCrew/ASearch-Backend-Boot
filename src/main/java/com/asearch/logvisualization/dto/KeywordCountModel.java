package com.asearch.logvisualization.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class KeywordCountModel {
	private List<KeywordByDate> dateList = new ArrayList<KeywordByDate>();
}
