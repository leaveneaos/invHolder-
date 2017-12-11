//package com.rjxx.taxeasy.controller;
//
//import com.rjxx.taxeasy.comm.BaseController;
//import com.rjxx.utils.yjapi.QCCUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
///**
// * Created by wangyahui on 2017/11/27 0027.
// * 企查查API
// */
//@RestController
//@RequestMapping("/companyInfo")
//public class QCCController extends BaseController{
//
//    private Logger logger = LoggerFactory.getLogger(this.getClass());
//
//    @Autowired
//    private QCCUtils qccUtils;
//
//    @RequestMapping(value = "/getNames", method = RequestMethod.POST)
//    public String getNames(@RequestParam("name")String name) {
//        logger.info("输入的参数为："+name);
//        String result =qccUtils.getQccSearch(name);
//        logger.info("输出为："+result);
//        return result;
//    }
//}
