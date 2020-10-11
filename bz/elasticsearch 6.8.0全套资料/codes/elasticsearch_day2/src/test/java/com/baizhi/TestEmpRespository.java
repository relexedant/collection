package com.baizhi;

import com.baizhi.elasticsearch.dao.EmpRepository;
import com.baizhi.entity.Emp;
import lombok.ToString;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.sql.SQLOutput;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootTest
public class TestEmpRespository {


    @Autowired
    private EmpRepository empRepository;  //1

    @Autowired
    private RestHighLevelClient restHighLevelClient;//2


    //保存|更新一条文档 id存在更新  id不存在 添加
    @Test
    public void testSave(){
        Emp s = new Emp();
        //s.setId("a1389021-2ecf-40cb-95e7-4c3be73604d9");
        s.setAge(2);
        s.setBir(new Date());
        s.setName("张君宝一代武侠");
        s.setAddress("武当山武侠学院毕业");
        s.setContent("武侠大师!");
        empRepository.save(s);

    }

    //根据id删除一条文档
    @Test
    public void testDelete(){
        empRepository.deleteById("a1389021-2ecf-40cb-95e7-4c3be73604d9");
    }


    //删除所有
    @Test
    public void testDeleteAll(){
        empRepository.deleteAll();
    }

    //检索一条记录
    @Test
    public void testFindOne(){
        Optional<Emp> optional = empRepository.findById("a1389021-2ecf-40cb-95e7-4c3be73604d9");
        System.out.println(optional.get());
    }

    //查询所有 排序
    @Test
    public void testFindAll(){
        Iterable<Emp> all = empRepository.findAll(Sort.by(Sort.Order.asc("age")));
        all.forEach(emp-> System.out.println(emp));
    }

    //分页
    @Test
    public void testFindPage(){
        //PageRequest.of 参数1: 当前页-1
        Page<Emp> search = empRepository.search(QueryBuilders.matchAllQuery(), PageRequest.of(1, 1));
        search.forEach(emp-> System.out.println(emp));
    }

    //基础查询 根据姓名  姓名和年龄  地址 等
    @Test
    public void testFindByName(){
        List<Emp> emps = empRepository.findByName("张君宝");
        List<Emp> emps1 = empRepository.findByAge(23);
        List<Emp> emps2 = empRepository.findByNameAndAddressAndAge("张君宝", "武当山",23);
        List<Emp> emps3 = empRepository.findByNameOrAge("张君宝", 2);
        List<Emp> emps4 = empRepository.findByAgeGreaterThanEqual(23);
        emps4.forEach(emp-> System.out.println(emp));
    }



    //复杂查询RestHighLevelClient  高亮
    @Test
    public void testSearchQuery() throws IOException, ParseException {
        List<Emp> emps = new ArrayList<Emp>();
        //创建搜索请求
        SearchRequest searchRequest = new SearchRequest("ems");

        //创建搜索对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery("content","武侠"))//设置条件
                .sort("age", SortOrder.DESC)//排序
                .from(0) //起始条数(当前页-1)*size的值
                .size(20)//每页展示条数
                .highlighter(new HighlightBuilder().field("*").requireFieldMatch(false).preTags("<span style='color:red'>").postTags("</span>"));//设置高亮

        searchRequest.types("emp").source(searchSourceBuilder);

        //执行搜索
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        SearchHit[] hits = searchResponse.getHits().getHits();
        for (SearchHit hit : hits) {
            //原始文档
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            Emp emp =  new Emp();
            emp.setId(hit.getId());
            emp.setAge(Integer.parseInt(sourceAsMap.get("age").toString()));
            emp.setBir(new SimpleDateFormat("yyyy-MM-dd").parse(sourceAsMap.get("bir").toString()));
            emp.setContent(sourceAsMap.get("content").toString());
            emp.setName(sourceAsMap.get("name").toString());
            emp.setAddress(sourceAsMap.get("address").toString());

            //高亮字段
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if(highlightFields.containsKey("content")){
                emp.setContent(highlightFields.get("content").fragments()[0].toString());
            }
            if(highlightFields.containsKey("name")){
                emp.setName(highlightFields.get("name").fragments()[0].toString());
            }
            if(highlightFields.containsKey("address")){
                emp.setAddress(highlightFields.get("address").fragments()[0].toString());
            }

            //放入集合
            emps.add(emp);
        }


        emps.forEach(emp-> System.out.println(emp));
    }





}
