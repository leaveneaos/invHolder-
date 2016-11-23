package com.rjxx.taxeasy.dao;

import com.rjxx.comm.mybatis.MybatisRepository;
import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.domains.Dzfplog;

import java.util.List;
import java.util.Map;

/**
 * 由GenJavaCode类自动生成
 * <p>
 * Thu Nov 17 10:05:41 GMT+08:00 2016
 *
 * @ZhangBing
 */ 
@MybatisRepository
public interface DzfplogMapper {

    public Dzfplog findOneByParams(Map params);

    public List<Dzfplog> findAllByParams(Map params);

    public List<Dzfplog> findByPage(Pagination pagination);

}

