package com.asearch.logvisualization.service;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.json.JSONObject;

import java.util.Map;

public interface BaseService {

    SearchRequest buildSearchRequest(String index, String type);


    SearchSourceBuilder buildSearchSourceRequest();


    IndexRequest buildIndexRequest(String index, String type, Map<String, Object> jsonMap);
}
