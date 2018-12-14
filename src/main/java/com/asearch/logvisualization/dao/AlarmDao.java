package com.asearch.logvisualization.dao;

import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.Map;

public interface AlarmDao {

    GetResponse getExistedKeywords(GetRequest getRequest,
                                   String field,
                                   String content) throws IOException;


    IndexResponse indexNewKeyword(IndexRequest indexRequest) throws IOException;



    SearchHit[] getKeywordList(SearchRequest searchRequest, SearchSourceBuilder searchSourceBuilder, String where, int size) throws IOException;



    DeleteResponse removeKeywordDocument(DeleteRequest deleteRequest) throws IOException;



    UpdateResponse makeNewKeywords(Map<String, Object> parameters, UpdateRequest updateRequest) throws IOException;



    UpdateResponse addKeyword(Map<String, Object> parameters, UpdateRequest updateRequest) throws IOException;


    UpdateResponse removeKeyword(Map<String, Object> parameters, UpdateRequest buildUpdateRequest, int position) throws IOException;
}
