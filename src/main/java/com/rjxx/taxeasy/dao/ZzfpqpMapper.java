package com.rjxx.taxeasy.dao;

import com.rjxx.comm.mybatis.MybatisRepository;
import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.domains.Zzfpqp;

import java.util.List;
import java.util.Map;

/**
 * 由GenJavaCode类自动生成
 * <p>
 * Thu Nov 17 16:47:06 GMT+08:00 2016
 *
 * @ZhangBing
 */ 
@MybatisRepository
public interface ZzfpqpMapper {

    public Zzfpqp findOneByParams(Map params);

    public List<Zzfpqp> findAllByParams(Map params);

    public List<Zzfpqp> findByPage(Pagination pagination);

}

