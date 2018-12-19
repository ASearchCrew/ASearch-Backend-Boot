package com.asearch.logvisualization.service;

import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.asearch.logvisualization.dto.IndexCacheModel;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Service
@Slf4j
public class IndexCacheServiceImpl implements IndexCacheService{
	
	private IndexCacheModel indexCacheModel;
	
	public IndexCacheServiceImpl() {
		indexCacheModel = new IndexCacheModel();
	}
	
	@Override
	public HashMap<String, List<String>> getIndexList() {
		return indexCacheModel.getIndexs();
	}

	@Override
	public void setIndexList(HashMap<String, List<String>> indexs) {
		indexCacheModel.setIndexs(indexs);
	}
}
