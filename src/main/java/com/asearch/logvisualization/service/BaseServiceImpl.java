package com.asearch.logvisualization.service;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class BaseServiceImpl implements BaseService {

    @Override
    public SearchRequest buildSearchRequest(String index, String type) {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(index);
        searchRequest.types(type);
        return searchRequest;
    }

    @Override
    public SearchSourceBuilder buildSearchSourceRequest() {
        return new SearchSourceBuilder();
    }

    @Override
    public IndexRequest buildIndexRequest(String index, String type, Map<String, Object> jsonMap) {
        return new IndexRequest(
                index, type)
                .source(jsonMap);
    }
}
