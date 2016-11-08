package com.rjxx.taxeasy.service;

import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.dao.SmtqJpaDao;
import com.rjxx.taxeasy.dao.SmtqMapper;
import com.rjxx.taxeasy.domains.Smtq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 由GenJavaCode类自动生成
 * <p>
 * Mon Nov 07 11:17:35 CST 2016
 *
 * @ZhangBing
 */ 
@Service
public class SmtqService {

    @Autowired
    private SmtqJpaDao smtqJpaDao;

    @Autowired
    private SmtqMapper smtqMapper;

    public Smtq findOne(int id) {
        return smtqJpaDao.findOne(id);
    }

    public void save(Smtq smtq) {
        smtqJpaDao.save(smtq);
    }

    public void save(List<Smtq> smtqList) {
        smtqJpaDao.save(smtqList);
    }

    public Smtq findOneByParams(Map params) {
        return smtqMapper.findOneByParams(params);
    }

    public List<Smtq> findAllByParams(Map params) {
        return smtqMapper.findAllByParams(params);
    }

    public List<Smtq> findByPage(Pagination pagination) {
        return smtqMapper.findByPage(pagination);
    }

}

