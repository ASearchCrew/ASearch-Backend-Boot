package com.asearch.logvisualization.service;

import com.asearch.logvisualization.dao.LogDao;
import com.asearch.logvisualization.dto.LogInfoDto;
import com.asearch.logvisualization.dto.LogModel;
import com.asearch.logvisualization.dto.PushTokenDto;
import com.asearch.logvisualization.exception.AlreadyExistsException;
import io.micrometer.core.lang.Nullable;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.asearch.logvisualization.util.Constant.FILEBEAT_INDEX;
import static com.asearch.logvisualization.util.Constant.TOKEN_SERVER_INDEX;
import static com.asearch.logvisualization.util.Constant.TOKEN_SERVER_TYPE;

//        searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").from("1544502133329").to(String.valueOf(calendar.getTimeInMillis())));
//        searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").from("1544501113329").to("1544502133329"));

@AllArgsConstructor
@Service
@Slf4j
public class LogServiceImpl extends BaseServiceImpl implements LogService {

    private RestHighLevelClient client;
    private LogDao logDao;


    /**
     * 로그를 가져오는 API
     */
    @Override
    public LogInfoDto getRawLogs(String direction,
                                 String hostName,
                                 String time,
                                 String search,
                                 boolean isStream,
                                 long initialCount,
                                 long upScrollOffset,
                                 String id,
                                 String calendarStartTime,
                                 String calendarEndTime) throws IOException, ParseException {

        //TODO Refactoring
        /**
         *  Dao 에 DataAccess 를 요청한다.
         */
        SearchResponse response = logDao.getLogs(
                buildSearchRequest(hostName+"*", null, null),
                buildSearchSourceRequest(),
                direction,
                hostName,
                time,
                search,
                isStream,
                initialCount,
                upScrollOffset,
                id,
                calendarStartTime,
                calendarEndTime);

        log.info("");
        log.info("총 Count = {}" , response.getHits().getTotalHits());

        /**
         *  isStream == true 일때, fix size (temp=100) > 100 일때는, 그냥 데이터를 return 해준다.
         */
        if (isStream && direction.equals("stream")) {
            if (response.getHits().getHits().length > 100)
                response = logDao.getStreamBigData(buildSearchRequest(hostName+"*", null, null),
                        buildSearchSourceRequest(),
                        direction,
                        hostName,
                        time,
                        search,
                        id);
        }

        /**
         * Data 담기와, 시간 변환
         */
        LogInfoDto infoDto = new LogInfoDto();
        infoDto.setSumCount(response.getHits().getTotalHits());

        ArrayList<LogModel> logList = new ArrayList<>();
        SearchHit[] results = response.getHits().getHits();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        for (SearchHit hit : results) {
            Date date = dateFormat.parse(hit.getSourceAsMap().get("@timestamp").toString());
            calendar.setTime(date);
//            log.info("밀리세컨 : {} ", calendar.getTimeInMillis());
            log.info("id = {}",hit.getId());
            log.info(hit.getSourceAsMap().get("@timestamp").toString());
            logList.add(new LogModel(hit.getId(), hit.getSourceAsMap().get("@timestamp").toString(), hit.getSourceAsMap().get("message").toString()));
        }
        infoDto.setLogs(logList);
        log.info("===================================한개의 Request 끝==========================================");
        return infoDto;
    }

    @Override
    public String getDocument(String id) throws IOException {
        return logDao.getDocumentDetail(buildSearchRequest(FILEBEAT_INDEX, null, null), buildSearchSourceRequest(), id).getHits().getHits()[0].getSourceAsString();
    }

    @Override
    public void registerPushToken(PushTokenDto dto) throws IOException {
        SearchRequest searchRequest = buildSearchRequest(TOKEN_SERVER_INDEX, TOKEN_SERVER_TYPE, null);
        SearchSourceBuilder searchSourceBuilder = buildSearchSourceRequest();
        searchSourceBuilder.query(QueryBuilders.matchQuery("token", dto.getToken()));
        log.info("token = {}", dto.getToken());
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
//        fGdccQnF6KA:APA91bEOZEi5mePQ7Zt_c5fztzwJOBs6D9Q5q9_fbjww98vPb8kAYm9jr97rYqXulQAUDAwWxNX63o9pX_bi9oAWXUb6f3mIHzGIMZ-QDFb54wXyS7QuZxgthRjUdsA6oRIx908d8TFY
        log.info(Arrays.toString(response.getHits().getHits()));
        if (response.getHits().getTotalHits() > 0)
            throw new AlreadyExistsException("Already Existed");
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("token", dto.getToken());
        IndexRequest indexRequest = buildIndexRequest(TOKEN_SERVER_INDEX, TOKEN_SERVER_TYPE, null, jsonMap);
        IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
    }
}