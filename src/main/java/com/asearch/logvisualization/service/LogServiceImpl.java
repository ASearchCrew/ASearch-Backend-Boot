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
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

import static com.asearch.logvisualization.util.Constant.FILEBEAT_LOG;

@AllArgsConstructor
@Service
@Slf4j
public class LogServiceImpl extends BaseServiceImpl implements LogService {

    private RestHighLevelClient client;
    private LogDao logDao;

    @Override
    public List<LogModel> getRawLogs(String direction, String time, String search) throws IOException {

        SearchRequest searchRequest = buildSearchRequest(FILEBEAT_LOG, null);
        SearchResponse response = logDao.getLogs(searchRequest, buildSearchSourceRequest(), direction, time, search);
        log.info("총 Count = " + response.getHits().getTotalHits());
        //From
//        searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").from("1544502133329").to(String.valueOf(calendar.getTimeInMillis())));
//        searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").from("1544501113329").to("1544502133329"));

        ArrayList<LogModel> logList = new ArrayList<>();
        SearchHit[] results = response.getHits().getHits();
        for (SearchHit hit : results) {
            logList.add(new LogModel(hit.getId(), hit.getSourceAsMap().get("@timestamp").toString(), hit.getSourceAsMap().get("message").toString()));
        }
        return logList;
    }

    @Override
    public List<String> searchLog(String word) throws IOException {
        SearchRequest searchRequest = new SearchRequest();

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("message", word));

        searchRequest.source(searchSourceBuilder);

        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        log.info(Arrays.toString(response.getHits().getHits()));
        log.info(response.getHits().getHits().getClass().toString());

        ArrayList<String> list = new ArrayList<>();
        SearchHit[] results = response.getHits().getHits();
        for (SearchHit hit : results) {
            log.info(hit.getSourceAsString());
            list.add(hit.getSourceAsString());
        }
        return list;
    }
}
