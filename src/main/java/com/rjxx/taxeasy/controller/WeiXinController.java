package com.rjxx.taxeasy.controller;

import com.rjxx.taxeasy.bizcomm.utils.GetDataService;
import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.comm.SigCheck;
import com.rjxx.taxeasy.dao.*;
import com.rjxx.taxeasy.domains.WxFpxx;
import com.rjxx.taxeasy.domains.WxToken;
import com.rjxx.taxeasy.service.*;
import com.rjxx.taxeasy.service.adapter.AdapterService;
import com.rjxx.taxeasy.wechat.task.WeixinTask;
import com.rjxx.utils.weixin.WechatBatchCard;
import com.rjxx.utils.weixin.WeiXinConstants;
import com.rjxx.utils.weixin.WeixinUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private XfService xfService;

    @Autowired
    private SkpService skpService;

    @Autowired
    private WechatBatchCard wechatBatchCard;

    @Autowired
    private PpJpaDao ppJpaDao;

    @Autowired
    private XfJpaDao xfJpaDao;

    @Autowired
    private AdapterService adapterService;

    @Autowired
    private SkpJpaDao skpJpaDao;

    @Autowired
    private JylsJpaDao jylsJpaDao;

    @Autowired
    private KplsJpaDao kplsJpaDao;

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
                String BatchInsert = requestMap.get("BatchInsert");
                logger.info("批量插卡类型BatchInsert为"+BatchInsert);
                if(BatchInsert!=null && BatchInsert.equals("1")){
                    logger.info("一次授权批量插卡处理");
                    String authid = requestMap.get("SuccOrderId");
                    String spappid = weixinUtils.getSpappid(access_token);
                    WxFpxx wxFpxx = wxfpxxJpaDao.selectByAuthId(authid);
                    if(wxFpxx == null){
                        //拒绝开票
                        String re ="批量插卡失败";
                        wechatBatchCard.batchRefuseKp(authid, re, access_token, spappid);
                        return  "";
                    }
                    //主动查询授权状态
                    Map resultMap =  wechatBatchCard.batchZDCXstatus(authid,access_token,spappid);
                    if(resultMap !=null && resultMap.get("msg").equals("72038")){
                        //没有授权
                        String re ="批量插卡失败";
                        wechatBatchCard.batchRefuseKp(authid, re, access_token, spappid);
                        return  "";
                    }
                    WeixinTask weixinTask = new WeixinTask();
                    weixinTask.setWxFpxx(wxFpxx);
                    weixinTask.setAccess_token(access_token);
                    weixinTask.setAuthid(authid);
                    weixinTask.setOpenid(openid);
                    weixinTask.setResultMap(resultMap);
                    weixinTask.setWeixinUtils(weixinUtils);
                    weixinTask.setFailOrderId(FailOrderId);
                    weixinTask.setWxfpxxJpaDao(wxfpxxJpaDao);
                    weixinTask.setKplsService(kplsService);
                    weixinTask.setKpspmxService(kpspmxService);
                    weixinTask.setPdf_file_url(pdf_file_url);
                    weixinTask.setWechatBatchCard(wechatBatchCard);
                    weixinTask.setSkpService(skpService);
                    weixinTask.setPpJpaDao(ppJpaDao);
                    weixinTask.setXfJpaDao(xfJpaDao);
                    weixinTask.setAdapterService(adapterService);
                    weixinTask.setSkpJpaDao(skpJpaDao);
                    weixinTask.setJylsJpaDao(jylsJpaDao);
                    weixinTask.setKplsJpaDao(kplsJpaDao);
                    Thread thread = new Thread(weixinTask);
                    thread.start();
                    return "";
                }
                if(null!=SuccOrderId &&!SuccOrderId.equals("")){
                    logger.info("拿到微信回传的订单编号为"+SuccOrderId);
                    WxFpxx wxFpxx = wxfpxxJpaDao.selectByWeiXinOrderNo(SuccOrderId);
                    if(null==wxFpxx){
                        String re = "发票开具失败，请重试！";
                        weixinUtils.jujuekp(SuccOrderId, re, access_token);
                        return "";
                    }
                    Map resultMap =  weixinUtils.zdcxstatus(SuccOrderId,access_token);
                    //if(null==resultMap&&wxFpxx.getWxtype().equals("1")){
                    if(null!=resultMap&&resultMap.get("msg").equals("72038")){
                        String re = "发票开具失败，请重试！";
                        weixinUtils.jujuekp(SuccOrderId, re, access_token);
                        return "";
                    }
                    String gsdm = wxFpxx.getGsdm();
                    String q = wxFpxx.getQ();
                    String tqm = wxFpxx.getTqm();
                    WeixinTask weixinTask = new WeixinTask();
                    weixinTask.setWxFpxx(wxFpxx);
                    weixinTask.setResultMap(resultMap);
                    weixinTask.setAccess_token(access_token);
                    weixinTask.setGsxxService(gsxxService);
                    weixinTask.setSuccOrderId(SuccOrderId);
                    weixinTask.setOpenid(openid);
                    weixinTask.setGetDataService(getDataService);
                    weixinTask.setCszbService(cszbService);
                    weixinTask.setWeixinUtils(weixinUtils);
                    weixinTask.setFailOrderId(FailOrderId);
                    weixinTask.setWxfpxxJpaDao(wxfpxxJpaDao);
                    weixinTask.setKplsService(kplsService);
                    weixinTask.setKpspmxService(kpspmxService);
                    weixinTask.setPdf_file_url(pdf_file_url);
                    weixinTask.setBarcodeService(barcodeService);
                    weixinTask.setXfService(xfService);
                    weixinTask.setSkpService(skpService);
                    weixinTask.setPpJpaDao(ppJpaDao);
                    weixinTask.setXfJpaDao(xfJpaDao);
                    weixinTask.setAdapterService(adapterService);
                    weixinTask.setSkpJpaDao(skpJpaDao);
                    weixinTask.setJylsJpaDao(jylsJpaDao);
                    weixinTask.setKplsJpaDao(kplsJpaDao);
                    Thread thread = new Thread(weixinTask);
                    thread.start();
                    return "";
                }
                if(null!=FailOrderId && !FailOrderId.equals("")){
                    String re = "订单"+FailOrderId+"的发票开具异常,请联系商家！";
                    weixinUtils.jujuekp(FailOrderId,re,access_token);
                    return "";
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
    public synchronized boolean paichong(String createTime, String fromUserName) {
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
