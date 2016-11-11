package com.rjxx.taxeasy.service;

import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.dao.YhJpaDao;
import com.rjxx.taxeasy.dao.YhMapper;
import com.rjxx.taxeasy.domains.Yh;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 由GenJavaCode类自动生成
 * <p>
 * Thu Nov 10 14:37:56 CST 2016
 *
 * @ZhangBing
 */ 
@Service
public class YhService {

    @Autowired
    private YhJpaDao yhJpaDao;

    @Autowired
    private YhMapper yhMapper;

    public Yh findOne(int id) {
        return yhJpaDao.findOne(id);
    }

    public void save(Yh yh) {
        yhJpaDao.save(yh);
    }

    public void save(List<Yh> yhList) {
        yhJpaDao.save(yhList);
    }

    public Yh findOneByParams(Map params) {
        return yhMapper.findOneByParams(params);
    }

    public List<Yh> findAllByParams(Map params) {
        return yhMapper.findAllByParams(params);
    }

    public List<Yh> findByPage(Pagination pagination) {
        return yhMapper.findByPage(pagination);
    }

}

