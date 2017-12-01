package com.rjxx.taxeasy.wechat.task;

import com.rjxx.taxeasy.bizcomm.utils.GetDataService;
import com.rjxx.taxeasy.dao.WxfpxxJpaDao;
import com.rjxx.taxeasy.domains.*;
import com.rjxx.taxeasy.service.*;
import com.rjxx.utils.weixin.WeixinUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                    resultSjMap = getDataService.getData(tqm, "Family");
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
            }else if (null != gsdm && (gsdm.equals("chamate") || "dicos".equals(gsdm))) {
                logger.info("一茶一坐开票---德克士---------");
                try {
                    barcodeService.makeInvoice(gsdm, q, (String) resultMap.get("title"), (String) resultMap.get("tax_no"),
                            (String) resultMap.get("email"), (String) resultMap.get("bank_type"), (String) resultMap.get("bank_no"),
                            (String) resultMap.get("addr"), (String) resultMap.get("phone"), tqm, openid, "4",access_token,SuccOrderId);
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
                weixinUtils.jujuekp(FailOrderId,re,access_token);
            }
        }else if(null!=wxFpxx.getWxtype() && "2".equals(wxFpxx.getWxtype())) {
            logger.info("进入领取发票类型---2------------直接插入卡包");
            WxFpxx wxFpxxIncard = wxfpxxJpaDao.selsetByOrderNo(SuccOrderId);
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
}
