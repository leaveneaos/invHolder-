package com.rjxx.taxeasy.dao;

import com.rjxx.comm.mybatis.MybatisRepository;
import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.domains.Fpj;
import com.rjxx.taxeasy.vo.FpjVo;

import java.util.List;
import java.util.Map;

/**
 * 由GenJavaCode类自动生成
 * <p>
 * Thu Nov 03 14:27:31 GMT+08:00 2016
 *
 * @ZhangBing
 */ 
@MybatisRepository
public interface FpjMapper {

    public Fpj findOneByParams(Map params);

    public List<Fpj> findAllByParams(Map params);
    
    public List<FpjVo> findAllByParam(Map params);

    public List<FpjVo> findByPage(Pagination pagination);

}

