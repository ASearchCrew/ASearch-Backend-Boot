package com.asearch.logvisualization.job;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.asearch.logvisualization.service.IndexCacheService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Component
@Slf4j
public class IndexCacheJob {
	
	private RestHighLevelClient client;
	private IndexCacheService indexCacheService;
	
	@Scheduled(fixedDelay=30000)
	public void aJob() throws IOException {		
		GetIndexRequest request = new GetIndexRequest().indices("_all"); 
    	request.includeDefaults(true); 
    	request.indicesOptions(IndicesOptions.lenientExpandOpen());
    	
    	GetIndexResponse getIndexResponse = client.indices().get(request, RequestOptions.DEFAULT);
		
		Date date = new Date();
    	SimpleDateFormat  simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd");
    	Calendar calendar = Calendar.getInstance();
    	
    	HashMap<String, List<String>> indexs = new HashMap<String, List<String>>();
    	
    	for(int i = 9; i >= 0; i--) {
    		calendar.setTime(date);
    		calendar.add(Calendar.DATE, -i);
    		List<String> indexList = new ArrayList<String>();
    		
    		for(int j = 0; j < getIndexResponse.getIndices().length; j++) {
        		String index = getIndexResponse.getIndices()[j].toString();
        		
        		if(index.contains("filebeat")) {
        			if(index.contains(simpleDateFormat.format(calendar.getTime()).toString())) {
        				//List<String> indexList = indexs.get(simpleDateFormat.format(calendar.getTime()).toString());
        				indexList.add(index);
        			}
        		}
        	}
    		indexs.put(simpleDateFormat.format(calendar.getTime()).toString(), indexList);
    	}
    	indexCacheService.setIndexList(indexs);
	}
}
