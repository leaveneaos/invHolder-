package com.rjxx.taxeasy.service;

import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.dao.JyspmxJpaDao;
import com.rjxx.taxeasy.dao.JyspmxMapper;
import com.rjxx.taxeasy.domains.Jyspmx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 由GenJavaCode类自动生成
 * <p>
 * Wed Oct 19 09:07:25 CST 2016
 *
 * @ZhangBing
 */ 
@Service
public class JyspmxService {

    @Autowired
    private JyspmxJpaDao jyspmxJpaDao;

    @Autowired
    private JyspmxMapper jyspmxMapper;

    public Jyspmx findOne(int id) {
        return jyspmxJpaDao.findOne(id);
    }

    public void save(Jyspmx jyspmx) {
        jyspmxJpaDao.save(jyspmx);
    }

    public void save(List<Jyspmx> jyspmxList) {
        jyspmxJpaDao.save(jyspmxList);
    }

    public Jyspmx findOneByParams(Map params) {
        return jyspmxMapper.findOneByParams(params);
    }

    public List<Jyspmx> findAllByParams(Map params) {
        return jyspmxMapper.findAllByParams(params);
    }

    public List<Jyspmx> findByPage(Pagination pagination) {
        return jyspmxMapper.findByPage(pagination);
    }

}

