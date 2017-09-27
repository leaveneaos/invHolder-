package com.rjxx.taxeasy.wechat.controller;

import com.rjxx.taxeasy.wechat.service.SimpleInvoiceService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Administrator on 2017/9/27 0027.
 */
@RestController
@RequestMapping("/extractData")
public class UploadDataController {
    @Autowired
    private SimpleInvoiceService simpleInvoiceService;

    @ApiOperation(value = "上传交易数据")
    @RequestMapping(value = "/upload",method = RequestMethod.POST)
    public String upload(@RequestParam String appid, @RequestParam String sign, @RequestParam String orderData){
        return simpleInvoiceService.getMsg(appid, sign, orderData);
    }
}
