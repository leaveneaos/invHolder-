package com.rjxx.taxeasy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rjxx.taxeasy.bizcomm.utils.*;
import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.domains.*;
import com.rjxx.taxeasy.service.*;
import com.rjxx.utils.HtmlUtils;
import com.rjxx.utils.StringUtils;
import com.rjxx.utils.weixin.WeiXinConstants;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by zsq on 2017/7/25
 * 模板
 */
@Controller
@RequestMapping("/cmsc")
public class CmscController extends BaseController {
    @Autowired
    private GsxxService gsxxservice;//公司信息
    @Autowired
    private JylsService jylsService;//交易流水
    @Autowired
    private TqmtqService tqmtqService;//提取码提取
    @Autowired
    private KplsService kplsService;
    @Autowired
    public static final String APP_ID ="wx9abc729e2b4637ee";

    public static final String GET_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";

    public static final String SECRET = "6415ee7a53601b6a0e8b4ac194b382eb";

    @RequestMapping
    @ResponseBody
    public String index() throws Exception {

        return init("cmsc");
    }
    @RequestMapping(value = "/mb", method = RequestMethod.GET)
    public String init(String gsdm ) throws Exception{
        Map<String,Object> params = new HashMap<>();
        params.put("gsdm",gsdm);
        request.getSession().setAttribute("gsdm",gsdm);
        Gsxx gsxx = gsxxservice.findOneByParams(params);
        if(gsxx.getWxappid() == null || gsxx.getWxsecret() == null){
            gsxx.setWxappid(APP_ID);
            gsxx.setWxsecret(SECRET);
        }
        String ua = request.getHeader("user-agent").toLowerCase();
        if (ua.indexOf("micromessenger") > 0) {
            String url = HtmlUtils.getBasePath(request);
            String openid = String.valueOf(session.getAttribute("openid"));
            if (openid == null || "null".equals(openid)) {
                logger.info("进入重定向");
                String ul = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinConstants.APP_ID + "&redirect_uri="
                        + url + "/getWx" + "&response_type=code&scope=snsapi_base&state=" + gsdm + "#wechat_redirect";
                response.sendRedirect(ul);
                return null;
            } else {
                response.sendRedirect(request.getContextPath() + "/" + gsdm + "_page.html?_t=" + System.currentTimeMillis());
                return null;
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/" + gsdm + "_page.html?_t=" + System.currentTimeMillis());
            return null;
        }
    }
    @RequestMapping(value = "/getWx")
    @ResponseBody
    public void getWx(String state,String code) throws IOException{
        logger.info("进入获取微信openid---");
        Map params = new HashMap<>();
        params.put("gsdm",state);
        Gsxx gsxx = gsxxservice.findOneByParams(params);

        String turl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + WeiXinConstants.APP_ID + "&secret="
                + WeiXinConstants.APP_SECRET+ "&code=" + code + "&grant_type=authorization_code";
        HttpClient client = new DefaultHttpClient();
        HttpGet get  = new HttpGet(turl);
        ObjectMapper jsonparer = new ObjectMapper();
        try{
            HttpResponse res = client.execute(get);
            String responseContent = null;
            HttpEntity entity = res.getEntity();
            responseContent = EntityUtils.toString(entity,"UTF-8");
            Map map = jsonparer.readValue(responseContent,Map.class);

            //将json字符串转为json对象
            if(res.getStatusLine().getStatusCode()== HttpStatus.SC_OK){
                if(map.get("errcode") != null){
                }else{
                    session.setAttribute("access_token",map.get("access_token"));
                    session.setAttribute("openid", map.get("openid"));
                    logger.info(session.getAttribute("openid").toString());
                    map.put("success", true);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            //关闭连接,释放资源
            client.getConnectionManager().shutdown();
        }
        response.sendRedirect(request.getContextPath() + "/" + state + "_page.html?_t=" + System.currentTimeMillis());
        return;
    }

    /*校验提取码是否正确*/
    @RequestMapping(value = "/fptq")
    @ResponseBody
    public Map Fptq(String khh, String code) {
        String sessionCode = (String) session.getAttribute("rand");
        String opendid = (String) session.getAttribute("openid");
        String gsdm = (String) session.getAttribute("gsdm");
        Map<String, Object> result = new HashMap<String, Object>();
        if (code != null && sessionCode != null && code.equals(sessionCode)) {
            List<Kpls> list = new ArrayList<>();

            Map map = new HashMap<>();
            map.put("khh", khh);
            map.put("gsdm", gsdm);
            map.put("month", "");
            list = jylsService.findBykhh(map);
            boolean f = true;
            for (int i = 0; i < list.size(); i++) {
                if (!list.get(0).getFpztdm().equals("00")) {
                    f = false;
                }
            }
            if (f) {
                if (list.size() > 0) {
                    result.put("khh", khh);
                    result.put("num", "2");
                    result.put("gsdm", gsdm);
                    request.getSession().setAttribute("khh", khh);
                    request.getSession().setAttribute("gsdm", gsdm);

                } else {
                    result.put("num", "3");
                }
            } else {
                result.put("num", "3");
            }
        } else {
            result.put("num", "4");
        }
        return result;
    }



}