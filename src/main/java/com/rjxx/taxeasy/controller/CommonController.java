package com.rjxx.taxeasy.controller;

import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.dao.WxfpxxJpaDao;
import com.rjxx.taxeasy.domains.WxFpxx;
import com.rjxx.taxeasy.service.BarcodeService;
import com.rjxx.utils.weixin.WeixinUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017-08-16.
 */
@Controller
@RequestMapping("/common")
public class CommonController extends BaseController {


    @Autowired
    private WxfpxxJpaDao wxfpxxJpaDao;
    @Autowired
    private BarcodeService barcodeService;
    //判断是否微信浏览器
    @RequestMapping(value = "/isBrowser")
    @ResponseBody
    public Map isWeiXin(String storeNo, String orderNo, String orderTime, String price){
        String redirectUrl ="";
        Map resultMap = new HashMap();
        if(WeixinUtils.isWeiXinBrowser(request)){
            logger.info("微信浏览器--------------");
            WeixinUtils weixinUtils = new WeixinUtils();
            try {
                //查询是否开具
                WxFpxx wxFpxx = wxfpxxJpaDao.selsetByOrderNo(orderNo);
                String status = barcodeService.checkStatus(wxFpxx.getTqm(),wxFpxx.getGsdm());
                if(status!=null&& status.equals("开具中")){
                    //开具中对应的url
                    response.sendRedirect(request.getContextPath() + "/QR/zzkj.html?t=" + System.currentTimeMillis());
                    return null;
                }else if(status!=null && status.equals("可开具")){
                    //可开具 跳转微信授权链接
                    redirectUrl = weixinUtils.getTiaoURL(orderNo,price,orderTime, storeNo);
                    if(null==redirectUrl||redirectUrl.equals("")){
                        //获取授权失败
                        request.getSession().setAttribute("msg", "获取微信授权失败!请重试!");
                        response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                        return null;
                    }else {
                        //成功跳转
                        response.sendRedirect(redirectUrl);
                        return null;
                    }
                }else {
                    request.getSession().setAttribute("msg", "获取微信授权失败!请重试!");
                    response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                    return null;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            logger.info("不是微信浏览器-------------");
            resultMap.put("num" ,"1");
        }
        return resultMap;
    }
}
