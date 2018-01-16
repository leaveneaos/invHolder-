package com.rjxx.taxeasy.controller;

import com.rjxx.taxeasy.comm.BaseController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

/**
 * Created by Administrator on 2018-01-16.
 */
@RestController
@RequestMapping(value = "/e-invoice-file")
public class WjtzController extends BaseController {

    @RequestMapping(value = "/{a}/{b}/{c}")
    public void index(@PathVariable String a,@PathVariable String b,@PathVariable String c) throws Exception{
        logger.info("----"+a);
        logger.info("----"+b);
        logger.info("----"+c);
        File file =new File("/usr/local/e-invoice-file/e-invoice-file/" + a + "/"+ b +"/"+ c +".pdf");
        if(file.exists()) {
            logger.info("存在");
            response.sendRedirect("http://test.datarj.com/now/"+a+"/"+b+"/"+c+".pdf");
        }else {
            logger.info("不存在");
            response.sendRedirect("http://test.datarj.com/find/"+a+"/"+b+"/"+c+".pdf");
        }

    }
}
