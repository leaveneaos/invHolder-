package com.rjxx.taxeasy.dao;

import com.rjxx.comm.mybatis.MybatisRepository;
import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.domains.Fphkyj;

import java.util.List;
import java.util.Map;

/**
 * 由GenJavaCode类自动生成
 * <p>
 * Thu Oct 20 13:06:31 CST 2016
 *
 * @ZhangBing
 */ 
@MybatisRepository
public interface FphkyjMapper {

    public Fphkyj findOneByParams(Map params);

    public List<Fphkyj> findAllByParams(Map params);

    public List<Fphkyj> findByPage(Pagination pagination);

}

