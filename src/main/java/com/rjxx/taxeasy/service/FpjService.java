package com.rjxx.taxeasy.service;

import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.dao.FpjJpaDao;
import com.rjxx.taxeasy.dao.FpjMapper;
import com.rjxx.taxeasy.domains.Fpj;
import com.rjxx.taxeasy.vo.FpjVo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 由GenJavaCode类自动生成
 * <p>
 * Thu Nov 03 14:27:31 GMT+08:00 2016
 *
 * @ZhangBing
 */ 
@Service
public class FpjService {

    @Autowired
    private FpjJpaDao fpjJpaDao;

    @Autowired
    private FpjMapper fpjMapper;

    public Fpj findOne(int id) {
        return fpjJpaDao.findOne(id);
    }

    public void save(Fpj fpj) {
        fpjJpaDao.save(fpj);
    }

    public void save(List<Fpj> fpjList) {
        fpjJpaDao.save(fpjList);
    }

    public Fpj findOneByParams(Map params) {
        return fpjMapper.findOneByParams(params);
    }

    public List<Fpj> findAllByParams(Map params) {
        return fpjMapper.findAllByParams(params);
    }

    public List<FpjVo> findAllByParam(Map params) {
        return fpjMapper.findAllByParam(params);
    }

    public List<FpjVo> findByPage(Pagination pagination) {
        return fpjMapper.findByPage(pagination);
    }

}

