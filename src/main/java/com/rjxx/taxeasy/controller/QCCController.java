package com.rjxx.taxeasy.controller;

import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.dao.QccJpaDao;
import com.rjxx.utils.yjapi.QCCUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by wangyahui on 2017/11/27 0027.
 * 企查查API
 */
@RestController
@RequestMapping("/companyInfo")
public class QCCController extends BaseController{

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private QCCUtils qccUtils;

    @Autowired
    private QccJpaDao qccJpaDao;

    @RequestMapping(value = "/getNames", method = RequestMethod.GET)
    public String getNames(@RequestParam("name")String name) {
        logger.info("输入的参数为："+name);
        return qccUtils.getQccSearch(name);
    }

    @RequestMapping(value = "/getMsg",method = RequestMethod.GET)
    public String getMsg(@RequestParam("name")String name){
        logger.info("输入的参数为："+name);
        return qccJpaDao.findNsrsbhByGsmc(name);
    }
}
