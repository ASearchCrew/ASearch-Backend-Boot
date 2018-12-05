package com.asearch.logvisualization.config;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchConfig {

    @Value("${elasticsearch.host}")
    private String elasticsearchHost;

    @Value("${elasticsearch.port}")
    private int port;

    /**
     * The high-level client will internally create the low-level client used to perform requests based on the
     * provided builder. That low-level client maintains a pool of connections and starts some threads so
     * you should close the high-level client when you are well and truly done with it and it will in turn close
     * the internal low-level client to free those resources. This can be done through the close:
     */

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        return new RestHighLevelClient(
                RestClient.builder(new HttpHost(elasticsearchHost, port))
        );
    }
}
