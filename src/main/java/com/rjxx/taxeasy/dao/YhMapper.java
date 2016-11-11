package com.rjxx.taxeasy.dao;

import com.rjxx.comm.mybatis.MybatisRepository;
import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.domains.Yh;

import java.util.List;
import java.util.Map;

/**
 * 由GenJavaCode类自动生成
 * <p>
 * Thu Nov 10 14:37:56 CST 2016
 *
 * @ZhangBing
 */ 
@MybatisRepository
public interface YhMapper {

    public Yh findOneByParams(Map params);

    public List<Yh> findAllByParams(Map params);

    public List<Yh> findByPage(Pagination pagination);

}

