package com.rjxx.taxeasy.controller;

import com.alibaba.fastjson.JSON;
import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.dao.WxTokenJpaDao;
import com.rjxx.taxeasy.dao.WxfpxxJpaDao;
import com.rjxx.taxeasy.domains.*;
import com.rjxx.taxeasy.service.BarcodeService;
import com.rjxx.taxeasy.service.GsxxService;
import com.rjxx.taxeasy.service.JyxxsqService;
import com.rjxx.taxeasy.service.KplsService;
import com.rjxx.utils.weixin.WeixinUtils;
import com.rjxx.utils.weixin.wechatFpxxServiceImpl;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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

    @Autowired
    private JyxxsqService jyxxsqService;

    @Autowired
    private WeixinUtils weixinUtils;

    @Autowired
    private KplsService kplsService;

    @Autowired
    private GsxxService gsxxService;

    @Autowired
    private WxTokenJpaDao wxTokenJpaDao;

    @Autowired
    private wechatFpxxServiceImpl wechatFpxxService;

    //判断是否微信浏览器
    @RequestMapping(value = "/isBrowser")
    @ResponseBody
    public Map isWeiXin(String storeNo, String orderNo, String orderTime, String price){
        String redirectUrl ="";
        Map resultMap = new HashMap();
        if(weixinUtils.isWeiXinBrowser(request)){
            logger.info("微信浏览器--------------");
            logger.info("截取前--"+price);
            try {
                logger.info("------orderNo---------"+orderNo);
                if(null==orderNo || "".equals(orderNo)){
                    request.getSession().setAttribute("msg", "获取微信授权失败!请重试!");
                    response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                    return null;
                }
                if(null == orderTime || "".equals(orderTime)){
                    request.getSession().setAttribute("msg", "获取微信授权失败!请重试!");
                    response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                    return null;
                }
                if(null == price || "".equals(price)){
                    request.getSession().setAttribute("msg", "获取微信授权失败!请重试!");
                    response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                    return null;
                }
                WxFpxx wxFpxx = wxfpxxJpaDao.selsetByOrderNo(orderNo);
                if(null==wxFpxx){
                    request.getSession().setAttribute("msg", "获取微信授权失败!请重试!");
                    response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                    return null;
                }
                List<String> status = barcodeService.checkStatus(wxFpxx.getTqm(),wxFpxx.getGsdm());
                if(status!=null){
                    if(status.contains("开具中")){
                        //开具中对应的url
                        response.sendRedirect(request.getContextPath() + "/QR/zzkj.html?t=" + System.currentTimeMillis());
                        return null;
                    }else if(status.contains("可开具")){
                        String access_token ="";
                        String ticket = "";
                        WxToken wxToken = wxTokenJpaDao.findByFlag("01");
                        if(wxToken==null){
                            access_token= (String) weixinUtils.hqtk().get("access_token");
                            ticket = weixinUtils.getTicket(access_token);
                        }else {
                            access_token = wxToken.getAccessToken();
                            ticket= wxToken.getTicket();
                        }
                        String spappid = weixinUtils.getSpappid(access_token);//获取平台开票信息
                        if(null==spappid ||"".equals(spappid)){
                            //获取授权失败
                            request.getSession().setAttribute("msg", "获取微信授权失败!请重试!");
                            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                            return null;
                        }
                        String weixinOrderNo = wechatFpxxService.getweixinOrderNo(orderNo);
                        logger.info("orderNo---"+orderNo+"传给微信的weixinOrderNo"+weixinOrderNo);
                        //可开具 跳转微信授权链接
                        redirectUrl = weixinUtils.getTiaoURL(weixinOrderNo,price,orderTime, storeNo,"1",access_token,ticket,spappid);
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
                }
//                if(status!=null&& status.equals("开具中")){
//                    //开具中对应的url
//                    response.sendRedirect(request.getContextPath() + "/QR/zzkj.html?t=" + System.currentTimeMillis());
//                    return null;
//                }else if(status!=null && status.equals("可开具")){
//                    String access_token ="";
//                    String ticket = "";
//                    WxToken wxToken = wxTokenJpaDao.findByFlag("01");
//                    if(wxToken==null){
//                        access_token= (String) weixinUtils.hqtk().get("access_token");
//                        ticket = weixinUtils.getTicket(access_token);
//                    }else {
//                        access_token = wxToken.getAccessToken();
//                        ticket= wxToken.getTicket();
//                    }
//                    String spappid = weixinUtils.getSpappid(access_token);//获取平台开票信息
//                    logger.info("----获取的spappid"+spappid);
//                    if(null==spappid ||"".equals(spappid)){
//                        //获取授权失败
//                        request.getSession().setAttribute("msg", "获取微信授权失败!请重试!");
//                        response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
//                        return null;
//                    }
//                    //可开具 跳转微信授权链接
//                    redirectUrl = weixinUtils.getTiaoURL(orderNo,price,orderTime, storeNo,"1",access_token,ticket,spappid);
//                    if(null==redirectUrl||redirectUrl.equals("")){
//                        //获取授权失败
//                        request.getSession().setAttribute("msg", "获取微信授权失败!请重试!");
//                        response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
//                        return null;
//                    }else {
//                        //成功跳转
//                        response.sendRedirect(redirectUrl);
//                        return null;
//                    }
//                }else {
//                    request.getSession().setAttribute("msg", "获取微信授权失败!请重试!");
//                    response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
//                    return null;
//                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            logger.info("不是微信浏览器-------------");
            resultMap.put("num" ,"1");
        }
        return resultMap;
    }


    @RequestMapping(value = "/fpInfo")
    @ResponseBody
    public void fpInfoPageUrl(String encrypt_code ,String card_id) throws IOException {
        if(null == encrypt_code || null == card_id ){
            request.getSession().setAttribute("msg", "发票跳转失败了，请重试!");
            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            return ;
        }
        String access_token ="";
        WxToken wxToken = wxTokenJpaDao.findByFlag("01");
        if(wxToken==null){
            access_token= (String) weixinUtils.hqtk().get("access_token");
        }else {
            access_token = wxToken.getAccessToken();
        }
        String code = weixinUtils.decode(encrypt_code,access_token);
        if(null == code ){
            request.getSession().setAttribute("msg", "获取数据失败了，请重试!");
            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            return ;
        }
        logger.info("拿到解码code----------"+code);
        WxFpxx wxFpxx = wxfpxxJpaDao.selectByCode(code);
        if(null!=wxFpxx){
            Map jyxxsqMap = new HashMap();
            jyxxsqMap.put("gsdm",wxFpxx.getGsdm());
            jyxxsqMap.put("tqm",wxFpxx.getTqm());
            jyxxsqMap.put("openid",wxFpxx.getOpenId());
            Jyxxsq jyxxsq = jyxxsqService.findOneByParams(jyxxsqMap);
            Map kplsMap = new HashMap();
            kplsMap.put("gsdm",wxFpxx.getGsdm());
            kplsMap.put("jylsh",jyxxsq.getJylsh());
            List<Kpls> kpls = kplsService.findAll(kplsMap);
            if(kpls.size() > 0 ){
                Integer kplsh=kpls.get(0).getKplsh();
                response.sendRedirect(request.getContextPath() + "/Family/wxfpxq.html?kbs="+kplsh+"&&_t=" + System.currentTimeMillis());
                return;
            }
            else
            {
                request.getSession().setAttribute("msg", "获取数据失败了，请重试!");
                response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                return;
            }
        }else {
            request.getSession().setAttribute("msg", "获取数据失败了，请重试!");
            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            return;
        }

    }
    @RequestMapping(value = "/wxfpxq")
    @ResponseBody
    public String wxfpxq(String kplsh ) throws IOException {
        logger.info("收到请求-----"+kplsh);
        if(null == kplsh ){
            request.getSession().setAttribute("msg", "发票跳转失败了，请重试!");
            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            return null;
        }
        logger.info("拿到kplsh----------"+kplsh);
        Map kplsMap = new HashMap();
        kplsMap.put("kplsh",kplsh);
        Kpls kpls = kplsService.findOneByParams(kplsMap);
        Map map = new HashMap();
        map.put("gfmc",kpls.getGfmc());
        map.put("xfmc",kpls.getXfmc());
        map.put("jshj",kpls.getJshj());
        map.put("kprq", DateFormatUtils.format(kpls.getKprq(),"yyyy-MM-dd"));
        map.put("xfsh",kpls.getXfsh());
        map.put("fpdm",kpls.getFpdm());
        map.put("fphm",kpls.getFphm());
        map.put("jym", kpls.getJym());
        map.put("pdfurl",kpls.getPdfurl());
        return  JSON.toJSONString(map);
    }

    @RequestMapping(value = "/syncWeiXin")
    @ResponseBody
    private String syncWeiXin(String orderNo, String price, String orderTime){
        String redirectUrl="";
        logger.info("取到的数据orderNo----"+orderNo);
        logger.info("数据金额price----"+price);
        logger.info("取到的数据orderTime----"+orderTime);
        try {
            //判断是否是微信浏览
            if (!weixinUtils.isWeiXinBrowser(request)) {
                request.getSession().setAttribute("msg", "请使用微信进行该操作");
                response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                return null;
            }
            if(null == orderNo || "".equals(orderNo)){
                request.getSession().setAttribute("msg", "获取微信授权失败!请重试!");
                response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                return null;
            }
            if(null == price || "".equals(price)){
                request.getSession().setAttribute("msg", "获取微信授权失败!请重试!");
                response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                return null;
            }
            if(null == orderTime || "".equals(orderTime)){
                request.getSession().setAttribute("msg", "获取微信授权失败!请重试!");
                response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                return null;
            }
            WxFpxx wxFpxx = wxfpxxJpaDao.selsetByOrderNo(orderNo);
            if(null!=wxFpxx.getKplsh()&&!"".equals(wxFpxx.getKplsh())){
                String access_token ="";
                String ticket = "";
                WxToken wxToken = wxTokenJpaDao.findByFlag("01");
                if(wxToken==null){
                    access_token= (String) weixinUtils.hqtk().get("access_token");
                    ticket = weixinUtils.getTicket(access_token);
                }else {
                    access_token = wxToken.getAccessToken();
                    ticket= wxToken.getTicket();
                }
                String spappid = weixinUtils.getSpappid(access_token);//获取平台开票信息
                if(null==spappid || "".equals(spappid)){
                    //获取授权失败
                    request.getSession().setAttribute("msg", "获取微信授权失败!请重试!");
                    response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                    return null;
                }
                String weixinOrderNo = wechatFpxxService.getweixinOrderNo(orderNo);
                logger.info("orderNo---"+orderNo+"传给微信的weixinOrderNo"+weixinOrderNo);
                //可开具 跳转微信授权链接
                redirectUrl = weixinUtils.getTiaoURL(weixinOrderNo,price,orderTime, "","2",access_token,ticket,spappid);
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
                logger.info("获取开票数据失败");
                request.getSession().setAttribute("msg", "获取开票数据失败，请重试!");
                response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return redirectUrl;
    }
    @RequestMapping(value = "/getGsxx")
    @ResponseBody
    private String getGsxx(String gsdm){
        String gsInfo="";
        if(gsdm==null){
            return  null;
        }
        Map map = new HashMap();
        map.put("gsdm",gsdm);
        Gsxx gsxx = gsxxService.findOneByGsdm(map);
        if(gsxx!=null){
            gsInfo=gsxx.getGsmc();
        }
        return gsInfo;
    }
}
