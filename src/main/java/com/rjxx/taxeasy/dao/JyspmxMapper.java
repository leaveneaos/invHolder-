package com.rjxx.taxeasy.dao;

import com.rjxx.comm.mybatis.MybatisRepository;
import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.domains.Jyspmx;

import java.util.List;
import java.util.Map;

/**
 * 由GenJavaCode类自动生成
 * <p>
 * Wed Oct 19 09:07:25 CST 2016
 *
 * @ZhangBing
 */ 
@MybatisRepository
public interface JyspmxMapper {

    public Jyspmx findOneByParams(Map params);

    public List<Jyspmx> findAllByParams(Map params);

    public List<Jyspmx> findByPage(Pagination pagination);

}

