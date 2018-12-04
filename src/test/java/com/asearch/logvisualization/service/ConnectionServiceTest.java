package com.asearch.logvisualization.service;

import com.asearch.logvisualization.config.ElasticSearchConfiguration;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ConnectionServiceTest {

    @Autowired
    ElasticSearchConfiguration configuration;

    @Autowired
    RestHighLevelClient client;

    private final String ELASTIC_INDEX = "classes";
    private final String ELASTIC_TYPE = "class";

    @Test
    public void 엘라스틱서치_GET() throws IOException {
//        String url = ELASTIC_INDEX + "/" + ELASTIC_TYPE;
//        Weather weather = new Weather();
//        weather.setCity("Seoul");
//        weather.setTemperature(10.2);
//        weather.setSeason("Winter");
//
//        Map<String, Object> result = client.callElasticApi("POST", url, weather, null);
//        System.out.println(result.get("resultCode"));
//        System.out.println(result.get("resultBody"));


//        GetRequest getRequest = new GetRequest("classes");
        GetRequest getRequest = new GetRequest("filebeat-6.5.0-2018.11.22", "doc", "Gq-XOmcBOxmke417TvSE");
        String[] includes = new String[]{"message", "@timestamp"};
        String[] excludes = Strings.EMPTY_ARRAY;
        FetchSourceContext fetchSourceContext =
                new FetchSourceContext(true, includes, excludes);
        getRequest.fetchSourceContext(fetchSourceContext);

        GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);

        Map<String, Object> resultMap = getResponse.getSource();

        System.out.println(resultMap.getClass().toString());
        System.out.println(resultMap.toString());
//        return resultMap.toString();
    }

}
