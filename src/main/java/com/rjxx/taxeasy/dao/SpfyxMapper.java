package com.rjxx.taxeasy.dao;

import com.rjxx.comm.mybatis.MybatisRepository;
import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.domains.Spfyx;

import java.util.List;
import java.util.Map;

/**
 * 由GenJavaCode类自动生成
 * <p>
 * Fri Nov 18 11:40:10 CST 2016
 *
 * @ZhangBing
 */ 
@MybatisRepository
public interface SpfyxMapper {

    public Spfyx findOneByParams(Map params);

    public List<Spfyx> findAllByParams(Map params);

    public List<Spfyx> findByPage(Pagination pagination);

}

