package com.asearch.logvisualization.dao;

import io.micrometer.core.lang.Nullable;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;

public interface LogDao {

    SearchResponse getLogs(SearchRequest searchRequest,
                           SearchSourceBuilder searchSourceBuilder,
                           String direction,
                           String hostName,
                           String time,
                           @Nullable String search,
                           boolean isStream,
                           long initialCount,
                           long upScrollOffset,
                           @Nullable String id) throws IOException;

    SearchResponse getDocumentDetail(SearchRequest searchRequest, SearchSourceBuilder searchSourceBuilder, String id) throws IOException;
}
