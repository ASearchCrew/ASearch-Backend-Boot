package com.asearch.logvisualization.dao;

import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;

public interface AlarmDao {

    SearchHit[] getExistedKeyword(SearchRequest searchRequest, SearchSourceBuilder searchSourceBuilder,
                                  String[] fields, String[] contents) throws IOException;


    IndexResponse indexNewKeyword(IndexRequest indexRequest) throws IOException;



    SearchHit[] getKeywordList(SearchRequest searchRequest, SearchSourceBuilder searchSourceBuilder, String where, int size) throws IOException;


    DeleteResponse removeKeywordDocument(DeleteRequest deleteRequest) throws IOException;
}
