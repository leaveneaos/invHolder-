package com.rjxx.taxeasy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rjxx.taxeasy.bizcomm.utils.SendalEmail;
import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.domains.*;
import com.rjxx.taxeasy.service.*;
import com.rjxx.utils.MailUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/tijiao")
public class TijiaoController extends BaseController {
    @Autowired
    private JyspmxService jyspmxService;
    @Autowired
    private TqjlService tqjlService;
    @Autowired
    private WxkbService wxkbService;
    @Autowired
    private FphkyjService fphkyjService;
    @Autowired
    private JylsService jylsService;
    @Autowired
    private KplsService kplsService;
    @Autowired
    private SpfyxService spfyxService;
	
	@Autowired
	private SendalEmail se;
	
    @Value("${emailHost}")
    private String emailHost;
    @Value("${emailUserName}")
    private String emailUserName;
    @Value("${emailPwd}")
    private String emailPwd;
    @Value("${emailForm}")
    private String emailForm;
    @Value("${emailTitle}")
    private static String emailTitle;
    public static final String GET_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";// 获取access
    // url
    //public static final String APP_ID = "wxfd8a87be91984480";

    public static final String APP_ID = "wxfd8a87be91984480";

    //public static final String SECRET = "6415ee7a53601b6a0e8b4ac194b382eb";
    public static final String SECRET = "c14a1f54c4013b60c70f92c2b2d4681e";

    @RequestMapping(value = "/hqmx")
    @ResponseBody
    public Map hqmx(String djh) {
        Map<String, Object> result = new HashMap<String, Object>();
        Map params = new HashMap<>();
        if (null == djh || "".equals(djh)) {
            result.put("mxList", null);
            return result;
        }
        params.put("djh", djh);
        List<Jyspmx> mxList = jyspmxService.findAll(params);
        result.put("mxList", mxList);
        return result;
    }

    @RequestMapping(value = "/fpxx")
    @ResponseBody
    public Map fpxx(String fptt, String nsrsbh, String dz, String dh, String khh, String yhzh, String djh) {
        Map<String, Object> result = new HashMap<String, Object>();
        Map map = new HashMap<>();
        map.put("djh", djh);
        Jyls jyls = jylsService.findOne(map);
        if (!"00".equals(jyls.getClztdm())) {
            result.put("msg", "1");
            return result;
        }
        jyls.setGfmc(fptt);
        jyls.setGfsh(nsrsbh);
        jyls.setGfdz(dz);
        jyls.setGfdh(dh);
        jyls.setGfyh(khh);
        jyls.setGfyhzh(yhzh);
        jyls.setClztdm("01");
        jylsService.save(jyls);
        result.put("msg", "2");
        return result;
    }

    @RequestMapping(value = "/fpzt")
    @ResponseBody
    public Map fpzt(String djh) {
        Map<String, Object> result = new HashMap<String, Object>();
        Map map = new HashMap<>();
        map.put("tqm", djh);
        map.put("gsdm", "sqj");
        Jyls jyls = jylsService.findOne(map);
        Map kpmap = new HashMap<>();
        if (null == jyls) {
            result.put("msg", false);
            return result;
        } else {
            kpmap.put("tqm", jyls.getTqm());
            kpmap.put("gsdm", "sqj");
        }
        List<Kpls> list = jylsService.findByTqm(kpmap);
        if (list.size() > 0) {
            String pdfdzs = "";
            request.getSession().setAttribute("djh", list.get(0).getDjh());
            for (Kpls kpls2 : list) {
                pdfdzs += kpls2.getPdfurl().replace(".pdf", ".jpg") + ",";
            }
            if (pdfdzs.length() > 0) {
                result.put("pdfdzs", pdfdzs.substring(0, pdfdzs.length() - 1));
                request.getSession().setAttribute("pdfdzs", pdfdzs.substring(0, pdfdzs.length() - 1));
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
                visiterIP = request.getRemoteAddr();// 访问者IP
            }
            tqjl.setIp(visiterIP);
            String llqxx = request.getHeader("User-Agent");
            tqjl.setLlqxx(llqxx);
            tqjlService.save(tqjl);
            result.put("msg", true);
        } else {
            result.put("msg", false);
        }
        return result;
    }

    /**
     * 新邮箱发送
     * @param yx
     * @param
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/xyxfs")
    @ResponseBody
    public Map xyxfs(String yx, String serialorder) throws Exception {
        Map<String, Object> result = new HashMap<String, Object>();
        Map params = new HashMap<>();
        params.put("serialorder", serialorder);
        params.put("yx", yx);

        boolean flag = false;
        List<Kpls> kplsList = kplsService.findAll(params);
        params.put("djh",kplsList.get(0).getDjh());
        Jyls jyls = jylsService.findOne(params);
        List<String> pdfUrlList = new ArrayList<>();
        for (Kpls kpls : kplsList) {
            pdfUrlList.add(kpls.getPdfurl());
        }
        if (kplsList.size() > 0) {
            try {
                sendMail(jyls.getDdh(), yx, pdfUrlList, jyls.getXfmc(), String.valueOf(kplsList.get(0).getDjh()), jyls.getGsdm());
                Spfyx spfyx = spfyxService.findOneByParams(params);
                if (null == spfyx) {
                    spfyx = new Spfyx();
                    spfyx.setEmail(yx);
                    spfyx.setYxbz("1");
                    spfyx.setLrsj(new Date());
                    spfyx.setDjh(Integer.valueOf(kplsList.get(0).getDjh()));
                    spfyxService.save(spfyx);
                }
                flag = true;
            } catch (Exception e) {
                flag = false;
            }
        }
        result.put("msg", flag);
        return result;
    }
    @RequestMapping(value = "/yxfs")
    @ResponseBody
    public Map yxfs(String yx, String djh) throws Exception {
        Map<String, Object> result = new HashMap<String, Object>();
        Map params = new HashMap<>();
        params.put("djh", djh);
        params.put("yx", yx);

        boolean flag = false;
        Jyls jyls = jylsService.findOne(params);
        List<Kpls> kplsList = kplsService.findAll(params);
        List<String> pdfUrlList = new ArrayList<>();
        for (Kpls kpls : kplsList) {
            pdfUrlList.add(kpls.getPdfurl());
        }
        if (kplsList.size() > 0) {
            try {
                sendMail(jyls.getDdh(), yx, pdfUrlList, jyls.getXfmc(), String.valueOf(djh), jyls.getGsdm());
                Spfyx spfyx = spfyxService.findOneByParams(params);
                if (null == spfyx) {
                    spfyx = new Spfyx();
                    spfyx.setEmail(yx);
                    spfyx.setYxbz("1");
                    spfyx.setLrsj(new Date());
                    spfyx.setDjh(Integer.valueOf(djh));
                    spfyxService.save(spfyx);
                }
                flag = true;
            } catch (Exception e) {
                flag = false;
            }


        }
        result.put("msg", flag);
        return result;
    }

    /**
     * A发送邮件的内容
     *
     * @param ddh 订单号
     * @return
     * @throws Exception
     */
    private static String getAFMailContent(String ddh, List<String> pdfUrlList, String gsdm) throws Exception {
        StringBuffer sb = new StringBuffer();
        // sb.append(null2Wz(iurb.get("BUYER_NAME")));
        sb.append(" 先生/小姐您好：<br/>");
        sb.append("<br/>");
        sb.append("您的订单号码： ");
        sb.append(ddh).append("的电子发票已开具成功，电子发票下载地址：<br>");
        for (String pdfUrl : pdfUrlList) {
            sb.append("<a href='" + pdfUrl + "'>" + null2Wz(pdfUrl) + "</a><br>");
        }
        sb.append("请及时下载您的发票。<br><br>");
        sb.append("提示:苹果浏览器无法显示发票章,只能下载pdf显示<br>");
        sb.append("<br/><br/>");
        sb.append(gsdm);
        sb.append("<br/>");
        sb.append("<br/>");
        Date d = new Date();
        sb.append(1900 + d.getYear()).append("年").append(d.getMonth() + 1).append("月").append(d.getDate()).append("日");
        return sb.toString();
    }

    // 判空
    private static Object null2Wz(Object s) {
        return s == null || "".equals(s) ? "未知" : s;
    }

    /**
     * 发送邮件
     *
     * @param ddh
     * @param email
     * @param pdfUrlList
     * @param gsdm
     * @throws Exception
     */
    public void sendMail(String ddh, String email, List<String> pdfUrlList, String xfmc, String djh, String gsdm) throws Exception {
    	 se.sendEmail(djh, gsdm, email, "发票提取", djh, getAFMailContent(ddh, pdfUrlList, xfmc), "电子发票");
        // TODO 这里需要根据邮件摸板内容进行调整。

        // XXX 先生/小姐您好：
        //
        // 订单号码： XXXXXXXX, 您的发票信息如下：
        //
        //
        // 发票将邮寄至地址（即订单收货地址）： XXXXXXX 收件人（即定单收货人）： xxxxxx
        // 上述资料是您在个人基本资料中所登陆的地址，并已输入发票系统，为避免退货情况产生，
        // 请您再次确认住址是否正确，若有需要修改邮寄资料请联络客服中心进行修改。
        //
        // 在此提醒若您是在收到此邮件后才修改个人基本资料，则新登陆的邮寄资料将会在下次发票开立时生效
        //
        //
        //
        // 爱芙趣商贸（上海）有限公司
        // 20xx年x月x日

        Tqjl tqjl = new Tqjl();
        tqjl.setDjh(String.valueOf(request.getSession().getAttribute("djh")));
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
        tqjl.setJlly("2");
        tqjlService.save(tqjl);
        /* Tqjl tqjl = new Tqjl();
         tqjl.setDjh(String.valueOf( request.getSession().getAttribute("djh")));
         tqjl.setTqsj(new Date());
         String visiterIP=request.getRemoteAddr();//访问者IP  
         tqjl.setIp(visiterIP);
         String llqxx =request.getHeader("User-Agent");
         tqjl.setLlqxx(llqxx);
         tqjlService.save(tqjl);*/
        Thread.sleep(5000);
    }

    @RequestMapping(value = "/sqyj")
    @ResponseBody
    public Map sqyj(Fphkyj fphkyj) {
        Map<String, Object> result = new HashMap<String, Object>();
        Map params = new HashMap<>();
        params.put("djh", fphkyj.getDjh());
        Fphkyj fphkyj2 = fphkyjService.findOneByParams(params);
        if (null != fphkyj2) {
            result.put("msg", false);
            result.put("sh", fphkyj2.getSfyj());
            return result;
        }
        Jyls jyls = jylsService.findOne(params);
        List<Kpls> list = kplsService.findAll(params);
        for (Kpls kpls : list) {
            fphkyj.setYxbz("0");
            fphkyj.setLrsj(new Date());
            fphkyj.setXgsj(new Date());
            fphkyj.setDdh(jyls.getDdh());
            ;
            fphkyj.setFpdm(kpls.getFpdm());
            fphkyj.setFphm(kpls.getFphm());
            fphkyj.setGfmc(kpls.getGfmc());
            fphkyj.setSfyj("0");
            fphkyj.setXfid(kpls.getXfid());
            fphkyj.setKplsh(String.valueOf(kpls.getKplsh()));
            fphkyj.setKprq(kpls.getKprq());
            fphkyjService.save(fphkyj);
            fphkyj.setId(null);
        }

        result.put("msg", true);
        return result;
    }

    @RequestMapping(value = "/tjsession")
    @ResponseBody
    public Map tjsession() {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("djh", request.getSession().getAttribute("djh"));
        result.put("tqm", request.getSession().getAttribute("tqm"));
        result.put("zje", request.getSession().getAttribute("je"));
        result.put("slv", request.getSession().getAttribute("slv"));
        result.put("khh", request.getSession().getAttribute("khh"));
        result.put("gsdm", request.getSession().getAttribute("gsdm"));
        return result;
    }

    @RequestMapping(value = "/yxsession")
    @ResponseBody
    public Map yxsession() {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("djh", request.getSession().getAttribute("djh"));
        result.put("serialorder", request.getSession().getAttribute("serialorder"));
        if (null != request.getSession().getAttribute("djh")) {
            Map params = new HashMap<>();
            params.put("djh", String.valueOf(request.getSession().getAttribute("djh")));
            List<Spfyx> list = spfyxService.findAllByParams(params);
            result.put("yx", list);
        }
        request.getSession().setAttribute("msg", "请重新扫描二维码");
        return result;
    }

    @RequestMapping(value = "/fpsession")
    @ResponseBody
    public Map fpsession() {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("serialorder", request.getSession().getAttribute("serialorder"));
        result.put("djh", request.getSession().getAttribute("djh"));
        result.put("pdfdz", request.getSession().getAttribute("pdfdzs"));
        request.getSession().setAttribute("msg", "请重新扫描二维码");
        return result;
    }

    @RequestMapping(value = "/wxkb")
    @ResponseBody
    public Map wxkb(Wxkb wxkb) {
        Map<String, Object> result = new HashMap<String, Object>();
        wxkb.setScsj(new Date());
        wxkbService.save(wxkb);
        result.put("msg", true);
        return result;

    }

    @RequestMapping(value = "/kbcx")
    @ResponseBody
    public Map kbcx() {
        Map<String, Object> result = new HashMap<String, Object>();
        List<Wxkb> list = wxkbService.findAllByParams(null);
        if (list.size() > 0) {
            Wxkb wxkb = list.get(0);
            result.put("accessToken", wxkb.getAccessToken());
            Date old = wxkb.getScsj();
            Date current = new Date();
            long interval = (current.getTime() - old.getTime()) / 1000;
            if (interval > Long.parseLong(wxkb.getExpiresIn())) {
                return hqtk(GET_TOKEN_URL, APP_ID, SECRET);
            } else {
                return scewm("", "");
            }
        } else {
            return hqtk(GET_TOKEN_URL, APP_ID, SECRET);
        }
    }

    public Map hqtk(String apiurl, String appid, String secret) {
        Map<String, Object> result = new HashMap<String, Object>();
        // 获取token
        String turl = String.format("%s?grant_type=client_credential&appid=%s&secret=%s", apiurl, appid, secret);
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
                    // appid"}
                    result.put("success", false);
                    result.put("msg", "获取微信token失败,错误代码为" + map.get("errcode"));
                    return result;
                } else {// 正常情况下{"access_token":"ACCESS_TOKEN","expires_in":7200}
                    List<Wxkb> list = wxkbService.findAllByParams(null);
                    Wxkb wxkb = list.get(0);
                    wxkb.setAccessToken((String) map.get("access_token"));
                    wxkb.setExpiresIn(map.get("expires_in").toString());
                    wxkb.setScsj(new Date());
                    wxkbService.save(wxkb);
                    Map map2 = this.scewm("", "");
                    if ((boolean) map2.get("success")) {

                        return map;
                    } else {
                        result.put("success", false);
                        result.put("msg", (String) map.get("msg") + "错误代码" + (String) map.get("errcode") + "错误信息" + (String) map.get("errmsg"));
                        return result;
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭连接 ,释放资源
            client.getConnectionManager().shutdown();
        }
        return result;
    }

    @RequestMapping(value = "/sckj")
    @ResponseBody
    public Map sckj(String token) {
        token = "tqqALVgPAcVrLMIAIv78ulmoNQqGCa5lMK1xhG6hH4Emxhqw4NNoR_ffsDUaN1tMZj2lbUJ1bXipBIURThDfhD2J0ee5VtIZXpWMgQPTMuZDgKf3BtGMyNwGDVHYnvpxFCEaAIAPMO";
        HttpClient client = new DefaultHttpClient();
        Map map = new HashMap<>();
        try {
            String url = "https://api.weixin.qq.com/card/create?access_token=" + token;
            HttpPost httpPost = new HttpPost(url);
            ObjectMapper mapper = new ObjectMapper();
            Map<Object, Object> date_info = new HashMap<>();
            date_info.put("type", "DATE_TYPE_FIX_TIME_RANGE");
            date_info.put("begin_timestamp", new Date().getTime() / 1000);
            date_info.put("end_timestamp", new Date().getTime() / 1000 + 72 * 60 * 60);
            Map<Object, Object> sku = new HashMap<>();
            sku.put("quantity", 10000000);
            Map<Object, Object> base_info = new HashMap<>();
            base_info.put("logo_url", "http://mmbiz.qpic.cn/mmbiz_png/SGSA3AKjmxdJREpvW9kv6ERMHyUmT4NbWbEHK5p3naVquws5mrL04Bq7zibtTL8d9iccKcZdOf1OYPibKzDib2rDsw/0");
            base_info.put("brand_name", "电子发票");
            base_info.put("code_type", "CODE_TYPE_TEXT");
            base_info.put("title", "电子发票");
            base_info.put("sub_title", "电子发票");
            base_info.put("color", "Color010");
            base_info.put("notice", "使用时向服务员出示此券");
            //base_info.put("service_phone", "020-88888888");
            base_info.put("description", "描述");
            base_info.put("date_info", date_info);
            base_info.put("sku", sku);
            //base_info.put("get_limit", 3);
            //base_info.put("use_custom_code", false);
            base_info.put("bind_openid", false);
            base_info.put("can_share", false);
            base_info.put("can_give_friend", false);
            //base_info.put("center_title", "顶部居中按钮");
            //base_info.put("center_sub_title", "按钮下方的wording");
            //base_info.put("center_url", "www.baidu.com");
            //base_info.put("custom_url_name", "立即使用");
            //base_info.put("custom_url", "http://www.qq.com");
            //base_info.put("custom_url_sub_title", "6个汉字tips");
            //base_info.put("promotion_url_name", "更多优惠");
            //base_info.put("promotion_url",  "http://www.qq.com");
            //base_info.put("source", "大众点评");
            Map<Object, Object> groupon = new HashMap<>();
            groupon.put("base_info", base_info);
            groupon.put("deal_detail", "电子发票");
            Map<Object, Object> card = new HashMap<>();
            card.put("card_type", "GROUPON");
            card.put("groupon", groupon);
            Map<Object, Object> sul = new HashMap<>();
            sul.put("card", card);
            String json = mapper.writeValueAsString(sul);
            httpPost.setEntity(new StringEntity(json, "UTF-8"));
            HttpResponse response = client.execute(httpPost);
            HttpEntity entity = response.getEntity();
            String responseContent = EntityUtils.toString(entity, "UTF-8");
            ObjectMapper jsonparer = new ObjectMapper();
            map = jsonparer.readValue(responseContent, Map.class);
            if (!map.get("errcode").toString().equals("0")) {
                map.put("msg", "生成卡卷错误!" + map.get("errcode"));
                map.put("success", false);
            } else {
                map.put("success", true);
            }
        } catch (Exception e) {
            map.put("success", false);
            map.put("msg", "生成卡卷错误!");
            e.printStackTrace();
            return map;
        } finally {
            client.getConnectionManager().shutdown();
        }
        return map;
    }

    //创建二维码接口
    @RequestMapping(value = "/scewm")
    @ResponseBody
    public Map scewm(String token, String card_id) {
        card_id = "pB0fJv0qJSxG1MWewz5_kGUUE4js";
        token = "tqqALVgPAcVrLMIAIv78ulmoNQqGCa5lMK1xhG6hH4Emxhqw4NNoR_ffsDUaN1tMZj2lbUJ1bXipBIURThDfhD2J0ee5VtIZXpWMgQPTMuZDgKf3BtGMyNwGDVHYnvpxFCEaAIAPMO";
        HttpClient client = new DefaultHttpClient();
        Map<Object, Object> map = new HashMap<>();
        try {
            String url = "https://api.weixin.qq.com/card/qrcode/create?access_token=" + token;
            HttpPost httpPost = new HttpPost(url);
            ObjectMapper mapper = new ObjectMapper();
            Map<Object, Object> sul = new HashMap<>();
            sul.put("action_name", "QR_CARD");
            //sul.put("expire_seconds", 1800);
            Map<Object, Object> action_info = new HashMap<>();
            Map<Object, Object> card = new HashMap<>();
            card.put("card_id", card_id);
            //card.put("code", value)
            //card.put("openid", "oFS7Fjl0WsZ9AMZqrI80nbIq8xrA");
            //card.put("is_unique_code", false);
            //card.put("outer_id", 1);
            action_info.put("card", card);
            sul.put("action_info", action_info);
            String json = mapper.writeValueAsString(sul);
            httpPost.setEntity(new StringEntity(json, "UTF-8"));
            HttpResponse response = client.execute(httpPost);
            HttpEntity entity = response.getEntity();
            String responseContent = EntityUtils.toString(entity, "UTF-8");
            ObjectMapper jsonparer = new ObjectMapper();
            map = jsonparer.readValue(responseContent, Map.class);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.getConnectionManager().shutdown();
        }
        if (map.get("errcode").equals("0")) {
            map.put("msg", "生成错误!" + map.get("errcode"));
            map.put("success", false);
        } else {
            map.put("success", true);
        }
        return map;
    }

    public static void main(String[] args) {
        TijiaoController t = new TijiaoController();
        t.hqtk(GET_TOKEN_URL, APP_ID, SECRET);
    }
}
