package com.rjxx.taxeasy.dao;

import com.rjxx.comm.mybatis.MybatisRepository;
import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.domains.Wxkb;

import java.util.List;
import java.util.Map;

/**
 * 由GenJavaCode类自动生成
 * <p>
 * Tue Oct 25 17:47:52 CST 2016
 *
 * @ZhangBing
 */ 
@MybatisRepository
public interface WxkbMapper {

    public Wxkb findOneByParams(Map params);

    public List<Wxkb> findAllByParams(Map params);

    public List<Wxkb> findByPage(Pagination pagination);

}

