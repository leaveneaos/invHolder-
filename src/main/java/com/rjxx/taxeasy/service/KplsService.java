package com.rjxx.taxeasy.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.dao.KplsJpaDao;
import com.rjxx.taxeasy.dao.KplsMapper;
import com.rjxx.taxeasy.domains.Kpls;

/**
 * 由GenJavaCode类自动生成
 * <p>
 * Tue Oct 18 15:54:11 CST 2016
 *
 * @ZhangBing
 */ 
@Service
public class KplsService {

    @Autowired
    private KplsJpaDao kplsJpaDao;

    @Autowired
    private KplsMapper kplsMapper;

    public Kpls findOne(int id) {
        return kplsJpaDao.findOne(id);
    }

    public void save(Kpls kpls) {
        kplsJpaDao.save(kpls);
    }

    public void save(List<Kpls> kplsList) {
        kplsJpaDao.save(kplsList);
    }

    public Kpls findOneByParams(Map params) {
        return kplsMapper.findOneByParams(params);
    }

    public List<Kpls> findAllByParams(Map params) {
        return kplsMapper.findAllByParams(params);
    }

    public List<Kpls> findByPage(Pagination pagination) {
        return kplsMapper.findByPage(pagination);
    }
    
    public List<Kpls> findByDjh(Kpls kpls){
    	return kplsMapper.findByDjh(kpls);
    }

}

