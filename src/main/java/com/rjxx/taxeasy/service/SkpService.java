package com.rjxx.taxeasy.service;

import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.dao.SkpJpaDao;
import com.rjxx.taxeasy.dao.SkpMapper;
import com.rjxx.taxeasy.domains.Skp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 由GenJavaCode类自动生成
 * <p>
 * Thu Nov 10 12:49:54 CST 2016
 *
 * @ZhangBing
 */ 
@Service
public class SkpService {

    @Autowired
    private SkpJpaDao skpJpaDao;

    @Autowired
    private SkpMapper skpMapper;

    public Skp findOne(int id) {
        return skpJpaDao.findOne(id);
    }

    public void save(Skp skp) {
        skpJpaDao.save(skp);
    }

    public void save(List<Skp> skpList) {
        skpJpaDao.save(skpList);
    }

    public Skp findOneByParams(Map params) {
        return skpMapper.findOneByParams(params);
    }

    public List<Skp> findAllByParams(Map params) {
        return skpMapper.findAllByParams(params);
    }

    public List<Skp> findByPage(Pagination pagination) {
        return skpMapper.findByPage(pagination);
    }
    
    public List<Skp> getKpd(Map params){
    	return skpMapper.getKpd(params);
    }

}

