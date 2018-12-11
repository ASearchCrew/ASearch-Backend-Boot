package com.asearch.logvisualization.service;

import com.asearch.logvisualization.dao.AlarmDaoImpl;
import com.asearch.logvisualization.dto.AlarmKeywordDto;
import com.asearch.logvisualization.dto.KeywordListModel;
import com.asearch.logvisualization.dto.KeywordModel;
import com.asearch.logvisualization.exception.AlreadyExistsException;
import com.asearch.logvisualization.exception.InternalServerErrorException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

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
        SearchHit[] searchHits = alarmDao.getExistedKeyword(searchRequest, buildSearchSourceRequest(), new String[]{"keyword", "host_ip"},
                new String[]{keyword.getKeyword(), keyword.getHostIp()});

        for (SearchHit searchHit : searchHits)
            if (searchHit.getSourceAsMap().get("keyword").equals(keyword.getKeyword()))
                throw new AlreadyExistsException("Already Exist");

        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("keyword", keyword.getKeyword());
        jsonMap.put("host_ip", keyword.getHostIp());
        IndexRequest indexRequest = buildIndexRequest(KEYWORD_INDEX, KEYWORD_TYPE, jsonMap);
        IndexResponse indexResponse = alarmDao.indexNewKeyword(indexRequest);
        if (!indexResponse.getResult().toString().equals("CREATED")) throw new InternalServerErrorException("Not created");
    }

    //TODO length가 1일때만 지우기.
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
    public List<KeywordListModel> getKeywordList() throws IOException {
        SearchRequest searchRequest = buildSearchRequest("keyword", "doc");
        SearchHit[] searchHits = alarmDao.getKeywordList(searchRequest, buildSearchSourceRequest(), "all", 1000);
        List<KeywordListModel> keywordListModels = new ArrayList<>();
        Stream<SearchHit> hitStream = Arrays.stream(searchHits);
        hitStream.forEach(x -> {
            if (keywordListModels.size() == 0) {
                keywordListModels.add(new KeywordListModel(x.getSourceAsMap().get("host_ip").toString(), new ArrayList<>()));
                keywordListModels.get(0).getKeywords().add(new KeywordModel(x.getSourceAsMap().get("keyword").toString()));
            } else {
                AtomicBoolean flag = new AtomicBoolean(false);
                keywordListModels.forEach(y -> {
                    if (y.getHostIp().equals(x.getSourceAsMap().get("host_ip"))) {
                        y.getKeywords().add(new KeywordModel(x.getSourceAsMap().get("keyword").toString()));
                        flag.set(true);
                    }
                });
                if (!flag.get()) {
                    keywordListModels.add(new KeywordListModel(x.getSourceAsMap().get("host_ip").toString(), new ArrayList<>()));
                    keywordListModels.get(keywordListModels.size()-1).getKeywords().add(new KeywordModel(x.getSourceAsMap().get("keyword").toString()));
                }
            }
        });
        return keywordListModels;
    }
}
