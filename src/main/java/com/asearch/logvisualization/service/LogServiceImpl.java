package com.asearch.logvisualization.service;

import com.asearch.logvisualization.dao.LogDao;
import com.asearch.logvisualization.dto.LogModel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.joda.time.chrono.ISOChronology;

import org.joda.time.format.DateTimeFormat;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.*;
import java.util.*;

import static com.asearch.logvisualization.util.Constant.FILEBEAT_INDEX;

@AllArgsConstructor
@Service
@Slf4j
public class LogServiceImpl extends BaseServiceImpl implements LogService {

    private RestHighLevelClient client;
    private LogDao logDao;

    @Override
    public List<LogModel> getRawLogs(String direction, String time, String search, boolean isStream) throws IOException, ParseException {

        SearchResponse response = logDao.getLogs(buildSearchRequest(FILEBEAT_INDEX, null, null), buildSearchSourceRequest(), direction, time, search, isStream);
        log.info("총 Count = {}" , response.getHits().getTotalHits());
//        searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").from("1544502133329").to(String.valueOf(calendar.getTimeInMillis())));
//        searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").from("1544501113329").to("1544502133329"));
        ArrayList<LogModel> logList = new ArrayList<>();
        SearchHit[] results = response.getHits().getHits();
        Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        for (SearchHit hit : results) {
            Date date = dateFormat.parse(hit.getSourceAsMap().get("@timestamp").toString());
            calendar.setTime(date);
            log.info("밀리세컨 : {} ", calendar.getTimeInMillis());
            log.info(hit.getSourceAsMap().get("@timestamp").toString());
            log.info(hit.getSourceAsMap().get("message").toString());
            logList.add(new LogModel(hit.getId(), hit.getSourceAsMap().get("@timestamp").toString(), hit.getSourceAsMap().get("message").toString()));
        }
        log.info("==========================================================");
        return logList;
    }

    @Override
    public String getDocument(String id) throws IOException {
        return logDao.getDocumentDetail(buildSearchRequest(FILEBEAT_INDEX, null, null), buildSearchSourceRequest(), id).getHits().getHits()[0].getSourceAsString();
    }
}