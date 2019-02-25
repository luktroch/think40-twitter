package org.think40.twitter.repository;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.logging.Logger;

@Configuration
public class ElasticConfiguration {

    @Value("${elasticsearch.host}")
    private String elasticsearchHost;
    @Value("${elasticsearch.port}")
    private Integer elasticsearchPort;

    @Bean(destroyMethod = "close")
    public RestHighLevelClient client() {
        LoggerFactory.getLogger(ElasticConfiguration.class).info("Using elastic {}:{}", elasticsearchHost, elasticsearchPort);
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost(elasticsearchHost, elasticsearchPort)));

        return client;

    }
}
