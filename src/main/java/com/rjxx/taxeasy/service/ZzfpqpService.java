package com.rjxx.taxeasy.service;

import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.dao.ZzfpqpJpaDao;
import com.rjxx.taxeasy.dao.ZzfpqpMapper;
import com.rjxx.taxeasy.domains.Zzfpqp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 由GenJavaCode类自动生成
 * <p>
 * Thu Nov 17 16:47:06 GMT+08:00 2016
 *
 * @ZhangBing
 */ 
@Service
public class ZzfpqpService {

    @Autowired
    private ZzfpqpJpaDao zzfpqpJpaDao;

    @Autowired
    private ZzfpqpMapper zzfpqpMapper;

    public Zzfpqp findOne(int id) {
        return zzfpqpJpaDao.findOne(id);
    }

    public void save(Zzfpqp zzfpqp) {
        zzfpqpJpaDao.save(zzfpqp);
    }

    public void save(List<Zzfpqp> zzfpqpList) {
        zzfpqpJpaDao.save(zzfpqpList);
    }

    public Zzfpqp findOneByParams(Map params) {
        return zzfpqpMapper.findOneByParams(params);
    }

    public List<Zzfpqp> findAllByParams(Map params) {
        return zzfpqpMapper.findAllByParams(params);
    }

    public List<Zzfpqp> findByPage(Pagination pagination) {
        return zzfpqpMapper.findByPage(pagination);
    }

}

