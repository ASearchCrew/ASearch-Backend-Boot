package com.asearch.logvisualization.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import com.asearch.logvisualization.dto.LogCountBySecondsModel;
import com.asearch.logvisualization.dto.RegisterServerDto;
import com.asearch.logvisualization.exception.AlreadyExistsException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Service
@Slf4j
public class ManagementServiceImpl implements ManagementService {

    private RestHighLevelClient client;
    //private AlarmService alarmService;
    
    @Override
    public void modifyFilebeatConf(String path) throws Exception{
    	/*int PORT = 8080;
    	Socket socket = new Socket("192.168.157.128", 8080);
    	
    	OutputStream stream = socket.getOutputStream();
		stream.write(path.getBytes());
		socket.close();*/
    	/*
    	GetIndexRequest request = new GetIndexRequest().indices("_all"); 
    	request.includeDefaults(true); 
    	request.indicesOptions(IndicesOptions.lenientExpandOpen());
    	
    	GetIndexResponse getIndexResponse = client.indices().get(request, RequestOptions.DEFAULT);
    	
    	for(int i = 0; i < getIndexResponse.getIndices().length; i++) {
    		System.out.println(getIndexResponse.getIndices()[i]);
    	}
    	
    	Date date = new Date();
    	SimpleDateFormat  simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd");
    	Calendar calendar = Calendar.getInstance();
    	
    	HashMap<String, List<String>> indexs = new HashMap<String, List<String>>();
    	
    	for(int i = 9; i >= 0; i--) {
    		calendar.setTime(date);
    		calendar.add(Calendar.DATE, -i);
    		
    		for(int j = 0; j < getIndexResponse.getIndices().length; j++) {
        		String index = getIndexResponse.getIndices()[j].toString();
        		
        		if(index.contains("filebeat")) {
        			if(index.contains(simpleDateFormat.format(calendar.getTime()).toString())) {
        				List<String> indexList = indexs.get(simpleDateFormat.format(calendar.getTime()).toString());
        				
        				if(indexList == null) {
        					indexList = new ArrayList<String>();
        					indexList.add(index);
        				}else {
        					indexList.add(index);
        				}
        				
        				indexs.put(simpleDateFormat.format(calendar.getTime()).toString(), indexList);
        			}
        		}
        	}
    	}*/    	
    }

    @Override
    public void registerServerToMonitor(RegisterServerDto serverInfo) throws IOException {
        SearchRequest searchRequest = new SearchRequest("server");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("host_ip", serverInfo.getHostIp()));
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        if (response.getHits().getHits().length != 0) throw new AlreadyExistsException("Already Exist");
        else {
            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("host_ip", serverInfo.getHostIp());
            jsonMap.put("host_name", serverInfo.getHostName());

            IndexRequest request = new IndexRequest(
                    "server",
                    "doc")
                    .source(jsonMap);
            IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT); //Todo Exception?
        }
    }

    @Override
    public List<HashMap<String, Object>> getLogCountList() throws Exception{
    	//filebeat-6.5.0
    	//bootwas-6.5.1
    	Date date = new Date();
    	SimpleDateFormat  simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd");
    	List<HashMap<String, Object>> result = new ArrayList<HashMap<String, Object>>();
    	List<Object> serverList = getServerList();
    	
    	GetIndexRequest request = new GetIndexRequest().indices("_all"); 
    	request.includeDefaults(true); 
    	request.indicesOptions(IndicesOptions.lenientExpandOpen());
    	
    	GetIndexResponse getIndexResponse = client.indices().get(request, RequestOptions.DEFAULT);

    	for(int j = 0; j < getIndexResponse.getIndices().length; j++) {
    		String index = getIndexResponse.getIndices()[j].toString();

    		if(index.contains("filebeat")) {
    			if(index.contains(simpleDateFormat.format(date))) {
    				for(int i = 0; i < serverList.size(); i++) {
    		    		HashMap<String, Object> convert = (HashMap<String, Object>)serverList.get(i);
    		    		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    		    		searchSourceBuilder.size(1);
    		    		searchSourceBuilder.query(QueryBuilders.matchQuery("beat.name", convert.get("hostName")));
    		    		searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.DESC));

    		    		SearchRequest searchRequest = new SearchRequest(index).types("doc").source(searchSourceBuilder);

    		    		SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
    		    		
    		    		if(response.getHits().getTotalHits() == 0) { //12시정도 애매한 상황일 경우를 대비해서 이전날 로그까지 조회하는 예외처리가 필요.
    		    			HashMap<String, Object> putData = new HashMap<String, Object>();
    		    			putData.put("timeStamp", "Exception");
    		    			putData.put("lastTime", 9999);
    		    			putData.put("hostIp", convert.get("hostIp"));
    						putData.put("hostName", convert.get("hostName"));
    		    			
    		    			result.add(putData);

    		    			//이곳에서 다시 쿼리문 작성? try catch를 없애버리고 throws로 처리해버리자.
    		    			//
    		    			/*searchSourceBuilder = new SearchSourceBuilder();

    							searchSourceBuilder.size(1);
    							searchSourceBuilder.query(QueryBuilders.matchQuery("beat.name", serverList[i]));
    							searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.DESC));

    							SearchRequest reSearchRequest = new SearchRequest(index).types("doc").source(searchSourceBuilder);
    							// 이곳에서 다시 쿼리를 받아온다.

    							if(response.getHits().getTotalHits() == 0) {
    								putData.put("timestamp", "Exception");
    								putData.put("lasttime", 24);
    								putData.put("server", serverName);

    								result.add(putData);
    							}else {
    								//이곳에서 timestamp 다시 정의해줘서 보내준다.
    								//시간도 계산해서 보내줘야함.
    								//현재시간과 과거 데이터 시간.
    								//현재시간이 01시. 과거시간이 23시면...
    								//2시간차이.
    								//정확한 계산이 힘들다

    								putData.put("timestamp", "Exception");
    								putData.put("lasttime", 24);
    								putData.put("server", serverName);

    								result.add(putData);
    							}*/
    		    		}else {
    		    			response.getHits().forEach(item -> {
    		    				ZonedDateTime utcDateTime = ZonedDateTime.now(ZoneId.of("UTC"));

    		    				String timezone = utcDateTime.toString();
    		    				String logTime = item.getSourceAsMap().get("@timestamp").toString();
    		    				int nowHour = Integer.parseInt(timezone.split("T")[1].split(":")[0]);
    		    				int nowMinute = Integer.parseInt(timezone.split("T")[1].split(":")[1]);
    		    				int logHour = Integer.parseInt(logTime.split("T")[1].split(":")[0]);
    		    				int logMinute = Integer.parseInt(logTime.split("T")[1].split(":")[1]);
    		    				int resultHour = nowHour - logHour;

    		    				if(nowMinute - logMinute < 0) {
    		    					if(resultHour > 0) {
    		    						resultHour -= 1;
    		    					}
    		    				}
    		    				//String beatName = item.getSourceAsMap().get("beat").toString().split(",")[0].split("=")[1];

    		    				HashMap<String, Object> putData = new HashMap<String, Object>();
    		    				putData.put("timeStamp", logTime);
    		    				putData.put("lastTime", resultHour);
    		    				putData.put("hostIp", convert.get("hostIp"));
    		    				putData.put("hostName", convert.get("hostName"));

    		    				result.add(putData);
    		    			});
    		    		}
    			}
    		}
    	}

    	//만약 오늘날짜 로그가 존재하지 않는다면, 이전날짜의 마지막 로그를 불러와서 보여준다.
    	//이전날짜 로그마저 존재하지 않는다면 error처리로 보낸다.
    	//이전날짜 시간처리도 생각해야한다.
    	}

    	return result;
	}

	@Override
	public List<HashMap<String, Object>> getDateCountList() throws Exception {
		List<HashMap<String, Object>> result = new ArrayList<HashMap<String, Object>>();
		SimpleDateFormat  simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd");
		Calendar calendar = Calendar.getInstance();
		Date date = new Date();
		List<Object> serverList = getServerList();
		
		GetIndexRequest request = new GetIndexRequest().indices("_all"); 
    	request.includeDefaults(true); 
    	request.indicesOptions(IndicesOptions.lenientExpandOpen());
    	
    	GetIndexResponse getIndexResponse = client.indices().get(request, RequestOptions.DEFAULT);
    	HashMap<String, List<String>> indexs = new HashMap<String, List<String>>();
    	
    	for(int i = 9; i >= 0; i--) {
    		calendar.setTime(date);
    		calendar.add(Calendar.DATE, -i);
    		
    		for(int j = 0; j < getIndexResponse.getIndices().length; j++) {
        		String index = getIndexResponse.getIndices()[j].toString();
        		
        		if(index.contains("filebeat")) {
        			if(index.contains(simpleDateFormat.format(calendar.getTime()).toString())) {
        				List<String> indexList = indexs.get(simpleDateFormat.format(calendar.getTime()).toString());
        				
        				if(indexList == null) {
        					indexList = new ArrayList<String>();
        					indexList.add(index);
        				}else {
        					indexList.add(index);
        				}
        				
        				indexs.put(simpleDateFormat.format(calendar.getTime()).toString(), indexList);
        			}
        		}
        	}
    	}
		
		for(int i = 0; i < serverList.size(); i++) {
			HashMap<String, Object> convert = (HashMap<String, Object>)serverList.get(i);
			HashMap<String, Object> answer = new HashMap<String, Object>();
			List<HashMap<String, Object>> tempList = new ArrayList<HashMap<String, Object>>();
			
			answer.put("hostIp", convert.get("hostIp"));
			answer.put("hostName", convert.get("hostName"));
			
			for(int j = 9; j >= 0; j--) {
				calendar.setTime(date);
				calendar.add(Calendar.DATE, -j);
				
				String nowDate = simpleDateFormat.format(calendar.getTime()).toString();
				
				List<String> nowIndexList = indexs.get(nowDate);
				
				for(int k = 0; k < nowIndexList.size(); k++) {
					String index = nowIndexList.get(k);
					HashMap<String, Object> innerValue = new HashMap<String, Object>();
					innerValue.put("date", simpleDateFormat.format(calendar.getTime()));
					
					SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
					searchSourceBuilder.query(QueryBuilders.matchQuery("beat.name", convert.get("hostName")));
					
					SearchRequest searchRequest = new SearchRequest(index).types("doc").source(searchSourceBuilder);
					
					try {
						long count = client.search(searchRequest, RequestOptions.DEFAULT).getHits().getTotalHits();
						Integer tempValue = (Integer) innerValue.get("count");
						
						if(tempValue == null) {
							innerValue.put("count", count);
						}else {
							innerValue.put("count", tempValue+count);
						}

						tempList.add(innerValue);
					}catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
			answer.put("chartDatas", tempList);
			result.add(answer);
		}
		
		return result;
	}

	@Override
	public List<Object> getServerList() throws IOException {
		List<Object> result = new ArrayList<Object>();
		
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        SearchRequest searchRequest = new SearchRequest("server-info").types("info").source(searchSourceBuilder);
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        response.getHits().forEach(item -> {
        	HashMap<String, Object> innerTemp = new HashMap<String, Object>();
        	innerTemp.put("hostIp", item.getSourceAsMap().get("hostIp").toString());
        	innerTemp.put("hostName", item.getId());
        	result.add(innerTemp);
		});
        
		return result;
	}

	@Override
	public List<HashMap<String, Object>> getKeywordCountList(String hostName) throws IOException {
		List<HashMap<String, Object>> realResult = new ArrayList<HashMap<String, Object>>();
		
		SimpleDateFormat  simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd");
		Calendar calendar = Calendar.getInstance();
		Date date = new Date();
		
		GetIndexRequest request = new GetIndexRequest().indices("_all"); 
    	request.includeDefaults(true); 
    	request.indicesOptions(IndicesOptions.lenientExpandOpen());
    	
    	GetIndexResponse getIndexResponse = client.indices().get(request, RequestOptions.DEFAULT);
    	HashMap<String, List<String>> indexs = new HashMap<String, List<String>>();
    	
    	for(int i = 9; i >= 0; i--) {
    		calendar.setTime(date);
    		calendar.add(Calendar.DATE, -i);
    		
    		for(int j = 0; j < getIndexResponse.getIndices().length; j++) {
        		String index = getIndexResponse.getIndices()[j].toString();
        		
        		if(index.contains("filebeat")) {
        			if(index.contains(simpleDateFormat.format(calendar.getTime()).toString())) {
        				List<String> indexList = indexs.get(simpleDateFormat.format(calendar.getTime()).toString());
        				
        				if(indexList == null) {
        					indexList = new ArrayList<String>();
        					indexList.add(index);
        				}else {
        					indexList.add(index);
        				}
        				
        				indexs.put(simpleDateFormat.format(calendar.getTime()).toString(), indexList);
        			}
        		}
        	}
    	}
		
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.matchQuery("_id", hostName));
		SearchRequest searchRequest = new SearchRequest("server-info").types("info").source(searchSourceBuilder);
		
		SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

		response.getHits().forEach(item -> {
			String resultHostName = item.getId();
			List<HashMap<String, Object>> resultKeywordList = (List<HashMap<String, Object>>) item.getSourceAsMap().get("keywords");

			for(int j = 9; j >= 0; j--) {
				calendar.setTime(date);
				calendar.add(Calendar.DATE, -j);
				
				if(resultKeywordList == null) {
					HashMap<String, Object> contents = new HashMap<String, Object>();
					contents.put("date", simpleDateFormat.format(calendar.getTime()));
				}else {
					HashMap<String, Object> contents = new HashMap<String, Object>();
					contents.put("date", simpleDateFormat.format(calendar.getTime()));

					realResult.add(contents);

					for(int i = 0; i < resultKeywordList.size(); i++) {
						String resultKeyword = resultKeywordList.get(i).get("keyword").toString();
						
						List<String> nowIndexList = indexs.get(simpleDateFormat.format(calendar.getTime()).toString());
						for(int k = 0; k < nowIndexList.size(); k++) {
							String index = nowIndexList.get(k);
							
							SearchSourceBuilder searchSourceBuilder1 = new SearchSourceBuilder();
							searchSourceBuilder1.query(QueryBuilders.matchQuery("beat.name", resultHostName));
							searchSourceBuilder1.query(QueryBuilders.matchQuery("message", resultKeyword));
							SearchRequest searchRequest1 = new SearchRequest(index).types("doc").source(searchSourceBuilder1);

							try {
								SearchResponse response1 = client.search(searchRequest1, RequestOptions.DEFAULT);
								Object tempValue = contents.get("resultKeyword");
								
								if(tempValue == null) {
									contents.put(resultKeyword, (long)response1.getHits().getTotalHits());
								}else {
									contents.put(resultKeyword, (long)tempValue+(long)response1.getHits().getTotalHits());
								}
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		});

		return realResult;
	}

	@Override
	public void deleteServerToMonitor(String hostIp) throws IOException {
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.matchQuery("host_ip", hostIp));
		SearchRequest searchRequest = new SearchRequest("server").types("doc").source(searchSourceBuilder);
		
		SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
		
		response.getHits().forEach(item -> {
			DeleteRequest deleteRequest = new DeleteRequest("server", "doc", item.getId());
	        try {
				DeleteResponse deleteResponse = client.delete(deleteRequest, RequestOptions.DEFAULT);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		
		//DeleteRequest deleteRequest = new DeleteRequest("server", "doc", hostIp);
        //DeleteResponse deleteResponse = client.delete(deleteRequest, RequestOptions.DEFAULT);
		
	}

	@Override
	public LogCountBySecondsModel getLogCountBySeconds() throws IOException {
		Date date = new Date();
		SimpleDateFormat  simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd");
    	LogCountBySecondsModel logCountBySecondsModel = new LogCountBySecondsModel();
    	
    	GetIndexRequest request = new GetIndexRequest().indices("_all"); 
    	request.includeDefaults(true); 
    	request.indicesOptions(IndicesOptions.lenientExpandOpen());
    	
    	GetIndexResponse getIndexResponse = client.indices().get(request, RequestOptions.DEFAULT);
    	
    	for(int i = 0; i < getIndexResponse.getIndices().length; i++) {
    		String index = getIndexResponse.getIndices()[i].toString();

    		if(index.contains("filebeat")) {
    			if(index.contains(simpleDateFormat.format(date))) {
    				Instant instant = Instant.now();
    				ZoneId zoneId = ZoneId.of("UTC");
    				ZonedDateTime endTime = ZonedDateTime.ofInstant(instant, zoneId);
    				ZonedDateTime startTime = endTime.minusSeconds(60);
    		    	
    		    	SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    				searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").gte(startTime.toString().split("Z")[0]+"Z").lte(endTime.toString().split("Z")[0]+"Z"));
    				SearchRequest searchRequest = new SearchRequest(index).types("doc").source(searchSourceBuilder);
    				
    				SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
    				
    				logCountBySecondsModel.setLogCount(response.getHits().getTotalHits());
    				logCountBySecondsModel.setStartTime(startTime.toString().split("Z")[0]+"Z");
    				logCountBySecondsModel.setEndTime(endTime.toString().split("Z")[0]+"Z");
    			}
    		}
    	}

    	return logCountBySecondsModel;
	}
}
