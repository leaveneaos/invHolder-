package com.rjxx.taxeasy.dao;

import com.rjxx.comm.mybatis.MybatisRepository;
import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.domains.Kpspmx;

import java.util.List;
import java.util.Map;

/**
 * 由GenJavaCode类自动生成
 * <p>
 * Thu Nov 17 10:07:01 GMT+08:00 2016
 *
 * @ZhangBing
 */ 
@MybatisRepository
public interface KpspmxMapper {

    public Kpspmx findOneByParams(Map params);

    public List<Kpspmx> findAllByParams(Map params);

    public List<Kpspmx> findByPage(Pagination pagination);
    
    public List<Kpspmx> findMxList(Map params);

}

