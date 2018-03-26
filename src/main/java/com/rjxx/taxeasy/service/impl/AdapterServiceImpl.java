package com.rjxx.taxeasy.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rjxx.taxeasy.bizcomm.utils.GetXmlUtil;
import com.rjxx.taxeasy.bizcomm.utils.HttpUtils;
import com.rjxx.taxeasy.dao.*;
import com.rjxx.taxeasy.domains.*;
import com.rjxx.taxeasy.dto.*;
import com.rjxx.taxeasy.service.AdapterService;
import com.rjxx.taxeasy.service.CszbService;
import com.rjxx.taxeasy.service.SpvoService;
import com.rjxx.taxeasy.utils.NumberUtil;
import com.rjxx.taxeasy.vo.Spvo;
import com.rjxx.taxeasy.wechat.util.TaxUtil;
import com.rjxx.utils.RJCheckUtil;
import com.rjxx.utils.StringUtil;
import com.rjxx.utils.XmlUtil;
import com.rjxx.utils.weixin.WeixinUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Override
    public Map getGrandMsg(String gsdm, String q) {
        Map map = RJCheckUtil.decodeForAll(q);
        String data = (String) map.get("A0");
        JSONObject jsonData = JSON.parseObject(data);
        String on = jsonData.getString("on");
        String sn = jsonData.getString("sn");
        //如果门店号为空则认为是该公司下只有一个税号一个门店号
        if (!StringUtil.isNotBlankList(sn)) {
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
        //如果门店号为空则认为是该公司下只有一个税号一个门店号
        if (!StringUtil.isNotBlankList(storeNo)) {
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
                result.put("orderNo", orderNo);
                result.put("orderTime", orderTime);
                result.put("storeNo", storeNo);
                result.put("price", price);
                result.put("spsl", spsl.toString());
                result.put("spmc", spmc.toString());
                result.put("kpdmc", skp.getKpdmc());
                result.put("gsmc", gsdm);
                Integer pid = skp.getPid();
                if (pid == null) {
                    logger.info("pid is null");
                    return null;
                } else {
                    Pp pp = ppJpaDao.findOneById(pid);
                    result.put("tqm", pp.getPpdm() + orderNo);
                }
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
        if (StringUtil.isNotBlankList(orderNo, orderTime, price, storeNo, gfmc)) {
            try {
                Skp skp = skpJpaDao.findOneByKpddmAndGsdm(storeNo, gsdm);
                Integer xfid = skp.getXfid(); //销方id
                Xf xf = xfJpaDao.findOneById(xfid);
                Integer kpdid = skp.getId();//税控盘id(开票点id)
                Jyxxsq jyxxsq = new Jyxxsq();
                jyxxsq.setJshj(0d);
                jyxxsq.setDdh(orderNo);
                jyxxsq.setGsdm(gsdm);
                jyxxsq.setKpddm(storeNo);
                jyxxsq.setXfmc(xf.getXfmc());
                jyxxsq.setKpr(skp.getKpr());
                jyxxsq.setFhr(skp.getFhr());
                jyxxsq.setSkr(skp.getSkr());
                jyxxsq.setXfid(xfid);
                jyxxsq.setXfsh(xf.getXfsh());
                jyxxsq.setXfyhzh(skp.getYhzh());
                jyxxsq.setXfyh(skp.getKhyh());
                jyxxsq.setXfdh(skp.getLxdh());//销方电话
                jyxxsq.setXfdz(skp.getLxdz());//销方地址
                jyxxsq.setXflxr(xf.getXflxr());//销方联系人
                jyxxsq.setXfyb(xf.getXfyb());//销方邮编
                jyxxsq.setGfmc(gfmc);
                jyxxsq.setGfsh(gfsh);
                if (null != gfsh && !"".equals(gfsh)) {
                    jyxxsq.setGflx("1");
                } else {
                    jyxxsq.setGflx("0");
                }
                jyxxsq.setGfemail(email);
                jyxxsq.setGfdz(gfdz);
                jyxxsq.setGfdh(gfdh);
                jyxxsq.setGfyhzh(gfyhzh);
                jyxxsq.setGfyh(gfyh);
                jyxxsq.setJylsh(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + NumberUtil.getRandomLetter());
                jyxxsq.setFpzldm("12");
                jyxxsq.setFpczlxdm("11");
                jyxxsq.setSffsyj("1");
                jyxxsq.setZsfs("0");
                jyxxsq.setHsbz("1");
                jyxxsq.setSjly(sjly);
                jyxxsq.setOpenid(openid);
                jyxxsq.setLrsj(new Date());
                jyxxsq.setXgsj(new Date());
                jyxxsq.setDdrq(new SimpleDateFormat("yyyyMMddHHmmss").parse(orderTime));
                if (tqm == null) {
                    Integer pid = skp.getPid();
                    if (pid == null) {
                        jyxxsq.setTqm(orderNo);
                    } else {
                        Pp pp = ppJpaDao.findOneById(pid);
                        jyxxsq.setTqm(pp.getPpdm() + orderNo);
                    }
                } else {
                    jyxxsq.setTqm(tqm);
                }


                List<Jymxsq> jymxsqList = new ArrayList<>();
                if (price.indexOf(",") != -1 && spdm.indexOf(",") != -1) {
//                    BigDecimal jyxxsqPrice = new BigDecimal("0");
                    Integer priceSize = RJCheckUtil.getSize(price, ",");
                    Integer spdmSize = RJCheckUtil.getSize(spdm, ",");
                    if (priceSize != spdmSize) {
                        return "-1";
                    }
                    String[] priceArray = price.split(",");
                    String[] spdmArray = spdm.split(",");
                    for (int i = 0; i < priceSize + 1; i++) {
                        jyxxsq.setJshj(jyxxsq.getJshj() + Double.valueOf(priceArray[i]));
//                        jyxxsqPrice.add(new BigDecimal(priceArray[i]));
                        Jymxsq jymxsq = new Jymxsq();
                        jymxsq.setSpdm(spdmArray[i]);
                        jymxsq.setJshj(Double.valueOf(priceArray[i]));
                        jymxsqList.add(jymxsq);
                    }
//                    jyxxsq.setJshj(jyxxsqPrice.doubleValue());
                } else {
                    jyxxsq.setJshj(Double.valueOf(price));
                    Jymxsq jymxsq = new Jymxsq();
                    jymxsq.setSpdm(spdm);
                    jymxsq.setJshj(Double.valueOf(price));
                    jymxsqList.add(jymxsq);
                }
                for (Jymxsq jymxsq : jymxsqList) {
                    Cszb cszb = cszbService.getSpbmbbh(gsdm, xfid, kpdid, "dyspbmb");
                    Map map = new HashMap();
                    map.put("gsdm", gsdm);
                    if (cszb.getCsz() != null) {
                        map.put("spdm", cszb.getCsz());
                    } else {
                        //如果没有参数，则表明是传商品代码,此时交易明细申请中税收分类编码暂时放商品编码
                        if (StringUtils.isNotBlank(jymxsq.getSpdm())) {
                            map.put("spdm", jymxsq.getSpdm());
                        } else {
                            return "-1";
                        }
                    }
                    Spvo spvo = spvoService.findOneSpvo(map);
                    jymxsq.setSpdm(spvo.getSpbm());
                    jymxsq.setYhzcmc(spvo.getYhzcmc());
                    jymxsq.setYhzcbs(spvo.getYhzcbs());
                    jymxsq.setLslbz(spvo.getLslbz());
                    jymxsq.setFphxz("0");
                    jymxsq.setSpmc(spvo.getSpmc());
                    jymxsq.setLrsj(new Date());
                    jymxsq.setXgsj(new Date());
                    jymxsq.setSpsl(spvo.getSl());
                    jymxsq.setSpje(jymxsq.getJshj());
                }
                List<Jymxsq> jymxsqs = TaxUtil.separatePrice(jymxsqList);

                List<Jyzfmx> jyzfmxList = new ArrayList<>();

                String xml = GetXmlUtil.getFpkjXml(jyxxsq, jymxsqs, jyzfmxList);
                Gsxx oneByGsdm = gsxxJpaDao.findOneByGsdm(gsdm);
                String appid = oneByGsdm.getAppKey();
                String key = oneByGsdm.getSecretKey();
                String resultxml = HttpUtils.HttpUrlPost(xml, appid, key);
                String json = "";
                try {
                    Map<String, Object> resultMap = XmlUtil.xml2Map(resultxml);
                    String returnMsg = resultMap.get("ReturnMessage").toString();
                    String returnCode = resultMap.get("ReturnCode").toString();
                    Map map2 = new HashMap();
                    map2.put("returnMsg", returnMsg);
                    map2.put("returnCode", returnCode);
                    map2.put("serialorder", jyxxsq.getJylsh() + jyxxsq.getDdh());
                    json = JSONObject.toJSONString(map2);
                    if (null != returnCode && "9999".equals(returnCode)) {
                        logger.info("进入拒绝开票-----错误原因为" + returnMsg);
                        String reason = returnMsg;
                        if (null != sjly && "4".equals(sjly)) {
                            logger.info("进行拒绝开票的weixinOrderN+++++" + weixinOrderNo);
                            String str = weixinUtils.jujuekp(weixinOrderNo, reason, access_token);
                        }
                        return "-1";
                    }

                } catch (Exception e) {
                    String serialorder = resultxml;
                    Kpls oneBySerialorder = kplsJpaDao.findOneBySerialorder(serialorder);
                    String fphm = oneBySerialorder.getFphm();
                    String fpdm = oneBySerialorder.getFpdm();
                    if (fphm == null || fpdm == null) {
                        return "-1";
                    }
                    Map map3 = new HashMap();
                    map3.put("fphm", fphm);
                    map3.put("fpdm", fpdm);
                    map3.put("serialorder", jyxxsq.getJylsh() + jyxxsq.getDdh());
                    json = JSONObject.toJSONString(map3);
                }
                return json;
            } catch (Exception e) {
                e.printStackTrace();
                return "-1";
            }
        } else {
            return "0";
        }
    }

    public String makeInvoices(String gsdm, String q, String gfmc, String gfsh, String email,
                               String gfyh, String gfyhzh, String gfdz, String gfdh, String tqm, String openid, String sjly, String access_token, String weixinOrderNo) {
        Map decode = RJCheckUtil.decodeForAll(q);
        String data = (String) decode.get("A0");
        JSONObject jsonData = JSON.parseObject(data);
        String orderNo = jsonData.getString("on");
        String orderTime = jsonData.getString("ot");
        String price = jsonData.getString("pr");
        String storeNo = jsonData.getString("sn");
        String spdm = jsonData.getString("sp");
        if (StringUtil.isNotBlankList(orderNo, orderTime, price, storeNo, gfmc)) {
            try {
                AdapterPost adapterPost = new AdapterPost();
                AdapterData adapterData = new AdapterData();
                AdapterDataOrder order = new AdapterDataOrder();
                AdapterDataSeller seller = new AdapterDataSeller();
                AdapterDataOrderBuyer buyer = new AdapterDataOrderBuyer();
                List<AdapterDataOrderDetails> details = new ArrayList<>();
                List<AdapterDataOrderPayments> payments = new ArrayList<>();

                Skp skp = skpJpaDao.findOneByKpddmAndGsdm(storeNo, gsdm);
                Integer xfid = skp.getXfid(); //销方id
                Xf xf = xfJpaDao.findOneById(xfid);
                Integer kpdid = skp.getId();//税控盘id(开票点id)
                Jyxxsq jyxxsq = new Jyxxsq();

                jyxxsq.setJshj(0d);
                order.setTotalAmount(0d);

                jyxxsq.setDdh(orderNo);
                order.setOrderNo(orderNo);

                jyxxsq.setGsdm(gsdm);
                Gsxx gsxx = gsxxJpaDao.findOneByGsdm(gsdm);
                adapterPost.setAppId(gsxx.getAppKey());

                jyxxsq.setKpddm(storeNo);
                adapterPost.setClientNo(storeNo);

                jyxxsq.setXfmc(xf.getXfmc());
                seller.setName(xf.getXfmc());

                jyxxsq.setKpr(skp.getKpr());
                adapterData.setDrawer(skp.getKpr());

                jyxxsq.setFhr(skp.getFhr());
                adapterData.setReviewer(skp.getFhr());

                jyxxsq.setSkr(skp.getSkr());
                adapterData.setPayee(skp.getSkr());

                jyxxsq.setXfid(xfid);

                jyxxsq.setXfsh(xf.getXfsh());
                seller.setIdentifier(xf.getXfsh());

                jyxxsq.setXfyhzh(skp.getYhzh());
                seller.setBankAcc(skp.getYhzh());

                jyxxsq.setXfyh(skp.getKhyh());
                seller.setBank(skp.getKhyh());

                jyxxsq.setXfdh(skp.getLxdh());//销方电话
                seller.setTelephoneNo(skp.getLxdh());

                jyxxsq.setXfdz(skp.getLxdz());//销方地址
                jyxxsq.setXflxr(xf.getXflxr());//销方联系人
                jyxxsq.setXfyb(xf.getXfyb());//销方邮编
                jyxxsq.setGfmc(gfmc);
                jyxxsq.setGfsh(gfsh);
                if (null != gfsh && !"".equals(gfsh)) {
                    jyxxsq.setGflx("1");
                } else {
                    jyxxsq.setGflx("0");
                }
                jyxxsq.setGfemail(email);
                jyxxsq.setGfdz(gfdz);
                jyxxsq.setGfdh(gfdh);
                jyxxsq.setGfyhzh(gfyhzh);
                jyxxsq.setGfyh(gfyh);
                jyxxsq.setJylsh(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + NumberUtil.getRandomLetter());
                jyxxsq.setFpzldm("12");
                jyxxsq.setFpczlxdm("11");
                jyxxsq.setSffsyj("1");
                jyxxsq.setZsfs("0");
                jyxxsq.setHsbz("1");
                jyxxsq.setSjly(sjly);
                jyxxsq.setOpenid(openid);
                jyxxsq.setLrsj(new Date());
                jyxxsq.setXgsj(new Date());
                jyxxsq.setDdrq(new SimpleDateFormat("yyyyMMddHHmmss").parse(orderTime));
                if (tqm == null) {
                    Integer pid = skp.getPid();
                    if (pid == null) {
                        jyxxsq.setTqm(orderNo);
                    } else {
                        Pp pp = ppJpaDao.findOneById(pid);
                        jyxxsq.setTqm(pp.getPpdm() + orderNo);
                    }
                } else {
                    jyxxsq.setTqm(tqm);
                }


                List<Jymxsq> jymxsqList = new ArrayList<>();
                if (price.indexOf(",") != -1 && spdm.indexOf(",") != -1) {
//                    BigDecimal jyxxsqPrice = new BigDecimal("0");
                    Integer priceSize = RJCheckUtil.getSize(price, ",");
                    Integer spdmSize = RJCheckUtil.getSize(spdm, ",");
                    if (priceSize != spdmSize) {
                        return "-1";
                    }
                    String[] priceArray = price.split(",");
                    String[] spdmArray = spdm.split(",");
                    for (int i = 0; i < priceSize + 1; i++) {
                        jyxxsq.setJshj(jyxxsq.getJshj() + Double.valueOf(priceArray[i]));
//                        jyxxsqPrice.add(new BigDecimal(priceArray[i]));
                        Jymxsq jymxsq = new Jymxsq();
                        jymxsq.setSpdm(spdmArray[i]);
                        jymxsq.setJshj(Double.valueOf(priceArray[i]));
                        jymxsqList.add(jymxsq);
                    }
//                    jyxxsq.setJshj(jyxxsqPrice.doubleValue());
                } else {
                    jyxxsq.setJshj(Double.valueOf(price));
                    Jymxsq jymxsq = new Jymxsq();
                    jymxsq.setSpdm(spdm);
                    jymxsq.setJshj(Double.valueOf(price));
                    jymxsqList.add(jymxsq);
                }
                for (Jymxsq jymxsq : jymxsqList) {
                    Cszb cszb = cszbService.getSpbmbbh(gsdm, xfid, kpdid, "dyspbmb");
                    Map map = new HashMap();
                    map.put("gsdm", gsdm);
                    if (cszb.getCsz() != null) {
                        map.put("spdm", cszb.getCsz());
                    } else {
                        //如果没有参数，则表明是传商品代码,此时交易明细申请中税收分类编码暂时放商品编码
                        if (StringUtils.isNotBlank(jymxsq.getSpdm())) {
                            map.put("spdm", jymxsq.getSpdm());
                        } else {
                            return "-1";
                        }
                    }
                    Spvo spvo = spvoService.findOneSpvo(map);
                    jymxsq.setSpdm(spvo.getSpbm());
                    jymxsq.setYhzcmc(spvo.getYhzcmc());
                    jymxsq.setYhzcbs(spvo.getYhzcbs());
                    jymxsq.setLslbz(spvo.getLslbz());
                    jymxsq.setFphxz("0");
                    jymxsq.setSpmc(spvo.getSpmc());
                    jymxsq.setLrsj(new Date());
                    jymxsq.setXgsj(new Date());
                    jymxsq.setSpsl(spvo.getSl());
                    jymxsq.setSpje(jymxsq.getJshj());
                }
                List<Jymxsq> jymxsqs = TaxUtil.separatePrice(jymxsqList);

                List<Jyzfmx> jyzfmxList = new ArrayList<>();

                String xml = GetXmlUtil.getFpkjXml(jyxxsq, jymxsqs, jyzfmxList);
                Gsxx oneByGsdm = gsxxJpaDao.findOneByGsdm(gsdm);
                String appid = oneByGsdm.getAppKey();
                String key = oneByGsdm.getSecretKey();
                String resultxml = HttpUtils.HttpUrlPost(xml, appid, key);
                String json = "";
                try {
                    Map<String, Object> resultMap = XmlUtil.xml2Map(resultxml);
                    String returnMsg = resultMap.get("ReturnMessage").toString();
                    String returnCode = resultMap.get("ReturnCode").toString();
                    Map map2 = new HashMap();
                    map2.put("returnMsg", returnMsg);
                    map2.put("returnCode", returnCode);
                    map2.put("serialorder", jyxxsq.getJylsh() + jyxxsq.getDdh());
                    json = JSONObject.toJSONString(map2);
                    if (null != returnCode && "9999".equals(returnCode)) {
                        logger.info("进入拒绝开票-----错误原因为" + returnMsg);
                        String reason = returnMsg;
                        if (null != sjly && "4".equals(sjly)) {
                            logger.info("进行拒绝开票的weixinOrderN+++++" + weixinOrderNo);
                            String str = weixinUtils.jujuekp(weixinOrderNo, reason, access_token);
                        }
                        return "-1";
                    }

                } catch (Exception e) {
                    String serialorder = resultxml;
                    Kpls oneBySerialorder = kplsJpaDao.findOneBySerialorder(serialorder);
                    String fphm = oneBySerialorder.getFphm();
                    String fpdm = oneBySerialorder.getFpdm();
                    if (fphm == null || fpdm == null) {
                        return "-1";
                    }
                    Map map3 = new HashMap();
                    map3.put("fphm", fphm);
                    map3.put("fpdm", fpdm);
                    map3.put("serialorder", jyxxsq.getJylsh() + jyxxsq.getDdh());
                    json = JSONObject.toJSONString(map3);
                }
                return json;
            } catch (Exception e) {
                e.printStackTrace();
                return "-1";
            }
        } else {
            return "0";
        }
    }
}
