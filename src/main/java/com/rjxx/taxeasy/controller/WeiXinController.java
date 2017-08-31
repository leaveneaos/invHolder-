package com.rjxx.taxeasy.controller;

import com.alibaba.druid.sql.visitor.functions.Lpad;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rjxx.taxeasy.bizcomm.utils.GetDataService;
import com.rjxx.taxeasy.bizcomm.utils.GetXmlUtil;
import com.rjxx.taxeasy.bizcomm.utils.HttpUtils;
import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.comm.SigCheck;
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
    private JylsService jylsService;

    @Autowired
    private GetDataService getDataService;

    @Autowired
    private KpspmxService kpspmxService;

    @Autowired
    private TqmtqService tqmtqService;

    @Autowired
    private  BarcodeService barcodeService;

    @Autowired
    private WxfpxxJpaDao wxfpxxJpaDao;

    @Autowired
    private WeixinUtils weixinUtils;
    @Autowired
    private  GsxxService gsxxService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Value("${rjxx.pdf_file_url:}")
    private String pdf_file_url;

   // private  Integer counter = 0;
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

    @RequestMapping(value = WeiXinConstants.AFTER_WEIXIN_REDIRECT_URL,method = RequestMethod.POST)
    public String postWeiXin() throws Exception {
        System.out.println("进入微信发送的post请求");
        //WeixinUtils weixinUtils = new WeixinUtils();
        Map<String, String> requestMap = null;
        try {
            System.out.println("微信推送事件");
            //微信事件xml转map
            requestMap = parseXml(request);
            logger.info("接收微信返回xml变map"+requestMap.toString());
            //处理微信推送事件： 微信授权完成事件推送
            if(requestMap.get("MsgType").equals("event")&&requestMap.get("Event").equals("user_authorize_invoice")){
                logger.info("进入开票处理----");
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
                String  access_token = (String)weixinUtils.hqtk().get("access_token");
                System.out.println("传递的token----"+access_token);
                request.setAttribute("access_token",access_token);
                if(null!=SuccOrderId &&!SuccOrderId.equals("")){
                    System.out.println("拿到成功的订单id了");
                    WxFpxx oneByOrderNo = wxfpxxJpaDao.selsetByOrderNo(SuccOrderId);

                    String gsdm = oneByOrderNo.getGsdm();
                    logger.info("根据订单编号查询交易信息数据"+oneByOrderNo.toString());
                    if(null==gsdm && gsdm.equals("")){
                        logger.info("公司代码为空！");
                        return "";
                    }
                    String q = oneByOrderNo.getQ();
                    if (null==q && q.equals("")){
                        logger.info("参数q为空");
                        return "";
                    }
                    String tqm = oneByOrderNo.getTqm();
                    if (null==tqm && tqm.equals("")){
                        logger.info("提取码为空");
                        return "";
                    }
                    if(null!=oneByOrderNo.getWxtype() && "1".equals(oneByOrderNo.getWxtype())){
                        logger.info("进入申请开票类型------------开始开票");
                        //主动获取授权状态，成功会返回数据
                        Map resultMap =  weixinUtils.zdcxstatus(SuccOrderId,access_token);
                        if(null==resultMap){
                            logger.info("进入--主动查询授权是空。");
                            return "";
                        }else {
                            System.out.println("开始封装数据并进行开票" + resultMap.toString());
                            //全家进行开票
                            if (null != gsdm && gsdm.equals("Family")) {
                                Map parms = new HashMap();
                                parms.put("gsdm", gsdm);
                                Gsxx gsxx = gsxxService.findOneByParams(parms);
                                logger.info("进入全家开票");
                                //拉取数据
                                Map resultSjMap = getDataService.getData(tqm, "Family");
                                logger.info("全家拉取数据成功--------开始开票");
                                String status = barcodeService.pullInvioce(resultSjMap, gsdm, (String) resultMap.get("title"),
                                        (String) resultMap.get("tax_no"), (String) resultMap.get("email"), (String) resultMap.get("bank_type")
                                        , (String) resultMap.get("bank_no"), (String) resultMap.get("addr"), (String) resultMap.get("phone"),
                                        tqm, openid, "4", access_token, gsxx.getAppKey(), gsxx.getSecretKey());
                                if ("-1".equals(status)) {
                                    logger.info("开具失败");
                                } else if ("-2".equals(status)) {
                                    logger.info("开具失败，拒绝开票");
                                } else {
                                    logger.info("开具成功");
                                    System.out.println("开票成功");
                                }
                                return "";
                            }
                            if (null != gsdm && gsdm.equals("chamate")) {
                                logger.info("进入一茶一坐开票处理");
                                //Thread.sleep(5000);
                                String status = barcodeService.makeInvoice(gsdm, q, (String) resultMap.get("title"),
                                        (String) resultMap.get("tax_no"), (String) resultMap.get("email"), (String) resultMap.get("bank_type")
                                        , (String) resultMap.get("bank_no"), (String) resultMap.get("addr"), (String) resultMap.get("phone"), tqm, openid, "4");
                                    if ("-1".equals(status)) {
                                        logger.info("开具失败");
                                    } else if ("0".equals(status)) {
                                        logger.info("所需数据为空");
                                    } else {
                                        logger.info("开具成功");
                                        System.out.println("开票成功");
                                    }
                                    return "";
                                }
                            }
                        }
                        if(null!=oneByOrderNo.getWxtype() && "2".equals(oneByOrderNo.getWxtype())){
                            logger.info("进入领取发票类型------------直接插入卡包");
                            WxFpxx wxFpxxIncard = wxfpxxJpaDao.selsetByOrderNo(SuccOrderId);
                            if(null==wxFpxxIncard.getCode()||"".equals(wxFpxxIncard.getCode())){
                                logger.info("进入插卡方法-----");
                                //没有插入过卡包
                                Map kplsMap = new HashMap();
                                kplsMap.put("kplsh",wxFpxxIncard.getKplsh());
                                Kpls kpls = kplsService.findOneByParams(kplsMap);
                                if(null==kpls){
                                    logger.info("kpls为空");
                                    return "";
                                }
                                Map params2 = new HashMap();
                                params2.put("kplsh", wxFpxxIncard.getKplsh());
                                List<Kpspmx> kpspmxList = kpspmxService.findMxNewList(params2);
                                if(null==kpspmxList){
                                    logger.info("商品明细为空");
                                    return "";
                                }
                                logger.info("开票流水为---"+JSON.toJSONString(kpls));
                                logger.info("开票商品明细为---"+JSON.toJSONString(kpspmxList));
                                logger.info("PDFurl---"+pdf_file_url);
                                //插入卡包
                                String result = weixinUtils.fpInsertCardBox(SuccOrderId, pdf_file_url, kpspmxList, kpls);
                                return "";
                            }
                        }
                }
                if(null!=FailOrderId && !FailOrderId.equals("")){
                    System.out.println("失败的订单id"+FailOrderId);
                    String re = "微信授权失败,请重新开票";
                    String msg= weixinUtils.jujuekp(FailOrderId,re,access_token);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "" ;
    }

    public Map<String, String> parseXml(HttpServletRequest request) throws Exception {
        System.out.println("解析微信推送xml————————————");
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

    /**
     * 获取微信授权链接
     *
     * @return
     */
    /*@RequestMapping(value = WeiXinConstants.BEFORE_WEIXIN_REDIRECT_URL)
    @ResponseBody
    public String getTiaozhuanURL(*//*String orderid,int money,int timestamp,String mendianId*//*) throws Exception {

        WeixinUtils weixinUtils = new WeixinUtils();
        //判断是否是微信浏览器
       *//* if (!weixinUtils.isWeiXinBrowser(request)) {
            request.getSession().setAttribute("msg", "请使用支付宝进行该操作");
            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            return null;
        }*//*

        String access_token = (String) weixinUtils.hqtk().get("access_token");//获取token
        request.getSession().setAttribute("access_token",access_token);//token放进session里
        String spappid =  weixinUtils.getSpappid();//获取开票平台
        String ticket = weixinUtils.getTicket();
        String orderid="123145322200112234";
        int money = 12;
        int timestamp = 1574875876;
        String  source = "web";
        String redirect_url = "https://baidu.com";
        int type = 1;//填写抬头申请开票类型
        Map nvps = new HashMap();
        nvps.put("s_pappid",spappid);
        nvps.put("order_id",orderid);
        nvps.put("money",money);
        nvps.put("timestamp",timestamp);
        nvps.put("source",source);
        nvps.put("redirect_url",redirect_url);
        nvps.put("ticket",ticket);
        nvps.put("type",type);
        String sj = JSON.toJSONString(nvps);
        System.out.println("封装数据"+sj);
        String urls ="https://api.weixin.qq.com/card/invoice/getauthurl?access_token="+access_token;
        String jsonStr3 = WeixinUtil.httpRequest(urls, "POST", sj);
        System.out.println("返回信息"+jsonStr3.toString());
        if(null!=jsonStr3){
            ObjectMapper jsonparer = new ObjectMapper();// 初始化解析json格式的对象
            try {
                Map map = jsonparer.readValue(jsonStr3, Map.class);
                String auth_url = (String) map.get("auth_url");
                System.out.println("授权链接"+auth_url);
                logger.info("跳转url"+auth_url);
                response.sendRedirect(auth_url);
                request.getSession().setAttribute(orderid+"auth_url",auth_url);//跳转url放进session
            } catch (Exception e) {
                //处理异常
                logger.error("Get Ali Access_token error", e);
                request.getSession().setAttribute("msg", "获取微信授权出现异常!");
                response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            }
        }
        return null;
    }*/


    /**
     * 将发票信息同步到微信卡包
     *
     * @return
     */
    @RequestMapping(value = "/syncWeiXin")
    @ResponseBody
    public String syncWeiXin(@RequestParam(required = false) String order_id) throws Exception {
        if (order_id == null) {
            Object orderObject = session.getAttribute("order");
            if (orderObject == null) {
                request.getSession().setAttribute("msg", "会话超时，请重新开始操作!");
                response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                return null;
            }
            order_id = orderObject.toString();
        }
        logger.info("order_id++++++++++"+order_id);
        String serialorder= "";
        Object serialorderObject = session.getAttribute("serialorder");
        if (serialorderObject == null) {
            request.getSession().setAttribute("msg", "会话超时，请重新开始操作!");
            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            return null;
        }
        serialorder = serialorderObject.toString();
        logger.info("serialorder++++++++"+serialorder);

        //判断是否是微信浏览
//       if (!WeixinUtils.isWeiXinBrowser(request)) {
//            request.getSession().setAttribute("msg", "请使用微信进行该操作");
//            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
//            return null;
//        }
        //主动查询授权状态
        WeixinUtils weixinUtils = new WeixinUtils();
        String  access_token = (String)weixinUtils.hqtk().get("access_token");
        request.setAttribute("access_token",access_token);
        Map weiXinDataMap = weixinUtils.zdcxstatus(order_id,access_token);
        if(null==weiXinDataMap){
            logger.info("主动查询授权失败++++++++++++");
            return null;
        }
        Map para = new HashMap();
        para.put("serialorder", serialorder);
        List<Kpls> kplsList = kplsService.findAll(para);

        String openid=null;
        for (Kpls kpls : kplsList) {
            int kplsh = kpls.getKplsh();
            Map params2 = new HashMap();
            params2.put("kplsh", kplsh);
            List<Kpspmx> kpspmxList = kpspmxService.findMxNewList(params2);

            String s_media_id =weixinUtils.creatPDF(kpls.getPdfurl(),pdf_file_url);
            if(null==s_media_id&&StringUtils.isBlank(s_media_id)){
                logger.info("上传PDF失败获取s_media_id为null");
                return  null;
            }

//           openid =  weixinUtils.dzfpInCard(order_id,WeiXinConstants.FAMILY_CARD_ID,pdf_file_url,weiXinDataMap,kpspmxList,kpls,access_token);
//            if(null==openid){
//                request.getSession().setAttribute("msg", "将发票插入用户卡包出现异常");
//                response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
//                return null;
//            }
        }

        if(null==openid){
            request.getSession().setAttribute("msg", "将发票插入用户卡包出现异常");
            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            return null;
        }else {

        }
        return  null;
    }
}
