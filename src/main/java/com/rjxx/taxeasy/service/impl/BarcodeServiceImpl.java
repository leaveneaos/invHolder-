package com.rjxx.taxeasy.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.rjxx.taxeasy.bizcomm.utils.GetDataService;
import com.rjxx.taxeasy.bizcomm.utils.GetXmlUtil;
import com.rjxx.taxeasy.bizcomm.utils.HttpUtils;
import com.rjxx.taxeasy.dao.*;
import com.rjxx.taxeasy.domains.*;
import com.rjxx.taxeasy.service.*;
import com.rjxx.taxeasy.utils.NumberUtil;
import com.rjxx.taxeasy.vo.Spvo;
import com.rjxx.taxeasy.wechat.util.TaxUtil;
import com.rjxx.utils.RJCheckUtil;
import com.rjxx.utils.XmlUtil;
import com.rjxx.utils.weixin.WeixinUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by wangyahui on 2017/8/9 0009
 */
@Service
public class BarcodeServiceImpl implements BarcodeService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private CszbService cszbService;//参数主表
    @Autowired
    private XfJpaDao xfJpaDao;
    @Autowired
    private PpJpaDao ppJpaDao;
    @Autowired
    private SpvoService spvoService;
    @Autowired
    private GsxxJpaDao gsxxJpaDao;
    @Autowired
    private KplsJpaDao kplsJpaDao;
    @Autowired
    private SkpJpaDao skpJpaDao;
    @Autowired
    private JylsJpaDao jylsJpaDao;
    @Autowired
    private TqmtqService tqmtqService;
    @Autowired
    private JylsService jylsService;

    @Autowired
    private  WxfpxxJpaDao wxfpxxJpaDao;

    @Autowired
    private GetDataService getDataService;

    @Autowired
    private FpjService fpjService;

    @Autowired
    private TqjlService tqjlService;

    @Autowired
    private  WeixinUtils weixinUtils;

    @Autowired
    private GsxxService gsxxService;

    @Override
    public Map sm(String gsdm, String q) {
        try {
            Gsxx gsxx = gsxxJpaDao.findOneByGsdm(gsdm);
            String secretKey = gsxx.getSecretKey();
            Boolean b = RJCheckUtil.checkMD5ForAll(secretKey, q);
            if (b) {
                Map decode = RJCheckUtil.decodeForAll(q);
                String storeNo = decode.get("A3").toString();
                String orderNo = decode.get("A0").toString();
                Skp skp = skpJpaDao.findOneByKpddmAndGsdm(storeNo, gsdm);
                Integer pid = skp.getPid();
                Integer xfid = skp.getXfid(); //销方id
                Integer kpdid = skp.getId();//税控盘id(开票点id)
                String ppdm = "";
                String ppurl = "";
                if (pid != null) {
                    Pp pp = ppJpaDao.findOneById(pid);
                    ppdm = pp.getPpdm();
                    ppurl = pp.getPpurl();
                } else {
                    Cszb cszb = cszbService.getSpbmbbh(gsdm, xfid, kpdid, "mrmburlid");
                    ppurl = cszb.getCsz();
                }
                Map map = new HashMap();
                map.put("ppdm", ppdm);
                map.put("ppurl", ppurl);
                map.put("orderNo", orderNo);
                return map;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getSpxx(String gsdm, String q) {
        Map decode = RJCheckUtil.decodeForAll(q);
        Integer size = (Integer) decode.get("size");
        String spdm = "";
        if(size==6){
            spdm = decode.get("A4").toString();
        }
        String orderNo = decode.get("A0").toString();
        String orderTime = decode.get("A1").toString();
        String price = decode.get("A2").toString();
        String storeNo = decode.get("A3").toString();
        StringBuilder spsl = new StringBuilder();
        StringBuilder spmc = new StringBuilder();
        if (StringUtils.isNotBlank(orderNo) &&
                StringUtils.isNotBlank(orderTime) &&
                StringUtils.isNotBlank(price) &&
                StringUtils.isNotBlank(storeNo)){
            try {
                Skp skp = skpJpaDao.findOneByKpddmAndGsdm(storeNo,gsdm);
                Integer xfid = skp.getXfid(); //销方id
                Integer kpdid = skp.getId();//税控盘id(开票点id)
                Cszb cszb = cszbService.getSpbmbbh(gsdm, xfid, kpdid, "dyspbmb");

                List<String> spdmList = new ArrayList();
                //如果价格和商品代码有多个
                if(price.indexOf(",")!=-1 && spdm.indexOf(",")!=-1){
                    Integer priceSize = RJCheckUtil.getSize(price, ",");
                    Integer spdmSize = RJCheckUtil.getSize(spdm, ",");
                    if(priceSize!=spdmSize){
                        return null;
                    }
                    String[] spdmArray = spdm.split(",");
                    for(String str:spdmArray){
                        spdmList.add(str);
                    }
                    //如果只有一个或没有传商品代码
                }else{
                    if(StringUtils.isNotBlank(spdm)){
                        spdmList.add(spdm);
                    }
                }
                if(spdmList!=null&&spdmList.size()>0){
                    Map map = new HashMap();
                    map.put("gsdm", gsdm);
                    for(int i=0;i<spdmList.size();i++){
                        map.put("spdm", spdmList.get(i));
                        Spvo oneSpvo = spvoService.findOneSpvo(map);
                        if(i==0){
                            spsl.append(oneSpvo.getSl());
                            spmc.append(oneSpvo.getSpmc());
                        }else{
                            spsl.append(","+oneSpvo.getSl());
                            spmc.append(","+oneSpvo.getSpmc());
                        }
                    }
                    //如果没有传商品代码
                }else{
                    if(cszb.getCsz()!=null){
                        Map map = new HashMap();
                        map.put("gsdm", gsdm);
                        map.put("spdm", cszb.getCsz());
                        Spvo oneSpvo = spvoService.findOneSpvo(map);
                        spsl.append(oneSpvo.getSl());
                        spmc.append(oneSpvo.getSpmc());
                    }else{
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
                /*Map map= new HashMap();
                map.put("gsdm",gsdm);
                Gsxx gsxx = gsxxService.findOneByGsdm(map);
                if(gsxx.getXgsdm()!=null && !"".equals(gsxx.getXgsdm())){
                    result.put("xgsdm", gsxx.getXgsdm());
                }*/
                Integer pid=skp.getPid();
                if(pid==null){
                    result.put("tqm", orderNo);
                }else{
                    Pp pp = ppJpaDao.findOneById(pid);
                    result.put("tqm", pp.getPpdm() + orderNo);
                }
                logger.info("getSpxx结果==="+JSONObject.toJSONString(result));
                return JSONObject.toJSONString(result);
            } catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }else{
            return null;
        }
    }

    @Override
    public String makeInvoice(String gsdm, String q, String gfmc, String gfsh, String email,
                              String gfyh, String gfyhzh, String gfdz, String gfdh,String tqm,String openid,String sjly,String access_token,String weixinOrderNo) {
        Map decode = RJCheckUtil.decodeForAll(q);
        String size = decode.get("size").toString();
        String spdm = "";
        if("6".equals(size)){
            spdm = decode.get("A4").toString();
        }
        String orderNo = decode.get("A0").toString();
        String orderTime = decode.get("A1").toString();
        String price = decode.get("A2").toString();
        String storeNo = decode.get("A3").toString();
        if (StringUtils.isNotBlank(orderNo) &&
                StringUtils.isNotBlank(orderTime) &&
                StringUtils.isNotBlank(price) &&
                StringUtils.isNotBlank(storeNo) &&
                StringUtils.isNotBlank(gfmc)) {
            try {
                Skp skp = skpJpaDao.findOneByKpddmAndGsdm(storeNo,gsdm);
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
                if(null!=gfsh&&!"".equals(gfsh)){
                    jyxxsq.setGflx("1");
                }else {
                    jyxxsq.setGflx("0");
                }
                jyxxsq.setGfemail(email);
                jyxxsq.setGfdz(gfdz);
                jyxxsq.setGfdh(gfdh);
                jyxxsq.setGfyhzh(gfyhzh);
                jyxxsq.setGfyh(gfyh);
                jyxxsq.setJylsh(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())+ NumberUtil.getRandomLetter());
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
                if(tqm==null){
                    Integer pid=skp.getPid();
                    if(pid==null){
                        jyxxsq.setTqm(orderNo);
                    }else{
                        Pp pp = ppJpaDao.findOneById(pid);
                        jyxxsq.setTqm(pp.getPpdm()+orderNo);
                    }
                }else{
                    jyxxsq.setTqm(tqm);
                }


                List<Jymxsq> jymxsqList = new ArrayList<>();
                if(price.indexOf(",")!=-1 && spdm.indexOf(",")!=-1){
//                    BigDecimal jyxxsqPrice = new BigDecimal("0");
                    Integer priceSize = RJCheckUtil.getSize(price, ",");
                    Integer spdmSize = RJCheckUtil.getSize(spdm, ",");
                    if(priceSize!=spdmSize){
                        return "-1";
                    }
                    String[] priceArray = price.split(",");
                    String[] spdmArray = spdm.split(",");
                    for (int i=0;i<priceSize+1;i++){
                        jyxxsq.setJshj(jyxxsq.getJshj()+Double.valueOf(priceArray[i]));
//                        jyxxsqPrice.add(new BigDecimal(priceArray[i]));
                        Jymxsq jymxsq = new Jymxsq();
                        jymxsq.setSpdm(spdmArray[i]);
                        jymxsq.setJshj(Double.valueOf(priceArray[i]));
                        jymxsqList.add(jymxsq);
                    }
//                    jyxxsq.setJshj(jyxxsqPrice.doubleValue());
                }else {
                    jyxxsq.setJshj(Double.valueOf(price));
                    Jymxsq jymxsq = new Jymxsq();
                    jymxsq.setSpdm(spdm);
                    jymxsq.setJshj(Double.valueOf(price));
                    jymxsqList.add(jymxsq);
                }
                for(Jymxsq jymxsq:jymxsqList){
                    Cszb cszb = cszbService.getSpbmbbh(gsdm, xfid, kpdid, "dyspbmb");
                    Map map = new HashMap();
                    map.put("gsdm", gsdm);
                    if(cszb.getCsz()!=null){
                        map.put("spdm", cszb.getCsz());
                    }else {
                        //如果没有参数，则表明是传商品代码,此时交易明细申请中税收分类编码暂时放商品编码
                        if(StringUtils.isNotBlank(jymxsq.getSpdm())){
                            map.put("spdm", jymxsq.getSpdm());
                        }else{
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

                String xml = GetXmlUtil.getFpkjXml(jyxxsq, jymxsqs,jyzfmxList);
                Gsxx oneByGsdm = gsxxJpaDao.findOneByGsdm(gsdm);
                String appid = oneByGsdm.getAppKey();
                String key = oneByGsdm.getSecretKey();
                String resultxml = HttpUtils.HttpUrlPost(xml, appid, key);
                String json = "";
                try {
                    Map<String, Object> resultMap = XmlUtil.xml2Map(resultxml);
                    String returnMsg=resultMap.get("ReturnMessage").toString();
                    String returnCode=resultMap.get("ReturnCode").toString();
                    Map map2 = new HashMap();
                    map2.put("returnMsg", returnMsg);
                    map2.put("returnCode", returnCode);
                    map2.put("serialorder",  jyxxsq.getJylsh()+jyxxsq.getDdh());
                    json=JSONObject.toJSONString(map2);
                    if(null!=returnCode && "9999".equals(returnCode)){
                            logger.info("进入拒绝开票-----错误原因为"+returnMsg);
                            String reason= returnMsg;
                        if(null!= sjly && "4".equals(sjly)){
                            logger.info("进行拒绝开票的weixinOrderN+++++"+weixinOrderNo);
                            String str=  weixinUtils.jujuekp(weixinOrderNo,reason,access_token);
                        }
                        return "-1";
                    }

                }catch (Exception e){
                    String serialorder=resultxml;
                    Kpls oneBySerialorder = kplsJpaDao.findOneBySerialorder(serialorder);
                    String fphm=oneBySerialorder.getFphm();
                    String fpdm = oneBySerialorder.getFpdm();
                    if(fphm==null || fpdm==null){
                        return "-1";
                    }
                    Map map3 = new HashMap();
                    map3.put("fphm", fphm);
                    map3.put("fpdm", fpdm);
                    map3.put("serialorder", jyxxsq.getJylsh()+jyxxsq.getDdh());
                    json=JSONObject.toJSONString(map3);
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

    @Override
    public List<String> checkStatus(String tqm, String gsdm) {
        try {
            List<String> result = new ArrayList();
            List<Integer> djhs = jylsJpaDao.findDjhByTqmAndGsdm(tqm, gsdm);
            if(djhs!=null&&djhs.size()>0){
                logger.info("list不等于空");
                for(Integer djh:djhs){
                    if(djh!=null){
                        logger.info("djh不等于空");
                        Kpls kpls = kplsJpaDao.findOneByDjh(djh);
                        logger.info("kpls=",kpls);
                        String fpztdm = kpls.getFpztdm();
                        String pdfurl = kpls.getPdfurl();
                        String fphm = kpls.getFphm();
                        String je = kpls.getJshj()+"";
                        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
                        String orderTime = sdf.format(kpls.getLrsj());
                        String kplsh = kpls.getKplsh()+"";
                        String serialorder=kpls.getSerialorder();
                        if("00".equals(fpztdm)&& StringUtils.isNotBlank(pdfurl)&&StringUtils.isNotBlank(fphm)){
                            result.add(pdfurl+"+"+je+"+"+orderTime+"+"+kplsh+"+"+serialorder);
                        }else {
                            result.add("开具中");
                        }
                    }else{
                        logger.info("djh等于空");
                        result.add("可开具");
                    }
                }
            }else {
                logger.info("list等于空");
                result.add("可开具");
            }
            logger.info("result=",result);
            return result;
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }


    //以下为非一茶一坐式业务
    @Override
    public String pullInvioce(Map resultSjMap,String gsdm,  String gfmc, String gfsh, String email,
                              String gfyh, String gfyhzh, String gfdz, String gfdh,String tqm,
                              String openid,String sjly,String access_token,String AppId,String key,String weixinOrderNo) {
            try {
                if(resultSjMap==null){
                    String reason="获取数据为空，开票失败，请重试!";
                    String str= weixinUtils.jujuekp(weixinOrderNo,reason,access_token);
                    logger.info("拒绝开票状态"+str);
                    return "-2";
                }
                List<Jyxxsq> jyxxsqList = (List) resultSjMap.get("jyxxsqList");
                List<Jymxsq> jymxsqList = (List) resultSjMap.get("jymxsqList");
                List<Jyzfmx> jyzfmxList=(List)resultSjMap.get("jyzfmxList");
                //封装数据
                Jyxxsq jyxxsq=jyxxsqList.get(0);
                jyxxsq.setGfmc(gfmc);
                jyxxsq.setGfemail(email);
                jyxxsq.setSffsyj("1");
                jyxxsq.setGfsh(gfsh);
                jyxxsq.setGfdz(gfdz);
                jyxxsq.setGfdh(gfdh);
                jyxxsq.setGfyh(gfyh);
                jyxxsq.setGfyhzh(gfyhzh);
                jyxxsq.setOpenid(openid);
                jyxxsq.setSjly(sjly);
                jyxxsq.setTqm(tqm);
                if(null!=gfsh && !"".equals(gfsh)){
                    jyxxsq.setGflx("1");
                }else {
                    jyxxsq.setGflx("0");
                }
                Map map = new HashMap<>();
                map.put("tqm",jyxxsq.getTqm());
                map.put("je",jyxxsq.getJshj());
                map.put("gsdm",jyxxsq.getGsdm());
                Tqmtq tqmtq = tqmtqService.findOneByParams(map);
                Jyls jyls1 = jylsService.findOne(map);
                if(tqmtq != null && tqmtq.getId() != null){
                    logger.info("该提取码已提交过申请!");
                    String reason="该订单已提交过申请!";
                    if(null!=sjly && "4".equals(sjly)){
                        logger.info("进行拒绝开票的weixinOrderN+++++"+weixinOrderNo);
                        //拒绝开票
                        String str= weixinUtils.jujuekp(weixinOrderNo,reason,access_token);
                        logger.info("拒绝开票状态"+str);
                    }
                    return "-2";
                }
                if(jyls1 != null){
                    logger.info("该订单正在开票!");
                    String reason="该订单正在开票!";
                    if(null!=sjly && "4".equals(sjly)){
                        logger.info("进行拒绝开票的weixinOrderN+++++"+weixinOrderNo);
                        //拒绝开票
                        String str= weixinUtils.jujuekp(weixinOrderNo,reason,access_token);
                        logger.info("拒绝开票状态"+str);
                    }
                    return "-2";
                }
                //调用接口开票,jyxxsq,jymxsqList,jyzfmxList
                try {
                    String xml= GetXmlUtil.getFpkjXml(jyxxsq,jymxsqList,jyzfmxList);
                    String resultxml= HttpUtils.HttpUrlPost(xml,AppId,key);
                    logger.info("-------返回值---------"+resultxml);
                    if(null==resultxml||resultxml.equals("")){
                        String reason="发票开具失败，请重试！";
                        String str=  weixinUtils.jujuekp(weixinOrderNo,reason,access_token);
                        logger.info("拒绝开票状态"+str);
                    }
                    Map<String, Object> resultMap = XmlUtil.xml2Map(resultxml);
                    //Document document = DocumentHelper.parseText(resultxml);
                    String returnMsg=resultMap.get("ReturnMessage").toString();
                    String returnCode=resultMap.get("ReturnCode").toString();
                    Map map2 = new HashMap();
                    map2.put("returnMsg", returnMsg);
                    map2.put("returnCode", returnCode);
                    //Element root = document.getRootElement();
                    //List<Element> childElements = root.elements();
                   // Map xmlMap = new HashMap();
                   // for (Element child : childElements) {
                   //     xmlMap.put(child.getName(),child.getText());
                  //  }
                    //String returncode=(String)xmlMap.get("ReturnCode");
                    //String ReturnMessage=(String)xmlMap.get("ReturnMessage");
                    if(returnCode.equals("9999")){
                       //返回错误 拒绝开票
                        String reason="错误信息为"+returnMsg;
                        if(null!=sjly && "4".equals(sjly)){
                            String str=  weixinUtils.jujuekp(weixinOrderNo,reason,access_token);
                            logger.info("进入拒绝开票-----"+returnMsg+"拒绝开票状态"+str);
                        }
                        return "-2";
                    }
                        //插入表
                        Tqmtq tqmtq1 = new Tqmtq();
                        tqmtq1.setDdh(jyxxsq.getTqm());
                        tqmtq1.setLrsj(new Date());
                        tqmtq1.setZje(Double.valueOf(jyxxsq.getJshj()));
                        tqmtq1.setGfmc(gfmc);
                        tqmtq1.setNsrsbh(gfsh);
                        tqmtq1.setDz(gfdz);
                        tqmtq1.setDh(gfdh);
                        tqmtq1.setKhh(gfyh);
                        tqmtq1.setKhhzh(gfyhzh);
                        tqmtq1.setFpzt("0");
                        tqmtq1.setYxbz("1");
                        tqmtq1.setGfemail(email);
                        tqmtq1.setGsdm(jyxxsq.getGsdm());
                        /*String llqxx = request.getHeader("User-Agent");
                        tqmtq1.setLlqxx(llqxx);*/
                        if(openid != null && !"null".equals(openid)){
                            tqmtq1.setOpenid(openid);
                        }
                        tqmtqService.save(tqmtq1);
                        return  "0";

                } catch (Exception e) {
                    e.printStackTrace();
                }


            } catch (Exception e) {
                e.printStackTrace();
                return "-1";
            }
            return "0";

    }

    /**
     * 食其家开票方法
     * @param q
     * @param gsdm
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
    public String sqjInvioce(String q,String gsdm,  String gfmc, String gfsh, String email,
                              String gfyh, String gfyhzh, String gfdz, String gfdh,String tqm,
                              String openid,String sjly,String access_token,String weixinOrderNo) {
        try {
            byte[] bytes = org.apache.commons.codec.binary.Base64.decodeBase64(q);
            String csc = new String(bytes);
            String[] cssz = csc.split("&");
            String orderNo = cssz[0].substring(cssz[0].lastIndexOf("=") + 1);
            String orderTime = cssz[1].substring(cssz[1].lastIndexOf("=") + 1);
            String price = cssz[2].substring(cssz[2].lastIndexOf("=") + 1);
            String storeNo = cssz[3].substring(cssz[3].lastIndexOf("=") + 1);
//                if(jyxx==null) {
//                    String reason = "获取数据为空，开票失败，请重试!";
//                    weixinUtils.jujuekp(weixinOrderNo, reason, access_token);
//                    return "1";
//                }
                Map map = new HashMap<>();
                map.put("tqm",orderNo);
                map.put("je",price);
                map.put("gsdm",gsdm);
                Tqmtq tqmtq = tqmtqService.findOneByParams(map);
                Jyls jyls1 = jylsService.findOne(map);
                if (null != tqmtq && null != tqmtq.getId()) {
                    String reason="该订单已提交过申请!";
                    if(null!=sjly && "4".equals(sjly)){
                        weixinUtils.jujuekp(weixinOrderNo,reason,access_token);
                    }
                    return "1";
                }
                if (jyls1 != null) {
                    String reason="该订单正在开票!";
                    if(null!=sjly && "4".equals(sjly)){
                        weixinUtils.jujuekp(weixinOrderNo,reason,access_token);
                    }
                    return  "1";
                }
                //String orderNo = jyxx.getOrderNo();
                //String orderTime = jyxx.getOrderTime();
                //String price = jyxx.getPrice().toString();
                //String storeNo = jyxx.getStoreNo();
                Skp skp = skpJpaDao.findOneByKpddmAndGsdm(storeNo, "sqj");
                Integer xfid = skp.getXfid(); //销方id
                Xf xf = xfJpaDao.findOneById(xfid);
                Integer kpdid = skp.getId();//税控盘id(开票点id)
                Jyxxsq jyxxsq = new Jyxxsq();
                jyxxsq.setJshj(Double.valueOf(price));
                jyxxsq.setDdh(orderNo);
                jyxxsq.setGsdm("sqj");
                jyxxsq.setKpddm(storeNo);
                jyxxsq.setXfmc(xf.getXfmc());
                jyxxsq.setKpr(xf.getKpr());
                jyxxsq.setFhr(xf.getFhr());
                jyxxsq.setSkr(xf.getSkr());
                jyxxsq.setXfid(xfid);
                jyxxsq.setXfsh(xf.getXfsh());
                jyxxsq.setXfyhzh(xf.getXfyhzh());
                jyxxsq.setXfyh(xf.getXfyh());
                jyxxsq.setXfdh(xf.getXfdh());//销方电话
                jyxxsq.setXfdz(xf.getXfdz());//销方地址
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
                jyxxsq.setTqm(orderNo);

                List<Jymxsq> jymxsqList = new ArrayList<>();
                Jymxsq jymxsq = new Jymxsq();
                jymxsq.setJshj(Double.valueOf(price));
                Cszb cszb = cszbService.getSpbmbbh("sqj", xfid, kpdid, "dyspbmb");
                Map mapoo = new HashMap();
                mapoo.put("gsdm", "sqj");
                if (cszb.getCsz() != null) {
                    mapoo.put("spdm", cszb.getCsz());
                }
                Spvo spvo = spvoService.findOneSpvo(mapoo);
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
                jymxsqList.add(jymxsq);
                List<Jyzfmx> jyzfmxList = new ArrayList<>();
                String xml = GetXmlUtil.getFpkjXml(jyxxsq, jymxsqList, jyzfmxList);
                Gsxx oneByGsdm = gsxxJpaDao.findOneByGsdm("sqj");
                String appid = oneByGsdm.getAppKey();
                String key = oneByGsdm.getSecretKey();
                String resultxml = HttpUtils.HttpUrlPost(xml, appid, key);
                Map<String, Object> resultMap = XmlUtil.xml2Map(resultxml);
                String returnMsg = resultMap.get("ReturnMessage").toString();
                String returnCode = resultMap.get("ReturnCode").toString();
                Map map2 = new HashMap();
                map2.put("returnMsg", returnMsg);
                map2.put("returnCode", returnCode);
                map2.put("serialorder",  jyxxsq.getJylsh()+jyxxsq.getDdh());
                    if(null!=returnCode && returnCode.equals("9999")){
                        String reason="错误信息为"+returnMsg;
                        if(null!=sjly && "4".equals(sjly)){
                            weixinUtils.jujuekp(weixinOrderNo,reason,access_token);
                        }
                        return "1";
                    }else {
                        //插入表
                        Tqmtq tqmtq1 = new Tqmtq();
                        tqmtq1.setDdh(jyxxsq.getTqm());
                        tqmtq1.setLrsj(new Date());
                        tqmtq1.setZje(Double.valueOf(jyxxsq.getJshj()));
                        tqmtq1.setGfmc(gfmc);
                        tqmtq1.setNsrsbh(gfsh);
                        tqmtq1.setDz(gfdz);
                        tqmtq1.setDh(gfdh);
                        tqmtq1.setKhh(gfyh);
                        tqmtq1.setKhhzh(gfyhzh);
                        tqmtq1.setFpzt("0");
                        tqmtq1.setYxbz("1");
                        tqmtq1.setGfemail(email);
                        tqmtq1.setGsdm(jyxxsq.getGsdm());
                        if(openid != null && !"null".equals(openid)){
                            tqmtq1.setOpenid(openid);
                        }
                        tqmtqService.save(tqmtq1);
                        return  "0";
                    }
            } catch (Exception e) {
                e.printStackTrace();
            }
        return "0";

    }
    @Override
    public  String savaWxFpxx(String tqm,String gsdm,String q,String openid,String orderNo){
        String status="";
        //微信写入数据库
        WxFpxx wxFpxx = new WxFpxx();
        wxFpxx.setTqm(tqm);
        wxFpxx.setGsdm(gsdm);
        wxFpxx.setQ(q);
        wxFpxx.setOpenId(openid);
        wxFpxx.setOrderNo(orderNo);
        logger.info("存入数据提取码" + tqm + "----公司代码" + gsdm + "----q值" + q + "----openid" + openid + "------订单编号" + orderNo);
        try {
            wxfpxxJpaDao.save(wxFpxx);
            status="0";
        } catch (Exception e) {
            logger.info("交易信息保存失败");
            status ="-1";
        }
        return  status;
    }

    @Override
    public Map redirct(String tqm,String gsdm,String opendid,String visiterIP,String llqxx){
        Map resultMap = new HashMap();
        Map parm = new HashMap<>();
        parm.put("tqm", tqm);
        parm.put("gsdm", gsdm);
        Jyls jyls = jylsService.findOne(parm);
        List<Kpls> list = jylsService.findByTqm(parm);
        /**
         * 代表申请已完成开票,跳转最终开票页面
         */
        if (list.size() > 0) {
            if (opendid != null && !"null".equals(opendid)) {
                Map<String, Object> params = new HashMap<>();
                params.put("djh", jyls.getDjh());
                params.put("unionid", opendid);
                Fpj fpj = fpjService.findOneByParams(params);
                if (fpj == null) {
                    fpj = new Fpj();
                    fpj.setDjh(jyls.getDjh());
                    fpj.setUnionid(opendid);
                    fpj.setYxbz("1");
                    fpj.setLrsj(new Date());
                    fpj.setXgsj(new Date());
                    fpjService.save(fpj);
                }
            }
            resultMap.put("num", "2");
            resultMap.put("list",list);
            //已经开过票
            Tqjl tqjl = new Tqjl();
            tqjl.setDjh((String.valueOf(list.get(0).getDjh())));
            tqjl.setJlly("1");
            tqjl.setTqsj(new Date());
            tqjl.setIp(visiterIP);
            tqjl.setLlqxx(llqxx);
            tqjlService.save(tqjl);
        } else if (null != jyls && null != jyls.getDjh()) {

            resultMap.put("num", "6");
        } else {
            //跳转发票提取页面
            Cszb zb1 = cszbService.getSpbmbbh(gsdm, null,null, "sfdyjkhqkp");
            if(list.size()== 0 && null!=zb1.getCsz()&&!zb1.getCsz().equals("")){
                //需要调用接口获取开票信息
                System.out.println("start+++++++++++");
                //全家调用接口 解析xml
                if(null!=gsdm && gsdm.equals("family")){
                    resultMap=getDataService.getData(tqm,gsdm);
                }
                //绿地优鲜 解析json
                else if(parm.get("gsdm").equals("ldyx")){
                    System.out.println("ldyx+++++++++++++++++Strat");
                    //第一次请求url获取token 验证
                  Map  resultFirMap=getDataService.getldyxFirData(tqm,gsdm);
                  if(null!=resultFirMap.get("accessToken")){
                      Map  resultSecMap = getDataService.getldyxSecData(tqm,gsdm,(String) resultFirMap.get("accessToken"));
                      if(null!=resultSecMap.get("tmp")){
                          resultMap.put("num","12");
                          resultMap.put("msg",resultMap.get("tmp"));
                      }
                  }

                }
            }
            List<Jyxxsq> jyxxsqList=(List)resultMap.get("jyxxsqList");
            List<Jymxsq> jymxsqList=(List)resultMap.get("jymxsqList");
            List<Jyzfmx> jyzfmxList = (List) resultMap.get("jyzfmxList");

            resultMap.put("num","5");
            resultMap.put("tqm",tqm);
            resultMap.put("gsdm",gsdm);
        }
        return  resultMap;
    }
    @Override
    public String chamateYX(String gsdm, String gfmc, String gfsh, String email,
                              String gfyh, String gfyhzh, String gfdz, String gfdh,String openid,String sjly,String access_token,String weixinOrderNo) {
        String orderNo = weixinOrderNo;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String orderTime = sdf.format(new Date());
        String price = "10.0";
        String storeNo = "chamate_test";
        String spdm = "";
        if (StringUtils.isNotBlank(orderNo) &&
                StringUtils.isNotBlank(orderTime) &&
                StringUtils.isNotBlank(price) &&
                StringUtils.isNotBlank(storeNo) &&
                StringUtils.isNotBlank(gfmc)) {
            try {
                Skp skp = skpJpaDao.findOneByKpddmAndGsdm(storeNo,gsdm);
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
                if(null!=gfsh&&!"".equals(gfsh)){
                    jyxxsq.setGflx("1");
                }else {
                    jyxxsq.setGflx("0");
                }
                jyxxsq.setGfemail(email);
                jyxxsq.setGfdz(gfdz);
                jyxxsq.setGfdh(gfdh);
                jyxxsq.setGfyhzh(gfyhzh);
                jyxxsq.setGfyh(gfyh);
                jyxxsq.setJylsh(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())+ NumberUtil.getRandomLetter());
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
                jyxxsq.setTqm("ycyz"+orderNo);

                List<Jymxsq> jymxsqList = new ArrayList<>();
                if(price.indexOf(",")!=-1 && spdm.indexOf(",")!=-1){
//                    BigDecimal jyxxsqPrice = new BigDecimal("0");
                    Integer priceSize = RJCheckUtil.getSize(price, ",");
                    Integer spdmSize = RJCheckUtil.getSize(spdm, ",");
                    if(priceSize!=spdmSize){
                        return "-1";
                    }
                    String[] priceArray = price.split(",");
                    String[] spdmArray = spdm.split(",");
                    for (int i=0;i<priceSize+1;i++){
                        jyxxsq.setJshj(jyxxsq.getJshj()+Double.valueOf(priceArray[i]));
//                        jyxxsqPrice.add(new BigDecimal(priceArray[i]));
                        Jymxsq jymxsq = new Jymxsq();
                        jymxsq.setSpdm(spdmArray[i]);
                        jymxsq.setJshj(Double.valueOf(priceArray[i]));
                        jymxsqList.add(jymxsq);
                    }
//                    jyxxsq.setJshj(jyxxsqPrice.doubleValue());
                }else {
                    jyxxsq.setJshj(Double.valueOf(price));
                    Jymxsq jymxsq = new Jymxsq();
                    jymxsq.setSpdm(spdm);
                    jymxsq.setJshj(Double.valueOf(price));
                    jymxsqList.add(jymxsq);
                }
                for(Jymxsq jymxsq:jymxsqList){
                    Cszb cszb = cszbService.getSpbmbbh(gsdm, xfid, kpdid, "dyspbmb");
                    Map map = new HashMap();
                    map.put("gsdm", gsdm);
                    if(cszb.getCsz()!=null){
                        map.put("spdm", cszb.getCsz());
                    }else {
                        //如果没有参数，则表明是传商品代码,此时交易明细申请中税收分类编码暂时放商品编码
                        if(StringUtils.isNotBlank(jymxsq.getSpdm())){
                            map.put("spdm", jymxsq.getSpdm());
                        }else{
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

                String xml = GetXmlUtil.getFpkjXml(jyxxsq, jymxsqs,jyzfmxList);
                Gsxx oneByGsdm = gsxxJpaDao.findOneByGsdm(gsdm);
                String appid = oneByGsdm.getAppKey();
                String key = oneByGsdm.getSecretKey();
                String resultxml = HttpUtils.HttpUrlPost(xml, appid, key);
                String json = "";
                try {
                    Map<String, Object> resultMap = XmlUtil.xml2Map(resultxml);
                    String returnMsg=resultMap.get("ReturnMessage").toString();
                    String returnCode=resultMap.get("ReturnCode").toString();
                    Map map2 = new HashMap();
                    map2.put("returnMsg", returnMsg);
                    map2.put("returnCode", returnCode);
                    map2.put("serialorder",  jyxxsq.getJylsh()+jyxxsq.getDdh());
                    json=JSONObject.toJSONString(map2);
                    if(null!=returnCode && "9999".equals(returnCode)){
                        logger.info("进入拒绝开票-----错误原因为"+returnMsg);
                        String reason= returnMsg;
                        if(null!= sjly && "4".equals(sjly)){
                            logger.info("进行拒绝开票的weixinOrderN+++++"+weixinOrderNo);
                            String str=  weixinUtils.jujuekp(weixinOrderNo,reason,access_token);
                        }
                        return "-1";
                    }

                }catch (Exception e){
                    String serialorder=resultxml;
                    Kpls oneBySerialorder = kplsJpaDao.findOneBySerialorder(serialorder);
                    String fphm=oneBySerialorder.getFphm();
                    String fpdm = oneBySerialorder.getFpdm();
                    if(fphm==null || fpdm==null){
                        return "-1";
                    }
                    Map map3 = new HashMap();
                    map3.put("fphm", fphm);
                    map3.put("fpdm", fpdm);
                    map3.put("serialorder", jyxxsq.getJylsh()+jyxxsq.getDdh());
                    json=JSONObject.toJSONString(map3);
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
