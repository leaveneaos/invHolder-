package com.rjxx.taxeasy.controller;

import com.alibaba.fastjson.JSON;
import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.dao.*;
import com.rjxx.taxeasy.domains.*;
import com.rjxx.taxeasy.service.*;
import com.rjxx.taxeasy.service.adapter.AdapterService;
import com.rjxx.taxeasy.utils.alipay.AlipayConstants;
import com.rjxx.utils.weixin.WechatBatchCard;
import com.rjxx.utils.weixin.WeixinUtils;
import com.rjxx.utils.weixin.wechatFpxxServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2017-08-16.
 */
@CrossOrigin
@RestController
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

    @Autowired
    private SkpService skpService;

    @Autowired
    private PpJpaDao ppJpaDao;

    @Autowired
    private JylsService jylsService;

    @Autowired
    private WechatBatchCard wechatBatchCard;
    @Autowired
    private AdapterService adapterService;

    @Value("${web.url.error}")
    private String errorUrl;

    @Value("${web.url.success}")
    private String succesUrl;

    @Value("${web.url.ticketing}")
    private String ticketingUrl;

    @Value("${web.url.luru}")
    private String luruUrl;

    @Value("${web.url.maked}")
    private String makedUrl;



    //判断是否微信浏览器
    @RequestMapping(value = "/isBrowser")
    public Map isWeiXin(String storeNo, String orderNo, String orderTime, String price,String gsdm){
        String redirectUrl ="";
        Map resultMap = new HashMap();
        if(weixinUtils.isWeiXinBrowser(request)){
            try {
                logger.info("------orderNo---------"+orderNo);
                if(null==orderNo || "".equals(orderNo)){
                    errorRedirect("订单号为空,获取微信授权失败!请重试!");
                    return null;
                }
                if(null == orderTime || "".equals(orderTime)){
                    errorRedirect("订单时间为空,获取微信授权失败!请重试!");
                    return null;
                }
                if(null == price || "".equals(price)){
                    errorRedirect("金额为空,获取微信授权失败!请重试!");
                    return null;
                }
                if("0.0".equals(price)){
                    errorRedirect("该订单可开票金额为0");
                    return null;
                }
                WxFpxx wxFpxx = wxfpxxJpaDao.selsetByOrderNo(orderNo,gsdm);
                if(null==wxFpxx){
                    errorRedirect("根据微信回传订单号未找到该笔订单");
                    return null;
                }
                List<String> status = barcodeService.checkStatus(wxFpxx.getTqm(),wxFpxx.getGsdm());
                if(status!=null){
                    if(status.contains("开具中")){
                        //开具中对应的url
//                        response.sendRedirect(request.getContextPath() + "/QR/zzkj.html?t=" + System.currentTimeMillis());
                        response.sendRedirect(ticketingUrl);
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
                        if(ticket==null){
                            //获取授权失败
                            errorRedirect("获取ticket失败!请重试");
                            return null;
                        }
                        String spappid = weixinUtils.getSpappid(access_token);//获取平台开票信息
                        if(null==spappid ||"".equals(spappid)){
                            //获取授权失败
                            errorRedirect("获取spappid失败!请重试");
                            return null;
                        }
                        Integer xfid = wechatFpxxService.getxfid(storeNo, gsdm);
                        String weixinOrderNo = wechatFpxxService.getweixinOrderNo(orderNo,gsdm,xfid);
                        logger.info("orderNo---"+orderNo+"传给微信的weixinOrderNo"+weixinOrderNo+"--销方id:"+xfid);
//                        String gsdm = wxFpxx.getGsdm();
                        //可开具 跳转微信授权链接
                        redirectUrl = weixinUtils.getTiaoURL(gsdm,weixinOrderNo,price,orderTime, storeNo,"1",access_token,ticket,spappid);
                        if(null==redirectUrl||redirectUrl.equals("")){
                            //获取授权失败
                            errorRedirect("获取微信授权页失败!请重试");
                            return null;
                        }else {
                            //成功跳转
                            response.sendRedirect(redirectUrl);
                            return null;
                        }
                    }else {
                        errorRedirect("获取微信授权失败!请重试!");
                        return null;
                    }
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

    //拉取微信授权type 0、没有抬头授权 1、微信抬头授权 2、领取发票
    @RequestMapping(value = "/weiXin")
    public Map weiXin(String storeNo, String orderNo, String orderTime, String price,String gsdm,String type){
        String redirectUrl ="";
        Map resultMap = new HashMap();
        if(weixinUtils.isWeiXinBrowser(request)){
            try {
                logger.info("------orderNo---------"+orderNo+"=-----------type"+type);
                if(null==orderNo || "".equals(orderNo)){
                    errorRedirect("订单号为空,获取微信授权失败!请重试!");
                    return null;
                }
                if(null == orderTime || "".equals(orderTime)){
                    errorRedirect("订单时间为空,获取微信授权失败!请重试!");
                    return null;
                }
                if(null == price || "".equals(price)){
                    errorRedirect("金额为空,获取微信授权失败!请重试!");
                    return null;
                }
                if("0.0".equals(price)){
                    errorRedirect("该订单可开票金额为0");
                    return null;
                }
                WxFpxx wxFpxx = wxfpxxJpaDao.selsetByOrderNo(orderNo,gsdm);
                if(null==wxFpxx){
                    errorRedirect("根据微信回传订单号未找到该笔订单");
                    return null;
                }
                List<String> status = adapterService.checkStatus(wxFpxx.getTqm(),wxFpxx.getGsdm(),null);
                if(status!=null){
                    if(status.contains("开具中")){
                        response.sendRedirect(ticketingUrl);
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
                        if(ticket==null){
                            //获取授权失败
                            errorRedirect("获取ticket失败!请重试");
                            return null;
                        }
                        String spappid = weixinUtils.getSpappid(access_token);//获取平台开票信息
                        if(null==spappid ||"".equals(spappid)){
                            //获取授权失败
                            errorRedirect("获取spappid失败!请重试");
                            return null;
                        }
                        Integer xfid = wechatFpxxService.getxfid(storeNo, gsdm);
                        String weixinOrderNo = wechatFpxxService.getweixinOrderNo(orderNo,gsdm,xfid);
                        logger.info("orderNo---"+orderNo+"传给微信的weixinOrderNo"+weixinOrderNo);
                        //可开具 跳转微信授权链接
                        redirectUrl = weixinUtils.getTiaoURL(gsdm,weixinOrderNo,price,orderTime, storeNo,type,access_token,ticket,spappid);
                        if(null==redirectUrl||redirectUrl.equals("")){
                            //获取授权失败
                            errorRedirect("获取微信授权页失败!请重试");
                            return null;
                        }else {
                            //成功跳转
                            response.sendRedirect(redirectUrl);
                            return null;
                        }
                    }else if(status.contains("纸票")){
                        errorRedirect("该订单已开具纸质发票，不能重复开具");
                        return null;
                    }else if(status.contains("红冲")){
                        errorRedirect("该订单已红冲");
                        return null;
                    }else {
                        errorRedirect("获取微信授权失败!请重试!");
                        return null;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            resultMap.put("num" ,"1");
        }
        return resultMap;
    }


    public Map isWeiXin(String storeNo, String orderNo, String orderTime, String price,String gsdm,String type){
        String redirectUrl ="";
        Map resultMap = new HashMap();
        if(weixinUtils.isWeiXinBrowser(request)){
            try {
                logger.info("------orderNo---------"+orderNo);
                if(null==orderNo || "".equals(orderNo)){
                    errorRedirect("订单号为空,获取微信授权失败!请重试!");
                    return null;
                }
                if(null == orderTime || "".equals(orderTime)){
                    errorRedirect("订单时间为空,获取微信授权失败!请重试!");
                    return null;
                }
                if(null == price || "".equals(price)){
                    errorRedirect("金额为空,获取微信授权失败!请重试!");
                    return null;
                }
                if("0.0".equals(price)){
                    errorRedirect("该订单可开票金额为0");
                    return null;
                }
                WxFpxx wxFpxx = wxfpxxJpaDao.selsetByOrderNo(orderNo,gsdm);
                if(null==wxFpxx){
                    errorRedirect("根据微信回传订单号未找到该笔订单");
                    return null;
                }
                List<String> status = adapterService.checkStatus(wxFpxx.getTqm(),wxFpxx.getGsdm(),null);
                if(status!=null){
                    if(status.contains("开具中")){
                        //开具中对应的url
//                        response.sendRedirect(request.getContextPath() + "/QR/zzkj.html?t=" + System.currentTimeMillis());
                        response.sendRedirect(ticketingUrl);
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
                        if(ticket==null){
                            //获取授权失败
                            errorRedirect("获取ticket失败!请重试");
                            return null;
                        }
                        String spappid = weixinUtils.getSpappid(access_token);//获取平台开票信息
                        if(null==spappid ||"".equals(spappid)){
                            //获取授权失败
                            errorRedirect("获取spappid失败!请重试");
                            return null;
                        }
                        Integer xfid = wechatFpxxService.getxfid(storeNo, gsdm);
                        String weixinOrderNo = wechatFpxxService.getweixinOrderNo(orderNo,gsdm,xfid);
                        logger.info("orderNo---"+orderNo+"传给微信的weixinOrderNo"+weixinOrderNo);
//                        String gsdm = wxFpxx.getGsdm();
                        //可开具 跳转微信授权链接
                        redirectUrl = weixinUtils.getTiaoURL(gsdm,weixinOrderNo,price,orderTime, storeNo,type,access_token,ticket,spappid);
                        if(null==redirectUrl||redirectUrl.equals("")){
                            //获取授权失败
                            errorRedirect("获取微信授权页失败!请重试");
                            return null;
                        }else {
                            //成功跳转
                            response.sendRedirect(redirectUrl);
                            return null;
                        }
                    }else if(status.contains("纸票")){
                        errorRedirect("该订单已开具纸质发票，不能重复开具");
                        return null;
                    }else if(status.contains("红冲")){
                        errorRedirect("该订单已红冲");
                        return null;
                    }else {
                        errorRedirect("获取微信授权失败!请重试!");
                        return null;
                    }
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
                response.sendRedirect(request.getContextPath() + "/CO/smfpxq.html?kplsh="+kplsh+"&&_t=" + System.currentTimeMillis());
                return;
            } else {
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
    @RequestMapping(value = "/smfpxq",method = RequestMethod.POST)
    public Map smfpxq(@RequestParam String serialOrder ) throws IOException {
        Map<String, Object> result = new HashMap<String, Object>();
        try {
//            if(null == serialOrder ){
//                request.getSession().setAttribute("msg", "出现未知错误，请重试!");
//                response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
//                return null;
//            }
            Map map2 = new HashMap();
            map2.put("serialorder",serialOrder);
            List<Kpls> kplsList = kplsService.findAll(map2);
            if(kplsList.size()==0){
//                request.getSession().setAttribute("msg", "出现未知错误，请重试!");
                result.put("msg","未查询到发票详情，请重试！");
                return result;
            }

            String gsdm = kplsList.get(0).getGsdm();

            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            result.put("orderTime",sdf.format(kplsList.get(0).getLrsj()));
            Jyls jyls = new Jyls();
            jyls.setGsdm(gsdm);
            jyls.setDjh(kplsList.get(0).getDjh());
            jyls.setJylsh(kplsList.get(0).getJylsh());
            Jyls jylsxx = jylsService.findOneByParams(jyls);
            String orderNo= "";
            String price ="";
            String kplsh="";
            if(kplsList.size()>1){
                for (Kpls kpls : kplsList) {
                    orderNo +=kpls.getKplsh()+",";
                    price  +=kpls.getJshj()+",";
                    kplsh +=kpls.getKplsh()+",";
                }
                result.put("price",price);
                result.put("orderNo",orderNo);
            }else {
                price = kplsList.get(0).getJshj().toString();
                result.put("price",price);
                if(jylsxx.getGsdm().equals("hdsc")||jylsxx.getGsdm().equals("cmsc")){
                    orderNo = jylsxx.getKhh();
                    result.put("orderNo",orderNo);
                }else {
                    orderNo = jylsxx.getDdh();
                    result.put("orderNo",orderNo);
                }
                kplsh= kplsList.get(0).getKplsh().toString();
            }
            result.put("kplsList", kplsList);
            String tqm="";
            if(jylsxx.getGsdm().equals("ssd")||jylsxx.equals("cmsc")||jylsxx.equals("hdsc")){
                tqm=jylsxx.getKhh();
            }else {
                tqm=jylsxx.getTqm();
            }
            //保存微信发票信息
            boolean b = wechatFpxxService.InFapxx(tqm, gsdm, orderNo, "", "2", (String) session.getAttribute("openid"),
                    (String) request.getSession().getAttribute(AlipayConstants.ALIPAY_USER_ID),kplsh, request);
            if(!b){
                result.put("msg","保存发票信息失败，请重试！");
                return result;
            }
            request.getSession().setAttribute("gsdm",kplsList.get(0).getGsdm());
            result.put("gsdm",kplsList.get(0).getGsdm());
            result.put("storeNo",kplsList.get(0).getKpddm());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return  result;
    }

    @RequestMapping(value = "/syncWeiXin")
    private String syncWeiXin(String orderNo, String price, String orderTime,String gsdm,String storeNo){
        String redirectUrl="";
        try {
            //判断是否是微信浏览
           /* if (!weixinUtils.isWeiXinBrowser(request)) {
                request.getSession().setAttribute("msg", "请使用微信进行该操作");
                response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                return null;
            }*/
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
            if(orderNo.indexOf(",")<0){
                logger.info("单个订单");
                WxFpxx wxFpxx = wxfpxxJpaDao.selsetByOrderNo(orderNo,gsdm);
                if(wxFpxx==null){
                    request.getSession().setAttribute("msg", "获取开票数据失败，请重试!");
                    response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                    return null;
                }
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
                    Integer xfid = wechatFpxxService.getxfid(storeNo, gsdm);
                    String weixinOrderNo = wechatFpxxService.getweixinOrderNo(orderNo,gsdm,xfid);
                    logger.info("orderNo---"+orderNo+"传给微信的weixinOrderNo"+weixinOrderNo);
                    //可开具 跳转微信授权链接
                    redirectUrl = weixinUtils.getTiaoURL(wxFpxx.getGsdm(),weixinOrderNo,price,orderTime, "","2",access_token,ticket,spappid);
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
                    request.getSession().setAttribute("msg", "获取开票数据失败，请重试!");
                    response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                    return null;
                }
            }else {
                logger.info("多个订单，一次授权插多张卡----------");
                List list = new ArrayList();
                String[] orderNos = orderNo.split(",");
                String[] prices = price.split(",");
                int length1 = orderNos.length;
                int length2 = prices.length;
                if(length1 != length2){
                    //获取授权失败
                    request.getSession().setAttribute("msg", "获取微信授权失败!请重试!");
                    response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                    return null;
                }
                for(int i=0;i<length1;i++){
                    String orderNo1 = orderNos[i];
                    String price1 = prices[i];
                    BigDecimal big = new BigDecimal(price1);
                    BigDecimal newbig = big.multiply(new BigDecimal(100));
                    Double doumoney = new Double(newbig.toString());
                    Map map = new HashMap();
                    map.put("order_id",orderNo1);
                    map.put("money",doumoney);
                    logger.info("订单号"+orderNo1);
                    logger.info("金额"+doumoney);
                    list.add(map);
                }
               logger.info(JSON.toJSONString(list));
                WxFpxx wxFpxx = wxfpxxJpaDao.selsetByOrderNo(orderNo,gsdm);
                if(wxFpxx==null || (wxFpxx.getKplsh() == null || wxFpxx.getKplsh().equals(""))){
                    request.getSession().setAttribute("msg", "获取开票数据失败，请重试!");
                    response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                    return null;
                }
                WxToken wxToken = wxTokenJpaDao.findByFlag("01");
                String spappid = weixinUtils.getSpappid(wxToken.getAccessToken());//获取平台开票信息
                Map wxurl = wechatBatchCard.getWXURL(wxFpxx.getGsdm(), list, orderTime,
                        "", "2", wxToken.getAccessToken(), wxToken.getTicket(), spappid);
                if(wxurl==null){
                    request.getSession().setAttribute("msg", "获取开票数据失败，请重试!");
                    response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                    return null;
                }
                String redicetURL=wxurl.get("auth_url").toString();
                String auth_id = wxurl.get("auth_id").toString();
                wxFpxx.setAuthid(auth_id);
                wxFpxx.setXgsj(new Date());
                wxFpxx.setWeixinOderno(orderNo);
                wxfpxxJpaDao.save(wxFpxx);
                //成功跳转
                response.sendRedirect(redicetURL);
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return redirectUrl;
    }
    @RequestMapping(value = "/getGsxx")
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

    /**
     * 扫码发票归入支付宝、微信
     * @param serialOrder
     * @return
     */
    @RequestMapping(value = "/smInOut")
    private String smInOut(String serialOrder){
        try {
            if(serialOrder==null){
                //获取授权失败
                request.getSession().setAttribute("msg", "扫码信息有误!请重试!");
                response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                return null;
            }else {
                request.getSession().setAttribute("serialorder",serialOrder);
                response.sendRedirect(request.getContextPath() + "/CO/smfpxq.html?serialOrder="+serialOrder+"&&_t=" + System.currentTimeMillis());
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void errorRedirect(String errorName) {
        try {
//            response.sendRedirect(request.getContextPath() + "/QR/error.html?t=" + System.currentTimeMillis() + "=" + errorName);
            response.sendRedirect(errorUrl + "/" + URLEncoder.encode(errorName)+"?t="+System.currentTimeMillis());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
