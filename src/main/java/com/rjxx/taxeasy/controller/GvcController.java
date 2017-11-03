package com.rjxx.taxeasy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.dao.WxfpxxJpaDao;
import com.rjxx.taxeasy.domains.*;
import com.rjxx.taxeasy.service.*;
import com.rjxx.utils.HtmlUtils;
import com.rjxx.utils.IMEIGenUtils;
import com.rjxx.utils.weixin.WeiXinConstants;
import com.rjxx.utils.weixin.WeixinUtils;
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
    private KplsService kplsService;

    @Autowired
    private FpjService fpjService;

    @Autowired
    private TqjlService tqjlService;

    @Autowired
    private WxfpxxJpaDao wxfpxxJpaDao;

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
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            if (code != null && sessionCode != null && code.equals(sessionCode)) {
                Map map = new HashMap<>();
                map.put("tqm", tqm);
                map.put("gsdm", gsdm);
                map.put("je",price);
                Jyls jyls = jylsService.findOne(map);
                List<Kpls> list = jylsService.findByTqm(map);
                if (list.size() > 0) {
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
                }if(null != jyls && null !=jyls.getDjh()){
                    result.put("num","3");
                }else {
                    //调用接口获取数据--首先存入微信发票信息
                    if(WeixinUtils.isWeiXinBrowser(request)){
                        logger.info("光唯尚----微信扫描存入微信发票信息------");
                        WxFpxx wxFpxxByTqm = wxfpxxJpaDao.selsetByOrderNo(tqm);
                        if(null==wxFpxxByTqm){
                            WxFpxx wxFpxx = new WxFpxx();
                            wxFpxx.setTqm(tqm);
                            wxFpxx.setGsdm(gsdm);
                            wxFpxx.setOrderNo(tqm);
                            wxFpxx.setQ("");
                            wxFpxx.setWxtype("1");
                            wxFpxx.setOpenId((String) session.getAttribute("openid"));
                            wxfpxxJpaDao.save(wxFpxx);
                        }else {
                            wxFpxxByTqm.setTqm(tqm);
                            wxFpxxByTqm.setGsdm(gsdm);
                            wxFpxxByTqm.setOrderNo(tqm);
                            wxFpxxByTqm.setQ("");
                            wxFpxxByTqm.setWxtype("1");
                            wxFpxxByTqm.setOpenId((String) session.getAttribute("openid"));
                            if(wxFpxxByTqm.getCode()!=null||!"".equals(wxFpxxByTqm.getCode())){
                                String notNullCode= wxFpxxByTqm.getCode();
                                wxFpxxByTqm.setCode(notNullCode);
                            }
                            wxfpxxJpaDao.save(wxFpxxByTqm);
                        }
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
