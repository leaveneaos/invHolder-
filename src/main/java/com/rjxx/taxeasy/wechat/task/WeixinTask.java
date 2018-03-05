package com.rjxx.taxeasy.wechat.task;

import com.alibaba.fastjson.JSON;
import com.rjxx.taxeasy.bizcomm.utils.GetDataService;
import com.rjxx.taxeasy.dao.PpJpaDao;
import com.rjxx.taxeasy.dao.WxfpxxJpaDao;
import com.rjxx.taxeasy.dao.XfJpaDao;
import com.rjxx.taxeasy.domains.*;
import com.rjxx.taxeasy.service.*;
import com.rjxx.utils.weixin.WechatBatchCard;
import com.rjxx.utils.weixin.WeixinUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by Administrator on 2017-12-01.
 */
public class WeixinTask implements Runnable{

    private static Logger logger = LoggerFactory.getLogger(WeixinUtils.class);

    private Thread id;

    private WxFpxx wxFpxx;

    private Map resultMap;

    private String access_token;

    private GsxxService gsxxService;

    private String SuccOrderId;

    private String openid;

    private GetDataService getDataService;

    private CszbService cszbService;

    private WeixinUtils weixinUtils;

    private String FailOrderId;

    private WxfpxxJpaDao wxfpxxJpaDao;

    private KplsService kplsService;

    private KpspmxService kpspmxService;

    private String pdf_file_url;

    private BarcodeService barcodeService;

    private XfService xfService;

    private SkpService skpService;

    private String authid;

    private WechatBatchCard wechatBatchCard;

    private PpJpaDao ppJpaDao;

    private XfJpaDao xfJpaDao;

    @Override
    public void run() {
        String gsdm = wxFpxx.getGsdm();
        String q = wxFpxx.getQ();
        String tqm = wxFpxx.getTqm();
        if(null!=wxFpxx.getWxtype() && "1".equals(wxFpxx.getWxtype())){
            logger.info("进入申请开票类型------1------------开始开票");
            if (null != gsdm && (gsdm.equals("Family")|| "bqw".equals(gsdm) || "ldyx".equals(gsdm)||"gvc".equals(gsdm))) {
                Map parms = new HashMap();
                parms.put("gsdm", gsdm);
                Gsxx gsxx = gsxxService.findOneByParams(parms);
                Map resultSjMap = new HashMap();
                if("Family".equals(gsdm)){
                    logger.info("进入全家开票处理---------");
//                    if(tqm.indexOf("RJ")<0){
                        resultSjMap = getDataService.getData(tqm, "Family");
//                    }else {
//                        resultSjMap = sj(tqm);
//                        logger.info("进入测试开票----写死的封装的数据"+ JSON.toJSONString(resultSjMap));
//                    }
                }else if("bqw".equals(gsdm)){
                    logger.info("波奇网开票-------");
                    Cszb  zb1 =  cszbService.getSpbmbbh(gsxx.getGsdm(), null,null, "sfhhurl");
                    resultSjMap = getDataService.getDataForBqw(tqm, gsxx.getGsdm(),zb1.getCsz());
                }else if("ldyx".equals(gsdm)){
                    logger.info("绿地优鲜微信开票-------");
                    Map MapldyxToken = getDataService.getldyxFirData(tqm,gsdm);
                    String accessToken = (String) MapldyxToken.get("accessToken");
                    if(null == accessToken){
                        String re = "发票开具失败，请重试！";
                        weixinUtils.jujuekp(SuccOrderId, re, access_token);
                        return ;
                    }
                    resultSjMap = getDataService.getldyxSecData(tqm,gsdm,accessToken);
                }else if("gvc".equals(gsdm)){
                    logger.info("进入光唯尚微信开票---------");
                    Cszb  csz =  cszbService.getSpbmbbh(gsdm, null,null, "sfhhurl");
                    resultSjMap = getDataService.getDataForGvc(tqm, gsdm, csz.getCsz());
                    String msg = (String) resultSjMap.get("msg");
                    if(msg!=null){
                        String re = msg;
                        weixinUtils.jujuekp(SuccOrderId, re, access_token);
                        return ;
                    }
                }
                try {
                    barcodeService.pullInvioce(resultSjMap, gsdm, (String) resultMap.get("title"),
                            (String) resultMap.get("tax_no"), (String) resultMap.get("email"), (String) resultMap.get("bank_type")
                            , (String) resultMap.get("bank_no"), (String) resultMap.get("addr"), (String) resultMap.get("phone"),
                            tqm, openid, "4", access_token, gsxx.getAppKey(), gsxx.getSecretKey(),SuccOrderId);
                } catch (Exception e) {
                    String re = "发票开具失败，请重试！";
                    weixinUtils.jujuekp(SuccOrderId, re, access_token);
                    e.printStackTrace();
                }
                return ;
            }else if (null != gsdm && (gsdm.equals("chamate") || "dicos".equals(gsdm)||"beautyfarm".equals(gsdm))) {
                logger.info("一茶一坐开票---德克士---------");
                try {
                    logger.info("进入全家开票处理---------");
//                    if(tqm.indexOf("RJ")<0){
                        barcodeService.makeInvoice(gsdm, q, (String) resultMap.get("title"), (String) resultMap.get("tax_no"),
                                (String) resultMap.get("email"), (String) resultMap.get("bank_type"), (String) resultMap.get("bank_no"),
                                (String) resultMap.get("addr"), (String) resultMap.get("phone"), tqm, openid, "4",access_token,SuccOrderId);
//                    }else {
//                        barcodeService.chamateYX(gsdm,(String) resultMap.get("title"), (String) resultMap.get("tax_no"),
//                                (String) resultMap.get("email"), (String) resultMap.get("bank_type"), (String) resultMap.get("bank_no"),
//                                (String) resultMap.get("addr"), (String) resultMap.get("phone"), openid, "4",access_token,SuccOrderId);
//                    }

                } catch (Exception e) {
                    String re = "发票开具失败，请重试！";
                    weixinUtils.jujuekp(SuccOrderId, re, access_token);
                    e.printStackTrace();
                }
                return ;
            }else if(null!=gsdm && gsdm.equals("sqj")){
                logger.info("食其家微信开票----");
                try {
                    barcodeService.sqjInvioce(q,"sqj",(String) resultMap.get("title"), (String) resultMap.get("tax_no"),(String) resultMap.get("email"), (String) resultMap.get("bank_type"), (String) resultMap.get("bank_no"),
                            (String) resultMap.get("addr"), (String) resultMap.get("phone"), tqm, openid, "4",access_token,SuccOrderId);
                } catch (Exception e) {
                    String re = "发票开具失败，请重试！";
                    weixinUtils.jujuekp(SuccOrderId, re, access_token);
                    e.printStackTrace();
                }
            }else {
                logger.info("------没有该公司的开票处理，---------公司为"+gsdm);
                String re = "发票开具异常,请联系商家！";
                weixinUtils.jujuekp(SuccOrderId,re,access_token);
            }
        }else if(null!=wxFpxx.getWxtype() && "2".equals(wxFpxx.getWxtype())) {
            if(authid!=null && !authid.equals("")){
                logger.info("领取发票类型---2--批量插卡");
                String spappid = weixinUtils.getSpappid(access_token);
                WxFpxx wxFpxxByAuthid = wxfpxxJpaDao.selectByAuthId(authid);
                if(wxFpxxByAuthid==null){
                    String re = "插入卡包失败！";
                    wechatBatchCard.batchRefuseKp(authid,re,access_token,spappid);
                }
                if(wxFpxxByAuthid.getKplsh()!=null && wxFpxxByAuthid.getKplsh().indexOf(",")>=0){
                    logger.info("多个开票流水号");
                    String[] splitKplsh = wxFpxxByAuthid.getKplsh().split(",");
                    List<Kpls> kplsList = new ArrayList();
                    String card_id ="";
                    try {
                        for(int i = 0 ; i< splitKplsh.length;i++){
                            Map map = new HashMap();
                            map.put("gsdm" , wxFpxxByAuthid.getGsdm());
                            map.put("kplsh",splitKplsh[i]);
                            Kpls kpls = kplsService.findOneByParams(map);
                           kplsList.add(kpls);
                            if (null == kpls.getSkpid()) {
                                logger.info("税控盘id为空！");
                                return ;
                            }
                            Map skpMap = new HashMap();
                            skpMap.put("kpdid", kpls.getSkpid());
                            Skp skp = skpService.findOneByParams(skpMap);
                            if (null == skp.getPid()) {
                                logger.info("pid 为空----");
                                return ;
                            }
                            Pp pp = ppJpaDao.findOneById(skp.getPid());
                            Xf xf = xfJpaDao.findOneById(skp.getXfid());
                           if(xf.getWechatCardId()==null  ||xf.getWechatCardId().equals("")){
                               logger.info("模板card_id 没有");
                               card_id = weixinUtils.creatMb(pp.getPpmc(), kpls.getXfmc(), pp.getWechatLogoUrl(),access_token);
                               xf.setWechatCardId(card_id);
                               xfJpaDao.save(xf);
                               //防止生成卡包模板和插卡时间间隔过短
                               //Thread.sleep(300000);
                           }
                           card_id=xf.getWechatCardId();
                        }
                        logger.info("传入的card_id"+card_id);
                        wechatBatchCard.batchDZFPInCard(authid,card_id,pdf_file_url,kplsList,access_token);
                    } catch (Exception e) {
                        String re = "插入卡包失败！";
                        wechatBatchCard.batchRefuseKp(authid,re,access_token,spappid);
                        e.printStackTrace();
                    }
                    return;
                }
            }
            logger.info("领取发票类型---2--------直接插入卡包-单张");
            WxFpxx wxFpxxIncard = wxfpxxJpaDao.selectByWeiXinOrderNo(SuccOrderId);
            if (null == wxFpxxIncard) {
                String re = "插入卡包失败！";
                weixinUtils.jujuekp(SuccOrderId, re, access_token);
            } else {
                if (null == wxFpxxIncard.getCode() || "".equals(wxFpxxIncard.getCode())) {
                    logger.info("进入插卡方法-----");
                    //没有插入过卡包
                    Map kplsMap = new HashMap();
                    kplsMap.put("kplsh", wxFpxxIncard.getKplsh());
                    Kpls kpls = kplsService.findOneByParams(kplsMap);
                    if (null == kpls) {
                        String re = "开票数据为空，插入卡包失败！";
                        weixinUtils.jujuekp(SuccOrderId, re, access_token);
                        return ;
                    }
                    Map params2 = new HashMap();
                    params2.put("kplsh", wxFpxxIncard.getKplsh());
                    List<Kpspmx> kpspmxList = kpspmxService.findMxNewList(params2);
                    if (null == kpspmxList) {
                        String re = "商品明细为空，插入卡包失败！";
                        weixinUtils.jujuekp(SuccOrderId, re, access_token);
                        return ;
                    }
                    try {
                        //插入卡包
                        weixinUtils.fpInsertCardBox(SuccOrderId, pdf_file_url, kpspmxList, kpls);
                    } catch (Exception e) {
                        String re = "插入卡包失败！";
                        weixinUtils.jujuekp(SuccOrderId, re, access_token);
                        e.printStackTrace();
                    }
                    return ;
                }
            }
        }
        return;
    }

    public WxFpxx getWxFpxx() {
        return wxFpxx;
    }

    public void setWxFpxx(WxFpxx wxFpxx) {
        this.wxFpxx = wxFpxx;
    }

    public Map getResultMap() {
        return resultMap;
    }

    public void setResultMap(Map resultMap) {
        this.resultMap = resultMap;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public GsxxService getGsxxService() {
        return gsxxService;
    }

    public void setGsxxService(GsxxService gsxxService) {
        this.gsxxService = gsxxService;
    }

    public String getSuccOrderId() {
        return SuccOrderId;
    }

    public void setSuccOrderId(String succOrderId) {
        SuccOrderId = succOrderId;
    }

    public GetDataService getGetDataService() {
        return getDataService;
    }

    public void setGetDataService(GetDataService getDataService) {
        this.getDataService = getDataService;
    }

    public CszbService getCszbService() {
        return cszbService;
    }

    public void setCszbService(CszbService cszbService) {
        this.cszbService = cszbService;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public WeixinUtils getWeixinUtils() {
        return weixinUtils;
    }

    public void setWeixinUtils(WeixinUtils weixinUtils) {
        this.weixinUtils = weixinUtils;
    }

    public String getFailOrderId() {
        return FailOrderId;
    }

    public void setFailOrderId(String failOrderId) {
        FailOrderId = failOrderId;
    }

    public WxfpxxJpaDao getWxfpxxJpaDao() {
        return wxfpxxJpaDao;
    }

    public void setWxfpxxJpaDao(WxfpxxJpaDao wxfpxxJpaDao) {
        this.wxfpxxJpaDao = wxfpxxJpaDao;
    }

    public KplsService getKplsService() {
        return kplsService;
    }

    public void setKplsService(KplsService kplsService) {
        this.kplsService = kplsService;
    }

    public KpspmxService getKpspmxService() {
        return kpspmxService;
    }

    public void setKpspmxService(KpspmxService kpspmxService) {
        this.kpspmxService = kpspmxService;
    }

    public String getPdf_file_url() {
        return pdf_file_url;
    }

    public void setPdf_file_url(String pdf_file_url) {
        this.pdf_file_url = pdf_file_url;
    }

    public BarcodeService getBarcodeService() {
        return barcodeService;
    }

    public void setBarcodeService(BarcodeService barcodeService) {
        this.barcodeService = barcodeService;
    }

    public Thread getId() {
        return id;
    }

    public void setId(Thread id) {
        this.id = id;
    }

    public XfService getXfService() {
        return xfService;
    }

    public void setXfService(XfService xfService) {
        this.xfService = xfService;
    }

    public SkpService getSkpService() {
        return skpService;
    }

    public void setSkpService(SkpService skpService) {
        this.skpService = skpService;
    }

    public String getAuthid() {
        return authid;
    }

    public void setAuthid(String authid) {
        this.authid = authid;
    }

    public WechatBatchCard getWechatBatchCard() {
        return wechatBatchCard;
    }

    public void setWechatBatchCard(WechatBatchCard wechatBatchCard) {
        this.wechatBatchCard = wechatBatchCard;
    }

    public PpJpaDao getPpJpaDao() {
        return ppJpaDao;
    }

    public void setPpJpaDao(PpJpaDao ppJpaDao) {
        this.ppJpaDao = ppJpaDao;
    }

    public XfJpaDao getXfJpaDao() {
        return xfJpaDao;
    }

    public void setXfJpaDao(XfJpaDao xfJpaDao) {
        this.xfJpaDao = xfJpaDao;
    }

    private Map sj (String str){
        Map resultSjMap = new HashMap();
        List<Jyxxsq> jyxxsqList = new ArrayList();
        List<Jymxsq> jymxsqList = new ArrayList();
        List<Jyzfmx> jyzfmxList = new ArrayList<Jyzfmx>();
        Xf x = new Xf();
        x.setGsdm("Family");
        x.setXfsh("500102010003697");
        Xf xf = xfService.findOneByParams(x);
        Map params=new HashMap();
        params.put("xfid",687);
        Skp skp=skpService.findOneByParams(params);
        Jyxxsq jyxxsq= new Jyxxsq();
        jyxxsq.setBz("交易小票号:10672360;商品折扣金额:0.00;支付折扣:0.00;店名:永新汇店");
        jyxxsq.setClztdm("00");
        jyxxsq.setDdh(str);
        jyxxsq.setDdrq(new Date());
        jyxxsq.setFpczlxdm("11");
        jyxxsq.setFpzldm("12");
        jyxxsq.setGsdm("Family");
        jyxxsq.setHsbz("1");
        jyxxsq.setJshj(19.9);
        jyxxsq.setJylsh(str);
        jyxxsq.setKpddm("family_test");
        jyxxsq.setKpr(xf.getKpr());
        jyxxsq.setLrry(95);
        jyxxsq.setLrsj(new Date());
        jyxxsq.setQjzk(0.0);
        jyxxsq.setSjly("4");
        jyxxsq.setSkr(xf.getSkr());
        jyxxsq.setTqm(str);
        jyxxsq.setXfdh(xf.getXfdh());
        jyxxsq.setXfdz(xf.getXfdz());
        jyxxsq.setXfid(xf.getId());
        jyxxsq.setXfmc(xf.getXfmc());
        jyxxsq.setXfsh("500102010003697");
        jyxxsq.setXfyh(xf.getXfyh());
        jyxxsq.setXfyhzh(xf.getXfyhzh());
        jyxxsq.setFhr(xf.getFhr());
        jyxxsq.setYkpjshj(0.0);
        jyxxsq.setYxbz("1");
        jyxxsq.setZsfs("0");
        jyxxsqList.add(jyxxsq);

        Jymxsq jymxsq1= new Jymxsq();
        jymxsq1.setDdh(str);
        jymxsq1.setFphxz("0");
        jymxsq1.setGsdm("Family");
        jymxsq1.setHsbz("1");
        jymxsq1.setJshj(9.9);
        jymxsq1.setKce(0.0);
        jymxsq1.setKkjje(9.9);
        jymxsq1.setLrry(95);
        jymxsq1.setLrsj(new Date());
        jymxsq1.setSpdj(9.9);
        jymxsq1.setSpdm("1030203030000000000");
        jymxsq1.setSpdw("");
        jymxsq1.setSpje(9.9);
        jymxsq1.setSpmc("综合寿司组合");
        jymxsq1.setSpmxxh(1);
        jymxsq1.setSps(1.0);
        jymxsq1.setSpsl(0.17);
        jymxsq1.setSpzxbm("20023409");
        jymxsq1.setYhzcbs("0");
        jymxsq1.setYkjje(0.0);
        jymxsq1.setYxbz("1");
        jymxsqList.add(jymxsq1);

        Jymxsq jymxsq2= new Jymxsq();
        jymxsq2.setDdh(str);
        jymxsq2.setFphxz("0");
        jymxsq2.setGsdm("Family");
        jymxsq2.setHsbz("1");
        jymxsq2.setJshj(10.0);
        jymxsq2.setKce(0.0);
        jymxsq2.setKkjje(10.0);
        jymxsq2.setLrry(95);
        jymxsq2.setLrsj(new Date());
        jymxsq2.setSpdj(10.0);
        jymxsq2.setSpdm("1030203030000000000");
        jymxsq2.setSpdw("");
        jymxsq2.setSpje(10.0);
        jymxsq2.setSpmc("奥尔良手枪腿");
        jymxsq2.setSpmxxh(2);
        jymxsq2.setSps(1.0);
        jymxsq2.setSpsl(0.17);
        jymxsq2.setSpzxbm("20473235");
        jymxsq2.setYhzcbs("0");
        jymxsq2.setYkjje(0.0);
        jymxsq2.setYxbz("1");
        jymxsqList.add(jymxsq2);
        //Jyzfmx jyzfmx = new Jyzfmx();
        //jyzfmxList.add(jyzfmx);
        resultSjMap.put("jyxxsqList",jyxxsqList);
        resultSjMap.put("jymxsqList",jymxsqList);
        resultSjMap.put("jyzfmxList",jyzfmxList);

        return  resultSjMap;
    }
}
