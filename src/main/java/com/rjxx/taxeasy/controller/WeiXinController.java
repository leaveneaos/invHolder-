package com.rjxx.taxeasy.controller;

import com.alibaba.druid.sql.visitor.functions.Lpad;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rjxx.taxeasy.bizcomm.utils.GetDataService;
import com.rjxx.taxeasy.bizcomm.utils.GetXmlUtil;
import com.rjxx.taxeasy.bizcomm.utils.HttpUtils;
import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.comm.SigCheck;
import com.rjxx.taxeasy.dao.WxTokenJpaDao;
import com.rjxx.taxeasy.dao.WxfpxxJpaDao;
import com.rjxx.taxeasy.domains.*;
import com.rjxx.taxeasy.service.*;
import com.rjxx.taxeasy.wechat.util.ResultUtil;
import com.rjxx.utils.weixin.WeiXinConstants;
import com.rjxx.utils.weixin.WeixinUtils;
import com.rjxx.utils.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by Administrator on 2017-06-26.
 */
@RestController

public class WeiXinController extends BaseController {

    @Autowired
    private KplsService kplsService;

    @Autowired
    private GetDataService getDataService;

    @Autowired
    private KpspmxService kpspmxService;

    @Autowired
    private  BarcodeService barcodeService;

    @Autowired
    private WxfpxxJpaDao wxfpxxJpaDao;

    @Autowired
    private WeixinUtils weixinUtils;

    @Autowired
    private  GsxxService gsxxService;

    @Autowired
    private WxTokenJpaDao wxTokenJpaDao;

    @Autowired
    private CszbService cszbService;

    @Autowired
    private JyxxService jyxxService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${rjxx.pdf_file_url:}")
    private String pdf_file_url;

    /**
     * 获取微信授权回调
     */
    @RequestMapping(value = WeiXinConstants.AFTER_WEIXIN_REDIRECT_URL,method = RequestMethod.GET)
    public void getWeiXin() throws Exception {
        System.out.println("进入回调验证token");
        //响应token
        String sign = request.getParameter("signature");
        String times = request.getParameter("timestamp");
        String nonce = request.getParameter("nonce");
        String echo = request.getParameter("echostr");
        if (SigCheck.checkSignature(sign, times, nonce)) {
            try {
                response.getOutputStream().print(request.getParameter("echostr"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            logger.info("isSuccess:" + echo);
        }
    }

    /**
     * 接收微信事件推送
     * @return
     * @throws Exception
     */
    @RequestMapping(value = WeiXinConstants.AFTER_WEIXIN_REDIRECT_URL,method = RequestMethod.POST)
    public String postWeiXin() throws Exception {
        Map<String, String> requestMap = null;
        try {
            requestMap = parseXml(request);
            logger.info("接收微信返回xml变map"+requestMap.toString());
            //处理微信推送事件： 微信授权完成事件推送
            if(requestMap.get("MsgType").equals("event")&&requestMap.get("Event").equals("user_authorize_invoice")){
                String SuccOrderId = requestMap.get("SuccOrderId");//微信回传成功的order_id
                String FailOrderId = requestMap.get("FailOrderId");//失败的order_id
                String openid = requestMap.get("FromUserName");    //opendid
                logger.info("拿到的opedid是---------"+openid);
                String createTime = requestMap.get("CreateTime");
                logger.info("排重前"+createTime+openid);
                //判断排重
                boolean paichongResult = this.paichong(createTime, openid);
                if(!paichongResult){
                    logger.info("该请求已接收过");
                    return "";
                }
                String access_token="";
                WxToken wxToken = wxTokenJpaDao.findByFlag("01");
                if(wxToken == null){
                    access_token = (String)weixinUtils.hqtk().get("access_token");
                }else {
                    access_token = wxToken.getAccessToken();
                }
                if(null!=SuccOrderId &&!SuccOrderId.equals("")){
                    System.out.println("拿到成功的订单id了");
                    //原始订单 ----- 开票平台的订单
                    String orderno_old="";
                    //拒绝之后的订单 --- 传给微信的订单---weixinorderno
                    String orderno_new="";
                    //int i = SuccOrderId.indexOf("-");
                    //if(i<0){
                    //    logger.info("没有-，表示没有拒绝过开票");
                    //    orderno_old=SuccOrderId;
                    //    orderno_new=SuccOrderId;
                    //}else {
                    //    logger.info("表示拒绝过开票");
                    //    orderno_new = SuccOrderId;
                    //    String[] split = SuccOrderId.split("-");
                    //    orderno_old = split[0];
                    //}

                    WxFpxx oneByOrderNo = wxfpxxJpaDao.selectByWeiXinOrderNo(SuccOrderId);
                    if(null==oneByOrderNo){
                        String re = "发票开具失败，请重试！";
                        weixinUtils.jujuekp(SuccOrderId, re, access_token);
                    }
                    String gsdm = oneByOrderNo.getGsdm();
                    String q = oneByOrderNo.getQ();
                    String tqm = oneByOrderNo.getTqm();

                    if(null!=oneByOrderNo.getWxtype() && "1".equals(oneByOrderNo.getWxtype())){
                        logger.info("进入申请开票类型------------开始开票");
                        Map resultMap =  weixinUtils.zdcxstatus(orderno_new,access_token);
                        if(null==resultMap){
                            String re = "发票开具失败，请重试！";
                            weixinUtils.jujuekp(SuccOrderId, re, access_token);
                            return "";
                        }else {
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
                                            return "";
                                        }
                                        resultSjMap = getDataService.getldyxSecData(tqm,gsdm,accessToken);
                                    }else if("gvc".equals(gsdm)){
                                        logger.info("进入光唯尚微信开票---------");
                                        Cszb  csz =  cszbService.getSpbmbbh(gsdm, null,null, "sfhhurl");
                                        resultMap = getDataService.getDataForGvc(tqm, gsdm, csz.getCsz());
                                        String msg = (String) resultMap.get("msg");
                                        if(msg!=null){
                                            String re = msg;
                                            weixinUtils.jujuekp(SuccOrderId, re, access_token);
                                            return "";
                                        }
                                    }
                                    try {
                                        barcodeService.pullInvioce(resultSjMap, gsdm, (String) resultMap.get("title"),
                                               (String) resultMap.get("tax_no"), (String) resultMap.get("email"), (String) resultMap.get("bank_type")
                                               , (String) resultMap.get("bank_no"), (String) resultMap.get("addr"), (String) resultMap.get("phone"),
                                               tqm, openid, "4", access_token, gsxx.getAppKey(), gsxx.getSecretKey(),orderno_new);
                                    } catch (Exception e) {
                                        String re = "发票开具失败，请重试！";
                                        weixinUtils.jujuekp(SuccOrderId, re, access_token);
                                        e.printStackTrace();
                                    }
                                    return "";
                                }else if (null != gsdm && (gsdm.equals("chamate") || "dicos".equals(gsdm))) {
                                    logger.info("一茶一坐开票---------");
                                    try {
                                        barcodeService.makeInvoice(gsdm, q, (String) resultMap.get("title"), (String) resultMap.get("tax_no"),
                                                (String) resultMap.get("email"), (String) resultMap.get("bank_type"), (String) resultMap.get("bank_no"),
                                                (String) resultMap.get("addr"), (String) resultMap.get("phone"), tqm, openid, "4",access_token,SuccOrderId);
                                    } catch (Exception e) {
                                        String re = "发票开具失败，请重试！";
                                        weixinUtils.jujuekp(SuccOrderId, re, access_token);
                                        e.printStackTrace();
                                    }
                                    return "";
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
                            }
                        }
                        if(null!=oneByOrderNo.getWxtype() && "2".equals(oneByOrderNo.getWxtype())) {
                            logger.info("进入领取发票类型------------直接插入卡包");
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
                                        return "";
                                    }
                                    Map params2 = new HashMap();
                                    params2.put("kplsh", wxFpxxIncard.getKplsh());
                                    List<Kpspmx> kpspmxList = kpspmxService.findMxNewList(params2);
                                    if (null == kpspmxList) {
                                        String re = "商品明细为空，插入卡包失败！";
                                        weixinUtils.jujuekp(SuccOrderId, re, access_token);
                                        return "";
                                    }
                                    try {
                                        //插入卡包
                                        weixinUtils.fpInsertCardBox(SuccOrderId, pdf_file_url, kpspmxList, kpls);
                                    } catch (Exception e) {
                                        String re = "插入卡包失败！";
                                        weixinUtils.jujuekp(SuccOrderId, re, access_token);
                                        e.printStackTrace();
                                    }
                                    return "";
                                }
                            }
                        }
                }
                if(null!=FailOrderId && !FailOrderId.equals("")){
                    String re = "订单"+FailOrderId+"的发票开具异常,请联系商家！";
                    weixinUtils.jujuekp(FailOrderId,re,access_token);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "" ;
    }

    /**
     * 解析微信推送消息xml
     * @param request
     * @return
     * @throws Exception
     */
    public Map<String, String> parseXml(HttpServletRequest request) throws Exception {
        // 将解析结果存储在HashMap中
        Map<String, String> map = new HashMap<String, String>();
        // 从request中取得输入流
        InputStream inputStream = request.getInputStream();
        // 读取输入流
        SAXReader reader = new SAXReader();
        Document document = reader.read(inputStream);
        String requestXml = document.asXML();
        String subXml = requestXml.split(">")[0] + ">";

        requestXml = requestXml.substring(subXml.length());
        // 得到xml根元素
        Element root = document.getRootElement();
        // 得到根元素的全部子节点
        List<Element> elementList = root.elements();
        // 遍历全部子节点
        for (Element e : elementList) {
            map.put(e.getName(), e.getText());
        }
        map.put("requestXml", requestXml);
        // 释放资源
        inputStream.close();
        inputStream = null;
        return map;

    }

    /**
     * 排重
     */
    private static final int MESSAGE_CACHE_SIZE = 1000;
    private static List<String> cacheList = new ArrayList<>(MESSAGE_CACHE_SIZE);
    public boolean paichong(String createTime, String fromUserName) {
        String flag = createTime + fromUserName;
        if (cacheList.contains(flag)) {
            logger.info("cacheList里已存在"+flag);
            return false;
        }
        if(cacheList.size()>=MESSAGE_CACHE_SIZE){
            cacheList.remove(0);
        }
        cacheList.add(flag);
        return true;
    }
}
