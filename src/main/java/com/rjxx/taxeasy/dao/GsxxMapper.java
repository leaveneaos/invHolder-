package com.rjxx.taxeasy.dao;

import com.rjxx.comm.mybatis.MybatisRepository;
import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.domains.Gsxx;

import java.util.List;
import java.util.Map;

/**
 * 由GenJavaCode类自动生成
 * <p>
 * Thu Nov 03 17:18:52 CST 2016
 *
 * @ZhangBing
 */ 
@MybatisRepository
public interface GsxxMapper {

    public Gsxx findOneByParams(Map params);

    public List<Gsxx> findAllByParams(Map params);

    public List<Gsxx> findByPage(Pagination pagination);

}

