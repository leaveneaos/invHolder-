package com.rjxx.taxeasy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rjxx.taxeasy.bizcomm.utils.AfFpclService;
import com.rjxx.taxeasy.bizcomm.utils.SendalEmail;
import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.domains.*;
import com.rjxx.taxeasy.service.*;
import com.rjxx.utils.HtmlUtils;
import com.rjxx.utils.MD5Util;
import com.rjxx.utils.StringUtils;
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
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2017/6/20 0020.
 */
@Controller
@RequestMapping("/af")
public class AfController extends BaseController {
    @Autowired
    private GsxxService gsxxservice;//公司信息
    @Autowired
    private SmtqService smtqService;//扫描提取
    @Autowired
    private JylsService jylsService;//交易流水
    @Autowired
    private TqjlService tqjlService;//提取记录
    @Autowired
    private CszbService cszbService;//参数主表
    @Autowired
    private SkpService skpService;//税控盘
    @Autowired
    private XfService xfService;//销方
    @Autowired
    private JyxxsqService jyxxsqservice;//交易信息申请
    @Autowired
    private JymxsqClService jymxsqClService;//交易明细申请
    @Autowired
    private TqmtqService tqmtqService;//提取码提取
    @Autowired
    private PpService ppService;//品牌
    @Autowired
    private FpjService fpjService;//发票夹
    @Autowired
    private AfFpclService afFpclService;
    @Autowired
    private KplsService kplsService;
    @Autowired
    private SendalEmail se;
    @Autowired
    private SpfyxService spfyxService;

    public static final String APP_ID ="wx9abc729e2b4637ee";

    public static final String GET_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";

    public static final String SECRET = "6415ee7a53601b6a0e8b4ac194b382eb";

    @RequestMapping
    @ResponseBody
    public void index() throws Exception{
        //String str = request.getParameter("q");
        Map<String,Object> params = new HashMap<>();
        params.put("gsdm","af");
        Gsxx gsxx = gsxxservice.findOneByParams(params);
        if(gsxx.getWxappid() == null || gsxx.getWxsecret() == null){
            gsxx.setWxappid(APP_ID);
            gsxx.setWxsecret(SECRET);
        }
        if (gsxx.getWxappid() == null || gsxx.getWxsecret() == null) {
            gsxx.setWxappid(APP_ID);
            gsxx.setWxsecret(SECRET);
        }
        String ua = request.getHeader("user-agent").toLowerCase();
        if (ua.indexOf("micromessenger") > 0) {
            String url = HtmlUtils.getBasePath(request);
            String openid = String.valueOf(session.getAttribute("openid"));
            if (openid == null || "null".equals(openid)) {
                String ul = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + gsxx.getWxappid() + "&redirect_uri="
                        + url + "/af/getWx&" + "response_type=code&scope=snsapi_base&state=" + "af"
                        + "#wechat_redirect";
                response.sendRedirect(ul);
                return;
            } else {
                response.sendRedirect(request.getContextPath() + "/af/af.html?_t=" + System.currentTimeMillis());
                return;
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/af/af.html?_t=" + System.currentTimeMillis());
            return;
        }

    }

    @RequestMapping(value = "/getWx")
    @ResponseBody
    public void getWx(String state,String code) throws IOException{
        Map params = new HashMap<>();
        params.put("gsdm","af");
        Gsxx gsxx = gsxxservice.findOneByParams(params);
        if(gsxx.getWxappid() == null || gsxx.getWxsecret() ==null){
            gsxx.setWxappid(APP_ID);
            gsxx.setWxsecret(SECRET);
        }
        String turl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + gsxx.getWxappid() + "&secret="
                + gsxx.getWxsecret() + "&code=" + code + "&grant_type=authorization_code";
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
                    session.setAttribute("access_token", map.get("access_token"));
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
        if(state.equals("af")){
            response.sendRedirect(request.getContextPath() + "/af/af.html?_t="+System.currentTimeMillis() );
            return;
        }else {
            sendHtml(state,gsxx);
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
            map2.put("gsdm", "af");
            //map2.put("kpddm", "AF001");
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
            request.getSession().setAttribute("orderNo", orderNo);
            request.getSession().setAttribute("orderTime1", orderTime);
            request.getSession().setAttribute("orderTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(new SimpleDateFormat("yyyyMMddHHmmss").parse(orderTime)));
            request.getSession().setAttribute("price", price);
            request.getSession().setAttribute("sn", sn);
            String ddh = (String) request.getSession().getAttribute("orderNo");
            String openid = (String) session.getAttribute("openid");
            Map map = new HashMap<>();
            map.put("ddh", ddh);
            map.put("gsdm", "af");
            Smtq smtq1 = smtqService.findOneByParams(map);
            Map mapo = new HashMap<>();
            mapo.put("tqm", ddh);
            mapo.put("je", String.valueOf(request.getSession().getAttribute("price")));
            mapo.put("gsdm", "af");
            Tqmtq tqmtq = tqmtqService.findOneByParams(mapo);
            List<Kpls> list = jylsService.findByTqm(map);
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
                response.sendRedirect(request.getContextPath() + "/fp.html?_t=" + System.currentTimeMillis());
                return;
            }
            if (null != smtq1 && null != smtq1.getId()) {
                response.sendRedirect(request.getContextPath() + "/smtq/smtq3.html?_t=" + System.currentTimeMillis());
                return;
            } else if (null != tqmtq && null != tqmtq.getId()) {
                response.sendRedirect(request.getContextPath() + "/smtq/smtq3.html?_t=" + System.currentTimeMillis());
                return;
            } else {
                if (null != skp.getPid()) {
                    Pp pp = ppService.findOne(skp.getPid());
                    if (null != pp.getPpurl()) {
                        response.sendRedirect(
                                request.getContextPath() + pp.getPpurl() + "?_t" + System.currentTimeMillis());
                        return;
                    }
                } else {
                    response.sendRedirect(
                            request.getContextPath() + "/af/af.html?_t=" + System.currentTimeMillis());
                    return;
                }
            }
        } catch (Exception e) {
            request.getSession().setAttribute("msg", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            return;
        }
    }


    @RequestMapping(value = "/bangzhu")
    @ResponseBody
    public void bangzhu() throws IOException {
        response.sendRedirect(request.getContextPath() + "/smtq/smtq2.html?_t=" + System.currentTimeMillis());
    }




    @RequestMapping(value = "/fpsession")
    @ResponseBody
    public Map fpsession() {
        Map<String, Object> result = new HashMap<String, Object>();
        Map map2 = new HashMap();

        map2.put("serialorder",request.getSession().getAttribute("serialorder"));
        List<Kpls> kplsList = kplsService.findAll(map2);
        String pdfdzs = "";
        for (Kpls kpls2 : kplsList) {
            pdfdzs += kpls2.getPdfurl().replace(".pdf", ".jpg") + ",";
        }
        if (pdfdzs.length() > 0) {
            result.put("pdfdzs", pdfdzs.substring(0, pdfdzs.length() - 1));
            request.getSession().setAttribute("pdfdzs", pdfdzs.substring(0, pdfdzs.length() - 1));
        }
        result.put("serialorder", request.getSession().getAttribute("serialorder"));
        request.getSession().setAttribute("msg", "请重新扫描二维码");
        return result;
    }

    public static void main(String[] args) {
        String str = "b3JkZXJObz0yMDE2MTAxMzEyNTUxMTEyMzQmb3JkZXJUaW1lPTIwMTYxMDEzMTI1NTExJnByaWNlPTIzJnNpZ249YjBjODdjY2U4NmE0ZGZlYmVkYzA1ZDgzZTdmNzY3OTA=";
        byte[] bt = null;
        try {
            sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();
            bt = decoder.decodeBuffer(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String csc = new String(bt);
        System.out.println(csc);
    }

    // 校验提取码是否正确
    @RequestMapping(value = "/tqyz")
    @ResponseBody
    public Map<String, Object> tqyz(String tqm, String code, String je) {
        String sessionCode = (String) session.getAttribute("rand");
        String openid = (String) session.getAttribute("openid");
        Map<String, Object> result = new HashMap<String, Object>();
        if (code != null && sessionCode != null && code.equals(sessionCode)) {
            Map map = new HashMap<>();
            map.put("tqm", tqm);
            map.put("gsdm", "af");
            Map map2 = new HashMap<>();
            Jyxxsq jyxxsq = jyxxsqservice.findOneByTqmAndJshj(map);//为了校验提取码是否正确,若正确进一步校验提取码加金额是否正确
            Jyxxsq jyxxsq2 = null;  //为了校验提取码加金额是否正确
            if(null == jyxxsq || jyxxsq.equals("")){
                result.put("num", "11"); //提取码不正确
                return result;
            }else{
                map2.put("tqm", tqm);
                map2.put("je", je);
                map2.put("gsdm", "af");
                jyxxsq2 = jyxxsqservice.findOneByTqmAndJshj(map2);
                if(null == jyxxsq2 || jyxxsq2.equals("")){
                    result.put("num", "3");//金额不正确
                    return result;
                }
            }
            Map param = new HashMap();
            param.put("kpddm", jyxxsq.getKpddm());
            param.put("gsdm","af");
            Skp skp = skpService.findOneByParams(param);
            Cszb cszb = cszbService.getSpbmbbh(skp.getGsdm(), skp.getXfid(), skp.getId(), "spsl");
            request.getSession().setAttribute("slv", cszb.getCsz());



            Map params3 = new HashMap();
            params3.put("sqlsh",jyxxsq.getSqlsh());
            List<JymxsqCl> jymxsqClList = jymxsqClService.findBySqlsh(params3);
            //取到商品名称spmc
            request.getSession().setAttribute("spmc",jymxsqClList.get(0).getSpmc());
            Jyls jyls = jylsService.findOne(map2);
            List<Kpls> list = jylsService.findByTqm(map2);
            if (list.size() > 0) {  //代表申请已完成开票,跳转最终开票页面
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
                request.getSession().setAttribute("serialorder", list.get(0).getSerialorder());

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
            } else if (null != jyls && null != jyls.getDjh()) {
                result.put("num", "6");
            } else if (null != jyxxsq && null != jyxxsq.getSqlsh()) {
                request.getSession().setAttribute("tqm", tqm);
                request.getSession().setAttribute("je", je);
                request.getSession().setAttribute("jyxxsq", jyxxsq);
                request.getSession().setAttribute("jymxsqClList",jymxsqClList);
                request.getSession().setAttribute("ppjg", "1");
                result.put("num", "5");
            } else {
                request.getSession().setAttribute("tqm", tqm);
                request.getSession().setAttribute("je", je);
                request.getSession().setAttribute("ppjg", "0");
                result.put("num", "5");
            }
        } else {
            result.put("num", "4");
        }
        return result;
    }

    // 获取购方信息,保存到交易流水
    @RequestMapping(value = "/saveLs")
    @ResponseBody
    public Map<String, Object> saveLs() {
        Map<String, Object> result = new HashMap<String, Object>();
        String tqm = String.valueOf(request.getSession().getAttribute("tqm"));
        String openid = String.valueOf(request.getSession().getAttribute("openid"));
        if (null == tqm || "".equals(tqm)) {
            result.put("msg", "提取码为空,请重新扫描输入!");
            return result;
        }
        Map map = new HashMap<>();
        map.put("tqm", tqm);
        map.put("je", String.valueOf(request.getSession().getAttribute("je")));
        map.put("gsdm", "af");
        Jyls jyls1 = jylsService.findOne(map);
        if (jyls1 != null) {
            result.put("msg", "该订单正在开票!");
            return result;
        }
        String ppjg = String.valueOf(request.getSession().getAttribute("ppjg"));
        String fptt = String.valueOf(request.getSession().getAttribute("fptt"));
        String yx = String.valueOf(request.getSession().getAttribute("yx"));
        String nsrsbh = String.valueOf(request.getSession().getAttribute("nsrsbh"));
        String dz = String.valueOf(request.getSession().getAttribute("dz"));
        String dh = String.valueOf(request.getSession().getAttribute("dh"));
        String khh = String.valueOf(request.getSession().getAttribute("khh"));
        String yhzh = String.valueOf(request.getSession().getAttribute("yhzh"));
        String sj = String.valueOf(request.getSession().getAttribute("sj"));
        if ("1".equals(ppjg)) {
            Jyxxsq jyxxsq = (Jyxxsq) request.getSession().getAttribute("jyxxsq");
            List<JymxsqCl> jymxsqClList = (List<JymxsqCl>) request.getSession().getAttribute("jymxsqClList");
            Map map2 = new HashMap<>();
            map2.put("gsdm", "af");
            map2.put("kpddm", jyxxsq.getKpddm());
            Skp skp = skpService.findOneByParams(map2);
            List<Jyxxsq> list = new ArrayList<Jyxxsq>();
            if(fptt != null && !fptt.equals("")){
                jyxxsq.setGfmc(fptt);
            }
            if(yx != null && !yx.equals("")){
                jyxxsq.setGfemail(yx);
            }
            if(nsrsbh != null && !nsrsbh.equals("")){
                jyxxsq.setGfsh(nsrsbh);
            }
            if(dz != null && !dz.equals("")) {
                jyxxsq.setGfdz(dz);
            }
            if(dh != null && !dh.equals("")){
                jyxxsq.setGfdh(dh);
            }
            if(khh != null && !khh.equals("")){
                jyxxsq.setGfyh(khh);
            }
            if(yhzh != null && !yhzh.equals("")){
                jyxxsq.setGfyhzh(yhzh);
            }
            list.add(jyxxsq);
            try {
               List resultList =  afFpclService.afzjkp(list,"01");
               request.getSession().setAttribute("serialorder",resultList.get(0));
                result.put("msg", "1");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Tqmtq tqmtq1 = new Tqmtq();
            tqmtq1.setDdh(tqm);
            tqmtq1.setLrsj(new Date());
            tqmtq1.setZje(Double.valueOf(String.valueOf(request.getSession().getAttribute("je"))));
            tqmtq1.setGfmc(fptt);
            tqmtq1.setNsrsbh(nsrsbh);
            tqmtq1.setDz(dz);
            tqmtq1.setDh(dh);
            tqmtq1.setKhh(khh);
            tqmtq1.setKhhzh(yhzh);
            tqmtq1.setFpzt("0");
            tqmtq1.setYxbz("1");
            tqmtq1.setGfsjh(sj);
            tqmtq1.setGfemail(yx);
            tqmtq1.setGsdm("af");
            String llqxx = request.getHeader("User-Agent");
            tqmtq1.setLlqxx(llqxx);
            if (openid != null && !"null".equals(openid)) {
                tqmtq1.setOpenid(openid);
            }
            tqmtqService.save(tqmtq1);
            result.put("msg", "1");
        }
        return result;
    }
    @RequestMapping(value = "/spmcAndje")//显示项目和金额
    @ResponseBody
    public Map tjsession() {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("je", request.getSession().getAttribute("je"));
        result.put("slv", request.getSession().getAttribute("slv"));
        result.put("tqm",request.getSession().getAttribute("tqm"));
        result.put("spmc",request.getSession().getAttribute("spmc"));
        return result;
    }

    @RequestMapping(value = "/sqkpsession")//申请开票(信息放入session)
    @ResponseBody
    @Transactional
    public Map xgsession(String fptt, String nsrsbh, String dz, String dh, String khh, String yhzh, String yx){
        Map<String, Object> result = new HashMap<String, Object>();
        request.getSession().setAttribute("fptt", fptt);
        request.getSession().setAttribute("nsrsbh", nsrsbh);
        request.getSession().setAttribute("dz", dz);
        request.getSession().setAttribute("dh", dh);
        request.getSession().setAttribute("khh", khh);
        request.getSession().setAttribute("yhzh", yhzh);
        request.getSession().setAttribute("yx", yx);
        result.put("msg", "1");
        return  result;
    }

    @RequestMapping(value = "/qrxxsession")//确认信息
    @ResponseBody
    public Map xgsession(){
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("fptt", request.getSession().getAttribute("fptt"));
        result.put("yx", request.getSession().getAttribute("yx"));
        result.put("je", request.getSession().getAttribute("je"));
        return  result;
    }

    @RequestMapping(value = "/xgxxsession")//修改信息
    @ResponseBody
    public Map xgsession2(){
        Map<String, Object> result = new HashMap<String, Object>();

        result.put("fptt", request.getSession().getAttribute("fptt"));
        result.put("nsrsbh", request.getSession().getAttribute("nsrsbh"));
        result.put("dz", request.getSession().getAttribute("dz"));
        result.put("dh", request.getSession().getAttribute("dh"));
        result.put("khh", request.getSession().getAttribute("khh"));
        result.put("yhzh", request.getSession().getAttribute("yhzh"));
        result.put("yx", request.getSession().getAttribute("yx"));
        result.put("je", request.getSession().getAttribute("je"));
        result.put("tqm",request.getSession().getAttribute("tqm"));
        result.put("spmc",request.getSession().getAttribute("spmc"));


        return  result;
    }
}