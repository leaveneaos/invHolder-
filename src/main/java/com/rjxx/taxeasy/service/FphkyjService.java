package com.rjxx.taxeasy.service;

import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.dao.FphkyjJpaDao;
import com.rjxx.taxeasy.dao.FphkyjMapper;
import com.rjxx.taxeasy.domains.Fphkyj;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 由GenJavaCode类自动生成
 * <p>
 * Thu Oct 20 13:06:31 CST 2016
 *
 * @ZhangBing
 */ 
@Service
public class FphkyjService {

    @Autowired
    private FphkyjJpaDao fphkyjJpaDao;

    @Autowired
    private FphkyjMapper fphkyjMapper;

    public Fphkyj findOne(int id) {
        return fphkyjJpaDao.findOne(id);
    }

    public void save(Fphkyj fphkyj) {
        fphkyjJpaDao.save(fphkyj);
    }

    public void save(List<Fphkyj> fphkyjList) {
        fphkyjJpaDao.save(fphkyjList);
    }

    public Fphkyj findOneByParams(Map params) {
        return fphkyjMapper.findOneByParams(params);
    }

    public List<Fphkyj> findAllByParams(Map params) {
        return fphkyjMapper.findAllByParams(params);
    }

    public List<Fphkyj> findByPage(Pagination pagination) {
        return fphkyjMapper.findByPage(pagination);
    }

}

