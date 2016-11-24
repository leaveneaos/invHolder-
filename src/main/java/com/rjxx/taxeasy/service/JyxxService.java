package com.rjxx.taxeasy.service;

import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.dao.JyxxJpaDao;
import com.rjxx.taxeasy.dao.JyxxMapper;
import com.rjxx.taxeasy.domains.Jyxx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 由GenJavaCode类自动生成
 * <p>
 * Tue Nov 22 10:36:35 CST 2016
 *
 * @ZhangBing
 */ 
@Service
public class JyxxService {

    @Autowired
    private JyxxJpaDao jyxxJpaDao;

    @Autowired
    private JyxxMapper jyxxMapper;

    public Jyxx findOne(int id) {
        return jyxxJpaDao.findOne(id);
    }

    public void save(Jyxx jyxx) {
        jyxxJpaDao.save(jyxx);
    }

    public void save(List<Jyxx> jyxxList) {
        jyxxJpaDao.save(jyxxList);
    }

    public Jyxx findOneByParams(Map params) {
        return jyxxMapper.findOneByParams(params);
    }

    public List<Jyxx> findAllByParams(Map params) {
        return jyxxMapper.findAllByParams(params);
    }

    public List<Jyxx> findByPage(Pagination pagination) {
        return jyxxMapper.findByPage(pagination);
    }

}

