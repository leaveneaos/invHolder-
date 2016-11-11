package com.rjxx.taxeasy.service;

import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.dao.CszbJpaDao;
import com.rjxx.taxeasy.dao.CszbMapper;
import com.rjxx.taxeasy.domains.Cszb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 由GenJavaCode类自动生成
 * <p>
 * Thu Nov 10 09:53:40 CST 2016
 *
 * @ZhangBing
 */ 
@Service
public class CszbService {

    @Autowired
    private CszbJpaDao cszbJpaDao;

    @Autowired
    private CszbMapper cszbMapper;

    public Cszb findOne(int id) {
        return cszbJpaDao.findOne(id);
    }

    public void save(Cszb cszb) {
        cszbJpaDao.save(cszb);
    }

    public void save(List<Cszb> cszbList) {
        cszbJpaDao.save(cszbList);
    }

    public Cszb findOneByParams(Map params) {
        return cszbMapper.findOneByParams(params);
    }

    public List<Cszb> findAllByParams(Map params) {
        return cszbMapper.findAllByParams(params);
    }

    public List<Cszb> findByPage(Pagination pagination) {
        return cszbMapper.findByPage(pagination);
    }

}

