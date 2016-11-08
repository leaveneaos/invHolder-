package com.rjxx.taxeasy.dao;

import com.rjxx.comm.mybatis.MybatisRepository;
import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.domains.Smtq;

import java.util.List;
import java.util.Map;

/**
 * 由GenJavaCode类自动生成
 * <p>
 * Mon Nov 07 11:17:35 CST 2016
 *
 * @ZhangBing
 */ 
@MybatisRepository
public interface SmtqMapper {

    public Smtq findOneByParams(Map params);

    public List<Smtq> findAllByParams(Map params);

    public List<Smtq> findByPage(Pagination pagination);

}

