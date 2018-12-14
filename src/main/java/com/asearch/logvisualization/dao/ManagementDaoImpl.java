package com.asearch.logvisualization.dao;

import io.micrometer.core.instrument.search.Search;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Repository;

import java.io.IOException;

@Repository
public class ManagementDaoImpl extends BaseDaoImpl implements ManagementDao {

    @Override
    public int checkHostName(SearchRequest searchRequest, SearchSourceBuilder searchSourceBuilder, String hostName) throws IOException {
        return client.search(searchRequest.source(searchSourceBuilder.query(QueryBuilders.termsQuery("_id", hostName))), RequestOptions.DEFAULT).getHits().getHits().length;
    }

    @Override
    public RestStatus indexServer(IndexRequest buildIndexRequest) throws IOException {
        return client.index(buildIndexRequest, RequestOptions.DEFAULT).status();
    }

    @Override
    public SearchResponse getServerList(SearchRequest searchRequest, SearchSourceBuilder searchSourceBuilder) throws IOException {
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchRequest.source(searchSourceBuilder);
        return client.search(searchRequest, RequestOptions.DEFAULT);
    }
}
