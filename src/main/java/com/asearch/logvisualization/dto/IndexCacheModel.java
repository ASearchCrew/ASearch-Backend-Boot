package com.asearch.logvisualization.dto;

import java.util.HashMap;
import java.util.List;

import lombok.Data;

@Data
public class IndexCacheModel {
	private HashMap<String, List<String>> indexs = new HashMap<String, List<String>>();
}
