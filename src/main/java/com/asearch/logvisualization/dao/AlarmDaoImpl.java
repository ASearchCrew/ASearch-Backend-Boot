package com.asearch.logvisualization.dao;

import com.asearch.logvisualization.service.BaseServiceImpl;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Repository;

import java.io.IOException;

@Repository
public class AlarmDaoImpl extends BaseDaoImpl implements AlarmDao {

    @Override
    public SearchHit[] getExistedKeyword(SearchRequest searchRequest, SearchSourceBuilder searchSourceBuilder,
                                         String[] fields, String[] contents) throws IOException {
//        searchSourceBuilder.query(QueryBuilders.matchQuery(fields[0], contents[0]));
        searchSourceBuilder.query(QueryBuilders.termQuery(fields[1], contents[1]));
        searchRequest.source(searchSourceBuilder);
        return client.search(searchRequest, RequestOptions.DEFAULT).getHits().getHits();
    }


    @Override
    public IndexResponse indexNewKeyword(IndexRequest indexRequest) throws IOException {
        return client.index(indexRequest, RequestOptions.DEFAULT);
    }


    @Override
    public SearchHit[] getKeywordList(SearchRequest searchRequest, SearchSourceBuilder searchSourceBuilder, String where, int size) throws IOException {
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.size(1000);
        searchRequest.source(searchSourceBuilder);
        return client.search(searchRequest, RequestOptions.DEFAULT).getHits().getHits();
    }


    @Override
    public DeleteResponse removeKeywordDocument(DeleteRequest deleteRequest) throws IOException {
        return client.delete(deleteRequest, RequestOptions.DEFAULT);
    }
}
