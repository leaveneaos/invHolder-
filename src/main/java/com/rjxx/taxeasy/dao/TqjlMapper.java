package com.rjxx.taxeasy.dao;

import com.rjxx.comm.mybatis.MybatisRepository;
import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.domains.Tqjl;

import java.util.List;
import java.util.Map;

/**
 * 由GenJavaCode类自动生成
 * <p>
 * Thu Nov 03 14:27:00 CST 2016
 *
 * @ZhangBing
 */ 
@MybatisRepository
public interface TqjlMapper {

    public Tqjl findOneByParams(Map params);

    public List<Tqjl> findAllByParams(Map params);

    public List<Tqjl> findByPage(Pagination pagination);

}

