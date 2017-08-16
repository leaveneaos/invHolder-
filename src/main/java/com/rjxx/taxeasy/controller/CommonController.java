package com.rjxx.taxeasy.controller;

import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.utils.weixin.WeixinUtils;
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
                redirectUrl = weixinUtils.getTiaoURL(orderNo,price,orderTime, storeNo);
                if(null==redirectUrl||redirectUrl.equals("")){
                   /* resultMap.put("msg","获取微信授权失败!请重试");
                    resultMap.put("num","2");
                    return resultMap;*/
                    request.getSession().setAttribute("msg", "获取微信授权失败!请重试!");
                    response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                    return null;
                }else {
                    response.sendRedirect(redirectUrl);
                    return null;
                }

               /* resultMap.put("num","0");
                resultMap.put("redirectUrl",redirectUrl);*/
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
