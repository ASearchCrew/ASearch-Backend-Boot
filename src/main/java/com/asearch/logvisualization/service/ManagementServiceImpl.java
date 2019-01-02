package com.asearch.logvisualization.service;

import com.asearch.logvisualization.dao.ManagementDao;
import com.asearch.logvisualization.dto.*;
import com.asearch.logvisualization.exception.AlreadyExistsException;
import com.asearch.logvisualization.exception.InternalServerErrorException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static com.asearch.logvisualization.util.Constant.*;

import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;


@AllArgsConstructor
@Service
@Slf4j
public class ManagementServiceImpl extends BaseServiceImpl implements ManagementService {

    private ManagementDao managementDao;
    private RestHighLevelClient client;

    //private AlarmService alarmService;
    private IndexCacheService indexCacheService;
    
    @Override
    public void modifyFilebeatConf(String path) throws Exception{
    	Socket socket = new Socket("52.79.220.131", 9001);
    	
    	OutputStream stream = socket.getOutputStream();
		stream.write(path.getBytes());
		socket.close();
    }

    @Override
    public void registerServerToMonitor(RegisterServerModel serverInfo) throws IOException {
        int checkResult = managementDao.checkHostName(
                buildSearchRequest(MANAGEMENT_SERVER_INDEX, MANAGEMENT_SERVER_TYPE, null),
                buildSearchSourceRequest(),
                serverInfo.getHostName());
        RestStatus status;
        switch (checkResult) {
            case NO_DATA:
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("hostIp", serverInfo.getHostIp());
                dataMap.put("interval", serverInfo.getInterval());
                status = managementDao.indexServer(
                        buildIndexRequest(MANAGEMENT_SERVER_INDEX, MANAGEMENT_SERVER_TYPE, serverInfo.getHostName(), dataMap));
                if (status != RestStatus.CREATED) throw new InternalServerErrorException("DB Error");
                break;
            case IS_DATA:
                throw new AlreadyExistsException("Already Exist");
            case ERROR_LOGIC_DATA:
                throw new InternalServerErrorException("DB Error");
        }
    }


	@Override
	public List<HashMap<String, Object>> getLogCountList() throws Exception{
//		Date date = new Date();
//		SimpleDateFormat  simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd");
//		List<HashMap<String, Object>> result = new ArrayList<HashMap<String, Object>>();
//		List<Object> serverList = getServerList();
//
//		List<String> nowIndexList = indexCacheService.getIndexList().get(simpleDateFormat.format(date).toString());
//
//		for(int i = 0; i < serverList.size(); i++) {
//			HashMap<String, Object> convert = (HashMap<String, Object>)serverList.get(i);
//			HashMap<String, Object> putData = new HashMap<String, Object>();
//
//			for(int j = 0; j < nowIndexList.size(); j++) {
//				String index = nowIndexList.get(j);
//
//				SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//				searchSourceBuilder.size(1);
//				searchSourceBuilder.query(QueryBuilders.matchQuery("beat.name", convert.get("hostName")));
//				searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.DESC));
//
//				SearchRequest searchRequest = new SearchRequest(index).types("doc").source(searchSourceBuilder);
//
//				SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
//
//				if(response.getHits().getTotalHits() == 0) { //12시정도 애매한 상황일 경우를 대비해서 이전날 로그까지 조회하는 예외처리가 필요.
//					Object lastTime = putData.get("lastTime");
//
//					if(lastTime == null) {
//						putData.put("timeStamp", "Exception");
//						putData.put("lastTime", 9999);
//						putData.put("hostIp", convert.get("hostIp"));
//						putData.put("hostName", convert.get("hostName"));
//					}
//
//					result.add(putData);
//				}else {
//					Object lastTime = putData.get("lastTime");
//
//					response.getHits().forEach(item -> {
//						ZonedDateTime utcDateTime = ZonedDateTime.now(ZoneId.of("UTC"));
//
//						String timezone = utcDateTime.toString();
//						String logTime = item.getSourceAsMap().get("@timestamp").toString();
//						int nowHour = Integer.parseInt(timezone.split("T")[1].split(":")[0]);
//						int nowMinute = Integer.parseInt(timezone.split("T")[1].split(":")[1]);
//						int logHour = Integer.parseInt(logTime.split("T")[1].split(":")[0]);
//						int logMinute = Integer.parseInt(logTime.split("T")[1].split(":")[1]);
//						int resultHour = nowHour - logHour;
//
//						if(nowMinute - logMinute < 0) {
//							if(resultHour > 0) {
//								resultHour -= 1;
//							}
//						}
//						//String beatName = item.getSourceAsMap().get("beat").toString().split(",")[0].split("=")[1];
//
//						if(lastTime != null) {
//							if((int)lastTime >= resultHour) {
//								putData.put("timeStamp", logTime);
//								putData.put("lastTime", resultHour);
//								putData.put("hostIp", convert.get("hostIp"));
//								putData.put("hostName", convert.get("hostName"));
//							}
//						}else {
//							putData.put("timeStamp", logTime);
//							putData.put("lastTime", resultHour);
//							putData.put("hostIp", convert.get("hostIp"));
//							putData.put("hostName", convert.get("hostName"));
//						}
//
//						result.add(putData);
//					});
//				}
//			}
//
//		}
//
//		return result;
		return null;
	}

	@Override
	public List<HashMap<String, Object>> getDateCountList() throws Exception {
		List<HashMap<String, Object>> result = new ArrayList<HashMap<String, Object>>();
		SimpleDateFormat  simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd");
		Calendar calendar = Calendar.getInstance();
		Date date = new Date();
		List<Object> serverList = getServerList();

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
				List<String> nowIndexList = indexCacheService.getIndexList().get(nowDate);
				HashMap<String, Object> innerValue = new HashMap<String, Object>();
				innerValue.put("date", nowDate);
				tempList.add(innerValue);

				for(int k = 0; k < nowIndexList.size(); k++) {
					String index = nowIndexList.get(k);
					SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
					searchSourceBuilder.query(QueryBuilders.matchQuery("beat.name", convert.get("hostName")));

					SearchRequest searchRequest = new SearchRequest(index).types("doc").source(searchSourceBuilder);

					try {
						long count = client.search(searchRequest, RequestOptions.DEFAULT).getHits().getTotalHits();
						Object tempValue = innerValue.get("count");

						if(tempValue == null) {
							innerValue.put("count", count);
						}else {
							innerValue.put("count", (long)tempValue+count);
						}
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
						List<String> nowIndexList = indexCacheService.getIndexList().get(simpleDateFormat.format(calendar.getTime()).toString());

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
	public void deleteServerToMonitor(DeleteServerModel deleteServerModel) throws IOException {

		DeleteRequest deleteRequest = buildDeleteRequest(MANAGEMENT_SERVER_INDEX, MANAGEMENT_SERVER_TYPE, deleteServerModel.getHostName());
		DeleteResponse deleteResponse = client.delete(deleteRequest, RequestOptions.DEFAULT);
	}

	@Override
	public LogCountBySecondsModel getLogCountBySeconds() throws IOException {
		Date date = new Date();
		SimpleDateFormat  simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd");
		LogCountBySecondsModel logCountBySecondsModel = new LogCountBySecondsModel();
		List<String> nowIndexList = indexCacheService.getIndexList().get(simpleDateFormat.format(date).toString());

		Instant instant = Instant.now();
		ZoneId zoneId = ZoneId.of("UTC");
		ZonedDateTime nowTime = ZonedDateTime.ofInstant(instant, zoneId);
		String endString = nowTime.toString().split(":")[0]+":"+nowTime.toString().split(":")[1]+":00.000Z";
		ZonedDateTime endTime = ZonedDateTime.parse(endString);

		int minusMinute = 1;

		ZonedDateTime startTime;
		for(int i = 1; i < 61; i++) {
			startTime = endTime.minusMinutes(minusMinute);

			LogCountByMinutesModel logCountByMinutesModel = new LogCountByMinutesModel();
			logCountByMinutesModel.setStartTime(startTime.toString());
			logCountByMinutesModel.setEndTime(endTime.toString());

			for(int j = 0; j < nowIndexList.size(); j++) {
				String index = nowIndexList.get(j);

				SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
				searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").gte(startTime.toString().split("Z")[0]+"Z").lte(endTime.toString().split("Z")[0]+"Z"));
				SearchRequest searchRequest = new SearchRequest(index).types("doc").source(searchSourceBuilder);

				SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

				logCountByMinutesModel.setLogCount(logCountByMinutesModel.getLogCount()+response.getHits().getTotalHits());
			}

			endTime = startTime;
			logCountBySecondsModel.getCharDatas().add(logCountByMinutesModel);
		}

		return logCountBySecondsModel;
	}
}
