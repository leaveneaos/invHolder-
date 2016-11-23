package com.rjxx.taxeasy.service;

import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.dao.DzfplogJpaDao;
import com.rjxx.taxeasy.dao.DzfplogMapper;
import com.rjxx.taxeasy.domains.Dzfplog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 由GenJavaCode类自动生成
 * <p>
 * Thu Nov 17 10:05:41 GMT+08:00 2016
 *
 * @ZhangBing
 */ 
@Service
public class DzfplogService {

    @Autowired
    private DzfplogJpaDao dzfplogJpaDao;

    @Autowired
    private DzfplogMapper dzfplogMapper;

    public Dzfplog findOne(int id) {
        return dzfplogJpaDao.findOne(id);
    }

    public void save(Dzfplog dzfplog) {
        dzfplogJpaDao.save(dzfplog);
    }

    public void save(List<Dzfplog> dzfplogList) {
        dzfplogJpaDao.save(dzfplogList);
    }

    public Dzfplog findOneByParams(Map params) {
        return dzfplogMapper.findOneByParams(params);
    }

    public List<Dzfplog> findAllByParams(Map params) {
        return dzfplogMapper.findAllByParams(params);
    }

    public List<Dzfplog> findByPage(Pagination pagination) {
        return dzfplogMapper.findByPage(pagination);
    }

}

