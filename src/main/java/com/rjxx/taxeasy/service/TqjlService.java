package com.rjxx.taxeasy.service;

import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.dao.TqjlJpaDao;
import com.rjxx.taxeasy.dao.TqjlMapper;
import com.rjxx.taxeasy.domains.Tqjl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 由GenJavaCode类自动生成
 * <p>
 * Thu Nov 03 14:27:00 CST 2016
 *
 * @ZhangBing
 */ 
@Service
public class TqjlService {

    @Autowired
    private TqjlJpaDao tqjlJpaDao;

    @Autowired
    private TqjlMapper tqjlMapper;

    public Tqjl findOne(int id) {
        return tqjlJpaDao.findOne(id);
    }

    public void save(Tqjl tqjl) {
        tqjlJpaDao.save(tqjl);
    }

    public void save(List<Tqjl> tqjlList) {
        tqjlJpaDao.save(tqjlList);
    }

    public Tqjl findOneByParams(Map params) {
        return tqjlMapper.findOneByParams(params);
    }

    public List<Tqjl> findAllByParams(Map params) {
        return tqjlMapper.findAllByParams(params);
    }

    public List<Tqjl> findByPage(Pagination pagination) {
        return tqjlMapper.findByPage(pagination);
    }

}

