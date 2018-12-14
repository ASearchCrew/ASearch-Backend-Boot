package com.asearch.logvisualization.service;

import io.micrometer.core.lang.Nullable;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.json.JSONObject;

import java.util.Map;

public interface BaseService {

    SearchRequest buildSearchRequest(String index, @Nullable String type, @Nullable String indices);


    SearchSourceBuilder buildSearchSourceRequest();


    IndexRequest buildIndexRequest(String index, String type, @Nullable String id, Map<String, Object> jsonMap);


    DeleteRequest buildDeleteRequest(String index, String type, String id);


    GetRequest buildGetRequest(String index, String type, String id);


    UpdateRequest buildUpdateRequest(String index, String type, String id);
}
