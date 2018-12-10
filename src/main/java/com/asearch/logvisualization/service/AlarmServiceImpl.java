package com.asearch.logvisualization.service;

import com.asearch.logvisualization.dao.AlarmDaoImpl;
import com.asearch.logvisualization.dto.AlarmKeywordDto;
import com.asearch.logvisualization.exception.AlreadyExistsException;
import com.asearch.logvisualization.exception.InternalServerErrorException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import netscape.javascript.JSObject;
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
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerErrorException;

import java.io.IOException;
import java.util.*;

import static com.asearch.logvisualization.util.Constant.KEYWORD_INDEX;
import static com.asearch.logvisualization.util.Constant.KEYWORD_TYPE;

@AllArgsConstructor
@Service
@Slf4j
public class AlarmServiceImpl extends BaseServiceImpl implements AlarmService {

    private RestHighLevelClient client;
    private AlarmDaoImpl alarmDao;

    @Override
    public void registerAlarmKeyword(AlarmKeywordDto keyword) throws IOException {

        SearchRequest searchRequest = buildSearchRequest(KEYWORD_INDEX, KEYWORD_TYPE);
        SearchSourceBuilder searchSourceBuilder = buildSearchSourceRequest();
        SearchHit[] searchHits = alarmDao.getExistedKeyword(searchRequest, searchSourceBuilder, new String[]{"keyword", "host_ip"},
                new String[]{keyword.getKeyword(), keyword.getHostIp()});
        if (searchHits.length == 1) throw new AlreadyExistsException("Already exist");
        else {
            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("keyword", keyword.getKeyword());
            jsonMap.put("host_ip", keyword.getHostIp());
            IndexRequest indexRequest = buildIndexRequest(KEYWORD_INDEX, KEYWORD_TYPE, jsonMap);
            IndexResponse indexResponse = alarmDao.indexNewKeyword(indexRequest);
            if (!indexResponse.getResult().toString().equals("CREATED")) throw new InternalServerErrorException("Not created");
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
    public List<String> getKeywordList() throws IOException {
        SearchRequest searchRequest = new SearchRequest("keyword");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.size(1000);
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        List<String> results = new ArrayList<>();
        for (SearchHit searchHit : response.getHits().getHits()) {
            results.add(searchHit.getSourceAsMap().get("keyword").toString());
            JSONObject jsonObject = new JSONObject(searchHit.getSourceAsString());
            log.info(jsonObject.toString());
            log.info(searchHit.getSourceAsString());
        }
        return results;
    }

//    private boolean makeKeyword(RestHighLevelClient client, String keyword) {
//        Map<String, Object> jsonMap = new HashMap<>();
//        jsonMap.put("keyword", keyword);
//        jsonMap.put("status", "200");
////            jsonMap.put("serverHost", ip);
//
//        IndexRequest request = new IndexRequest(
//                "mytest",
//                "keyword")
//                .source(jsonMap);
//        try {
//            IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
//            log.info(indexResponse.toString());
//        } catch (ElasticsearchException e) {
//            if (e.status() == RestStatus.CONFLICT) {}
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return true;
//    }
}
