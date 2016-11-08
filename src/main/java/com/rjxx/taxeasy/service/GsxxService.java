package com.rjxx.taxeasy.service;

import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.dao.GsxxJpaDao;
import com.rjxx.taxeasy.dao.GsxxMapper;
import com.rjxx.taxeasy.domains.Gsxx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 由GenJavaCode类自动生成
 * <p>
 * Thu Nov 03 17:18:52 CST 2016
 *
 * @ZhangBing
 */ 
@Service
public class GsxxService {

    @Autowired
    private GsxxJpaDao gsxxJpaDao;

    @Autowired
    private GsxxMapper gsxxMapper;

    public Gsxx findOne(int id) {
        return gsxxJpaDao.findOne(id);
    }

    public void save(Gsxx gsxx) {
        gsxxJpaDao.save(gsxx);
    }

    public void save(List<Gsxx> gsxxList) {
        gsxxJpaDao.save(gsxxList);
    }

    public Gsxx findOneByParams(Map params) {
        return gsxxMapper.findOneByParams(params);
    }

    public List<Gsxx> findAllByParams(Map params) {
        return gsxxMapper.findAllByParams(params);
    }

    public List<Gsxx> findByPage(Pagination pagination) {
        return gsxxMapper.findByPage(pagination);
    }

}

