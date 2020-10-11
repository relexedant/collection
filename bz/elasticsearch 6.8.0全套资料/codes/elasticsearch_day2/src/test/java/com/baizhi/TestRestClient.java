package com.baizhi;

import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;


/**
 * client 操作
 *
 *  RestHighLevelClient         强大 更灵活  不能友好的对象操作
 *
 *  ElasticSearchRepository     对象操作友好
 *
 */
@SpringBootTest
public class TestRestClient {

    @Autowired
    private RestHighLevelClient restHighLevelClient;//复杂查询使用  高亮查询  指定条件查询  transportClient



    //添加文档
    @Test
    public void testAddIndex() throws IOException {
        IndexRequest indexRequest = new IndexRequest("ems","emp","12");
        indexRequest.source("{\"name\":\"小黑\",\"age\":23}", XContentType.JSON);
        IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        System.out.println(indexResponse.status());
    }



    //删除文档
    @Test
    public void testDeleteIndex() throws IOException {

        //参数1: 索引  参数2:类型  参数3:删除id
        DeleteRequest deleteRequest = new DeleteRequest("ems","emp","cO78FnMBpzP5KMxRDdbD");
        DeleteResponse deleteResponse = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
        System.out.println(deleteResponse.status());
    }


    //更新文档
    @Test
    public void testUpdate() throws IOException {
        UpdateRequest updateRequest = new UpdateRequest("ems","emp","12");
        updateRequest.doc("{\"name\":\"张三\",\"age\":23}",XContentType.JSON);
        UpdateResponse updateResponse = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        System.out.println(updateResponse.status());
    }

    //批量更新
    @Test
    public void testBulk() throws IOException {
        BulkRequest bulkRequest = new BulkRequest();

        //添加
        IndexRequest request = new IndexRequest("ems","emp","11");
        request.source("{\"name\":\"李四\",\"age\":23}",XContentType.JSON);
        //删除

        //修改
        bulkRequest.add(request);
        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        BulkItemResponse[] items = bulkResponse.getItems();
        for (BulkItemResponse item : items) {
            System.out.println(item.status());
        }
    }


    //查询所有
    @Test
    public void testQueryAll() throws IOException {
        SearchRequest searchRequest = new SearchRequest("ems");
        //创建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //查询所有
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());

        searchRequest.types("emp").source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        SearchHit[] hits = searchResponse.getHits().getHits();
        for (SearchHit hit : hits) {
            System.out.println(hit.getSourceAsString());
        }

    }



    @Test
    public void testSearch() throws IOException {
        //创建搜索对象
        SearchRequest searchRequest = new SearchRequest("ems");
        //搜索构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.query(QueryBuilders.matchAllQuery())//执行查询条件
                        .from(0)//起始条数
                        .size(20)//每页展示记录
                        .postFilter(QueryBuilders.matchAllQuery()) //过滤条件
                        .sort("age", SortOrder.DESC)//排序
                        .highlighter(new HighlightBuilder().field("*").requireFieldMatch(false));//高亮

        //创建搜索请求
        searchRequest.types("emp").source(searchSourceBuilder);

        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        System.out.println("符合条件的文档总数: "+searchResponse.getHits().getTotalHits());
        System.out.println("符合条件的文档最大得分: "+searchResponse.getHits().getMaxScore());
        SearchHit[] hits = searchResponse.getHits().getHits();
        for (SearchHit hit : hits) {
            System.out.println(hit.getSourceAsMap());
        }
    }



}
