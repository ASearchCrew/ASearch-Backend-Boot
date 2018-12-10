package com.asearch.logvisualization.service;

import com.asearch.logvisualization.dto.RegisterServerDto;
import com.asearch.logvisualization.exception.AlreadyExistsException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchException;
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
public class ManagementServiceImpl implements ManagementService {

    private RestHighLevelClient client;

    @Override
    public String modifyFilebeatConf() {
        return null;
    }

    @Override
    public void registerServerToMonitor(RegisterServerDto serverInfo) throws IOException {
        SearchRequest searchRequest = new SearchRequest("server");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("host_ip", serverInfo.getHostIp()));
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        if (response.getHits().getHits().length != 0) throw new AlreadyExistsException("Already Exist");
        else {
            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("host_ip", serverInfo.getHostIp());
            jsonMap.put("host_name", serverInfo.getHostName());

            IndexRequest request = new IndexRequest(
                    "server",
                    "doc")
                    .source(jsonMap);
            IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT); //Todo Exception?
        }
    }
}
