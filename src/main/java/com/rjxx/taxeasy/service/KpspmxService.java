package com.rjxx.taxeasy.service;

import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.dao.KpspmxJpaDao;
import com.rjxx.taxeasy.dao.KpspmxMapper;
import com.rjxx.taxeasy.domains.Kpspmx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 由GenJavaCode类自动生成
 * <p>
 * Thu Nov 17 10:07:01 GMT+08:00 2016
 *
 * @ZhangBing
 */ 
@Service
public class KpspmxService {

    @Autowired
    private KpspmxJpaDao kpspmxJpaDao;

    @Autowired
    private KpspmxMapper kpspmxMapper;

    public Kpspmx findOne(int id) {
        return kpspmxJpaDao.findOne(id);
    }

    public void save(Kpspmx kpspmx) {
        kpspmxJpaDao.save(kpspmx);
    }

    public void save(List<Kpspmx> kpspmxList) {
        kpspmxJpaDao.save(kpspmxList);
    }

    public Kpspmx findOneByParams(Map params) {
        return kpspmxMapper.findOneByParams(params);
    }

    public List<Kpspmx> findAllByParams(Map params) {
        return kpspmxMapper.findAllByParams(params);
    }

    public List<Kpspmx> findByPage(Pagination pagination) {
        return kpspmxMapper.findByPage(pagination);
    }
    
    public List<Kpspmx> findMxList(Map params){
    	return kpspmxMapper.findMxList(params);
    }

}

