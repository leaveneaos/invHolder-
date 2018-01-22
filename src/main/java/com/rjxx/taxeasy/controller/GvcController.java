package com.rjxx.taxeasy.controller;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rjxx.taxeasy.bizcomm.utils.GetDataService;
import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.dao.WxTokenJpaDao;
import com.rjxx.taxeasy.dao.WxfpxxJpaDao;
import com.rjxx.taxeasy.domains.*;
import com.rjxx.taxeasy.service.*;
import com.rjxx.taxeasy.utils.alipay.AlipayConstants;
import com.rjxx.taxeasy.utils.alipay.AlipayUtils;
import com.rjxx.utils.HtmlUtils;
import com.rjxx.utils.IMEIGenUtils;
import com.rjxx.utils.weixin.WeiXinConstants;
import com.rjxx.utils.weixin.WeixinUtils;
import com.rjxx.utils.weixin.wechatFpxxServiceImpl;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zsq on 2017-11-02.
 */
@Controller
@RequestMapping("/gvc")
public class GvcController extends BaseController {

    @Autowired
    private GsxxService gsxxService;

    @Autowired
    private JylsService jylsService;

    @Autowired
    private FpjService fpjService;

    @Autowired
    private TqjlService tqjlService;

    @Autowired
    private WxfpxxJpaDao wxfpxxJpaDao;

    @Autowired
    private CszbService cszbService;

    @Autowired
    private GetDataService getDataService;

    @Autowired
    private WeixinUtils weixinUtils;

    @Autowired
    private wechatFpxxServiceImpl wechatFpxxService;

    @Autowired
    private WxTokenJpaDao wxTokenJpaDao;

    @Autowired
    private SkpService skpService;

    @RequestMapping
    @ResponseBody
    public void index() throws Exception{
        String gsdm = "gvc";
        Map<String,Object> params = new HashMap<>();
        params.put("gsdm",gsdm);
        request.getSession().setAttribute("gsdm",gsdm);
        Gsxx gsxx = gsxxService.findOneByParams(params);
        if(gsxx == null ){
            request.getSession().setAttribute("msg", "出现未知异常!请重试!");
            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            return ;
        }
        if(request.getHeader("user-agent")== null){
            request.getSession().setAttribute("msg", "出现未知异常!请重试!");
            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            return ;
        }
        String ua = request.getHeader("user-agent").toLowerCase();
        if (ua.indexOf("micromessenger") > 0) {
            String url = HtmlUtils.getBasePath(request);
            String openid = String.valueOf(session.getAttribute("openid"));
            if (openid == null || "null".equals(openid)) {
                String ul = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinConstants.APP_ID + "&redirect_uri="
                        + url + "gvc/getWx&" + "response_type=code&scope=snsapi_base&state=" + gsdm + "#wechat_redirect";
                response.sendRedirect(ul);
                logger.info("转发的url为"+ul);
                return;
            } else {
                response.sendRedirect(request.getContextPath() + "/GV/gvc.html?gsdm=" + gsdm + "&&t=" + System.currentTimeMillis());
                return;
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/GV/gvc.html?gsdm=" + gsdm + "&&t=" + System.currentTimeMillis());
            return;
        }
    }

    /**
     * 获取微信openid
     * @param state
     * @param code
     * @throws IOException
     */
    @RequestMapping(value = "/getWx")
    @ResponseBody
    public void getWx(String state, String code) throws IOException {
        Map params = new HashMap<>();
        params.put("gsdm", state);
        Gsxx gsxx = gsxxService.findOneByParams(params);
        if (gsxx == null) {
            request.getSession().setAttribute("msg", "出现未知异常!请重试!");
            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            return ;
        }
        String turl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + WeiXinConstants.APP_ID + "&secret="
                + WeiXinConstants.APP_SECRET + "&code=" + code + "&grant_type=authorization_code";
        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(turl);
        ObjectMapper jsonparer = new ObjectMapper();// 初始化解析json格式的对象
        try {
            HttpResponse res = client.execute(get);
            String responseContent = null; // 响应内容
            HttpEntity entity = res.getEntity();
            responseContent = EntityUtils.toString(entity, "UTF-8");
            Map map = jsonparer.readValue(responseContent, Map.class);
            // 将json字符串转换为json对象
            if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                if (map.get("errcode") != null) {// 错误时微信会返回错误码等信息，{"errcode":40013,"errmsg":"invalid

                } else {// 正常情况下{"access_token":"ACCESS_TOKEN","expires_in":7200}
                    session.setAttribute("access_token", map.get("access_token"));
                    session.setAttribute("openid", map.get("openid"));
                    map.put("success", true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭连接 ,释放资源
            client.getConnectionManager().shutdown();
        }
        response.sendRedirect(request.getContextPath() + "/GV/gvc.html?gsdm=" + state + "&&t=" + System.currentTimeMillis());
        return;
    }

    /**
     * 发票提交--提取验证
     * @param tqm
     * @param price
     * @param gsdm
     * @param code
     * @return num : 2已经开过发票,3发票正在开具,4验证码不正确,1调用接口获取数据
     */
    @RequestMapping(value = "/tqyz",method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public Map<String,Object> tqyz(String tqm,String price,String gsdm,String code) {
        String sessionCode = (String) session.getAttribute("rand");
        String opendid = (String) session.getAttribute("openid");
        String userId = (String) request.getSession().getAttribute(AlipayConstants.ALIPAY_USER_ID);
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            String tqms = tqm.toUpperCase().trim();
            if (code != null && sessionCode != null && code.equals(sessionCode)) {
                Map map = new HashMap<>();
                map.put("tqm", tqms);
                map.put("gsdm", gsdm);
                //map.put("je",price);
                Jyls jyls = jylsService.findOne(map);
                List<Kpls> list = jylsService.findByTqm(map);
                if (list.size() > 0) {
                    Map maps = new HashMap();
                    Cszb zb1 = cszbService.getSpbmbbh(gsdm, null,null, "sfdyjkhqkp");
                    if(zb1.getCsz().equals("是")){
                        Cszb  csz =  cszbService.getSpbmbbh(gsdm, null,null, "sfhhurl");
                        maps = getDataService.getDataForGvc(tqms, gsdm, csz.getCsz());
                    }
                    //DecimalFormat decimalFormat = new DecimalFormat("###################.###########");
                    BigDecimal bigPrice = new BigDecimal(price);
                    logger.info("输入金额1"+bigPrice);
                    BigDecimal bigResult = new BigDecimal(maps.get("zkjine").toString());
                    logger.info("返回金额2"+bigResult);
                    int i = bigPrice.compareTo(bigResult);
                    if(i != 0){
                        result.put("num","12");
                        result.put("msg","金额输入错误，请输入销售票据上的“订单总计”金额！");
                        return result;
                    }
                    if (opendid != null && !"null".equals(opendid)) {
                        Map<String, Object> params = new HashMap<>();
                        params.put("djh", jyls.getDjh());
                        params.put("unionid",opendid);
                        Fpj fpj = fpjService.findOneByParams(params);
                        if(fpj == null){
                            fpj = new Fpj();
                            fpj.setDjh(jyls.getDjh());
                            fpj.setUnionid(opendid);
                            fpj.setYxbz("1");
                            fpj.setLrsj(new Date());
                            fpj.setXgsj(new Date());
                            fpjService.save(fpj);
                        }
                    }
                    String pdfdzs = "";
                    request.getSession().setAttribute("djh",list.get(0).getDjh());
                    request.getSession().setAttribute("serialorder",list.get(0).getSerialorder());
                    for (Kpls kpls2: list) {
                        pdfdzs += kpls2.getPdfurl().replace(".pdf",".jpg") + ",";
                    }
                    if(pdfdzs.length() > 0){
                        result.put("pdfdzs",pdfdzs.substring(0,pdfdzs.length() - 1));
                        request.getSession().setAttribute("pdfdzs",pdfdzs.substring(0,pdfdzs.length() - 1));
                    }
                    Tqjl tqjl = new Tqjl();
                    tqjl.setDjh((String.valueOf(list.get(0).getDjh())));
                    tqjl.setJlly("1");
                    tqjl.setTqsj(new Date());
                    String visiterIP;
                    if(request.getHeader("x-forwarded-for") == null){
                        visiterIP = request.getRemoteAddr();/*访问者IP*/
                    }else {
                        visiterIP = request.getHeader("x-forwarded-for");
                    }
                    tqjl.setIp(visiterIP);
                    String llqxx = request.getHeader("User-Agent");
                    tqjl.setLlqxx(llqxx);
                    tqjlService.save(tqjl);
                    result.put("num","2");
                    result.put("serialOrder",list.get(0).getSerialorder());
                }else if(null != jyls && null !=jyls.getDjh()){
                    result.put("num","3");
                }else {
                    Map resultMap = new HashMap();
                    //调用接口获取数据--首先存入微信发票信息
                    boolean b = wechatFpxxService.InFapxx(tqms, gsdm, tqms, "", "1", opendid, userId, "", request);
                    if(!b){
                        result.put("num","12");
                        result.put("msg","保存信息失败，请重试！");
                        return result;
                    }
                    Cszb zb1 = cszbService.getSpbmbbh(gsdm, null,null, "sfdyjkhqkp");
                    if(zb1.getCsz().equals("是")){
                        Cszb  csz =  cszbService.getSpbmbbh(gsdm, null,null, "sfhhurl");
                        resultMap = getDataService.getDataForGvc(tqms, gsdm, csz.getCsz());
                    }
                    if(null!=resultMap.get("msg")){
                        result.put("num","12");
                        result.put("msg",resultMap.get("msg"));
                        return result;
                    }
                    List<Jyxxsq> jyxxsqList=(List)resultMap.get("jyxxsqList");
                    List<Jymxsq> jymxsqList=(List)resultMap.get("jymxsqList");
                    List<Jyzfmx> jyzfmxList = (List) resultMap.get("jyzfmxList");
                    String nowdate = (String) resultMap.get("nowdate");
                    String storeno = (String) resultMap.get("storeno");
                    String kpqssj = (String) resultMap.get("kpqssj");
                    String kpjssj = (String) resultMap.get("kpjssj");
                    logger.info("--------当前时间-------"+nowdate);
                    logger.info("------开票起始时间------"+kpqssj);
                    logger.info("------开票结束时间------"+kpjssj);
                    DateFormat df = new SimpleDateFormat("HH:mm:ss");
                    Date nowDate = df.parse(nowdate);
                    if(nowdate.equals("")){
                        result.put("num","12");
                        result.put("msg","系统出现错误，请重新输入！");
                        return result;
                    }
                    if(!kpqssj.equals("") && !kpjssj.equals("")){
                        Date endDate = df.parse(kpjssj);
                        Date startDate = df.parse(kpqssj);
                        if(nowDate.getTime()<endDate.getTime() && nowDate.getTime()>startDate.getTime()){
                            logger.info("-----------------系统时间小于开票起始时间"+kpqssj);
                            result.put("num","22");
                            result.put("endDate",kpjssj);
                            result.put("startDate",kpqssj);
                            if(kpjssj.equals(kpqssj)){
                                Map skpmap = new HashMap();
                                skpmap.put("gsdm", "gvc");
                                skpmap.put("kpddm", storeno);
                                Skp skpdata = skpService.findOneByParams(skpmap);
                                result.put("msg","此订单不在电子发票的有效开票期内，请联系店铺开具发票！");
                            }
                            return result;
                        }
                    }
                    //DecimalFormat decimalFormat = new DecimalFormat("###################.###########");
                    //logger.info("_____________返回金额"+decimalFormat.format(resultMap.get("zkjine")));
                    BigDecimal bigPrice = new BigDecimal(price);
                    logger.info("输入金额3"+bigPrice);
                    BigDecimal bigResult = new BigDecimal(resultMap.get("zkjine").toString());
                    logger.info("返回金额4"+bigResult);
                    int i = bigPrice.compareTo(bigResult);
                    if(i != 0){
                        result.put("num","12");
                        result.put("msg","金额输入错误，请输入销售票据上的“订单总计”金额！");
                        return result;
                    }
                    String orderNo = tqms;
                    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String orderTime = sdf.format(jyxxsqList.get(0).getDdrq());
                    if(WeixinUtils.isWeiXinBrowser(request)){
                        String access_token ="";
                        String ticket = "";
                        try {
                            WxToken wxToken = wxTokenJpaDao.findByFlag("01");
                            if(wxToken==null){
                                result.put("num","12");
                                result.put("msg","出现未知异常，请重试！");
                                return result;
                            }else {
                                access_token = wxToken.getAccessToken();
                                ticket= wxToken.getTicket();
                            }
                            String spappid = weixinUtils.getSpappid(access_token);//获取平台开票信息
                            if(null==spappid ||"".equals(spappid)){
                                result.put("num","12");
                                result.put("msg","出现未知异常，请重试！");
                                return result;
                            }
                            String weixinOrderNo = wechatFpxxService.getweixinOrderNo(orderNo,gsdm);
                            if(weixinOrderNo==null){
                                result.put("num","12");
                                result.put("msg","出现未知异常，请重试！");
                                return result;
                            }
                            String redirectUrl = weixinUtils.getTiaoURL(gsdm,weixinOrderNo,price,orderTime, "","1",access_token,ticket,spappid);
                            result.put("num","20");
                            result.put("redirectUrl",redirectUrl);
                            return result;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }else {
                        result.put("num","12");
                        result.put("msg","请使用微信浏览器进行此操作！");
                        return result;
                    }
                }
            }else {
                result.put("num","4");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
