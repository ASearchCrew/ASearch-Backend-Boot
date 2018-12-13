package com.asearch.logvisualization.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class KeywordCountModel {
	private List<KeywordByDate> dateList = new ArrayList<KeywordByDate>();
}
