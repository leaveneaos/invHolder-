package com.rjxx.taxeasy.service;

import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.dao.JylsJpaDao;
import com.rjxx.taxeasy.dao.JylsMapper;
import com.rjxx.taxeasy.domains.Jyls;
import com.rjxx.taxeasy.domains.Kpls;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 由GenJavaCode类自动生成
 * <p>
 * Tue Oct 18 12:47:38 CST 2016
 *
 * @ZhangBing
 */ 
@Service
public class JylsService {

    @Autowired
    private JylsJpaDao jylsJpaDao;

    @Autowired
    private JylsMapper jylsMapper;

    public Jyls findOne(int id) {
        return jylsJpaDao.findOne(id);
    }

    public void save(Jyls jyls) {
        jylsJpaDao.save(jyls);
    }

    public void save(List<Jyls> jylsList) {
        jylsJpaDao.save(jylsList);
    }

    public Jyls findOneByParams(Map params) {
        return jylsMapper.findOneByParams(params);
    }

    public List<Jyls> findAllByParams(Map params) {
        return jylsMapper.findAllByParams(params);
    }

    public List<Jyls> findByPage(Pagination pagination) {
        return jylsMapper.findByPage(pagination);
    }
   
    public List<Kpls> findByTqm(Map params){
    	 return jylsMapper.findByTqm(params);
    }

}

