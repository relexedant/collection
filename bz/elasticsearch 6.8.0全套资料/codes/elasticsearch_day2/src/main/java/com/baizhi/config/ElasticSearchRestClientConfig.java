package com.baizhi.config;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;

@Configuration
public class ElasticSearchRestClientConfig extends AbstractElasticsearchConfiguration {


    //这个client 用来替换 transportClient(9300)对象
    @Override
    @Bean
    public RestHighLevelClient elasticsearchClient() {
        //定义客户端配置对象 RestClient  9200
        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                //书写集群中所有节点
                .connectedTo("192.168.202.207:9201","192.168.202.207:9202","192.168.202.207:9203")
                .build();
        //通过RestClients对象创建
        return RestClients.create(clientConfiguration).rest();
    }
}
