package com.rjxx.taxeasy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rjxx.taxeasy.bizcomm.utils.*;
import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.domains.*;
import com.rjxx.taxeasy.service.*;
import com.rjxx.utils.HtmlUtils;
import com.rjxx.utils.MD5Util;
import com.rjxx.utils.StringUtils;
import org.apache.commons.codec.binary.*;
import org.apache.commons.codec.digest.DigestUtils;
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


import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2017/6/20 0020.
 */
@Controller
@RequestMapping("/family")
public class BaseClController extends BaseController {
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
    private JyxxsqService jyxxsqService;//交易信息申请
    @Autowired
    private JymxsqClService jymxsqClService;//交易明细申请
    @Autowired
    private TqmtqService tqmtqService;//提取码提取
    @Autowired
    private PpService ppService;//品牌
    @Autowired
    private FpjService fpjService;//发票夹
    @Autowired
    private FpclService fpclService;
    @Autowired
    private KplsService kplsService;
    @Autowired
    private SendalEmail se;
    @Autowired
    private SpfyxService spfyxService;
    @Autowired
    private JymxsqService jymxsqService;
    @Autowired
    private GetDataService getDataService;
    @Autowired
    private DiscountDealUtil discountDealUtil;

    public static final String APP_ID ="wx9abc729e2b4637ee";

    public static final String GET_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";

    public static final String SECRET = "6415ee7a53601b6a0e8b4ac194b382eb";

    @RequestMapping
    @ResponseBody
    public void index(String gsdm) throws Exception{
        String str = request.getParameter("q");
        Map<String,Object> params = new HashMap<>();
        params.put("gsdm","Family");
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
                String ul = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + gsxx.getWxappid() + "&redirect_uri="
                             + url + "/base/getWx&" + "response_type=code&scope=snsapi_base&state=" + str + "#wechat_redirect";
                response.sendRedirect(ul);
                return;
            } else {
                sendHtml(str, gsxx);
            }
        } else {
            sendHtml(str, gsxx);
        }
    }
    @RequestMapping(value = "/sendHtml")
    @ResponseBody
    public void sendHtml(String state, Gsxx gsxx) throws IOException {

            Map<String, Object> result = new HashMap<String, Object>();

        try {
            /**
             * 如果q参数为空则跳转到发票提取页面
             */
            if (null==state) {
                response.sendRedirect(request.getContextPath() + "/Family/qj.html?_t=" + System.currentTimeMillis());
                return;
              }else {
                byte[] bytes = org.apache.commons.codec.binary.Base64.decodeBase64(state);
                String csc = new String(bytes);
                String[] cssz = csc.split("&");
                String tqm = cssz[0].substring(cssz[0].lastIndexOf("=") + 1);
                String sign = cssz[1].substring(cssz[1].lastIndexOf("=") + 1);
                if (null == gsxx) {
                    request.getSession().setAttribute("msg", "公司信息不正确!");
                    response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                    return;
                }
                String newsign = cssz[0] += "key=" + gsxx.getSecretKey();
                String key1 = DigestUtils.md5Hex(newsign);
                if (!sign.equals(key1.toLowerCase())) {
                    request.getSession().setAttribute("msg", "秘钥不匹配!");
                    response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                    return;
                }
                String mdh = tqm.substring(4, 10);//门店号
                String jylsh = tqm.substring(12, 20);//交易流水号
                request.getSession().setAttribute("orderNo", mdh);
                request.getSession().setAttribute("order", jylsh);
                String opendid = (String) session.getAttribute("openid");
                Map map = new HashMap<>();
                map.put("tqm", tqm);
                map.put("gsdm", gsxx.getGsdm());
                Jyls jyls = jylsService.findOne(map);
                List<Kpls> list = jylsService.findByTqm(map);
                /**
                 * 代表申请已完成开票,跳转最终开票页面
                 */
                if (list.size() > 0) {
                    if (opendid != null && !"null".equals(opendid)) {
                        Map<String, Object> params = new HashMap<>();
                        params.put("djh", jyls.getDjh());
                        params.put("unionid", opendid);
                        Fpj fpj = fpjService.findOneByParams(params);
                        if (fpj == null) {
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
                    request.getSession().setAttribute("djh", list.get(0).getDjh());
                    request.getSession().setAttribute("serialorder", list.get(0).getSerialorder());
                    for (Kpls kpls2 : list) {
                        pdfdzs += kpls2.getPdfurl().replace(".pdf", ",jpg") + ",";
                    }
                    if (pdfdzs.length() > 0) {
                        result.put("pdfdzs", pdfdzs.substring(0, pdfdzs.length() - 1));
                        request.getSession().setAttribute("pdfdzs", pdfdzs.substring(0, pdfdzs.length() - 1));
                    }
                    /**
                     * num=2表示已经开过票
                     */
                    result.put("num", "2");
                    Tqjl tqjl = new Tqjl();
                    tqjl.setDjh((String.valueOf(list.get(0).getDjh())));
                    tqjl.setJlly("1");
                    tqjl.setTqsj(new Date());
                    String visiterIP;
                    if (request.getHeader("x-forwarded-for") == null) {
                        visiterIP = request.getRemoteAddr();/*访问者IP*/
                    } else {
                        visiterIP = request.getHeader("x-forwarded-for");
                    }
                    tqjl.setIp(visiterIP);
                    String llqxx = request.getHeader("User-Agent");
                    tqjl.setLlqxx(llqxx);
                    tqjlService.save(tqjl);
                    response.sendRedirect(request.getContextPath() + "/Family/fpxq.html?_t=" + System.currentTimeMillis());
                    return;
                } else if (null != jyls && null != jyls.getDjh()) {
                    /**
                     * 等待页面
                     */
                    result.put("num", "6");
                    response.sendRedirect(request.getContextPath() + "/Family/witting.html?_t=" + System.currentTimeMillis());
                    return;
                } else {
                    /**
                     * 没有开过票调用接口获取数据
                     */
                    Map resultMap = new HashMap();
                    resultMap = getDataService.getData(tqm, gsxx.getGsdm());
                    List<Jyxxsq> jyxxsqList = (List) resultMap.get("jyxxsqList");
                    List<Jymxsq> jymxsqList = (List) resultMap.get("jymxsqList");
                    String error = (String) resultMap.get("temp");
                    if (error != null) {
                        result.put("error", error);
                    }
                    Jyxxsq jyxxsq = jyxxsqList.get(0);
                    request.getSession().setAttribute("price", jyxxsq.getJshj());
                    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    request.getSession().setAttribute("orderTime",sdf.format(jyxxsq.getDdrq()));
                    request.getSession().setAttribute("resultMap", resultMap);
                    request.getSession().setAttribute("jymxsqList", jymxsqList);
                    request.getSession().setAttribute("tqm", tqm);
                    result.put("num", "5");
                    response.sendRedirect(request.getContextPath() + "/Family/ddqr.html?_t=" + System.currentTimeMillis());
                    return;
                }
            }
        } catch (Exception e) {
            request.getSession().setAttribute("msg", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            return;
        }
    }
    @RequestMapping(value = "/getWx")
    @ResponseBody
    public void getWx(String state,String code) throws IOException{
        Map params = new HashMap<>();
        params.put("gsdm",state);
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
        if (null==state) {
            response.sendRedirect(request.getContextPath() + "/Family/qj.html?_t=" + System.currentTimeMillis());
            return;
        }else{
            sendHtml(state, gsxx);
        }

    }
    @RequestMapping(value = "/bangzhu")
    @ResponseBody
    public void bangzhu() throws IOException {
        response.sendRedirect(request.getContextPath() + "bangzhu.html?_t=" + System.currentTimeMillis());
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

    /*校验提取码是否正确*/
    @RequestMapping(value = "/tqyz",method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public Map<String,Object> tqyz(String tqm,String code) {
        String sessionCode = (String) session.getAttribute("rand");
        String opendid = (String) session.getAttribute("openid");
        String gsdm = (String) session.getAttribute("gsdm");
        Map<String, Object> result = new HashMap<String, Object>();
        if (code != null && sessionCode != null && code.equals(sessionCode)) {
            Map map = new HashMap<>();
            map.put("tqm", tqm);
            map.put("gsdm", gsdm);
            /*调用接口获取jyxxsq等信息*/
            Map resultMap = new HashMap();
            resultMap=getDataService.getData(tqm,gsdm);
            List<Jyxxsq> jyxxsqList=(List)resultMap.get("jyxxsqList");
            List<Jymxsq> jymxsqList=(List)resultMap.get("jymxsqList");
            String error=(String)resultMap.get("temp");
             /*wait to do*/
            if(error!=null){
                result.put("error",error);
                return result;
            }
            Jyxxsq jyxxsq=jyxxsqList.get(0);
            request.getSession().setAttribute("je",jyxxsq.getJshj());
            Jyls jyls = jylsService.findOne(map);
            List<Kpls> list = jylsService.findByTqm(map);
            if (list.size() > 0) {/*代表申请已完成开票,跳转最终开票页面*/
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
                    pdfdzs += kpls2.getPdfurl().replace(".pdf",",jpg") + ",";
                }
                if(pdfdzs.length() > 0){
                    result.put("pdfdzs",pdfdzs.substring(0,pdfdzs.length() - 1));
                    request.getSession().setAttribute("pdfdzs",pdfdzs.substring(0,pdfdzs.length() - 1));
                }
                /**
                 * num=2表示已经开过票
                 */
                result.put("num","2");
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
            }else if(null != jyls && null !=jyls.getDjh()){
                result.put("num","6");
            }else {//跳转发票提取页面
                request.getSession().setAttribute("resultMap",resultMap);
                request.getSession().setAttribute("jymxsqList",jymxsqList);
                request.getSession().setAttribute("tqm",tqm);
                result.put("num","5");
            }
        }else {//校验码错误
            result.put("num","4");
        }
        return result;
    }

    /**
     *   获取商品明细
     *  获取spmx.html中需要展示的数据
     */
    @RequestMapping(value = "/getsp",method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public Map<String,Object> spmx(){
        Map<String, Object> result = new HashMap<String, Object>();
        List<Jymxsq> jymxsqList = (List)request.getSession().getAttribute("jymxsqList");
        result.put("jymxsqList",jymxsqList);
        return result;
    }


    @RequestMapping(value = "/sqkpsession")//申请开票(信息放入session)
    @ResponseBody
    public Map xgsession(String fptt, String nsrsbh, String dz, String dh, String khh, String yhzh, String yx){
        Map<String, Object> result = new HashMap<String, Object>();
        request.getSession().setAttribute("fptt", fptt);
        request.getSession().setAttribute("nsrsbh", nsrsbh);
        request.getSession().setAttribute("dz", dz);
        request.getSession().setAttribute("dh", dh);
        request.getSession().setAttribute("khh", khh);
        request.getSession().setAttribute("yhzh", yhzh);
        request.getSession().setAttribute("yx", yx);
        String tqm = String.valueOf(request.getSession().getAttribute("tqm"));
        if(tqm != null && !tqm.equals("")){
            result.put("msg", "1");
        }else {
            result.put("msg","信息保存出现错误,请返回上一级重新操作!");
        }
        return  result;
    }



    /*获取购方信息,保存到交易流水*/
    @RequestMapping(value ="/savels" ,method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public  Map<String,Object> savels(){
        Map<String,Object> result = new HashMap<String,Object>();
        Map  resultMap=(Map)request.getSession().getAttribute("resultMap");
        String openid = String.valueOf(request.getSession().getAttribute("openid"));
        List<Jyxxsq> jyxxsqList=(List)resultMap.get("jyxxsqList");
        List<Jymxsq> jymxsqList=(List)resultMap.get("jymxsqList");
        List<Jyzfmx> jyzfmxList=(List)resultMap.get("jyzfmxList");
        Jyxxsq jyxxsq=jyxxsqList.get(0);
        String fptt=String.valueOf(request.getSession().getAttribute("fptt"));
        String nsrsbh=String.valueOf(request.getSession().getAttribute("nsrsbh"));
        String dz=String.valueOf(request.getSession().getAttribute("dz"));
        String dh=String.valueOf(request.getSession().getAttribute("dh"));
        String khh=String.valueOf(request.getSession().getAttribute("khh"));
        String yhzh=String.valueOf(request.getSession().getAttribute("yhzh"));
        String yx=String.valueOf(request.getSession().getAttribute("yx"));
        jyxxsq.setGfmc(fptt.trim());
        jyxxsq.setGfemail(yx.trim());
        if (StringUtils.isNotBlank(jyxxsq.getGfemail())) {
            jyxxsq.setSffsyj("1");
        }
        jyxxsq.setGfsh(nsrsbh.trim());
        jyxxsq.setGfdz(dz.trim());
        jyxxsq.setGfdh(dh.trim());
        jyxxsq.setGfyh(khh.trim());
        jyxxsq.setGfyhzh(yhzh.trim());
        Map map = new HashMap<>();
        map.put("tqm",jyxxsq.getTqm());
        map.put("je",jyxxsq.getJshj());
        map.put("gsdm",jyxxsq.getGsdm());
        Tqmtq tqmtq = tqmtqService.findOneByParams(map);
        Jyls jyls1 = jylsService.findOne(map);
        if(tqmtq != null && tqmtq.getId() != null){
            result.put("msg","该提取码已提交过申请!");
            return result;
        }
        if(jyls1 != null){
            result.put("msg","该订单正在开票!");
            return result;
        }
        try{
           String xml=GetXmlUtil.getFpkjXml(jyxxsq,jymxsqList,jyzfmxList);
           String resultxml=HttpUtils.HttpUrlPost(xml,"RJe115dfb8f3f8","bd79b66f566b5e2de07f1807c56b2469");
            logger.info("-------返回值---------"+resultxml);
            request.getSession().setAttribute("serialorder",resultxml);
            result.put("msg", "1");
            }catch (Exception e){
                e.printStackTrace();
            }
            Tqmtq tqmtq1 = new Tqmtq();
            tqmtq1.setDdh(jyxxsq.getTqm());
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
            tqmtq1.setGfemail(yx);
            tqmtq1.setGsdm(jyxxsq.getGsdm());
            String llqxx = request.getHeader("User-Agent");
            tqmtq1.setLlqxx(llqxx);
            if(openid != null && !"null".equals(openid)){
                tqmtq1.setOpenid(openid);
            }
            tqmtqService.save(tqmtq1);
            result.put("msg","1");
            return  result;
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
        result.put("kplsList", kplsList);
        request.getSession().setAttribute("msg", "请重新扫描二维码");
        return result;
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
    @RequestMapping(value = "/qrxxsession")//确认信息
    @ResponseBody
    public Map xgsession(){
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("fptt", request.getSession().getAttribute("fptt"));
        result.put("yx", request.getSession().getAttribute("yx"));
        result.put("je", request.getSession().getAttribute("je"));
        result.put("nsrsbh", request.getSession().getAttribute("nsrsbh"));

        result.put("price",request.getSession().getAttribute("price"));
        result.put("orderTime",request.getSession().getAttribute("orderTime"));
        result.put("orderNo",request.getSession().getAttribute("orderNo"));
        result.put("order",request.getSession().getAttribute("order"));
        return  result;
    }
}