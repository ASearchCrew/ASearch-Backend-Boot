package com.asearch.logvisualization.dao;

import io.micrometer.core.lang.Nullable;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;

public interface LogDao {

    SearchResponse getLogs(SearchRequest searchRequest, SearchSourceBuilder searchSourceBuilder, String direction, String time, @Nullable String search) throws IOException;
}