package com.rjxx.taxeasy.controller;

import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.utils.weixin.WeixinUtils;
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
    public Map isWeiXin(String orderNo, String order, String orderTime, String price){
        String redirectUrl ="";
        String ua = request.getHeader("user-agent").toLowerCase();
        Map resultMap = new HashMap();
        if(WeixinUtils.isWeiXinBrowser(request)){
            logger.info("微信浏览器--------------");
            WeixinUtils weixinUtils = new WeixinUtils();
            try {
                redirectUrl = weixinUtils.getTiaoURL(order,price,orderTime,orderNo);
                if(null==redirectUrl||redirectUrl.equals("")){
                    resultMap.put("msg","获取微信授权失败!请重试");
                    resultMap.put("num","2");
                    return resultMap;
                }
                resultMap.put("num","0");
                resultMap.put("redirectUrl",redirectUrl);
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
