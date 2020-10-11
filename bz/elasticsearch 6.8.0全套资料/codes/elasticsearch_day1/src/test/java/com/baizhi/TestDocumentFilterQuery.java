package com.baizhi;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class TestDocumentFilterQuery {

    private TransportClient transportClient;

    @Before
    public void before() throws UnknownHostException {
        //创建客户端
        this.transportClient = new PreBuiltTransportClient(Settings.EMPTY);
        //设置es服务地址
        transportClient.addTransportAddress(new TransportAddress(InetAddress.getByName("192.168.202.200"),9300));
    }

    @After
    public void after(){
        transportClient.close();
    }


    /**
     * 过滤查询 主要用在查询执行之前对大量数据进行筛选
     * postFilter 用来过滤
     */
    @Test
    public void testQuery(){
        RangeQueryBuilder ageRangeQueryBuilder = QueryBuilders.rangeQuery("age").gte(0).lte(10);
        SearchResponse searchResponse = transportClient.prepareSearch("ems")
                .setTypes("emp")
                .setPostFilter(ageRangeQueryBuilder) //过滤查询
                .setQuery(QueryBuilders.matchAllQuery())//查询
                .get();

        SearchHit[] hits = searchResponse.getHits().getHits();
        for (SearchHit hit : hits) {
            System.out.println(hit.getSourceAsString());
        }

    }


    /**
     * 基于多字段关键词查询
     * 分页
     * 排序
     * 过滤
     * 执行字段返回
     * 高亮处理
     */
    @Test
    public void testSearch(){
        SearchResponse searchResponse = transportClient.prepareSearch("ems")
                .setTypes("emp")
                .setFrom(0)  //起始条数
                .setSize(20) //每页记录数
                .addSort("age", SortOrder.DESC)//执行排序条件
                .setSource(SearchSourceBuilder.searchSource().fetchSource("*", "bir"))//指定返回的字段
                .setPostFilter(QueryBuilders.rangeQuery("age").gte(0).lte(100))//过滤条件
                .setQuery(QueryBuilders.multiMatchQuery("框架爱好者","name","content"))//多字段搜索
                .highlighter(new HighlightBuilder().field("*").requireFieldMatch(false).preTags("<span style='color:red;'>").postTags("</span>")) //高亮处理
                .get();

        long totalHits = searchResponse.getHits().getTotalHits();
        System.out.println("符合条件的总条数: "+totalHits);
        float maxScore = searchResponse.getHits().getMaxScore();
        System.out.println("最大分数: "+maxScore);

        SearchHit[] hits = searchResponse.getHits().getHits();
        for (SearchHit hit : hits) {
            System.out.println("原始文档: "+hit.getSourceAsString());
            System.out.println("高亮字段: "+hit.getHighlightFields());
        }

    }




}
