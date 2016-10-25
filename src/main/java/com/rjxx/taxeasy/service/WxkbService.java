package com.rjxx.taxeasy.service;

import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.dao.WxkbJpaDao;
import com.rjxx.taxeasy.dao.WxkbMapper;
import com.rjxx.taxeasy.domains.Wxkb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 由GenJavaCode类自动生成
 * <p>
 * Tue Oct 25 17:47:52 CST 2016
 *
 * @ZhangBing
 */ 
@Service
public class WxkbService {

    @Autowired
    private WxkbJpaDao wxkbJpaDao;

    @Autowired
    private WxkbMapper wxkbMapper;

    public Wxkb findOne(int id) {
        return wxkbJpaDao.findOne(id);
    }

    public void save(Wxkb wxkb) {
        wxkbJpaDao.save(wxkb);
    }

    public void save(List<Wxkb> wxkbList) {
        wxkbJpaDao.save(wxkbList);
    }

    public Wxkb findOneByParams(Map params) {
        return wxkbMapper.findOneByParams(params);
    }

    public List<Wxkb> findAllByParams(Map params) {
        return wxkbMapper.findAllByParams(params);
    }

    public List<Wxkb> findByPage(Pagination pagination) {
        return wxkbMapper.findByPage(pagination);
    }

}

