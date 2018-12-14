package com.asearch.logvisualization.service;

import io.micrometer.core.lang.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class BaseServiceImpl implements BaseService {

    @Override
    public SearchRequest buildSearchRequest(String index, @Nullable String type, @Nullable String id) {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(index);
        if (type != null)
            searchRequest.types(type);
        return searchRequest;
    }

    @Override
    public SearchSourceBuilder buildSearchSourceRequest() {
        return new SearchSourceBuilder();
    }

    @Override
    public IndexRequest buildIndexRequest(String index, String type, @Nullable String id, Map<String, Object> jsonMap) {
        IndexRequest indexRequest = new IndexRequest();
        indexRequest.index(index);
        indexRequest.type(type);
        if (id != null)
            indexRequest.id(id);
        indexRequest.source(jsonMap);
        return indexRequest;
    }

    @Override
    public DeleteRequest
    buildDeleteRequest(String index, String type, String id) {
        return new DeleteRequest(
                index,
                type,
                id
        );
    }

    @Override
    public GetRequest buildGetRequest(String index, String type, String id) {
        return new GetRequest(
                index,
                type,
                id
        );
    }

    @Override
    public UpdateRequest buildUpdateRequest(String index, String type, String id) {
        return new UpdateRequest(
                index,
                type,
                id
        );
    }
}
