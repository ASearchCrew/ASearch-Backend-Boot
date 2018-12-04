package com.asearch.logvisualization.config;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchConfiguration {

    @Bean
    public RestHighLevelClient client(ElasticSearchProperties properties) {
        return new RestHighLevelClient(
                RestClient.builder(properties.hosts())
        );
    }
}
