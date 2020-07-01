package com.panprest.elastic.api.config;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

public class EsConfig {
//	@Value("${elasticsearch.cluster-name}")
//    private String clusterName;
//
//    @Value("${elasticsearch.port}")
//    private int port;
//
//    @Value("${elasticsearch.host}")
//    private String host;
//
//    @Bean
//    public Client client() throws UnknownHostException {
//        Settings settings = Settings.builder().put("cluster.name", clusterName).put("network.host", host).build();
//        return new PreBuiltTransportClient(settings).addTransportAddress(new TransportAddress(InetAddress.getByName(host), 9200));
//    }
//
//    @Bean
//    public ElasticsearchTemplate elasticsearchTemplate(Client client) {
//        return new ElasticsearchTemplate(client);
//    }
}
	