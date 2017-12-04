package com.rjxx.taxeasy.controller;

import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.utils.yjapi.QCCUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

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

    @RequestMapping(value = "/getNames", method = RequestMethod.GET)
    public String getNames(@RequestParam("name")String name) {
        logger.info("转码前："+name);
        byte[] b= new byte[0];//用tomcat的格式（iso-8859-1）方式去读。
        try {
            b = name.getBytes("ISO-8859-1");
            String str=new String(b,"utf-8");
            logger.info("转成utf8:",str);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String encodeName = null;
        try {
            encodeName = URLDecoder.decode(name, "UTF-8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        logger.info("输入的参数为："+encodeName);
        String result =qccUtils.getQccSearch(encodeName);
        logger.info("输出为："+result);
        return result;
    }
}
