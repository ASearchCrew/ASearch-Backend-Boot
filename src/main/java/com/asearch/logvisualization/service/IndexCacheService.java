package com.asearch.logvisualization.service;

import java.util.HashMap;
import java.util.List;

public interface IndexCacheService {
	HashMap<String, List<String>> getIndexList();
	void setIndexList(HashMap<String, List<String>> indexs);
}
