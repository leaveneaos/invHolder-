package com.rjxx.taxeasy.controller;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.domains.Jyls;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.domains.Kpspmx;
import com.rjxx.taxeasy.domains.Pp;
import com.rjxx.taxeasy.service.JylsService;
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
    private JylsService jylsService;


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
            WeixinUtils weixinUtils = new WeixinUtils();
            String access_token = (String) request.getSession().getAttribute("access_token");
            if(null==access_token){
                access_token=(String)weixinUtils.hqtk().get("access_token");
                request.setAttribute("access_token",access_token);
            }
        try {
            System.out.println("返回的数据"+data.toString());
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
                System.out.println("拿到成功的订单id了");
              Map resultMap =  weixinUtils.zdcxstatus(SuccOrderIdValue/*,access_token*/);//主动获取授权状态，成功会返回数据
                if(null!=resultMap.get("msg")){
                    logger.info("订单编号为"+SuccOrderIdValue+"的提取码,主动获取授权失败,订单没有授权"+resultMap.get("msg"));
                    return null;
                }

            }
            if(""!=FailOrderIdValue&&null!=FailOrderIdValue){
                System.out.println("拿到失败的订单id了...拒绝开票");
                String re = "微信授权失败,请重新开票";
               String msg= weixinUtils.jujuekp(FailOrderIdValue,re);

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
    /*@RequestMapping(value = WeiXinConstants.BEFORE_WEIXIN_REDIRECT_URL)
    @ResponseBody
    public String getTiaozhuanURL(*//*String orderid,int money,int timestamp,String mendianId*//*) throws Exception {

        WeixinUtils weixinUtils = new WeixinUtils();
        //判断是否是微信浏览器
       *//* if (!weixinUtils.isWeiXinBrowser(request)) {
            request.getSession().setAttribute("msg", "请使用支付宝进行该操作");
            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            return null;
        }*//*

        String access_token = (String) weixinUtils.hqtk().get("access_token");//获取token
        request.getSession().setAttribute("access_token",access_token);//token放进session里
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
                request.getSession().setAttribute(orderid+"auth_url",auth_url);//跳转url放进session
            } catch (Exception e) {
                //处理异常
                logger.error("Get Ali Access_token error", e);
                request.getSession().setAttribute("msg", "获取微信授权出现异常!");
                response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            }
        }
        return null;
    }*/


    /**
     * 将发票信息同步到微信卡包
     *
     * @return
     */
    @RequestMapping(value = "/syncWeiXin")
    @ResponseBody
    public String syncWeiXin(@RequestParam(required = false) String order_id) throws Exception {
        if(null==order_id){
            return  null;
        }
       String access_token = (String) request.getSession().getAttribute("access_token");

        //判断是否是支付宝内
//        if (!WeixinUtils.isWeiXinBrowser(request)) {
//            request.getSession().setAttribute("msg", "请使用支付宝进行该操作");
//            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
//            return null;
//        }
        //主动查询授权状态
        WeixinUtils weixinUtils = new WeixinUtils();
        Map weiXinDataMap = weixinUtils.zdcxstatus(order_id);
        if(null==weiXinDataMap){
            logger.info("主动查询授权失败++++++++++++");
            return null;
        }
        Map para = new HashMap();
        para.put("tqm",order_id);
        Jyls jyls = jylsService.findByTqm(order_id,"family");
        Map params = new HashMap();
        params.put("djh",jyls.getDjh());
        List<Kpls> kplsList = kplsService.findAll(params);
        for (Kpls kpls : kplsList) {
            int kplsh = kpls.getKplsh();
            Map params2 = new HashMap();
            params2.put("kplsh", kplsh);
            List<Kpspmx> kpspmxList = kpspmxService.findMxNewList(params2);

            String s_media_id =weixinUtils.creatPDF(kpls.getPdfurl());
            if(null==s_media_id&&StringUtils.isBlank(s_media_id)){
                logger.info("上传PDF失败获取s_media_id为null");
                return  null;
            }

            //weixinUtils.dzfpInCard(order_id,WeiXinConstants.FAMILY_CARD_ID,weiXinDataMap,kpspmxList,kpls);
        }
        return  null;
    }
}
