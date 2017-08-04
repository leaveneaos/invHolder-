package com.rjxx.taxeasy.controller;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.domains.Kpspmx;
import com.rjxx.taxeasy.domains.Pp;
import com.rjxx.taxeasy.service.KplsService;
import com.rjxx.taxeasy.service.KpspmxService;
import com.rjxx.taxeasy.service.PpService;
import com.rjxx.taxeasy.utils.alipay.AlipayConstants;
import com.rjxx.taxeasy.utils.alipay.AlipayUtils;
import com.rjxx.taxeasy.utils.weixin.WeiXinConstants;
import com.rjxx.taxeasy.utils.weixin.WeixinUtils;
import com.rjxx.utils.HtmlUtils;
import com.rjxx.utils.StringUtils;
import com.rjxx.utils.WeixinUtil;
import org.apache.commons.codec.binary.Base64;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017-06-26.
 */
@Controller
public class WeiXinController extends BaseController {

    @Autowired
    private KplsService kplsService;

    @Autowired
    private KpspmxService kpspmxService;

    @Autowired
    private PpService ppService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 获取微信授权回调
     */
    @RequestMapping(value = WeiXinConstants.AFTER_WEIXIN_REDIRECT_URL)
    @ResponseBody
    public String getWeiXin(String data) throws Exception {
        Document xmlDoc = null;

        try {
            //解析微信返回的消息推送的xml
            xmlDoc = DocumentHelper.parseText(data);
            Element rootElt = xmlDoc.getRootElement();
            System.out.println("根节点：" + rootElt.getName());
            List<Element> childElements = rootElt.elements();
            String SuccOrderIdValue = "";
            String FailOrderIdValue = "";
            for (Element e:childElements){
                if(e.getName().equals("SuccOrderId")&&null!=e.getName()){
                    SuccOrderIdValue = e.getText();
                    System.out.println("成功的订单id"+SuccOrderIdValue);
                }
                if(e.getName().equals("FailOrderId")&&null!=e.getName()){
                    FailOrderIdValue=e.getText();
                    System.out.println("失败的订单id"+FailOrderIdValue);
                }
            }
            if(""!=SuccOrderIdValue&&null!=SuccOrderIdValue){
                System.out.println("拿到成功的订单id了,主动获取授权状态和数据");
                WeixinUtils weixinUtils = new WeixinUtils();
                weixinUtils.zdcxstatus(SuccOrderIdValue);
            }
            if(""!=FailOrderIdValue&&null!=FailOrderIdValue){
                System.out.println("拿到失败的订单id了...拒绝开票");
            }
        } catch (Exception e) {
            //处理异常
            logger.error("Get Ali Access_token error", e);
            request.getSession().setAttribute("msg", "获取微信授权出现异常!");
            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
        }
        return null;
    }


    /**
     * 获取微信授权链接
     *
     * @return
     */
    @RequestMapping(value = WeiXinConstants.BEFORE_WEIXIN_REDIRECT_URL)
    @ResponseBody
    public String getTiaozhuanURL(/*String orderid,int money,int timestamp,String mendianId*/) throws Exception {

        WeixinUtils weixinUtils = new WeixinUtils();
        String access_token = (String) weixinUtils.hqtk().get("access_token");//获取token
        String spappid =  weixinUtils.getSpappid();//获取开票平台
        String ticket = weixinUtils.getTicket();
        String orderid="123145322200112234";
        int money = 12;
        int timestamp = 1574875876;
        String  source = "web";
        String redirect_url = "https://baidu.com";
        int type = 1;//填写抬头申请开票类型
        Map nvps = new HashMap();
        nvps.put("s_pappid",spappid);
        nvps.put("order_id",orderid);
        nvps.put("money",money);
        nvps.put("timestamp",timestamp);
        nvps.put("source",source);
        nvps.put("redirect_url",redirect_url);
        nvps.put("ticket",ticket);
        nvps.put("type",type);
        String sj = JSON.toJSONString(nvps);
        System.out.println("封装数据"+sj);
        String urls ="https://api.weixin.qq.com/card/invoice/getauthurl?access_token="+access_token;
        String jsonStr3 = WeixinUtil.httpRequest(urls, "POST", sj);
        System.out.println("返回信息"+jsonStr3.toString());
        if(null!=jsonStr3){
            ObjectMapper jsonparer = new ObjectMapper();// 初始化解析json格式的对象
            try {
                Map map = jsonparer.readValue(jsonStr3, Map.class);
                String auth_url = (String) map.get("auth_url");
                System.out.println("授权链接"+auth_url);
                logger.info("跳转url"+auth_url);
                response.sendRedirect(auth_url);
            } catch (Exception e) {
                //处理异常
                logger.error("Get Ali Access_token error", e);
                request.getSession().setAttribute("msg", "获取微信授权出现异常!");
                response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            }
        }
        return null;
    }


    /**
     * 将发票信息同步到卡包
     *
     * @return
     */

}
