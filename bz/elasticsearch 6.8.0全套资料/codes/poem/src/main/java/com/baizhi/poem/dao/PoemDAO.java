package com.baizhi.poem.dao;

import com.baizhi.poem.entity.Poem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PoemDAO {

     List<Poem> findAll();

    List<Poem> findByPage(@Param("start") Integer start, @Param("size") Integer size);

    Long findTotalCounts();
}
