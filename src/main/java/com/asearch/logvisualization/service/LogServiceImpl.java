package com.asearch.logvisualization.service;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@Service
@Slf4j
public class LogServiceImpl implements LogService {

    private RestHighLevelClient client;

    @Override
    public List<String> getRawLogs(int count) throws IOException {
        SearchRequest searchRequest = new SearchRequest("filebeat-6.5.0-2018.11.26");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").from("2018-11-23T05:13:41.342Z").to(new Date()));

        int tempCur;
        tempCur = count * 5;
        searchSourceBuilder.from(tempCur);
        searchSourceBuilder.size(5);

        String[] includeFields = new String[] {"@timestamp", "input", "message"};
        String[] excludeFields = new String[] {};
        searchSourceBuilder.fetchSource(includeFields, excludeFields);

        searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.DESC));

        searchRequest.source(searchSourceBuilder);

        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        log.info(response.toString());
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

    @Override
    public List<String> searchLog(String word) {
        return null;
    }
}
