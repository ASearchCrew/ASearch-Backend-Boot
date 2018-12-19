package com.asearch.logvisualization.service;

import com.asearch.logvisualization.dao.ManagementDao;
import com.asearch.logvisualization.dto.RegisterServerModel;
import com.asearch.logvisualization.dto.ServerListDto;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static com.asearch.logvisualization.util.Constant.*;

@AllArgsConstructor
@Service
@Slf4j
public class ManagementServiceImpl extends BaseServiceImpl implements ManagementService {

    private ManagementDao managementDao;
    private RestHighLevelClient client;

    @Override
    public void modifyFilebeatConf(String path) throws Exception {
        int PORT = 8080;
        Socket socket = new Socket("192.168.157.128", PORT);

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
    public List<HashMap<String, Object>> getLogCountList() throws Exception {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd");
        String index = "filebeat-6.5.0-" + simpleDateFormat.format(new Date());
        List<HashMap<String, Object>> result = new ArrayList<HashMap<String, Object>>();
        List<Object> serverList = getServerList();
        for (int i = 0; i < serverList.size(); i++) {
            HashMap<String, Object> convert = (HashMap<String, Object>) serverList.get(i);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.size(1);
            searchSourceBuilder.query(QueryBuilders.matchQuery("beat.hostname", convert.get("host_name")));
            searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.DESC));

            SearchRequest searchRequest = new SearchRequest(index).types("doc").source(searchSourceBuilder);

            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

            if (response.getHits().getTotalHits() == 0) { //12시정도 애매한 상황일 경우를 대비해서 이전날 로그까지 조회하는 예외처리가 필요.
                HashMap<String, Object> putData = new HashMap<String, Object>();
                putData.put("timestamp", "Exception");
                putData.put("lasttime", -1);
                putData.put("server", convert.get("host_ip"));

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
            } else {
                response.getHits().forEach(item -> {
                    //System.out.println(item.getSourceAsMap().get("@timestamp").toString());
                    ZonedDateTime utcDateTime = ZonedDateTime.now(ZoneId.of("UTC"));

                    String timezone = utcDateTime.toString();
                    String logTime = item.getSourceAsMap().get("@timestamp").toString();
                    int nowHour = Integer.parseInt(timezone.split("T")[1].split(":")[0]);
                    int nowMinute = Integer.parseInt(timezone.split("T")[1].split(":")[1]);
                    int logHour = Integer.parseInt(logTime.split("T")[1].split(":")[0]);
                    int logMinute = Integer.parseInt(logTime.split("T")[1].split(":")[1]);
                    int resultHour = nowHour - logHour;

                    if (nowMinute - logMinute < 0) {
                        if (resultHour > 0) {
                            resultHour -= 1;
                        }
                    }
                    //String beatName = item.getSourceAsMap().get("beat").toString().split(",")[0].split("=")[1];

                    HashMap<String, Object> putData = new HashMap<String, Object>();
                    putData.put("timestamp", logTime);
                    putData.put("lasttime", resultHour);
                    putData.put("server", convert.get("host_ip"));

                    result.add(putData);
                });
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
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd");
        Calendar calendar = Calendar.getInstance();
        Date date = new Date();

        List<Object> serverList = getServerList();

        for (int i = 0; i < serverList.size(); i++) {
            HashMap<String, Object> convert = (HashMap<String, Object>) serverList.get(i);
            HashMap<String, Object> answer = new HashMap<String, Object>();
            List<HashMap<String, Object>> tempList = new ArrayList<HashMap<String, Object>>();

            answer.put("server", convert.get("host_ip"));

            for (int j = 0; j < 10; j++) {
                calendar.setTime(date);
                calendar.add(Calendar.DATE, -j);
                String index = "filebeat-6.5.0-" + simpleDateFormat.format(calendar.getTime());

                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                searchSourceBuilder.query(QueryBuilders.matchQuery("beat.hostname", convert.get("host_name")));

                SearchRequest searchRequest = new SearchRequest(index).types("doc").source(searchSourceBuilder);

                try {
                    long count = client.search(searchRequest, RequestOptions.DEFAULT).getHits().getTotalHits();
                    HashMap<String, Object> temp = new HashMap<String, Object>();

                    temp.put("date", simpleDateFormat.format(calendar.getTime()));
                    temp.put("count", count);
                    tempList.add(temp);
                } catch (Exception e) {
                    e.printStackTrace();
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

        SearchRequest searchRequest = new SearchRequest(MANAGEMENT_SERVER_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchRequest.source(searchSourceBuilder);
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
    public List<ServerListDto> getServerListToMonitor() throws IOException {
        List<ServerListDto> models = new ArrayList<>();
        managementDao.getServerList(buildSearchRequest(MANAGEMENT_SERVER_INDEX, null, null), buildSearchSourceRequest())
                .getHits()  //x.getId(), x.getSourceAsMap().get("hostIp")
                .forEach(x -> {
                    models.add(ServerListDto
                            .builder()
                            .hostIp(x.getSourceAsMap().get("hostIp").toString())
                            .hostName(x.getId())
                            .build());
                });
        return models;
    }
}
