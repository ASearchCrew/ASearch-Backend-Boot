package com.asearch.logvisualization.service;

import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ConnectionServiceTest {

    @Autowired
    RestHighLevelClient client;

    private final String ELASTIC_INDEX = "filebeat-6.5.0-2018.11.22";
    private final String ELASTIC_TYPE = "doc";

    @Test
    public void 엘라스틱서치_GET() throws IOException {

        GetRequest getRequest = new GetRequest(ELASTIC_INDEX, ELASTIC_TYPE, "Gq-XOmcBOxmke417TvSE");
        String[] includes = new String[]{"message", "@timestamp"};
        String[] excludes = Strings.EMPTY_ARRAY;
        FetchSourceContext fetchSourceContext =
                new FetchSourceContext(true, includes, excludes);
        getRequest.fetchSourceContext(fetchSourceContext);

        GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);

        Map<String, Object> resultMap = getResponse.getSource();

        System.out.println(resultMap.toString());
    }


    @Test
    public void 스크롤_GET() throws IOException {

        final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));
        SearchRequest searchRequest = new SearchRequest("filebeat*");
        searchRequest.scroll(scroll);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(searchResponse.getHits().getTotalHits());
        String scrollId = searchResponse.getScrollId();
        System.out.println(scrollId);
        SearchHit[] searchHits = searchResponse.getHits().getHits();

        while (searchHits != null && searchHits.length > 0) {
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(scroll);
            searchResponse = client.scroll(scrollRequest, RequestOptions.DEFAULT);
            System.out.println(searchResponse.getHits().getTotalHits());
            scrollId = searchResponse.getScrollId();
            searchHits = searchResponse.getHits().getHits();

        }

        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        clearScrollRequest.addScrollId(scrollId);
        ClearScrollResponse clearScrollResponse = client.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
        boolean succeeded = clearScrollResponse.isSucceeded();
    }

}
