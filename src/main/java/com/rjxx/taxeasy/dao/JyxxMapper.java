package com.rjxx.taxeasy.dao;

import com.rjxx.comm.mybatis.MybatisRepository;
import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.domains.Jyxx;

import java.util.List;
import java.util.Map;

/**
 * 由GenJavaCode类自动生成
 * <p>
 * Tue Nov 22 10:36:35 CST 2016
 *
 * @ZhangBing
 */ 
@MybatisRepository
public interface JyxxMapper {

    public Jyxx findOneByParams(Map params);

    public List<Jyxx> findAllByParams(Map params);

    public List<Jyxx> findByPage(Pagination pagination);

}

