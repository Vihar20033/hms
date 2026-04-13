package com.hms.common.search.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Elasticsearch configuration for the HMS application.
 * Configures connection to Elasticsearch and enables repository scanning.
 */
@Configuration
@EnableElasticsearchRepositories(
        basePackages = "com.hms.common.search.repository"
)
public class ElasticsearchConfig extends AbstractElasticsearchConfiguration {

    @Value("${elasticsearch.host:localhost}")
    private String host;

    @Value("${elasticsearch.port:9200}")
    private int port;

    @Value("${elasticsearch.username:#{null}}")
    private String username;

    @Value("${elasticsearch.password:#{null}}")
    private String password;

    @Value("${elasticsearch.enabled:true}")
    private boolean elasticsearchEnabled;

    /**
     * Constructs the Elasticsearch client configuration.
     * 
     * @return ClientConfiguration for Elasticsearch
     */
    @Override
    public ClientConfiguration clientConfiguration() {
        if (!elasticsearchEnabled) {
            return ClientConfiguration.builder()
                    .connectedTo(host + ":" + port)
                    .build();
        }

        return ClientConfiguration.builder()
                .connectedTo(host + ":" + port)
                .withConnectTimeout(5000)
                .withSocketTimeout(10000)
                .build();
    }

    /**
     * Creates an ElasticsearchClient bean for direct client operations.
     * This allows for more fine-grained control when repositories alone aren't sufficient.
     */
    @Bean
    @ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = true)
    public ElasticsearchClient elasticsearchClient() {
        RestClient restClient = RestClient
                .builder(new HttpHost(host, port, "http"))
                .build();

        ElasticsearchTransport transport = new RestClientTransport(
                restClient,
                new JacksonJsonpMapper()
        );

        return new ElasticsearchClient(transport);
    }
}
