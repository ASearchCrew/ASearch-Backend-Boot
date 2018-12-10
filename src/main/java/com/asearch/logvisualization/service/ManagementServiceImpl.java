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
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Slf4j
public class ManagementServiceImpl implements ManagementService {

    private RestHighLevelClient client;

    @Override
    public String modifyFilebeatConf() {
        return null;
    }

    @Override
    public void registerServerToMonitor() {
        boolean flag = false;
        SearchRequest searchRequest = new SearchRequest("server");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("keyword", keyword.getKeyword()));
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        if (response.getHits().getHits().length == 0) return makeKeyword(client, keyword.getKeyword());
        else {
            for (SearchHit searchHit : response.getHits().getHits())
                if (keyword.getKeyword().equals(searchHit.getSourceAsMap().get("keyword").toString())) flag = true;
            return !flag && makeKeyword(client, keyword.getKeyword());
        }
    }
}
