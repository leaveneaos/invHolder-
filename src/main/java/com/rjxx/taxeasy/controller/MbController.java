package com.rjxx.taxeasy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rjxx.taxeasy.bizcomm.utils.*;
import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.domains.*;
import com.rjxx.taxeasy.service.*;
import com.rjxx.utils.BeanConvertUtils;
import com.rjxx.utils.HtmlUtils;
import com.rjxx.utils.StringUtils;
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
import java.util.*;

/**
 * Created by zsq on 2017/7/25
 * 模板
 */
@Controller
public class MbController extends BaseController {
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

    @RequestMapping(value = "/mb", method = RequestMethod.GET)
    public void index(String g) throws Exception{
        String gsdm = g;
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
                String ul = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + gsxx.getWxappid() + "&redirect_uri="
                        + url + "/mb/getWx&" + "response_type=code&scope=snsapi_base&state=" + gsdm + "#wechat_redirect";
                response.sendRedirect(ul);
                return;
            } else {
                //宏康页面已经有了,不跳转模板
                if(null!=gsdm&&gsdm.equals("hongkang")){
                    response.sendRedirect(request.getContextPath() + "/" + gsdm + "_page.html?_t=" + System.currentTimeMillis());
                }
                response.sendRedirect(request.getContextPath() + "/mb.jsp?gsdm="+gsdm+"&&t=" + System.currentTimeMillis());
                return;
            }
        } else {
            //宏康页面已经有了,不跳模板
            if(null!=gsdm&&gsdm.equals("hongkang")){
                response.sendRedirect(request.getContextPath() + "/" + gsdm + "_page.html?_t=" + System.currentTimeMillis());
            }
            response.sendRedirect(request.getContextPath() + "/mb.jsp?gsdm="+gsdm+"&&t=" + System.currentTimeMillis());
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
        //宏康页面已经有了,不跳模板
        if(state.equals("hongkang")){
            response.sendRedirect(request.getContextPath() + "/" + state + "_page.html?_t=" + System.currentTimeMillis());
            return;
        }
        response.sendRedirect(request.getContextPath() + "/mb.jsp?gsdm="+state+"&&_t="+System.currentTimeMillis() );
    }
    @RequestMapping(value = "/bangzhu")
    @ResponseBody
    public void bangzhu() throws IOException {
        response.sendRedirect(request.getContextPath() + "/smtq/smtq2.html?_t=" + System.currentTimeMillis());
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
    @RequestMapping(value = "/tqyz")
    @ResponseBody
    public Map<String,Object> tqyz(String tqm,String code,String gsdm) {
        String sessionCode = (String) session.getAttribute("rand");

        String opendid = (String) session.getAttribute("openid");
        //String gsdm = (String) session.getAttribute("gsdm");
        Map<String, Object> result = new HashMap<String, Object>();
        if (code != null && sessionCode != null && code.equals(sessionCode)) {
            Map map = new HashMap<>();
            map.put("tqm", tqm);
            map.put("gsdm", gsdm);

            /*调用接口获取jyxxsq等信息*/
            Map resultMap = new HashMap();

            String error=(String)resultMap.get("temp");
             /*wait to do*/
            if(error!=null){
                result.put("error",error);
                return result;
            }

            Jyls jyls = jylsService.findOne(map);
            List<Kpls> list = jylsService.findByTqm(map);
            //查询参数总表url 是否调用接口获取开票信息
            Cszb zb1 = cszbService.getSpbmbbh(gsdm, null,null, "sfdyjkhqkp");
            if(null!=zb1.getCsz()&&!zb1.getCsz().equals("")){
                //需要调用接口获取开票信息
                System.out.println("start+++++++++++");
                //Map resultMap1 = new HashMap();
                //全家调用接口 解析xml
                if(null!=gsdm && gsdm.equals("family")){
                    resultMap=getDataService.getData(tqm,gsdm);
                }
                //绿地优鲜 解析json
               else if(map.get("gsdm").equals("ldyx")){
                    System.out.println("ldyx+++++++++++++++++Strat");
                    //第一次请求url获取token 验证
                    resultMap=getDataService.getldyxFirData(tqm,gsdm);
                    if(null!=resultMap.get("accessToken")&&(null==request.getSession().getAttribute("crateDateTime"))){
                        //放入系统当前时间 直接是毫秒
                        Long dateTime = System.currentTimeMillis();
                        request.getSession().setAttribute("crateDateTime",dateTime);//创建时间
                        request.getSession().setAttribute("accessToken",resultMap.get("accessToken"));//token
                        request.getSession().setAttribute("expiresIn",resultMap.get("expiresIn"));//过期时间
                        resultMap = getDataService.getldyxSecData(tqm,gsdm,(String) resultMap.get("accessToken"));
                    }
                    /*Date dateNow = new Date(System.currentTimeMillis());
                    Long dateNowTime = dateNow.getTime();
                    long sfgq = 0;
                    if(null!=request.getSession().getAttribute("crateDateTime")&&null!=request.getSession().getAttribute("hm")){
                        Long  crateDateTime = (long) request.getSession().getAttribute("crateDateTime");
                        Long  exp  = (long) request.getSession().getAttribute("hm");
                        //时间差 = 当前时间- 创建时间
                        Long sjc = dateNowTime - crateDateTime ;
                        //时间差 - 过期时间
                        sfgq = sjc - exp ;
                    }

                    //判断token是否为空 是否过期
                    if (request.getSession().getAttribute("accessToken")==null
                            && sfgq >= 0){
                        //放入系统当前时间 直接是毫秒
                        Long dateTime = System.currentTimeMillis();
                        //token
                        request.getSession().setAttribute("accessToken",resultMap.get("accessToken"));
                        //放进session时间
                        request.getSession().setAttribute("crateDateTime",dateTime);
                        //过期时间 直接转成毫秒long型
                        Long ToLong =  (Long) resultMap.get("");
                        Long hm = ToLong * 1000;
                        request.getSession().setAttribute("",hm);
                        //获取第一次发送请求的token
                        String token = (String) resultMap.get("accessToken");
                        //发送第二次请求
                        resultMap=getDataService.getldyxSecData(tqm,gsdm,token);
                    }else
                        //session中有token 并且token没有过期
                        if((request.getSession().getAttribute("accessToken")!=null&&!request.getSession().getAttribute("accessToken").equals(""))&&(sfgq < 0)){
                            //获取session中的token
                            String token = (String) request.getSession().getAttribute("accessToken");
                            //发送第二次请求
                            resultMap=getDataService.getldyxSecData(tqm,gsdm,token);
                        }*/

                    //直接获取数据
                    // http://10.217.223.34:8081/querysale/queryticket
                    //resultMap=getDataService.getldyxSecData(tqm,gsdm,token);
                }
            }
            List<Jyxxsq> jyxxsqList=(List)resultMap.get("jyxxsqList");
            List<Jymxsq> jymxsqList=(List)resultMap.get("jymxsqList");
            List<Jyzfmx> jyzfmxList = (List) resultMap.get("jyzfmxList");
            if(null!=jyxxsqList){
                Jyxxsq jyxxsq=jyxxsqList.get(0);
                request.getSession().setAttribute(gsdm+tqm+"je",jyxxsq.getJshj());
            }
            if (list.size() > 0) {
                /*代表申请已完成开票,跳转最终开票页面*/
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
                //request.getSession().setAttribute(gsdm+"|djh",list.get(0).getDjh());
                //request.getSession().setAttribute(gsdm+"|serialorder",list.get(0).getSerialorder());
                for (Kpls kpls2: list) {
                    pdfdzs += kpls2.getPdfurl().replace(".pdf",",jpg") + ",";
                }
                if(pdfdzs.length() > 0){
                    result.put("pdfdzs",pdfdzs.substring(0,pdfdzs.length() - 1));
                    //request.getSession().setAttribute(gsdm+"|pdfdzs",pdfdzs.substring(0,pdfdzs.length() - 1));
                }
                /**
                 * num=2表示已经开过票
                 */
                result.put("num","2");
                result.put("tqm",tqm);
                result.put("gsdm",gsdm);
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
                if(resultMap!=null){
                    request.getSession().setAttribute(gsdm+tqm+"resultMap",resultMap);
                }
                if(jymxsqList!=null) {
                    request.getSession().setAttribute(gsdm+tqm+"jymxsqList", jymxsqList);
                }
                if(jyzfmxList!=null){
                    request.getSession().setAttribute(gsdm+tqm+"jyzfmxList",jyzfmxList);
                }
                request.getSession().setAttribute(gsdm+"tqm",tqm);
                //request.getSession().setAttribute(gsdm+tqm+"resultMap","1");
               // request.getSession().setAttribute(gsdm+"|tqm",tqm);
                //System.out.println(""+session.getAttribute(gsdm+tqm+"resultMap").toString());
                //System.out.println(""+session.getAttribute(gsdm+tqm+"jymxsqList").toString());
                result.put("num","5");
                result.put("tqm",tqm);
                result.put("gsdm",gsdm);
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
    @RequestMapping(value = "/spmx")
    @ResponseBody
    public Map<String,Object> spmx(String tqm,String gsdm){
        Map<String, Object> result = new HashMap<String, Object>();
        List<Jymxsq> jymxsqList = (List)request.getSession().getAttribute(gsdm+tqm+"jymxsqList");
        List<Jyzfmx> jyzfmxList = (List)request.getSession().getAttribute(gsdm+tqm+"jyzfmxList");
        Double zje = (Double) request.getSession().getAttribute(gsdm+tqm+"je");
        Double bkpje = 0.00;
        Double sjkpje = 0.00;
        if(null!=jyzfmxList){

            for (Jyzfmx jyzfmx2 : jyzfmxList){
                if(jymxsqList.get(0).getDdh().equals(jyzfmx2.getDdh())
                        &&(jyzfmx2.getZffsDm().equals("A")
                        ||jyzfmx2.getZffsDm().equals("B")
                        ||jyzfmx2.getZffsDm().equals("E")
                        ||jyzfmx2.getZffsDm().equals("F")
                        ||jyzfmx2.getZffsDm().equals("G")
                        ||jyzfmx2.getZffsDm().equals("h")
                        ||jyzfmx2.getZffsDm().equals("I")
                        ||jyzfmx2.getZffsDm().equals("J")
                        ||jyzfmx2.getZffsDm().equals("L")
                        ||jyzfmx2.getZffsDm().equals("N")
                        ||jyzfmx2.getZffsDm().equals("P")
                        ||jyzfmx2.getZffsDm().equals("Q")
                        ||jyzfmx2.getZffsDm().equals("S")
                        ||jyzfmx2.getZffsDm().equals("V")
                        ||jyzfmx2.getZffsDm().equals("X")
                        ||jyzfmx2.getZffsDm().equals("Y")
                        ||jyzfmx2.getZffsDm().equals("Z"))
                        ){

                    System.out.println("不开票支付金额为"+jyzfmx2.getZfje());
                        bkpje+=jyzfmx2.getZfje();
                }
                System.out.println("不开票金额为"+bkpje);
            }
              sjkpje = zje - bkpje;
            System.out.println("实际开票金额"+sjkpje);
        }
        request.getSession().setAttribute(gsdm+tqm+"sjkpje",sjkpje);
        result.put("jymxsqList",jymxsqList);
        result.put("jyzfmxList",jyzfmxList);
        result.put("sjkpje",sjkpje);
        return result;
    }



    @RequestMapping(value = "/sqkpsession")//申请开票(信息放入session)
    @ResponseBody
    public Map xgsession(String fptt, String nsrsbh, String dz, String dh, String khh, String yhzh, String yx,String gsdm,String tqm){
        Map<String, Object> result = new HashMap<String, Object>();

        request.getSession().setAttribute(gsdm+tqm+"fptt", fptt);
        request.getSession().setAttribute(gsdm+tqm+"nsrsbh", nsrsbh);
        request.getSession().setAttribute(gsdm+tqm+"dz", dz);
        request.getSession().setAttribute(gsdm+tqm+"dh", dh);
        request.getSession().setAttribute(gsdm+tqm+"khh", khh);
        request.getSession().setAttribute(gsdm+tqm+"yhzh", yhzh);
        request.getSession().setAttribute(gsdm+tqm+"yx", yx);
        if(tqm != null && !tqm.equals("")){
            result.put("msg", "1");
            result.put("gsdm",gsdm);
            result.put("tqm",tqm);
        }else {
            result.put("msg","信息保存出现错误,请返回上一级重新操作!");
        }

        return  result;
    }


    /*获取购方信息,保存到交易流水*/
    @RequestMapping(value ="/savels")
    @ResponseBody
    public  Map<String,Object> savels(String gsdm,String tqm){
        Map<String,Object> result = new HashMap<String,Object>();
        Map  resultMap=(Map)request.getSession().getAttribute(gsdm+tqm+"resultMap");
        String openid = String.valueOf(request.getSession().getAttribute("openid"));
        List<Jyxxsq> jyxxsqList = new ArrayList<>() ;
        List<Jymxsq> jymxsqList = new ArrayList<>() ;
        List<Jyzfmx> jyzfmxList = new ArrayList<>() ;
        Jyxxsq jyxxsq = new Jyxxsq();
        if(resultMap!=null){
            jyxxsqList=(List)resultMap.get("jyxxsqList");
             jymxsqList=(List)resultMap.get("jymxsqList");
             jyzfmxList=(List)resultMap.get("jyzfmxList");
             jyxxsq=jyxxsqList.get(0);
        }

        String fptt=String.valueOf(request.getSession().getAttribute(gsdm+tqm+"fptt"));
        String nsrsbh=String.valueOf(request.getSession().getAttribute(gsdm+tqm+"nsrsbh"));
        String dz=String.valueOf(request.getSession().getAttribute(gsdm+tqm+"dz"));
        String dh=String.valueOf(request.getSession().getAttribute(gsdm+tqm+"dh"));
        String khh=String.valueOf(request.getSession().getAttribute(gsdm+tqm+"khh"));
        String yhzh=String.valueOf(request.getSession().getAttribute(gsdm+tqm+"yhzh"));
        String yx=String.valueOf(request.getSession().getAttribute(gsdm+tqm+"yx"));
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
            String xml= GetXmlUtil.getFpkjXml(jyxxsq,jymxsqList,jyzfmxList);
            String resultxml=HttpUtils.HttpUrlPost(xml,"RJcb0cb4d18ce7","73e235a15ee5cb022691625a50edae3b");
            logger.info("-------返回值---------"+resultxml);
           /*List<JymxsqCl> jymxsqClList = new ArrayList<JymxsqCl>();
            //复制一个新的list用于生成处理表
            List<Jymxsq> jymxsqTempList = new ArrayList<Jymxsq>();
            jymxsqTempList = BeanConvertUtils.convertList(jymxsqList, Jymxsq.class);
            jymxsqClList = discountDealUtil.dealDiscount(jymxsqTempList, 0d, jyxxsq.getJshj(),jyxxsq.getHsbz());
            Integer sqlsh=jyxxsqService.saveJyxxsq(jyxxsq, jymxsqList,jymxsqClList,jyzfmxList);
            List jyxxsqlist=new ArrayList();
            jyxxsqlist.add(jyxxsq);
            List resultList=null;
                if(jyxxsq.getGsdm().equals("Family")){
                    resultList =  fpclService.skdzfp(jyxxsqlist,"03");
                }else if(jyxxsq.getGsdm().equals("ldyx")){
                    resultList =  fpclService.zjkp(jyxxsqlist,"01");
                }
                request.getSession().setAttribute("serialorder",resultList.get(0));
                result.put("msg", "1");*/
        }catch (Exception e){
            e.printStackTrace();
        }
        Tqmtq tqmtq1 = new Tqmtq();
        tqmtq1.setDdh(jyxxsq.getTqm());
        tqmtq1.setLrsj(new Date());
        tqmtq1.setZje(Double.valueOf(String.valueOf(request.getSession().getAttribute(gsdm+tqm+"je"))));
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
    public Map fpsession(String tqm,String gsdm) {
        Map<String, Object> result = new HashMap<String, Object>();
        Map map2 = new HashMap();
        Map csmap = new HashMap<>();
        csmap.put("tqm", tqm);
        csmap.put("gsdm", gsdm);
        List<Kpls> jylslist = jylsService.findByTqm(csmap);
        if(jylslist.size()>0){
            map2.put("serialorder",jylslist.get(0).getSerialorder());
        }
        List<Kpls> kplsList = kplsService.findAll(map2);
        String pdfdzs = "";
        for (Kpls kpls2 : kplsList) {
            pdfdzs += kpls2.getPdfurl().replace(".pdf", ".jpg") + ",";
        }
        if (pdfdzs.length() > 0) {
            result.put("pdfdzs", pdfdzs.substring(0, pdfdzs.length() - 1));
        }
        result.put("serialorder", jylslist.get(0).getSerialorder());
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
    public Map xgsession(String gsdm,String tqm){
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("fptt", request.getSession().getAttribute(gsdm+tqm+"fptt"));
        result.put("yx", request.getSession().getAttribute(gsdm+tqm+"yx"));
        result.put("je", request.getSession().getAttribute(gsdm+tqm+"je"));
        result.put("sjkpje", request.getSession().getAttribute(gsdm+tqm+"sjkpje"));
        System.out.println(""+session.getAttribute(gsdm+tqm+"fptt").toString());
        return  result;
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
    public Map xyxfs(String yx,String gsdm, String tqm) throws Exception {
        Map<String, Object> result = new HashMap<String, Object>();
        Map csmap = new HashMap<>();
        Map params = new HashMap<>();
        csmap.put("tqm", tqm);
        csmap.put("gsdm", gsdm);
        List<Kpls> jylslist = jylsService.findByTqm(csmap);
        if(jylslist.size()>0){
            params.put("serialorder",jylslist.get(0).getSerialorder());
        }

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

    /**
     * 发送邮件
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

        Thread.sleep(5000);
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

}