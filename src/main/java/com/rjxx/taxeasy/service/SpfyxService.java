package com.rjxx.taxeasy.service;

import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.dao.SpfyxJpaDao;
import com.rjxx.taxeasy.dao.SpfyxMapper;
import com.rjxx.taxeasy.domains.Spfyx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 由GenJavaCode类自动生成
 * <p>
 * Fri Nov 18 11:40:10 CST 2016
 *
 * @ZhangBing
 */ 
@Service
public class SpfyxService {

    @Autowired
    private SpfyxJpaDao spfyxJpaDao;

    @Autowired
    private SpfyxMapper spfyxMapper;

    public Spfyx findOne(int id) {
        return spfyxJpaDao.findOne(id);
    }

    public void save(Spfyx spfyx) {
        spfyxJpaDao.save(spfyx);
    }

    public void save(List<Spfyx> spfyxList) {
        spfyxJpaDao.save(spfyxList);
    }

    public Spfyx findOneByParams(Map params) {
        return spfyxMapper.findOneByParams(params);
    }

    public List<Spfyx> findAllByParams(Map params) {
        return spfyxMapper.findAllByParams(params);
    }

    public List<Spfyx> findByPage(Pagination pagination) {
        return spfyxMapper.findByPage(pagination);
    }

}

