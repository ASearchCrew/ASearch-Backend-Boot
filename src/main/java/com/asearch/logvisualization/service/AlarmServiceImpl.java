package com.asearch.logvisualization.service;

import com.asearch.logvisualization.dao.AlarmDaoImpl;
import com.asearch.logvisualization.dto.AlarmKeywordDto;
import com.asearch.logvisualization.dto.KeywordListModel;
import com.asearch.logvisualization.dto.KeywordModel;
import com.asearch.logvisualization.exception.AlreadyExistsException;
import com.asearch.logvisualization.exception.InternalServerErrorException;
import com.asearch.logvisualization.exception.NotFoundException;
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
            if (searchHit.getSourceAsMap().get("keyword").toString().equals(keyword.getKeyword()))
                throw new AlreadyExistsException("Already Exist");

        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("keyword", keyword.getKeyword());
        jsonMap.put("host_ip", keyword.getHostIp());
        IndexRequest indexRequest = buildIndexRequest(KEYWORD_INDEX, KEYWORD_TYPE, jsonMap);
        IndexResponse indexResponse = alarmDao.indexNewKeyword(indexRequest);
        if (!indexResponse.getResult().toString().equals("CREATED"))
            throw new InternalServerErrorException("Not created");
    }

    @Override
    public void removeKeyword(AlarmKeywordDto keyword) throws IOException {

        String documentId = null;

        SearchRequest searchRequest = buildSearchRequest(KEYWORD_INDEX, KEYWORD_TYPE);
        SearchHit[] searchHits = alarmDao.getExistedKeyword(searchRequest, buildSearchSourceRequest(), new String[]{"keyword", "host_ip"},
                new String[]{keyword.getKeyword(), keyword.getHostIp()});

        for (SearchHit searchHit : searchHits)
            if (searchHit.getSourceAsMap().get("keyword").toString().equals(keyword.getKeyword()))
                documentId = searchHit.getId();
        if (documentId == null)
            throw new NotFoundException("No Data");
        else {
            DeleteRequest deleteRequest = buildDeleteRequest(KEYWORD_INDEX, KEYWORD_TYPE, documentId);
            DeleteResponse deleteResponse = alarmDao.removeKeywordDocument(deleteRequest);
            //TODO 삭제 확인 할 것
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
                    keywordListModels.get(keywordListModels.size() - 1).getKeywords().add(new KeywordModel(x.getSourceAsMap().get("keyword").toString()));
                }
            }
        });
        return keywordListModels;
    }
}
