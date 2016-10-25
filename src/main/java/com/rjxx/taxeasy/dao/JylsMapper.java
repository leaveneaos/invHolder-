package com.rjxx.taxeasy.dao;

import com.rjxx.comm.mybatis.MybatisRepository;
import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.domains.Jyls;

import java.util.List;
import java.util.Map;

/**
 * 由GenJavaCode类自动生成
 * <p>
 * Tue Oct 18 12:47:38 CST 2016
 *
 * @ZhangBing
 */ 
@MybatisRepository
public interface JylsMapper {

    public Jyls findOneByParams(Map params);

    public List<Jyls> findAllByParams(Map params);

    public List<Jyls> findByPage(Pagination pagination);

    public Jyls findByTqm(Map params);
}

