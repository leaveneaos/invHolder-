package com.rjxx.taxeasy.controller;

import com.alibaba.fastjson.JSON;
import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.dao.WxfpxxJpaDao;
import com.rjxx.taxeasy.domains.Jyxxsq;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.domains.WxFpxx;
import com.rjxx.taxeasy.service.BarcodeService;
import com.rjxx.taxeasy.service.JyxxsqService;
import com.rjxx.taxeasy.service.KplsService;
import com.rjxx.utils.weixin.WeixinUtils;
import com.sun.tools.internal.jxc.api.JXC;
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
    //判断是否微信浏览器
    @RequestMapping(value = "/isBrowser")
    @ResponseBody
    public Map isWeiXin(String storeNo, String orderNo, String orderTime, String price){
        String redirectUrl ="";
        Map resultMap = new HashMap();
        if(weixinUtils.isWeiXinBrowser(request)){
            logger.info("微信浏览器--------------");
            //WeixinUtils weixinUtils = new WeixinUtils();
            try {
                //查询是否开具
                logger.info("------orderNo---------"+orderNo);
                WxFpxx wxFpxx = wxfpxxJpaDao.selsetByOrderNo(orderNo);
                logger.info("--------数据---------"+ JSON.toJSONString(wxFpxx));
                String status = barcodeService.checkStatus(wxFpxx.getTqm(),wxFpxx.getGsdm());
                if(status!=null&& status.equals("开具中")){
                    //开具中对应的url
                    response.sendRedirect(request.getContextPath() + "/QR/zzkj.html?t=" + System.currentTimeMillis());
                    return null;
                }else if(status!=null && status.equals("可开具")){
                    //可开具 跳转微信授权链接
                    redirectUrl = weixinUtils.getTiaoURL(orderNo,price,orderTime, storeNo,"1");
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


    @RequestMapping(value = "/fpInfo")
    @ResponseBody
    public void fpInfoPageUrl(String encrypt_code ,String card_id) throws IOException {
        if(null == encrypt_code || null == card_id ){
            request.getSession().setAttribute("msg", "发票跳转失败了，请重试!");
            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            return ;
        }
        logger.info("拿到加密code----------"+encrypt_code);
        logger.info("拿到卡券模板id----------"+card_id);
        //WeixinUtils weixinUtils = new WeixinUtils();
        String code = weixinUtils.decode(encrypt_code);
        if(null == code ){
            request.getSession().setAttribute("msg", "获取数据失败了，请重试!");
            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            return ;
        }
        logger.info("拿到解码code----------"+code);
        WxFpxx wxFpxx = wxfpxxJpaDao.selectByCode(code);
        logger.info("查询到的微信交易信息--------"+JSON.toJSONString(wxFpxx));
        Map jyxxsqMap = new HashMap();
        jyxxsqMap.put("gsdm",wxFpxx.getGsdm());
        jyxxsqMap.put("tqm",wxFpxx.getTqm());
        jyxxsqMap.put("openid",wxFpxx.getOpenId());
        Jyxxsq jyxxsq = jyxxsqService.findOneByParams(jyxxsqMap);
        logger.info("查询到的交易信息申请-----"+JSON.toJSONString(jyxxsq));
        Map kplsMap = new HashMap();
        kplsMap.put("gsdm",wxFpxx.getGsdm());
        kplsMap.put("jylsh",jyxxsq.getJylsh());
        List<Kpls> kpls = kplsService.findAll(kplsMap);
        //logger.info("开票流水"+JSON.toJSONString(kpls));
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

    }
    @RequestMapping(value = "/wxfpxq")
    @ResponseBody
    public String wxfpxq(String kplsh ) throws IOException {
        logger.info("收到请求-----"+kplsh);
        Map resultMap = new HashMap();
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
        //resultMap.put("kpls",kpls);
        logger.info("取到的数据——————"+JSON.toJSONString(map));
        return  JSON.toJSONString(map);
    }

    @RequestMapping(value = "/syncWeiXin")
    @ResponseBody
    private String syncWeiXin(String kplsh){
        String redirectUrl="";
        logger.info("开票流水号----"+kplsh);
        return redirectUrl;
    }
}
