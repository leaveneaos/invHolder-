package com.rjxx.taxeasy.dao;

import com.rjxx.comm.mybatis.MybatisRepository;
import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.domains.Skp;

import java.util.List;
import java.util.Map;

/**
 * 由GenJavaCode类自动生成
 * <p>
 * Thu Nov 10 12:49:54 CST 2016
 *
 * @ZhangBing
 */ 
@MybatisRepository
public interface SkpMapper {

    public Skp findOneByParams(Map params);

    public List<Skp> findAllByParams(Map params);

    public List<Skp> findByPage(Pagination pagination);

}

