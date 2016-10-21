package com.rjxx.taxeasy.dao;

import com.rjxx.comm.mybatis.MybatisRepository;
import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.domains.Kpls;

import java.util.List;
import java.util.Map;

/**
 * 由GenJavaCode类自动生成
 * <p>
 * Tue Oct 18 15:54:11 CST 2016
 *
 * @ZhangBing
 */ 
@MybatisRepository
public interface KplsMapper {

    public Kpls findOneByParams(Map params);

    public List<Kpls> findAllByParams(Map params);

    public List<Kpls> findByPage(Pagination pagination);
    
    public List<Kpls> findByDjh(Kpls kpls);
}

