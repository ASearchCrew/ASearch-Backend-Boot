package com.asearch.logvisualization.dao;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;

public interface ManagementDao {


    int checkHostName(SearchRequest searchRequest, SearchSourceBuilder searchSourceBuilder, String hostName) throws IOException;

    RestStatus indexServer(IndexRequest buildIndexRequest) throws IOException;

    SearchResponse getServerList(SearchRequest searchRequest, SearchSourceBuilder searchSourceBuilder) throws IOException;
}
