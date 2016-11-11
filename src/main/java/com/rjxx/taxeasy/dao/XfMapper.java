package com.rjxx.taxeasy.dao;

import com.rjxx.comm.mybatis.MybatisRepository;
import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.domains.Xf;

import java.util.List;
import java.util.Map;

/**
 * 由GenJavaCode类自动生成
 * <p>
 * Thu Nov 10 12:48:40 CST 2016
 *
 * @ZhangBing
 */ 
@MybatisRepository
public interface XfMapper {

    public Xf findOneByParams(Map params);

    public List<Xf> findAllByParams(Map params);

    public List<Xf> findByPage(Pagination pagination);

}

