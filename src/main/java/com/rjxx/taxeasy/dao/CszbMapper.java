package com.rjxx.taxeasy.dao;

import com.rjxx.comm.mybatis.MybatisRepository;
import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.domains.Cszb;

import java.util.List;
import java.util.Map;

/**
 * 由GenJavaCode类自动生成
 * <p>
 * Thu Nov 10 09:53:40 CST 2016
 *
 * @ZhangBing
 */ 
@MybatisRepository
public interface CszbMapper {

    public Cszb findOneByParams(Map params);

    public List<Cszb> findAllByParams(Map params);

    public List<Cszb> findByPage(Pagination pagination);

}

