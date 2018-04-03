package com.rjxx.taxeasy.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rjxx.taxeasy.dao.*;
import com.rjxx.taxeasy.domains.*;
import com.rjxx.taxeasy.dto.*;
import com.rjxx.taxeasy.invoice.DefaultResult;
import com.rjxx.taxeasy.invoice.KpService;
import com.rjxx.taxeasy.service.AdapterService;
import com.rjxx.taxeasy.service.CszbService;
import com.rjxx.taxeasy.service.SpvoService;
import com.rjxx.taxeasy.service.TransferExtractDataService;
import com.rjxx.taxeasy.service.jkpz.JkpzService;
import com.rjxx.taxeasy.utils.NumberUtil;
import com.rjxx.taxeasy.vo.Spvo;
import com.rjxx.utils.RJCheckUtil;
import com.rjxx.utils.StringUtil;
import com.rjxx.utils.XmlJaxbUtils;
import com.rjxx.utils.weixin.HttpClientUtil;
import com.rjxx.utils.weixin.WeixinUtils;
import com.rjxx.utils.yjapi.Result;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by wangyahui on 2018/3/13 0013
 */
@Service
public class AdapterServiceImpl implements AdapterService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private SkpJpaDao skpJpaDao;
    @Autowired
    private PpJpaDao ppJpaDao;
    @Autowired
    private CszbService cszbService;
    @Autowired
    private KplsJpaDao kplsJpaDao;
    @Autowired
    private JylsJpaDao jylsJpaDao;
    @Autowired
    private SpvoService spvoService;
    @Autowired
    private XfJpaDao xfJpaDao;
    @Autowired
    private GsxxJpaDao gsxxJpaDao;
    @Autowired
    private WeixinUtils weixinUtils;
    @Autowired
    private JkpzService jkpzService;
    @Autowired
    private TransferExtractDataService transferExtractDataService;
    @Autowired
    private KpService kpService;

    /**
     * GET_TYPE_2获取品牌信息
     * @param gsdm
     * @param q
     * @return
     */
    @Override
    public Map getGrandMsg(String gsdm, String q) {
        Map map = RJCheckUtil.decodeForAll(q);
        String data = (String) map.get("A0");
        JSONObject jsonData = JSON.parseObject(data);
        String on = jsonData.getString("on");
        String sn = jsonData.getString("sn");
        //如果门店号为空则认为是该公司下只有一个税号一个门店号
        if (StringUtil.isBlankList(sn)) {
            try {
                Xf xf = xfJpaDao.findOneByGsdm(gsdm);
                Skp skp = skpJpaDao.findOneByGsdmAndXfsh(gsdm, xf.getId());
                sn = skp.getKpddm();
            } catch (Exception e) {
                return null;
            }
        }
        try {
            Skp skp = skpJpaDao.findOneByKpddmAndGsdm(sn, gsdm);
            Integer pid = skp.getPid();
            String ppdm = "";
            String ppurl = "";
            String ppheadcolor = "no";
            String ppbodycolor = "no";
            if (pid != null) {
                Pp pp = ppJpaDao.findOneById(pid);
                ppdm = pp.getPpdm();
                ppurl = pp.getPpurl();
                if (StringUtil.isNotBlankList(pp.getPpheadcolor(), pp.getPpbodycolor())) {
                    ppheadcolor = pp.getPpheadcolor();
                    ppbodycolor = pp.getPpbodycolor();
                }
            }
            Map result = new HashMap();
            result.put("ppdm", ppdm);
            result.put("ppurl", ppurl);
            result.put("orderNo", on);
            result.put("headcolor", ppheadcolor);
            result.put("bodycolor", ppbodycolor);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * GET_TYPE_3获取品牌信息
     * @param gsdm
     * @param on
     * @param sn
     * @return
     */
    @Override
    public Map getGrandMsg(String gsdm, String on, String sn) {
        //如果门店号为空则认为是该公司下只有一个税号一个门店号
        if (StringUtil.isBlankList(sn)) {
            try {
                Xf xf = xfJpaDao.findOneByGsdm(gsdm);
                Skp skp = skpJpaDao.findOneByGsdmAndXfsh(gsdm, xf.getId());
                sn = skp.getKpddm();
            } catch (Exception e) {
                return null;
            }
        }
        try {
            Skp skp = skpJpaDao.findOneByKpddmAndGsdm(sn, gsdm);
            Integer pid = skp.getPid();
            String ppdm = "";
            String ppurl = "";
            String ppheadcolor = "no";
            String ppbodycolor = "no";
            if (pid != null) {
                Pp pp = ppJpaDao.findOneById(pid);
                ppdm = pp.getPpdm();
                ppurl = pp.getPpurl();
                if (StringUtil.isNotBlankList(pp.getPpheadcolor(), pp.getPpbodycolor())) {
                    ppheadcolor = pp.getPpheadcolor();
                    ppbodycolor = pp.getPpbodycolor();
                }
            }
            Map result = new HashMap();
            result.put("ppdm", ppdm);
            result.put("ppurl", ppurl);
            result.put("orderNo", on);
            result.put("headcolor", ppheadcolor);
            result.put("bodycolor", ppbodycolor);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 验证这笔订单是否已开具过
     * @param tqm
     * @param gsdm
     * @return
     */
    @Override
    public List<String> checkStatus(String tqm, String gsdm) {
        try {
            List<String> result = new ArrayList();
            List<Integer> djhs = jylsJpaDao.findDjhByTqmAndGsdm(tqm, gsdm);
            if (djhs != null && djhs.size() > 0) {
                logger.info("list不等于空");
                for (Integer djh : djhs) {
                    if (djh != null) {
                        logger.info("djh不等于空");
                        Kpls kpls = kplsJpaDao.findOneByDjh(djh);
                        logger.info("kpls=", kpls);
                        String fpztdm = kpls.getFpztdm();
                        String pdfurl = kpls.getPdfurl();
                        String fphm = kpls.getFphm();
                        String je = kpls.getJshj() + "";
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                        String orderTime = sdf.format(kpls.getLrsj());
                        String kplsh = kpls.getKplsh() + "";
                        String serialorder = kpls.getSerialorder();
                        if ("00".equals(fpztdm) && StringUtils.isNotBlank(pdfurl) && StringUtils.isNotBlank(fphm)) {
                            result.add(pdfurl + "+" + je + "+" + orderTime + "+" + kplsh + "+" + serialorder);
                        } else {
                            result.add("开具中");
                        }
                    } else {
                        logger.info("djh等于空");
                        result.add("可开具");
                    }
                }
            } else {
                logger.info("list等于空");
                result.add("可开具");
            }
            logger.info("result=", result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * GET_TYPE_2获取展示在确认页面的信息
     * @param gsdm
     * @param q
     * @return
     */
    @Override
    public String getSpxx(String gsdm, String q) {
        Map decode = RJCheckUtil.decodeForAll(q);
        String data = (String) decode.get("A0");
        JSONObject jsonData = JSON.parseObject(data);
        String orderNo = jsonData.getString("on");
        String orderTime = jsonData.getString("ot");
        String price = jsonData.getString("pr");
        String storeNo = jsonData.getString("sn");
        String spdm = jsonData.getString("sp");
        if(spdm==null){
            spdm = "";
        }
        //如果门店号为空则认为是该公司下只有一个税号一个门店号
        if (StringUtil.isBlankList(storeNo)) {
            try {
                Xf xf = xfJpaDao.findOneByGsdm(gsdm);
                Skp skp = skpJpaDao.findOneByGsdmAndXfsh(gsdm, xf.getId());
                storeNo = skp.getKpddm();
            } catch (Exception e) {
                return null;
            }
        }
        StringBuilder spsl = new StringBuilder();
        StringBuilder spmc = new StringBuilder();
        if (StringUtil.isNotBlankList(orderNo, orderTime, price, storeNo)) {
            try {
                Skp skp = skpJpaDao.findOneByKpddmAndGsdm(storeNo, gsdm);
                Integer xfid = skp.getXfid(); //销方id
                Integer kpdid = skp.getId();//税控盘id(开票点id)
                Cszb cszb = cszbService.getSpbmbbh(gsdm, xfid, kpdid, "dyspbmb");

                List<String> spdmList = new ArrayList();
                //如果价格和商品代码有多个
                if (price.indexOf(",") != -1 && spdm.indexOf(",") != -1) {
                    Integer priceSize = RJCheckUtil.getSize(price, ",");
                    Integer spdmSize = RJCheckUtil.getSize(spdm, ",");
                    if (priceSize != spdmSize) {
                        return null;
                    }
                    String[] spdmArray = spdm.split(",");
                    for (String str : spdmArray) {
                        spdmList.add(str);
                    }
                    //如果只有一个或没有传商品代码
                } else {
                    if (StringUtils.isNotBlank(spdm)) {
                        spdmList.add(spdm);
                    }
                }
                if (spdmList != null && spdmList.size() > 0) {
                    Map map = new HashMap();
                    map.put("gsdm", gsdm);
                    for (int i = 0; i < spdmList.size(); i++) {
                        map.put("spdm", spdmList.get(i));
                        Spvo oneSpvo = spvoService.findOneSpvo(map);
                        if (i == 0) {
                            spsl.append(oneSpvo.getSl());
                            spmc.append(oneSpvo.getSpmc());
                        } else {
                            spsl.append("," + oneSpvo.getSl());
                            spmc.append("," + oneSpvo.getSpmc());
                        }
                    }
                    //如果没有传商品代码
                } else {
                    if (cszb.getCsz() != null) {
                        Map map = new HashMap();
                        map.put("gsdm", gsdm);
                        map.put("spdm", cszb.getCsz());
                        Spvo oneSpvo = spvoService.findOneSpvo(map);
                        spsl.append(oneSpvo.getSl());
                        spmc.append(oneSpvo.getSpmc());
                    } else {
                        return null;
                    }
                }
                Map result = new HashMap();
                Integer pid = skp.getPid();
                if (pid == null) {
                    logger.info("pid is null");
                    return null;
                } else {
                    Pp pp = ppJpaDao.findOneById(pid);
                    result.put("tqm", pp.getPpdm() + orderNo);
                }
                result.put("orderNo", orderNo);
                result.put("orderTime", orderTime);
                result.put("storeNo", storeNo);
                result.put("price", price);
                result.put("spsl", spsl.toString());
                result.put("spmc", spmc.toString());
                result.put("kpdmc", skp.getKpdmc());
                result.put("gsmc", gsdm);
                logger.info("getSpxx结果===" + JSONObject.toJSONString(result));
                return JSONObject.toJSONString(result);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            logger.info("required msg is null");
            return null;
        }
    }

    /**
     * GET_TYPE_3获取确认页面的展示信息
     * @param gsdm
     * @param on
     * @param sn
     * @param tq
     * @return
     */
    @Override
    public String getSpxx(String gsdm, String on, String sn, String tq) {
        try {
            Skp skp = skpJpaDao.findOneByKpddmAndGsdm(sn, gsdm);
            Xf xf = xfJpaDao.findOneById(skp.getXfid());
            AdapterPost post = getApiMsg(gsdm, xf.getId(), skp.getId(), tq);
            if (post == null) {
                return "1";
            }
            String orderNo = on;
            String orderTime = new SimpleDateFormat("yyyyMMddHHmmss").format(post.getData().getOrder().getOrderDate());
            StringBuilder price = new StringBuilder();
            StringBuilder spsl = new StringBuilder();
            StringBuilder spmc = new StringBuilder();

            List<AdapterDataOrderDetails> orderDetails = post.getData().getOrder().getOrderDetails();
            for (int i = 0; i < orderDetails.size(); i++) {
                String amount = orderDetails.get(i).getAmount().toString();
                String taxRate = orderDetails.get(i).getTaxRate().toString();
                String productName = orderDetails.get(i).getProductName();
                if (!StringUtil.isNotBlankList(amount, taxRate, productName)) {
                    return null;
                }
                if (i == 0) {
                    price.append(amount);
                    spsl.append(taxRate);
                    spmc.append(productName);
                } else {
                    price.append("," + amount);
                    spsl.append("," + taxRate);
                    spmc.append("," + productName);
                }
            }
            Map result = new HashMap();
            Integer pid = skp.getPid();
            if (pid == null) {
                logger.info("pid is null");
                return null;
            } else {
                Pp pp = ppJpaDao.findOneById(pid);
                result.put("tqm", pp.getPpdm() + orderNo);
            }
            result.put("orderNo", on);
            result.put("orderTime", orderTime);
            result.put("storeNo", sn);
            result.put("price", price);
            result.put("spsl", spsl);
            result.put("spmc", spmc);
            result.put("kpdmc", skp.getKpdmc());
            result.put("gsmc", gsdm);
            logger.info("getSpxx结果===" + JSONObject.toJSONString(result));
            return JSONObject.toJSONString(result);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * GET_TYPE_2开票方法
     * @param gsdm
     * @param q
     * @param gfmc
     * @param gfsh
     * @param email
     * @param gfyh
     * @param gfyhzh
     * @param gfdz
     * @param gfdh
     * @param tqm
     * @param openid
     * @param sjly
     * @param access_token
     * @param weixinOrderNo
     * @return
     */
    @Override
    public String makeInvoice(String gsdm, String q, String gfmc, String gfsh, String email,
                              String gfyh, String gfyhzh, String gfdz, String gfdh, String tqm, String openid, String sjly, String access_token, String weixinOrderNo) {
        Map decode = RJCheckUtil.decodeForAll(q);
        String data = (String) decode.get("A0");
        JSONObject jsonData = JSON.parseObject(data);
        String orderNo = jsonData.getString("on");
        String orderTime = jsonData.getString("ot");
        String price = jsonData.getString("pr");
        String storeNo = jsonData.getString("sn");
        String spdm = jsonData.getString("sp");
        if(spdm==null){
            spdm = "";
        }
        //如果门店号为空则认为是该公司下只有一个税号一个门店号
        if (StringUtil.isBlankList(storeNo)) {
            try {
                Xf xf = xfJpaDao.findOneByGsdm(gsdm);
                Skp skp = skpJpaDao.findOneByGsdmAndXfsh(gsdm, xf.getId());
                storeNo = skp.getKpddm();
            } catch (Exception e) {
                return "0";
            }
        }
        if (StringUtil.isNotBlankList(orderNo, orderTime, price, storeNo, gfmc)) {
            try {
                Gsxx gsxx = gsxxJpaDao.findOneByGsdm(gsdm);
                Skp skp = skpJpaDao.findOneByKpddmAndGsdm(storeNo, gsdm);
                Xf xf = xfJpaDao.findOneById(skp.getXfid());

                AdapterPost adapterPost = new AdapterPost();
                AdapterData adapterData = new AdapterData();
                AdapterDataOrder order = new AdapterDataOrder();
                AdapterDataSeller seller = new AdapterDataSeller();
                AdapterDataOrderBuyer buyer = new AdapterDataOrderBuyer();
                List<AdapterDataOrderDetails> details = new ArrayList<>();
                List<AdapterDataOrderPayments> payments = new ArrayList<>();

                adapterPost.setReqType("01");
                adapterPost.setClientNo(storeNo);
                adapterPost.setAppId(gsxx.getAppKey());
                adapterPost.setTaxNo(xf.getXfsh());
                adapterPost.setData(adapterData);

                adapterData.setDatasource(sjly);
                adapterData.setOpenid(openid);
                adapterData.setSeller(seller);
                adapterData.setOrder(order);
                adapterData.setSerialNumber("JY" + System.currentTimeMillis() + NumberUtil.getRandomLetter());


                order.setOrderNo(orderNo);
                order.setOrderDetails(details);
                if(StringUtil.isNotBlankList(tqm)){
                    order.setExtractedCode(tqm);
                }else{
                    Integer pid = skp.getPid();
                    if (pid == null) {
                        logger.info("pid is null");
                        return "0";
                    } else {
                        Pp pp = ppJpaDao.findOneById(pid);
                        order.setExtractedCode(pp.getPpdm() + orderNo);
                    }
                }
                order.setBuyer(buyer);
                order.setPayments(payments);

                buyer.setEmail(email);
                buyer.setTelephoneNo(gfdh);
                buyer.setName(gfmc);
                buyer.setBankAcc(gfyhzh);
                buyer.setBank(gfyh);
                buyer.setAddress(gfdz);


                if (price.indexOf(",") != -1 && spdm.indexOf(",") != -1) {
                    Integer priceSize = RJCheckUtil.getSize(price, ",");
                    Integer spdmSize = RJCheckUtil.getSize(spdm, ",");
                    if (priceSize != spdmSize) {
                        return "-1";
                    }
                    String[] priceArray = price.split(",");
                    String[] spdmArray = spdm.split(",");
                    for (int i = 0; i < priceSize + 1; i++) {
                        AdapterDataOrderDetails detail = new AdapterDataOrderDetails();
                        detail.setAmount(Double.valueOf(priceArray[i]));
                        detail.setMxTotalAmount(Double.valueOf(priceArray[i]));
                        detail.setTaxAmount(0d);
                        detail.setVenderOwnCode(spdmArray[i]);
                        details.add(detail);
                        order.setTotalAmount(order.getTotalDiscount() + Double.valueOf(priceArray[i]));
                    }
                } else {
                    AdapterDataOrderDetails detail = new AdapterDataOrderDetails();
                    order.setTotalAmount(Double.valueOf(price));
                    detail.setAmount(Double.valueOf(price));
                    detail.setMxTotalAmount(Double.valueOf(price));
                    detail.setTaxAmount(0d);
                    if("".equals(spdm)){
                        detail.setVenderOwnCode(null);
                    }else{
                        detail.setVenderOwnCode(spdm);
                    }
                    details.add(detail);
                }
                String json = "";
                Result result = jkpzService.jkpzInvoice(JSON.toJSONString(adapterPost));
                if (null != result.getCode() && "9999".equals(result.getCode())) {
                    logger.info("进入拒绝开票-----错误原因为" + result.getMsg());
                    String reason = result.getMsg();
                    if (null != sjly && "4".equals(sjly)) {
                        logger.info("进行拒绝开票的weixinOrderN+++++" + weixinOrderNo);
                        weixinUtils.jujuekp(weixinOrderNo, reason, access_token);
                    }
                    return "-1";
                }
                Map map = new HashMap();
                map.put("returnMsg", result.getMsg());
                map.put("returnCode", result.getCode());
                map.put("serialorder", adapterData.getSerialNumber() + order.getOrderNo());
                json = JSON.toJSONString(map);
                return json;
            } catch (Exception e) {
                e.printStackTrace();
                return "-1";
            }
        } else {
            return "0";
        }
    }


    /**
     * GET_TYPE_3开票方法
     * @param gsdm
     * @param on
     * @param sn
     * @param tq
     * @param gfmc
     * @param gfsh
     * @param email
     * @param gfyh
     * @param gfyhzh
     * @param gfdz
     * @param gfdh
     * @param tqm
     * @param openid
     * @param sjly
     * @param access_token
     * @param weixinOrderNo
     * @return
     */
    @Override
    public String makeInvoice(String gsdm, String on, String sn, String tq, String gfmc, String gfsh, String email, String gfyh, String gfyhzh, String gfdz, String gfdh, String tqm, String openid, String sjly, String access_token, String weixinOrderNo) {
        if (StringUtil.isNotBlankList(on, sn, tq, gfmc)) {
            try {
                Skp skp = skpJpaDao.findOneByKpddmAndGsdm(sn, gsdm);
                Xf xf = xfJpaDao.findOneById(skp.getXfid());
                AdapterPost post = getApiMsg(gsdm,xf.getId(),skp.getId(), tq);
                if(post==null){
                    return "-2";
                }
                AdapterData data = new AdapterData();
                AdapterDataOrder order = new AdapterDataOrder();
                AdapterDataOrderBuyer buyer = new AdapterDataOrderBuyer();
                post.setData(data);
                data.setSerialNumber("JY" + System.currentTimeMillis() + NumberUtil.getRandomLetter());
                data.setDatasource(sjly);
                data.setOpenid(openid);
                data.setOrder(order);
                order.setBuyer(buyer);
                if(StringUtil.isNotBlankList(tqm)){
                    order.setExtractedCode(tqm);
                }else{
                    Integer pid = skp.getPid();
                    if (pid == null) {
                        logger.info("pid is null");
                        return "0";
                    } else {
                        Pp pp = ppJpaDao.findOneById(pid);
                        order.setExtractedCode(pp.getPpdm() + on);
                    }
                }
                buyer.setEmail(email);
                buyer.setTelephoneNo(gfdh);
                buyer.setName(gfmc);
                buyer.setBankAcc(gfyhzh);
                buyer.setBank(gfyh);
                buyer.setAddress(gfdz);
                //转换
                Map kpMap = transAdapterForSq(gsdm, post);
                String xmlString = kpService.uploadOrderData(gsdm, kpMap, "01");
                DefaultResult defaultResult = XmlJaxbUtils.convertXmlStrToObject(DefaultResult.class, xmlString);
                if (null != defaultResult.getReturnCode() && "9999".equals(defaultResult.getReturnCode())) {
                    logger.info("进入拒绝开票-----错误原因为" + defaultResult.getReturnMessage());
                    String reason = defaultResult.getReturnMessage();
                    if (null != sjly && "4".equals(sjly)) {
                        logger.info("进行拒绝开票的weixinOrderN+++++" + weixinOrderNo);
                        weixinUtils.jujuekp(weixinOrderNo, reason, access_token);
                    }
                    return "-1";
                }

                Map map = new HashMap();
                map.put("returnMsg", defaultResult.getReturnMessage());
                map.put("returnCode", defaultResult.getReturnCode());
                map.put("serialorder", data.getSerialNumber() + order.getOrderNo());
                return JSON.toJSONString(map);
            } catch (Exception e) {
                e.printStackTrace();
                return "-1";
            }
        } else {
            return "0";
        }
    }

    /**
     * 发送抬头数据给客户（根据参数获取客户接收抬头信息的接口），适用于GET_TYPE_1
     * @param gsdm
     * @param sn
     * @param buyer
     * @return
     */
    @Override
    public boolean sendBuyer(String gsdm,String sn,AdapterDataOrderBuyer buyer) {
        try {
            Skp skp = skpJpaDao.findOneByKpddmAndGsdm(sn, gsdm);
            Cszb cszb = cszbService.getSpbmbbh(gsdm, skp.getXfid(), skp.getId(), "sendBuyerUrl");
            String result=HttpClientUtil.doPostJson(cszb.getCsz(), JSON.toJSONString(buyer));
            if("0000".equals(JSON.parseObject(result).getString("returnCode"))){
                return true;
            }else{
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取抽取数据方法（根据参数值去取方法名），适用于GET_TYPE_3
     * @param gsdm
     * @param xfid
     * @param skpid
     * @param tq
     * @return
     */
    private AdapterPost getApiMsg(String gsdm,Integer xfid,Integer skpid, String tq){
        try {
            Cszb cszb = cszbService.getSpbmbbh(gsdm, xfid, skpid, "extractMethod");
            if(StringUtil.isNotBlankList(cszb.getCsz())){
                Class<? extends TransferExtractDataService> clazz = transferExtractDataService.getClass();
                Method method = clazz.getDeclaredMethod(cszb.getCsz(), String.class);
                AdapterPost post = (AdapterPost)method.invoke(transferExtractDataService, tq);
                return post;
            }else{
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 转换adapterpost对象为含有jyxxsq、jymxsq、jyzfmx的map
     * @param post
     * @return
     */
    public static Map transAdapterForSq(String gsdm,AdapterPost post){
        AdapterData adapterData=post.getData();
        AdapterDataOrder adapterDataOrder=adapterData.getOrder();
        AdapterDataSeller adapterDataSeller = adapterData.getSeller();
        AdapterDataOrderBuyer adapterDataOrderBuyer = adapterDataOrder.getBuyer();
        List<AdapterDataOrderDetails> adapterDataOrderOrderDetails = adapterDataOrder.getOrderDetails();
        List<AdapterDataOrderPayments> adapterDataOrderPayments = adapterDataOrder.getPayments();

        List<Jyxxsq> jyxxsqList = new ArrayList<>();
        List<Jymxsq> jymxsqList = new ArrayList<>();
        List<Jyzfmx> jyzfmxList = new ArrayList<>();

        Jyxxsq jyxxsq = new Jyxxsq();
        jyxxsq.setGsdm(gsdm);
        jyxxsq.setLrsj(new Date());
        jyxxsq.setXgsj(new Date());

        jyxxsq.setKpddm(post.getClientNo());

        jyxxsq.setJylsh(adapterData.getSerialNumber());
        jyxxsq.setKpr(adapterData.getDrawer());
        jyxxsq.setFhr(adapterData.getReviewer());
        jyxxsq.setSkr(adapterData.getPayee());
        jyxxsq.setSjly(adapterData.getDatasource());
        jyxxsq.setOpenid(adapterData.getOpenid());
        jyxxsq.setFpzldm(adapterData.getInvType());

        jyxxsq.setXfyhzh(adapterDataSeller.getBankAcc());
        jyxxsq.setXfyh(adapterDataSeller.getBank());
        jyxxsq.setXfdh(adapterDataSeller.getTelephoneNo());
        jyxxsq.setXfdz(adapterDataSeller.getAddress());
        jyxxsq.setXfsh(adapterDataSeller.getIdentifier());
        jyxxsq.setXfmc(adapterDataSeller.getName());

        jyxxsq.setDdh(adapterDataOrder.getOrderNo());
        jyxxsq.setSfdyqd(adapterDataOrder.getInvoiceList());
        jyxxsq.setSfcp(adapterDataOrder.getInvoiceSplit());
        jyxxsq.setSfdy(adapterDataOrder.getInvoiceSfdy());
        jyxxsq.setDdrq(adapterDataOrder.getOrderDate());
        jyxxsq.setZsfs(adapterDataOrder.getChargeTaxWay());
        jyxxsq.setJshj(adapterDataOrder.getTotalAmount());
        jyxxsq.setHsbz(adapterDataOrder.getTaxMark());
        jyxxsq.setBz(adapterDataOrder.getRemark());
        jyxxsq.setTqm(adapterDataOrder.getExtractedCode());

        jyxxsq.setGfemail(adapterDataOrderBuyer.getEmail());
        jyxxsq.setGfsh(adapterDataOrderBuyer.getIdentifier());
        jyxxsq.setGfmc(adapterDataOrderBuyer.getName());
        jyxxsq.setGfdz(adapterDataOrderBuyer.getAddress());
        jyxxsq.setGfdh(adapterDataOrderBuyer.getTelephoneNo());
        jyxxsq.setGfyh(adapterDataOrderBuyer.getBank());
        jyxxsq.setGfyhzh(adapterDataOrderBuyer.getBankAcc());
        jyxxsq.setGflx(adapterDataOrderBuyer.getCustomerType());
        jyxxsq.setSffsyj(adapterDataOrderBuyer.getIsSend());
        jyxxsq.setGflxr(adapterDataOrderBuyer.getRecipient());
        jyxxsq.setGfsjrdz(adapterDataOrderBuyer.getReciAddress());
        jyxxsq.setGfyb(adapterDataOrderBuyer.getZip());
        jyxxsqList.add(jyxxsq);

        for(int i=0;i<adapterDataOrderOrderDetails.size();i++){
            Jymxsq jymxsq = new Jymxsq();
            jymxsq.setLrsj(new Date());
            jymxsq.setXgsj(new Date());
            jymxsq.setDdh(jyxxsq.getDdh());
            jymxsq.setSpmxxh(i);
            jymxsq.setSpggxh(adapterDataOrderOrderDetails.get(i).getSpec());
            jymxsq.setJshj(adapterDataOrderOrderDetails.get(i).getMxTotalAmount());
            jymxsq.setSpje(adapterDataOrderOrderDetails.get(i).getAmount());
            jymxsq.setSpdj(adapterDataOrderOrderDetails.get(i).getUnitPrice());
            jymxsq.setSps(adapterDataOrderOrderDetails.get(i).getQuantity());
            jymxsq.setSpse(adapterDataOrderOrderDetails.get(i).getTaxAmount());
            jymxsq.setSpdm(adapterDataOrderOrderDetails.get(i).getProductCode());
            jymxsq.setFphxz(adapterDataOrderOrderDetails.get(i).getRowType());
            jymxsq.setSpsl(adapterDataOrderOrderDetails.get(i).getTaxRate());
            jymxsq.setSpmc(adapterDataOrderOrderDetails.get(i).getProductName());
            jymxsq.setSpggxh(adapterDataOrderOrderDetails.get(i).getSpec());
            jymxsq.setSpdw(adapterDataOrderOrderDetails.get(i).getUtil());
            jymxsq.setYhzcmc(adapterDataOrderOrderDetails.get(i).getPolicyName());
            jymxsq.setYhzcbs(adapterDataOrderOrderDetails.get(i).getPolicyMark());
            jymxsq.setLslbz(adapterDataOrderOrderDetails.get(i).getTaxRateMark());
            jymxsq.setSpzxbm(adapterDataOrderOrderDetails.get(i).getVenderOwnCode());
            jymxsq.setKce(adapterDataOrderOrderDetails.get(i).getDeductAmount());
            jymxsq.setYkjje(0d);
            jymxsq.setKkjje(adapterDataOrderOrderDetails.get(i).getMxTotalAmount());
            jymxsqList.add(jymxsq);
        }


        for(int i=0;i<adapterDataOrderPayments.size();i++){
            Jyzfmx jyzfmx = new Jyzfmx();
            jyzfmx.setLrsj(new Date());
            jyzfmx.setXgsj(new Date());
            jyzfmx.setDdh(jyxxsq.getDdh());
            jyzfmx.setZfje(adapterDataOrderPayments.get(i).getPayPrice());
            jyzfmx.setZffsDm(adapterDataOrderPayments.get(i).getPayCode());
            jyzfmxList.add(jyzfmx);
        }

        Map kpMap = new HashMap();
        kpMap.put("jyxxsqList",jyxxsqList);
        kpMap.put("jymxsqList",jymxsqList);
        kpMap.put("jyzfmxList",jyzfmxList);
        return kpMap;
    }
}
