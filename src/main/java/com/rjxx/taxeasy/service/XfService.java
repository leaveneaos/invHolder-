package com.rjxx.taxeasy.service;

import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.dao.XfJpaDao;
import com.rjxx.taxeasy.dao.XfMapper;
import com.rjxx.taxeasy.domains.Xf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 由GenJavaCode类自动生成
 * <p>
 * Thu Nov 10 12:48:40 CST 2016
 *
 * @ZhangBing
 */ 
@Service
public class XfService {

    @Autowired
    private XfJpaDao xfJpaDao;

    @Autowired
    private XfMapper xfMapper;

    public Xf findOne(int id) {
        return xfJpaDao.findOne(id);
    }

    public void save(Xf xf) {
        xfJpaDao.save(xf);
    }

    public void save(List<Xf> xfList) {
        xfJpaDao.save(xfList);
    }

    public Xf findOneByParams(Map params) {
        return xfMapper.findOneByParams(params);
    }

    public List<Xf> findAllByParams(Map params) {
        return xfMapper.findAllByParams(params);
    }

    public List<Xf> findByPage(Pagination pagination) {
        return xfMapper.findByPage(pagination);
    }

}

