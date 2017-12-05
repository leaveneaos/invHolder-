package com.rjxx.taxeasy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.domains.Fpj;
import com.rjxx.taxeasy.domains.Gsxx;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.domains.Tqjl;
import com.rjxx.taxeasy.service.FpjService;
import com.rjxx.taxeasy.service.GsxxService;
import com.rjxx.taxeasy.service.JylsService;
import com.rjxx.taxeasy.service.TqjlService;
import com.rjxx.utils.HtmlUtils;
import com.rjxx.utils.weixin.WeiXinConstants;
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
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xlm on 2017/6/16.
 */
@Controller
@RequestMapping("/ssd")
public class SsdController extends BaseController{


    public static String SESSION_KEY_FPTQ_GSDM = "fptq_gsdm";

    @Autowired
    private JylsService jylsService;

    @Autowired
    private TqjlService tqjlService;

    @Autowired
    private FpjService fpjService;

    @Autowired
    private GsxxService gsxxService;

    public static final String APP_ID = "wx9abc729e2b4637ee";

    public static final String SECRET = "6415ee7a53601b6a0e8b4ac194b382eb";

    @RequestMapping
    @ResponseBody
    public String index() throws Exception {
        return init("shssts");
    }

    private String init(String gsdm) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("gsdm", gsdm);
        Gsxx gsxx = gsxxService.findOneByParams(params);
        if (gsxx == null) {
            response.sendError(501, gsdm + " not exist!!!");
            return null;
        }
        session.setAttribute(SESSION_KEY_FPTQ_GSDM, gsdm);
        if(null==request.getHeader("user-agent")){
            request.getSession().setAttribute("msg", "出现未知异常!请重试!");
            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            return null;
        }
        String ua = request.getHeader("user-agent").toLowerCase();
        logger.info("------------"+ua);
        if (ua.indexOf("micromessenger") > 0) {
            if (gsxx.getWxappid() == null || gsxx.getWxsecret() == null) {
                gsxx.setWxappid(APP_ID);
                gsxx.setWxsecret(SECRET);
            }
            String url = HtmlUtils.getBasePath(request);
            String openid = (String) session.getAttribute("openid");
            if (openid == null || "null".equals(openid)) {
                String ul = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinConstants.APP_ID + "&redirect_uri="
                        + url + "ssd/getWx&" + "response_type=code&scope=snsapi_base&state=" + gsdm
                        + "#wechat_redirect";
                response.sendRedirect(ul);
                return null;
            } else {
                response.sendRedirect(request.getContextPath() + "/" + gsdm + ".html?_t=" + System.currentTimeMillis());
                return null;
            }
        }
        response.sendRedirect(request.getContextPath() + "/" + gsdm + ".html?_t=" + System.currentTimeMillis());
        return null;
    }

    @RequestMapping(value = "/getWx")
    @ResponseBody
    public void getWx(String state, String code) throws IOException {
        Map params = new HashMap<>();
        session.setAttribute(SESSION_KEY_FPTQ_GSDM, state);
        params.put("gsdm", state);
        Gsxx gsxx = gsxxService.findOneByParams(params);
        if (gsxx.getWxappid() == null || gsxx.getWxsecret() == null) {
            gsxx.setWxappid(APP_ID);
            gsxx.setWxsecret(SECRET);
        }
        String turl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + WeiXinConstants.APP_ID + "&secret="
                + WeiXinConstants.APP_SECRET + "&code=" + code + "&grant_type=authorization_code";
        // https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
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
        response.sendRedirect(request.getContextPath() + "/" + state + ".html?_t=" + System.currentTimeMillis());
        return;
    }

    @RequestMapping(value = "/fptq")
    @ResponseBody
    public Map Fptq(String khh, String code) {
        String sessionCode = (String) session.getAttribute("rand");
        if(sessionCode==null || khh==null || code==null){
            try {
                response.sendRedirect(request.getContextPath() + "/QR/error.html?t=" + System.currentTimeMillis() + "=您的会话已过期，请重新扫码");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String ua = request.getHeader("user-agent").toLowerCase();
        logger.info("------------"+ua);
        String openid = (String) session.getAttribute("openid");
        String gsdm = (String) session.getAttribute(SESSION_KEY_FPTQ_GSDM);
        Map<String, Object> result = new HashMap<String, Object>();
        if (code != null && sessionCode != null && code.equals(sessionCode)) {
            Map map = new HashMap<>();
            map.put("khh", khh);
            map.put("gsdm", gsdm);
            map.put("month", "");
            List<Kpls> list = jylsService.findBykhh(map);
            boolean f=true;
            for(int i=0;i<list.size();i++){
                if(!list.get(0).getFpztdm().equals("00")){
                    f=false;
                }
            }
            if(f){
                if (list.size() > 0) {
                    result.put("num", "2");
                    result.put("khh", khh);
                    result.put("gsdm", gsdm);
                    request.getSession().setAttribute("khh", khh);
                    request.getSession().setAttribute("gsdm", gsdm);
                    result.put("serialOrder",list.get(0).getSerialorder());
                } else {
                    result.put("num", "3");
                }
            }else{
                result.put("num", "3");
            }
        } else {
            result.put("num", "4");
        }
        return result;
    }
    @RequestMapping(value = "/xfp")
    @ResponseBody
    public Map xfp(String khh, String gsdm,String month) {
        Map result=new HashMap();
        String openid = (String) session.getAttribute("openid");
        Map map = new HashMap<>();
        map.put("khh", khh);
        map.put("gsdm", gsdm);
        List<Kpls> list=null;
        if(month.equals("this")){
            map.put("this", "  date_format(b.kprq,'%Y-%m')=date_format(now(),'%Y-%m')");
            list = jylsService.findBykhh(map);
        }else if(month.equals("previous")){
            map.put("previous", "   date_format(b.kprq,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m')");
            list = jylsService.findBykhh(map);
        }else if(month.equals("twobefore")){
            map.put("twobefore"," b.kprq between date_sub(now(),interval 2 month) and now()");
            list = jylsService.findBykhh(map);
        }else if(month.equals("Decemberbefore")){
            map.put("Decemberbefore", " b.kprq between date_sub(now(),interval 12 month) and now()");
            list = jylsService.findBykhh(map);
        }

        if (list.size() > 0) {
            String pdfdzs = "";
            request.getSession().setAttribute("serialorder", list.get(0).getSerialorder());
            request.getSession().setAttribute("djh", list.get(0).getDjh());
            for (Kpls kpls2 : list) {
                pdfdzs += kpls2.getPdfurl().replace(".pdf", ".jpg") + ",";
            }
            if (pdfdzs.length() > 0) {
                result.put("pdfdzs", pdfdzs.substring(0, pdfdzs.length() - 1));
                request.getSession().setAttribute("pdfdzs", pdfdzs.substring(0, pdfdzs.length() - 1));
            }
            if (openid != null && !"null".equals(openid)) {
                Map<String, Object> params = new HashMap<>();
                params.put("djh", list.get(0).getDjh());
                params.put("unionid", openid);
                Fpj fpj = fpjService.findOneByParams(params);
                if (fpj == null) {
                    fpj = new Fpj();
                    fpj.setDjh(list.get(0).getDjh());
                    fpj.setUnionid(openid);
                    fpj.setYxbz("1");
                    fpj.setLrsj(new Date());
                    fpj.setXgsj(new Date());
                    fpjService.save(fpj);
                }
            }
            result.put("num", "2");
            Tqjl tqjl = new Tqjl();
            tqjl.setDjh(String.valueOf(list.get(0).getDjh()));
            tqjl.setJlly("1");
            tqjl.setTqsj(new Date());
            String visiterIP;
            if (request.getHeader("x-forwarded-for") == null) {
                visiterIP = request.getRemoteAddr();// 访问者IP
            } else {
                visiterIP = request.getHeader("x-forwarded-for");
            }
            tqjl.setIp(visiterIP);
            String llqxx = request.getHeader("User-Agent");
            tqjl.setLlqxx(llqxx);
            tqjlService.save(tqjl);
            try {
                response.sendRedirect(request.getContextPath() + "/smtq/" + "xfp.html?_t=" + System.currentTimeMillis());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
