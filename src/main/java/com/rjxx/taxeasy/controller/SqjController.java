package com.rjxx.taxeasy.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rjxx.taxeasy.bizcomm.utils.FpclService;
import com.rjxx.taxeasy.bizcomm.utils.GetXmlUtil;
import com.rjxx.taxeasy.bizcomm.utils.HttpUtils;
import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.dao.GsxxJpaDao;
import com.rjxx.taxeasy.dao.SkpJpaDao;
import com.rjxx.taxeasy.dao.WxfpxxJpaDao;
import com.rjxx.taxeasy.dao.XfJpaDao;
import com.rjxx.taxeasy.domains.*;
import com.rjxx.taxeasy.service.*;
import com.rjxx.taxeasy.utils.NumberUtil;
import com.rjxx.taxeasy.utils.ResponseUtil;
import com.rjxx.taxeasy.utils.alipay.AlipayConstants;
import com.rjxx.taxeasy.utils.alipay.AlipayUtils;
import com.rjxx.taxeasy.vo.Spvo;
import com.rjxx.utils.HtmlUtils;
import com.rjxx.utils.MD5Util;
import com.rjxx.utils.StringUtils;
import com.rjxx.utils.XmlUtil;
import com.rjxx.utils.weixin.WeiXinConstants;
import com.rjxx.utils.weixin.WeixinUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/dzfp_sqj")
public class SqjController extends BaseController {

    @Autowired
    private GsxxService gsxxservice;//公司信息
    @Autowired
    private SmtqService smtqService;//扫描提取
    @Autowired
    private JylsService jylsService;//交易流水
    @Autowired
    private KplsService kplsService;//开票流水
    @Autowired
    private TqjlService tqjlService;//提取记录
    @Autowired
    private CszbService cszbService;//参数主表
    @Autowired
    private SkpService skpService;//税控盘
    @Autowired
    private XfService xfService;//销方
    @Autowired
    private YhService yhService;//用户
    @Autowired
    private JyxxService jyxxservice;//交易信息
    @Autowired
    private JyspmxService jyspmxService;//交易商品明细
    @Autowired
    private TqmtqService tqmtqService;//提取码提取
    @Autowired
    private PpService ppService;//品牌
    @Autowired
    private SpService spService;//商品
    @Autowired
    private FpjService fpjService;//发票夹
    @Autowired
    private JyxxsqService jyxxsqService;
    @Autowired
    private FpclService fpclservice;
    @Autowired
    private ResponseUtil responseUtil;

    @Autowired
    private WxfpxxJpaDao wxfpxxJpaDao;

    @Autowired
    private SkpJpaDao skpJpaDao;

    @Autowired
    private XfJpaDao xfJpaDao;

    @Autowired
    private SpvoService spvoService;
    @Autowired
    private GsxxJpaDao gsxxJpaDao;
    //public static final String APP_ID = "wx9abc729e2b4637ee";

    public static final String GET_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";// 获取access

    //public static final String SECRET = "6415ee7a53601b6a0e8b4ac194b382eb";

    @RequestMapping
    @ResponseBody
    public void index() throws Exception {
        String str = request.getParameter("q");
        Map<String, Object> params = new HashMap<>();
        params.put("gsdm", "sqj");
        Gsxx gsxx = gsxxservice.findOneByParams(params);
        if (null == gsxx) {
            request.getSession().setAttribute("msg", "出现未知错误!请重试!");
            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            return;
        }
        if (request.getHeader("user-agent") == null) {
            request.getSession().setAttribute("msg", "出现未知异常!请重试!");
            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            return;
        }
        String ua = request.getHeader("user-agent").toLowerCase();
        if (ua.indexOf("micromessenger") > 0) {
            String url = HtmlUtils.getBasePath(request);
            String openid = String.valueOf(session.getAttribute("openid"));
            if (openid == null || "null".equals(openid)) {
                String ul = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinConstants.APP_ID + "&redirect_uri="
                        + url + "/dzfp_sqj/getWx&" + "response_type=code&scope=snsapi_base&state=" + str
                        + "#wechat_redirect";
                response.sendRedirect(ul);
                return;
            } else {
                sendHtml(str, gsxx);
            }
        } else {
            sendHtml(str, gsxx);
        }
    }

    @RequestMapping(value = "/getWx")
    @ResponseBody
    public void getWx(String state, String code) throws IOException {
        Map params = new HashMap<>();
        params.put("gsdm", "sqj");
        Gsxx gsxx = gsxxservice.findOneByParams(params);
        if (null == gsxx) {
            request.getSession().setAttribute("msg", "出现未知异常!请重试!");
            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            return;
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
                    logger.info(session.getAttribute("openid").toString());
                    map.put("success", true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭连接 ,释放资源
            client.getConnectionManager().shutdown();
        }
        if (state.equals("sqj")) {
            response.sendRedirect(request.getContextPath() + "/smtq/sqj.html?_t=" + System.currentTimeMillis());
            return;
        } else if (state.equals("wdm")) {
            response.sendRedirect(request.getContextPath() + "/smtq/sqjwdm.html?_t=" + System.currentTimeMillis());
            return;
        } else if (state.equals("bss")) {
            response.sendRedirect(request.getContextPath() + "/smtq/sqjss.html?_t=" + System.currentTimeMillis());
            return;
        } else {
            sendHtml(state, gsxx);
        }
    }

    @RequestMapping(value = "/sendHtml")
    @ResponseBody
    public void sendHtml(String state, Gsxx gsxx) throws IOException {
        try {
            byte[] bytes = Base64.decodeBase64(state);
            String csc = new String(bytes);
            String[] cssz = csc.split("&");
            String orderNo = cssz[0].substring(cssz[0].lastIndexOf("=") + 1);
            String orderTime = cssz[1].substring(cssz[1].lastIndexOf("=") + 1);
            String price = cssz[2].substring(cssz[2].lastIndexOf("=") + 1);
            String sn = cssz[3].substring(cssz[3].lastIndexOf("=") + 1);
            String sign = cssz[4].substring(cssz[4].lastIndexOf("=") + 1);
            String dbs = csc.substring(0, csc.lastIndexOf("&") + 1);
            if (null == gsxx) {
                request.getSession().setAttribute("msg", "公司信息不正确!");
                response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                return;
            }
            dbs += "key=" + gsxx.getSecretKey();
            String key1 = MD5Util.generatePassword(dbs);

            if (!sign.equals(key1.toLowerCase())) {
                request.getSession().setAttribute("msg", "秘钥不匹配!");
                response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                return;
            }
            if (null == orderNo || "".equals(orderNo)) {
                request.getSession().setAttribute("msg", "未包含流水号!");
                response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                return;
            }
            if (null == sn || "".equals(sn)) {
                request.getSession().setAttribute("msg", "未包含门店信息!");
                response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                return;
            }
            if (null == orderTime || "".equals(orderTime)) {
                request.getSession().setAttribute("msg", "未包含流水时间!");
                response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                return;
            }
            if (null == price || "".equals(price)) {
                request.getSession().setAttribute("msg", "未包含金额!");
                response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                return;
            }
            Map map2 = new HashMap<>();
            map2.put("gsdm", "sqj");
            map2.put("kpddm", sn);
            Skp skp = skpService.findOneByParams(map2);
            if (null != skp) {
                Xf xf = xfService.findOne(skp.getXfid());
                if (null == xf) {
                    request.getSession().setAttribute("msg", "未查询到销方!");
                    response.sendRedirect(
                            request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                    return;
                }
            } else {
                request.getSession().setAttribute("msg", "未查询到门店号!");
                response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                return;
            }
            request.getSession().setAttribute("q", state);
            request.getSession().setAttribute("orderNo", orderNo);
            request.getSession().setAttribute("orderTime1", orderTime);
            request.getSession().setAttribute("orderTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(new SimpleDateFormat("yyyyMMddHHmmss").parse(orderTime)));
            request.getSession().setAttribute("price", price);
            request.getSession().setAttribute("sn", sn);
            String ddh = (String) request.getSession().getAttribute("orderNo");
            String openid = (String) session.getAttribute("openid");
            Map map = new HashMap<>();
            map.put("tqm", ddh);
            map.put("gsdm", "sqj");
            Jyls jyls = jylsService.findOne(map);
            Map mapo = new HashMap<>();
            mapo.put("tqm", ddh);
            mapo.put("je", price);
            mapo.put("gsdm", "sqj");
            Tqmtq tqmtq = tqmtqService.findOneByParams(mapo);
            List<Kpls> list = jylsService.findByTqm(mapo);
            //是否离线开票
            Cszb zb1 = cszbService.getSpbmbbh("sqj", null,null, "sflxkp");
            if(null==zb1.getCsz()|| zb1.getCsz().equals("否")){
                Map jyxxmap = new HashMap();
                jyxxmap.put("tqm",orderNo);
                jyxxmap.put("je",price);
                jyxxmap.put("gsdm","sqj");
                Jyxx jyxx = jyxxservice.findOneByParams(jyxxmap);
                if(jyxx==null){
                    request.getSession().setAttribute("msg", "交易数据未上传，请联系商家!");
                    response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                    return;
                }
            }
            if (list.size() > 0) {
                if (openid != null && !"null".equals(openid)) {
                    Map<String, Object> param = new HashMap<>();
                    param.put("djh", list.get(0).getDjh());
                    param.put("unionid", openid);
                    Fpj fpj = fpjService.findOneByParams(param);
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
                String pdfdzs = "";
                for (Kpls kpls2 : list) {
                    pdfdzs += kpls2.getPdfurl().replace(".pdf", ".jpg") + ",";
                }
                if (pdfdzs.length() > 0) {
                    request.getSession().setAttribute("djh", list.get(0).getDjh());
                    request.getSession().setAttribute("pdfdzs", pdfdzs.substring(0, pdfdzs.length() - 1));
                }
                Tqjl tqjl = new Tqjl();
                tqjl.setDjh(String.valueOf(list.get(0).getDjh()));
                tqjl.setTqsj(new Date());
                String visiterIP;
                if (request.getHeader("x-forwarded-for") == null) {
                    visiterIP = request.getRemoteAddr();// 访问者IP
                } else {
                    visiterIP = request.getHeader("x-forwarded-for");
                }
                tqjl.setIp(visiterIP);
                tqjl.setJlly("1");
                String llqxx = request.getHeader("User-Agent");
                tqjl.setLlqxx(llqxx);
                tqjlService.save(tqjl);
                //表示已经开过票
                if (WeixinUtils.isWeiXinBrowser(request)) {
                    WxFpxx wxFpxxByTqm = wxfpxxJpaDao.selsetByOrderNo(orderNo);
                    if (null == wxFpxxByTqm) {
                        WxFpxx wFpxx = new WxFpxx();
                        wFpxx.setTqm(orderNo);
                        wFpxx.setGsdm(gsxx.getGsdm());
                        wFpxx.setOrderNo(orderNo);
                        wFpxx.setQ(state);
                        wFpxx.setWxtype("2");
                        wFpxx.setOpenId(openid);
                        wFpxx.setKplsh(list.get(0).getKplsh().toString());
                        wxfpxxJpaDao.save(wFpxx);
                    } else {
                        wxFpxxByTqm.setTqm(orderNo);
                        wxFpxxByTqm.setGsdm(gsxx.getGsdm());
                        wxFpxxByTqm.setQ(state);
                        wxFpxxByTqm.setOpenId(openid);
                        wxFpxxByTqm.setOrderNo(orderNo);
                        wxFpxxByTqm.setWxtype("2");//1:申请开票2：领取发票
                        wxFpxxByTqm.setKplsh(list.get(0).getKplsh().toString());
                        if (wxFpxxByTqm.getCode() != null || !"".equals(wxFpxxByTqm.getCode())) {
                            String notNullCode = wxFpxxByTqm.getCode();
                            wxFpxxByTqm.setCode(notNullCode);
                        }
                        wxfpxxJpaDao.save(wxFpxxByTqm);
                    }
                }
                request.getSession().setAttribute("serialorder", list.get(0).getSerialorder());
                response.sendRedirect(request.getContextPath() + "/fp.html?_t=" + System.currentTimeMillis());
                return;
            } else if (null != jyls && null != jyls.getDjh()) {
                response.sendRedirect(request.getContextPath() + "/smtq/smtq3.html?_t=" + System.currentTimeMillis());
                return;
            } else if (null != tqmtq && null != tqmtq.getId()) {
                response.sendRedirect(request.getContextPath() + "/smtq/smtq3.html?_t=" + System.currentTimeMillis());
                return;
            } else {
                if (WeixinUtils.isWeiXinBrowser(request)) {
                    logger.info("微信扫描------");
                    WxFpxx wxFpxxByTqm = wxfpxxJpaDao.selsetByOrderNo(orderNo);
                    //第一次扫描
                    if (null == wxFpxxByTqm) {
                        WxFpxx wxFpxx = new WxFpxx();
                        wxFpxx.setTqm(orderNo);
                        wxFpxx.setGsdm(gsxx.getGsdm());
                        wxFpxx.setOrderNo(orderNo);
                        wxFpxx.setQ(state);
                        wxFpxx.setWxtype("1");
                        //微信
                        wxFpxx.setOpenId((String) session.getAttribute("openid"));
                        try {
                            wxfpxxJpaDao.save(wxFpxx);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        wxFpxxByTqm.setTqm(orderNo);
                        wxFpxxByTqm.setGsdm(gsxx.getGsdm());
                        wxFpxxByTqm.setOrderNo(orderNo);
                        wxFpxxByTqm.setQ(state);
                        wxFpxxByTqm.setWxtype("1");
                        wxFpxxByTqm.setOpenId((String) session.getAttribute("openid"));
                        if (wxFpxxByTqm.getCode() != null || !"".equals(wxFpxxByTqm.getCode())) {
                            String notNullCode = wxFpxxByTqm.getCode();
                            wxFpxxByTqm.setCode(notNullCode);
                        }
                        try {
                            wxfpxxJpaDao.save(wxFpxxByTqm);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (null != skp.getPid()) {
                    Pp pp = ppService.findOne(skp.getPid());
                    if (null != pp.getPpurl()) {
                        response.sendRedirect(request.getContextPath() + pp.getPpurl() + "?_t" + System.currentTimeMillis());
                        return;
                    }
                } else {
                    response.sendRedirect(request.getContextPath() + "/smtq/smtq1.html?_t=" + System.currentTimeMillis());
                    return;
                }
            }
        } catch (Exception e) {
            request.getSession().setAttribute("msg", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            return;
        }
    }

    @RequestMapping(value = "/smtq3")
    @ResponseBody
    public void tztz() throws IOException {
        response.sendRedirect(request.getContextPath() + "/smtq/smtq3.html?_t=" + System.currentTimeMillis());
    }

    @RequestMapping(value = "/demo")
    @ResponseBody
    public void demo() throws IOException {
        response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
    }

    @RequestMapping(value = "/smtq2")
    @ResponseBody
    public void smtq2(String openid) throws IOException {
        response.sendRedirect(request.getContextPath() + "/smtq/smtq2.html?_t=" + System.currentTimeMillis());
    }

    @RequestMapping(value = "/bangzhu")
    @ResponseBody
    public void bangzhu() throws IOException {
        response.sendRedirect(request.getContextPath() + "/smtq/smtq2.html?_t=" + System.currentTimeMillis());
    }

    @RequestMapping(value = "/yxxg")
    @ResponseBody
    public void yxxg() throws IOException {
        response.sendRedirect(request.getContextPath() + "/smtq/xgyx.html?_t=" + System.currentTimeMillis());
    }

    @RequestMapping(value = "/fp")
    @ResponseBody
    public void fp() throws IOException {
        response.sendRedirect(request.getContextPath() + "/fp.html?_t=" + System.currentTimeMillis());
    }

    @RequestMapping(value = "/getSmsj")
    @ResponseBody
    public Map getSmsj() {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("orderNo", request.getSession().getAttribute("orderNo"));
        result.put("orderTime", request.getSession().getAttribute("orderTime"));
        result.put("price", request.getSession().getAttribute("price"));
        result.put("sn", request.getSession().getAttribute("sn"));
        String llqxx = request.getHeader("User-Agent");
        if (llqxx.contains("MicroMessenger")) {
            result.put("isWx", true);
        } else {
            result.put("isWx", false);
        }
        return result;
    }

    @RequestMapping(value = "/xgyx")
    @ResponseBody
    public Map xgyx(String yx) {
        Map<String, Object> result = new HashMap<String, Object>();
        Map params = new HashMap<>();
        params.put("ddh", (String) request.getSession().getAttribute("orderNo"));
        params.put("tqm", (String) request.getSession().getAttribute("tqm"));
        Smtq smtq = smtqService.findOneByParams(params);
        Tqmtq tqmtq = tqmtqService.findOneByParams(params);
        if (null != params.get("ddh")) {
            smtq.setYx(yx);
            smtqService.save(smtq);
        } else if (null != params.get("tqm")) {
            tqmtq.setGfemail(yx);
            tqmtqService.save(tqmtq);
        }
        result.put("num", "1");
        return result;
    }

    @RequestMapping(value = "/save")
    @ResponseBody
    @Transactional
    public Map save(String fptt, String nsrsbh, String dz, String dh, String khh, String khhzh, String yx, String sj)
            throws Exception {
        Map<String, Object> result = new HashMap<>();
        String ddh = (String) request.getSession().getAttribute("orderNo");
        String openid = (String) request.getSession().getAttribute("openid");

        logger.info(openid);
        if (StringUtils.isBlank(ddh)) {
            result.put("msg", "1");
            request.getSession().setAttribute("msg", "会话已过期，请重新获取!");
            return result;
        }
        Map params = new HashMap<>();
        params.put("gsdm", "sqj");
        Gsxx gsxx = gsxxservice.findOneByParams(params);
        Map map = new HashMap<>();
        map.put("ddh", ddh);
        Smtq smtq1 = smtqService.findOneByParams(map);
        Smtq smtq;
        if (null != smtq1) {
            result.put("failure", true);
            result.put("xx", "您的请求已接收，请勿重复提交");
            return result;
        } else {
            smtq = new Smtq();
        }
        smtq.setDdh((String) request.getSession().getAttribute("orderNo"));
        smtq.setKpddm((String) request.getSession().getAttribute("sn"));
        smtq.setJylssj(
                new SimpleDateFormat("yyyyMMddHHmmss").parse((String) request.getSession().getAttribute("orderTime1")));
        smtq.setZje(Double.parseDouble((String) request.getSession().getAttribute("price")));
        smtq.setGfmc(fptt);
        smtq.setNsrsbh(nsrsbh);
        smtq.setDz(dz);
        smtq.setDh(dh);
        smtq.setKhh(khh);
        smtq.setKhhzh(khhzh);
        smtq.setYx(yx);
        smtq.setSj(sj);
        smtq.setFpzt("07");
        smtq.setYxbz("1");
        smtq.setGsdm(gsxx.getGsdm());
        if (openid != null && !"null".equals(openid)) {
            smtq.setOpenid(openid);
        }
        String llqxx = request.getHeader("User-Agent");
        smtq.setLlqxx(llqxx);
        smtq.setLrsj(new Date());
        smtqService.save(smtq);
        result.put("failure", false);
        result.put("msg", "2");
        return result;
    }

    @RequestMapping(value = "/getZje")
    @ResponseBody
    public Map getZje() {
        Map<String, Object> result = new HashMap<>();
        String sn = (String) request.getSession().getAttribute("sn");
        if (StringUtils.isNotBlank(sn)) {
            Map map2 = new HashMap<>();
            map2.put("gsdm", "sqj");
            map2.put("kpddm", sn);
            Skp skp = skpService.findOneByParams(map2);
            if (skp != null) {
                String spsl = cszbService.getSpbmbbh("sqj", skp.getXfid(), skp.getId(), "spsl").getCsz();
                result.put("slv", spsl);
            }
        }
        result.put("zje", request.getSession().getAttribute("price"));
        return result;
    }

    @RequestMapping(value = "/getmsg")
    @ResponseBody
    public Map getMsg() {
        Map<String, Object> result = new HashMap<>();
        result.put("msg", request.getSession().getAttribute("msg"));
        result.put("clztdm", request.getSession().getAttribute("clztdm"));
        return result;
    }

    @RequestMapping(value = "/fpsession")
    @ResponseBody
    public Map fpsession() {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("djh", request.getSession().getAttribute("djh"));
        result.put("pdfdz", request.getSession().getAttribute("pdfdzs"));
        return result;
    }

    @RequestMapping(value = "/saveOpenid")
    @ResponseBody
    public Map saveOpenid(String openid) {
        Map<String, Object> result = new HashMap<String, Object>();
        openid = (String) session.getAttribute("openid");
        Integer djh = (Integer) session.getAttribute("djh");
        if (djh != null && openid != null && !openid.equals("null")) {
            Map<String, Object> param = new HashMap<>();
            param.put("djh", djh);
            param.put("unionid", openid);
            Fpj fpj = fpjService.findOneByParams(param);
            if (fpj == null) {
                fpj = new Fpj();
                fpj.setDjh(djh);
                fpj.setUnionid(openid);
                fpj.setYxbz("1");
                fpj.setLrsj(new Date());
                fpj.setXgsj(new Date());
                fpjService.save(fpj);
            }
        }
        return result;
    }

//    public static void main(String[] args) {
//        String str = "b3JkZXJObz0yMDE2MTAxMzEyNTUxMTEyMzQmb3JkZXJUaW1lPTIwMTYxMDEzMTI1NTExJnByaWNlPTIzJnNpZ249YjBjODdjY2U4NmE0ZGZlYmVkYzA1ZDgzZTdmNzY3OTA=";
//        byte[] bt = null;
//        try {
//            sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();
//            bt = decoder.decodeBuffer(str);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        String csc = new String(bt);
//        System.out.println(csc);
//    }

    // 食其家 采用提取码提取方式
    // 跳转到sqj提取码提取页面
    @RequestMapping(value = "/sqj")
    @ResponseBody
    public void tqmtq() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("gsdm", "sqj");
        String str = "sqj";
        Gsxx gsxx = gsxxservice.findOneByParams(params);
        if (null == gsxx) {
            request.getSession().setAttribute("msg", "出现未知异常!请重试!");
            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            return;
        }
        if (request.getHeader("user-agent") == null) {
            request.getSession().setAttribute("msg", "出现未知异常!请重试!");
            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            return;
        }
        String ua = request.getHeader("user-agent").toLowerCase();
        if (ua.indexOf("micromessenger") > 0) {
            String url = HtmlUtils.getBasePath(request);
            String openid = String.valueOf(session.getAttribute("openid"));
            if (openid == null || "null".equals(openid)) {
                String ul = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinConstants.APP_ID + "&redirect_uri="
                        + url + "/dzfp_sqj/getWx&" + "response_type=code&scope=snsapi_base&state=" + str
                        + "#wechat_redirect";
                response.sendRedirect(ul);
                return;
            } else {
                response.sendRedirect(request.getContextPath() + "/smtq/sqj.html?_t=" + System.currentTimeMillis());
                return;
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/smtq/sqj.html?_t=" + System.currentTimeMillis());
            return;
        }

    }

    // 食其家 寿司采用提取码提取方式
    // 跳转到sqj提取码提取页面
    @RequestMapping(value = "/ss")
    @ResponseBody
    public void tqmsstq() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("gsdm", "sqj");
        String str = "bss";
        Gsxx gsxx = gsxxservice.findOneByParams(params);
        if (null == gsxx) {
            request.getSession().setAttribute("msg", "出现未知异常!请重试!");
            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            return;
        }
        if (request.getHeader("user-agent") == null) {
            request.getSession().setAttribute("msg", "出现未知异常!请重试!");
            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            return;
        }
        String ua = request.getHeader("user-agent").toLowerCase();
        if (ua.indexOf("micromessenger") > 0) {
            String url = HtmlUtils.getBasePath(request);
            String openid = String.valueOf(session.getAttribute("openid"));
            if (openid == null || "null".equals(openid)) {
                String ul = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinConstants.APP_ID + "&redirect_uri="
                        + url + "/dzfp_sqj/getWx&" + "response_type=code&scope=snsapi_base&state=" + str
                        + "#wechat_redirect";
                response.sendRedirect(ul);
                return;
            } else {
                response.sendRedirect(request.getContextPath() + "/smtq/sqjss.html?_t=" + System.currentTimeMillis());
                return;
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/smtq/sqjss.html?_t=" + System.currentTimeMillis());
            return;
        }

    }

    // 食其家 寿司采用提取码提取方式
    // 跳转到sqj提取码提取页面
    @RequestMapping(value = "/wdm")
    @ResponseBody
    public void tqmwdmtq() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("gsdm", "sqj");
        String str = "wdm";
        Gsxx gsxx = gsxxservice.findOneByParams(params);
        if (null == gsxx) {
            request.getSession().setAttribute("msg", "出现未知异常!请重试!");
            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            return;
        }
        if (request.getHeader("user-agent") == null) {
            request.getSession().setAttribute("msg", "出现未知异常!请重试!");
            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            return;
        }
        String ua = request.getHeader("user-agent").toLowerCase();
        if (ua.indexOf("micromessenger") > 0) {
            String url = HtmlUtils.getBasePath(request);
            String openid = String.valueOf(session.getAttribute("openid"));
            if (openid == null || "null".equals(openid)) {
                String ul = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinConstants.APP_ID + "&redirect_uri="
                        + url + "/dzfp_sqj/getWx&" + "response_type=code&scope=snsapi_base&state=" + str
                        + "#wechat_redirect";
                response.sendRedirect(ul);
                return;
            } else {
                response.sendRedirect(request.getContextPath() + "/smtq/sqjwdm.html?_t=" + System.currentTimeMillis());
                return;
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/smtq/sqjwdm.html?_t=" + System.currentTimeMillis());
            return;
        }

    }

    // 校验提取码是否正确
    @RequestMapping(value = "/tqyz")
    @ResponseBody
    public Map<String, Object> tqyz(String tqm, String code, String je) {
        String sessionCode = (String) session.getAttribute("rand");
        String openid = (String) session.getAttribute("openid");
        Map<String, Object> result = new HashMap<String, Object>();
        if (code != null && sessionCode != null && code.equals(sessionCode)) {
            //验证提取码
            String storeNo = null;
            if (tqm.length() > 12) {
                storeNo = tqm.substring(8, 12);
                Map params = new HashMap();
                params.put("kpddm", storeNo);
                Skp skp = skpService.findOneByParams(params);
                if (skp == null) {
                    //提取码不正确
                    result.put("num", "11");
                    return result;
                }
                Cszb cszb = cszbService.getSpbmbbh(skp.getGsdm(), skp.getXfid(), skp.getId(), "spsl");
                request.getSession().setAttribute("slv", cszb.getCsz());
            } else {
                //提取码不正确
                result.put("num", "11");
                return result;
            }
            Map map = new HashMap<>();
            map.put("tqm", tqm);
            map.put("je", je);
            map.put("gsdm", "sqj");
            Jyxx jyxx = jyxxservice.findOneByParams(map);
            Jyls jyls = jylsService.findOne(map);
            List<Kpls> list = jylsService.findByTqm(map);
            if (list.size() > 0) {
                if (openid != null && !"null".equals(openid)) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("djh", jyls.getDjh());
                    params.put("unionid", openid);
                    Fpj fpj = fpjService.findOneByParams(params);
                    if (fpj == null) {
                        fpj = new Fpj();
                        fpj.setDjh(jyls.getDjh());
                        fpj.setUnionid(openid);
                        fpj.setYxbz("1");
                        fpj.setLrsj(new Date());
                        fpj.setXgsj(new Date());
                        fpjService.save(fpj);
                    }
                }
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
                    visiterIP = request.getHeader("x-forwarded-for");
                }
                tqjl.setIp(visiterIP);
                String llqxx = request.getHeader("User-Agent");
                tqjl.setLlqxx(llqxx);
                tqjlService.save(tqjl);
                request.getSession().setAttribute("serialorder", list.get(0).getSerialorder());
            } else if (null != jyls && null != jyls.getDjh()) {
                result.put("num", "6");
            }  else if (null == jyxx) {
                result.put("num", "9");
            } else {
                request.getSession().setAttribute("tqm", tqm);
                request.getSession().setAttribute("je", je);
                request.getSession().setAttribute("jyxx", jyxx);
                result.put("num", "5");
            }
        } else {
            result.put("num", "4");
        }
        return result;
    }

    // 获取购方信息,保存到交易流水
    @RequestMapping(value = "/saveLs" ,method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public Map<String, Object> saveLs(String fptt, String nsrsbh, String dz, String dh, String khh, String khhzh,
                                      String yx) {
        Map<String, Object> result = new HashMap<String, Object>();
        String tqm = String.valueOf(request.getSession().getAttribute("tqm"));
        String openid = String.valueOf(request.getSession().getAttribute("openid"));
        try {
            if (null == tqm || "".equals(tqm)) {
                request.getSession().setAttribute("msg", "会话已过期，请重试!");
                response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                return null;
            }
            Map map = new HashMap<>();
            map.put("tqm", tqm);
            map.put("je", String.valueOf(request.getSession().getAttribute("je")));
            map.put("gsdm", "sqj");
            Tqmtq tqmtq = tqmtqService.findOneByParams(map);
            Jyls jyls1 = jylsService.findOne(map);
            if (null != tqmtq && null != tqmtq.getId()) {
                result.put("msg", "该提取码已提交过申请!");
                return result;
            }
            if (jyls1 != null) {
                result.put("msg", "该订单正在开票!");
                return result;
            }
            Jyxx jyxx = jyxxservice.findOneByParams(map);
            String orderNo="";
            String orderTime="";
            String price = "";
            String storeNo = "";
            //是否离线开票
            Cszb zb1 = cszbService.getSpbmbbh("sqj", null,null, "sflxkp");
            if(null!=zb1.getCsz()|| zb1.getCsz().equals("是")){
                if(null!=request.getSession().getAttribute("q")&& !"".equals(request.getSession().getAttribute("q"))){
                    String q = request.getSession().getAttribute("q").toString();
                    byte[] bytes = org.apache.commons.codec.binary.Base64.decodeBase64(q);
                    String csc = new String(bytes);
                    String[] cssz = csc.split("&");
                    orderNo = cssz[0].substring(cssz[0].lastIndexOf("=") + 1);
                    orderTime = cssz[1].substring(cssz[1].lastIndexOf("=") + 1);
                    price = cssz[2].substring(cssz[2].lastIndexOf("=") + 1);
                    storeNo = cssz[3].substring(cssz[3].lastIndexOf("=") + 1);
                }else {
                    request.getSession().setAttribute("msg", "没有交易数据，请联系商家!");
                    response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                    return null;
                }
            }
            if(null!=jyxx){
                 orderNo = jyxx.getOrderNo();
                 orderTime = jyxx.getOrderTime();
                 price = jyxx.getPrice().toString();
                 storeNo = jyxx.getStoreNo();
            }else {
                request.getSession().setAttribute("msg", "没有交易数据，请联系商家!");
                response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                return null;
            }
            Skp skp = skpJpaDao.findOneByKpddmAndGsdm(storeNo, "sqj");
            Integer xfid = skp.getXfid(); //销方id
            Xf xf = xfJpaDao.findOneById(xfid);
            Integer kpdid = skp.getId();//税控盘id(开票点id)
            Jyxxsq jyxxsq = new Jyxxsq();
            jyxxsq.setJshj(Double.valueOf(price));
            jyxxsq.setDdh(orderNo);
            jyxxsq.setGsdm("sqj");
            jyxxsq.setKpddm(storeNo);
            jyxxsq.setXfmc(xf.getXfmc());
            jyxxsq.setKpr(xf.getKpr());
            jyxxsq.setFhr(xf.getFhr());
            jyxxsq.setSkr(xf.getSkr());
            jyxxsq.setXfid(xfid);
            jyxxsq.setXfsh(xf.getXfsh());
            jyxxsq.setXfyhzh(xf.getXfyhzh());
            jyxxsq.setXfyh(xf.getXfyh());
            jyxxsq.setXfdh(xf.getXfdh());//销方电话
            jyxxsq.setXfdz(xf.getXfdz());//销方地址
            jyxxsq.setXflxr(xf.getXflxr());//销方联系人
            jyxxsq.setXfyb(xf.getXfyb());//销方邮编
            jyxxsq.setGfmc(fptt);
            jyxxsq.setGfsh(nsrsbh);
            if (null != nsrsbh && !"".equals(nsrsbh)) {
                jyxxsq.setGflx("1");
            } else {
                jyxxsq.setGflx("0");
            }

            jyxxsq.setGfemail(yx);
            jyxxsq.setGfdz(dz);
            jyxxsq.setGfdh(dh);
            jyxxsq.setGfyhzh(khhzh);
            jyxxsq.setGfyh(khh);
            jyxxsq.setJylsh(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + NumberUtil.getRandomLetter());
            jyxxsq.setFpzldm("12");
            jyxxsq.setFpczlxdm("11");
            jyxxsq.setSffsyj("1");
            jyxxsq.setZsfs("0");
            jyxxsq.setHsbz("1");
            //jyxxsq.setSjly("1");
            String userId = (String) request.getSession().getAttribute(AlipayConstants.ALIPAY_USER_ID);//支付宝userid
            if(AlipayUtils.isAlipayBrowser(request)){
                jyxxsq.setOpenid(userId);
                jyxxsq.setSjly("5");//数据来源--支付宝
            }else if(WeixinUtils.isWeiXinBrowser(request)){
                jyxxsq.setOpenid(openid);
                jyxxsq.setSjly("4");//数据来源--微信
            }else {
                jyxxsq.setSjly("1");//数据来源 --接口
            }
            jyxxsq.setOpenid(openid);
            jyxxsq.setLrsj(new Date());
            jyxxsq.setXgsj(new Date());
            jyxxsq.setDdrq(new SimpleDateFormat("yyyyMMddHHmmss").parse(orderTime));
            jyxxsq.setTqm(orderNo);

            List<Jymxsq> jymxsqList = new ArrayList<>();
            Jymxsq jymxsq = new Jymxsq();
            jymxsq.setJshj(Double.valueOf(price));
            Cszb cszb = cszbService.getSpbmbbh("sqj", xfid, kpdid, "dyspbmb");
            Map mapoo = new HashMap();
            mapoo.put("gsdm", "sqj");
            if (cszb.getCsz() != null) {
                mapoo.put("spdm", cszb.getCsz());
            }
            Spvo spvo = spvoService.findOneSpvo(mapoo);
            jymxsq.setSpdm(spvo.getSpbm());
            jymxsq.setYhzcmc(spvo.getYhzcmc());
            jymxsq.setYhzcbs(spvo.getYhzcbs());
            jymxsq.setLslbz(spvo.getLslbz());
            jymxsq.setFphxz("0");
            jymxsq.setSpmc(spvo.getSpmc());
            jymxsq.setLrsj(new Date());
            jymxsq.setXgsj(new Date());
            jymxsq.setSpsl(spvo.getSl());
            jymxsq.setSpje(jymxsq.getJshj());
            jymxsqList.add(jymxsq);

            List<Jyzfmx> jyzfmxList = new ArrayList<>();

            String xml = GetXmlUtil.getFpkjXml(jyxxsq, jymxsqList, jyzfmxList);
            Gsxx oneByGsdm = gsxxJpaDao.findOneByGsdm("sqj");
            String appid = oneByGsdm.getAppKey();
            String key = oneByGsdm.getSecretKey();
            String resultxml = HttpUtils.HttpUrlPost(xml, appid, key);

            Map<String, Object> resultMap = XmlUtil.xml2Map(resultxml);
            String returnMsg = resultMap.get("ReturnMessage").toString();
            String returnCode = resultMap.get("ReturnCode").toString();
            Map map2 = new HashMap();
            map2.put("returnMsg", returnMsg);
            map2.put("returnCode", returnCode);
            map2.put("serialorder",  jyxxsq.getJylsh()+jyxxsq.getDdh());
            if (null != returnCode && "0000".equals(returnCode)) {
                Tqmtq tqmtq1 = new Tqmtq();
                tqmtq1.setDdh(tqm);
                tqmtq1.setLrsj(new Date());
                tqmtq1.setZje(Double.valueOf(price));
                tqmtq1.setGfmc(fptt);
                tqmtq1.setNsrsbh(nsrsbh);
                tqmtq1.setDz(dz);
                tqmtq1.setDh(dh);
                tqmtq1.setKhh(khh);
                tqmtq1.setKhhzh(khhzh);
                tqmtq1.setFpzt("0");
                tqmtq1.setYxbz("1");
                tqmtq1.setGfemail(yx);
                tqmtq1.setGsdm("sqj");
                String llqxx = request.getHeader("User-Agent");
                tqmtq1.setLlqxx(llqxx);
                if (openid != null && !"null".equals(openid)) {
                    tqmtq1.setOpenid(openid);
                }
                tqmtqService.save(tqmtq1);
                result.put("msg", "1");
            } else {
                result.put("msg", returnMsg);
            }
            logger.info("=++++++++++++++++++++++++++++++++食其家跳转-----+++++"+ JSON.toJSONString(result));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
