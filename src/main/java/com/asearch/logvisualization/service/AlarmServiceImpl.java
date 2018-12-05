package com.asearch.logvisualization.service;

import com.asearch.logvisualization.dto.AlarmKeywordDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Service
@Slf4j
public class AlarmServiceImpl implements AlarmService {

    private RestHighLevelClient client;

    @Override
    public boolean registerAlarmKeyword(AlarmKeywordDto keyword) throws IOException {

        boolean flag = false;
        SearchRequest searchRequest = new SearchRequest("mytest");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("keyword", keyword.getKeyword()));
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        if (response.getHits().getHits().length == 0) return makeKeyword(client, keyword.getKeyword());
        else {
            for (SearchHit searchHit : response.getHits().getHits())
                if (keyword.getKeyword().equals(searchHit.getSourceAsMap().get("keyword").toString())) flag = true;
            return !flag && makeKeyword(client, keyword.getKeyword());
        }
    }

    @Override
    public boolean removeKeyword(AlarmKeywordDto keyword) throws IOException {
        boolean flag = false;
        String documentId = null;
        SearchRequest searchRequest = new SearchRequest("mytest");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("keyword", keyword.getKeyword()));
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        if (response.getHits().getHits().length == 0) return false;
        else {
            for (SearchHit searchHit : response.getHits().getHits())
                if (keyword.getKeyword().equals(searchHit.getSourceAsMap().get("keyword").toString())) {
                    flag = true;
                    documentId = searchHit.getId();
                }
            if (flag) {
                //같은게 있을시
                DeleteRequest deleteRequest = new DeleteRequest("mytest", "keyword", documentId);
                DeleteResponse deleteResponse = client.delete(deleteRequest, RequestOptions.DEFAULT);
                return true;
            } else return false;
        }
    }

    @Override
    public boolean getKeywordList() {

        return false;
    }


    private boolean makeKeyword(RestHighLevelClient client, String keyword) {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("keyword", keyword);
        jsonMap.put("status", "200");
//            jsonMap.put("serverHost", ip);

        IndexRequest request = new IndexRequest(
                "mytest",
                "keyword")
                .source(jsonMap);
        try {
            IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
            log.info(indexResponse.toString());
        } catch (ElasticsearchException e) {
            if (e.status() == RestStatus.CONFLICT) {}
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
