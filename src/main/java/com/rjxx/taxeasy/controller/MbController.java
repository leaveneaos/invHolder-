package com.rjxx.taxeasy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rjxx.taxeasy.bizcomm.utils.*;
import com.rjxx.taxeasy.bizcomm.utils.HttpUtils;
import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.dao.WxfpxxJpaDao;
import com.rjxx.taxeasy.domains.*;
import com.rjxx.taxeasy.service.*;
import com.rjxx.taxeasy.utils.NumberUtil;
import com.rjxx.taxeasy.utils.alipay.AlipayConstants;
import com.rjxx.taxeasy.utils.alipay.AlipayUtils;
import com.rjxx.utils.*;
import com.rjxx.utils.weixin.WeiXinConstants;
import com.rjxx.utils.weixin.WeixinUtils;
import org.apache.commons.codec.digest.DigestUtils;
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
import java.text.SimpleDateFormat;
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
    private JylsService jylsService;//交易流水

    @Autowired
    private TqjlService tqjlService;//提取记录

    @Autowired
    private CszbService cszbService;//参数主表

    @Autowired
    private TqmtqService tqmtqService;//提取码提取

    @Autowired
    private FpjService fpjService;//发票夹

    @Autowired
    private KplsService kplsService;
    @Autowired
    private SendalEmail se;

    @Autowired
    private SpfyxService spfyxService;

    @Autowired
    private GetDataService getDataService;

    @Autowired
    private WxfpxxJpaDao wxfpxxJpaDao;

    public static final String APP_ID ="wx9abc729e2b4637ee";

    public static final String GET_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";

    public static final String SECRET = "6415ee7a53601b6a0e8b4ac194b382eb";

    @RequestMapping(value = "/mb", method = RequestMethod.GET)
    public void index(String g,String q) throws Exception{

        logger.info("-----参数q的值为"+q);
        logger.info("-----参数g的值为"+g);
        String gsdm = g;
        Map<String,Object> params = new HashMap<>();
        params.put("gsdm",gsdm);
        request.getSession().setAttribute("gsdm",gsdm);
        Gsxx gsxx = gsxxservice.findOneByParams(params);
        if(gsxx == null){
            request.getSession().setAttribute("msg", "出现未知异常!请重试!");
            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            return ;
        }
        if(request.getHeader("user-agent")==null){
            request.getSession().setAttribute("msg", "出现未知异常!请重试!");
            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            return ;
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
                return;
            } else {
                barcodeCl(gsxx, q);
                return;
            }
        } else {
            barcodeCl(gsxx,q);
            return;
        }
    }

        public void barcodeCl(Gsxx gsxx, String q) throws IOException {
        Map<String, Object> result = new HashMap<String, Object>();
            logger.info("进入判断是否手机扫码处理公司代码是-----"+gsxx.getGsdm());
        try {
            if(gsxx == null){
                request.getSession().setAttribute("msg", "出现未知异常!请重试!");
                response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                return ;
            }
            /**
             * 如果q参数为空则跳转到发票提取页面
             */
            if (null==q) {
                logger.info("不带参数q,进入浏览器页面-------");
                //宏康页面已经有了,不跳模板
                if(null!=gsxx.getGsdm()&&gsxx.getGsdm().equals("hongkang")){
                    response.sendRedirect(request.getContextPath() + "/" + gsxx.getGsdm() + "_page.jsp?gsdm="+gsxx.getGsdm()+"&&_t=" + System.currentTimeMillis());
                    return;
                }
                response.sendRedirect(request.getContextPath() + "/mb.jsp?gsdm="+gsxx.getGsdm()+"&&t=" + System.currentTimeMillis());
                return;
            }else {
                logger.info("带有q参数，处理扫码开票");
                RJCheckUtil rjCheckUtil = new RJCheckUtil();
                Boolean b = rjCheckUtil.checkMD5ForAll(gsxx.getSecretKey(), q);
                if(b){
                    Map mapdecode = rjCheckUtil.decodeForAll(q);

                    String tqm = (String) mapdecode.get("A0");//解析code，第一个为提取码
                    logger.info("解析二维码-----参数q提取码"+tqm);
                    request.getSession().setAttribute("orderNo", System.currentTimeMillis()+ NumberUtil.getRandomLetter());//门店编号
                    request.getSession().setAttribute("order",tqm );//订单号
                    request.getSession().setAttribute("tqm", tqm);//提取码
                    request.getSession().setAttribute(gsxx.getGsdm()+"tqm",tqm);
                    String opendid = (String) session.getAttribute("openid");
                    String userId = (String) request.getSession().getAttribute(AlipayConstants.ALIPAY_USER_ID);

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
                            pdfdzs += kpls2.getPdfurl().replace(".pdf", ".jpg") + ",";
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
                        //表示已经开过票 --领取发票类型
                        if(WeixinUtils.isWeiXinBrowser(request)){
                            WxFpxx wxFpxxByTqm = wxfpxxJpaDao.selsetByOrderNo(tqm);
                            if(null==wxFpxxByTqm){
                                WxFpxx wFpxx = new WxFpxx();
                                wFpxx.setTqm(tqm);
                                wFpxx.setGsdm(gsxx.getGsdm());
                                wFpxx.setOrderNo(tqm);
                                wFpxx.setQ(q);
                                wFpxx.setWxtype("2");
                                wFpxx.setOpenId(opendid);
                                wFpxx.setKplsh(list.get(0).getKplsh().toString());
                                logger.info("已完成开票,归入卡包---"+tqm+"----公司代码"+gsxx.getGsdm()+"----q值"+
                                        q+"----微信"+opendid+
                                        "------订单编号"+wFpxx.getOrderNo()+"------发票类型"+wFpxx.getWxtype());
                                try {
                                    wxfpxxJpaDao.save(wFpxx);
                                }catch (Exception e){
                                    e.printStackTrace();
                                    logger.info("交易信息保存失败");
                                    return ;
                                }
                            }else {
                                wxFpxxByTqm.setTqm(tqm);
                                wxFpxxByTqm.setGsdm(gsxx.getGsdm());
                                wxFpxxByTqm.setQ(q);
                                wxFpxxByTqm.setOpenId(opendid);
                                wxFpxxByTqm.setOrderNo(tqm);
                                wxFpxxByTqm.setWxtype("2");//1:申请开票2：领取发票
                                wxFpxxByTqm.setKplsh(list.get(0).getKplsh().toString());
                                if(wxFpxxByTqm.getCode()!=null||!"".equals(wxFpxxByTqm.getCode())){
                                    String notNullCode= wxFpxxByTqm.getCode();
                                    wxFpxxByTqm.setCode(notNullCode);
                                }
                                try {
                                    wxfpxxJpaDao.save(wxFpxxByTqm);
                                }catch (Exception e){
                                    e.printStackTrace();
                                    logger.info("交易信息保存失败");
                                    return ;
                                }
                            }

                        }
                        //如果是多张的话，只能领取第一张
                        String redirectUrl = request.getContextPath() + "/smtq/" + "xfp.html?_t=" + System.currentTimeMillis();
                        if (AlipayUtils.isAlipayBrowser(request)) {
                            redirectUrl += "&isAlipay=true";
                        }
                        response.sendRedirect(redirectUrl);
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
                        if(gsxx.getGsdm().equals("bqw")){
                            Cszb  zb1 =  cszbService.getSpbmbbh(gsxx.getGsdm(), null,null, "sfhhurl");
                            resultMap = getDataService.getDataForBqw(tqm, gsxx.getGsdm(),zb1.getCsz());
                        }

                        List<Jyxxsq> jyxxsqList = (List) resultMap.get("jyxxsqList");
                        List<Jymxsq> jymxsqList = (List) resultMap.get("jymxsqList");
                        request.getSession().setAttribute(gsxx.getGsdm()+tqm+"resultMap",resultMap);
                        String error = (String) resultMap.get("error");
                        if(error!=null){
                            logger.info("---------错误信息------------"+error);
                            request.getSession().setAttribute("msg", error);
                            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                            return;
                        }

                        String msg = (String) resultMap.get("msg");
                        if(null!= msg && !"".equals(resultMap.get("msg"))){
                            logger.info("---------校验信息------------"+msg);
                            request.getSession().setAttribute("msg", msg);
                            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                            return;
                        }
                        Jyxxsq jyxxsq = jyxxsqList.get(0);
                        request.getSession().setAttribute("price", jyxxsq.getJshj());
                        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        request.getSession().setAttribute("orderTime",sdf.format(jyxxsq.getDdrq()));
                        request.getSession().setAttribute("resultMap", resultMap);
                        request.getSession().setAttribute("jymxsqList", jymxsqList);
                        request.getSession().setAttribute("tqm", tqm);
                        result.put("num", "5");
                        if(WeixinUtils.isWeiXinBrowser(request)){
                            WxFpxx wxFpxxByTqm = wxfpxxJpaDao.selsetByOrderNo(tqm);
                            //第一次扫描
                            if(null==wxFpxxByTqm){
                                WxFpxx wxFpxx = new WxFpxx();
                                wxFpxx.setTqm(tqm);
                                wxFpxx.setGsdm(gsxx.getGsdm());
                                wxFpxx.setOrderNo(tqm);
                                wxFpxx.setQ(q);
                                wxFpxx.setWxtype("1");
                                //微信
                                wxFpxx.setOpenId((String) session.getAttribute("openid"));
                                logger.info("第一次扫码存入数据提取码"+tqm+"----公司代码"+gsxx.getGsdm()+"----q值"+
                                        q+"----openid"+wxFpxx.getOpenId()+"----支付宝"+wxFpxx.getUserid()+
                                        "------订单编号"+wxFpxx.getOrderNo()+"------发票类型"+wxFpxx.getWxtype());
                                try {
                                    wxfpxxJpaDao.save(wxFpxx);
                                }catch (Exception e){
                                    e.printStackTrace();
                                    logger.info("交易信息保存失败");
                                    return ;
                                }
                            }else {
                                wxFpxxByTqm.setTqm(tqm);
                                wxFpxxByTqm.setGsdm(gsxx.getGsdm());
                                wxFpxxByTqm.setOrderNo(tqm);
                                wxFpxxByTqm.setQ(q);
                                wxFpxxByTqm.setWxtype("1");
                                //微信
                                wxFpxxByTqm.setOpenId((String) session.getAttribute("openid"));
                                if(wxFpxxByTqm.getCode()!=null||!"".equals(wxFpxxByTqm.getCode())){
                                    String notNullCode= wxFpxxByTqm.getCode();
                                    wxFpxxByTqm.setCode(notNullCode);
                                }
                                logger.info("第一次扫码之后所有---存入数据提取码"+tqm+"----公司代码"+gsxx.getGsdm()+"----q值"+q+
                                        "----微信openid"+wxFpxxByTqm.getOpenId()+"-------支付宝userid"
                                        +wxFpxxByTqm.getUserid()+"------订单编号"+wxFpxxByTqm.getOrderNo()+"-----发票类型"+wxFpxxByTqm.getWxtype());
                                try {
                                    wxfpxxJpaDao.save(wxFpxxByTqm);
                                }catch (Exception e){
                                    e.printStackTrace();
                                    logger.info("交易信息保存失败");
                                    return ;
                                }
                            }
                        }
                        response.sendRedirect(request.getContextPath() + "/mbddqr.html?_t=" + System.currentTimeMillis()
                                      +"=" + gsxx.getGsdm() + "=" + tqm + "=" + jyxxsq.getJshj() +"=" + sdf.format(jyxxsq.getDdrq()));
                        logger.info("手机扫码跳转订单确认页面地址------"+request.getContextPath() + "/mbddqr.html?_t=" + System.currentTimeMillis()
                                +"=" + gsxx.getGsdm() + "=" + tqm + "=" + jyxxsq.getJshj() +"=" + sdf.format(jyxxsq.getDdrq()));
                        return;
                    }
                }else {
                    request.getSession().setAttribute("msg", "秘钥不匹配,验签失败!");
                    response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
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
        logger.info("进入获取微信openid---");
        Map params = new HashMap<>();
        params.put("gsdm",state);
        Gsxx gsxx = gsxxservice.findOneByParams(params);
        if(gsxx.getWxappid() == null || gsxx.getWxsecret() ==null){
            gsxx.setWxappid(APP_ID);
            gsxx.setWxsecret(SECRET);
        }
        String turl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + WeiXinConstants.APP_ID + "&secret="
                + WeiXinConstants.APP_SECRET + "&code=" + code + "&grant_type=authorization_code";
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
        if(null!=gsxx.getGsdm()&&gsxx.getGsdm().equals("hongkang")){
            response.sendRedirect(request.getContextPath() + "/" + gsxx.getGsdm() + "_page.jsp?gsdm="+gsxx.getGsdm()+"&&_t=" + System.currentTimeMillis());
            return;
        }
        response.sendRedirect(request.getContextPath() + "SQ/mb.html?gsdm="+gsxx.getGsdm()+"&&t=" + System.currentTimeMillis());
        return;
    }

    /**
     * 提取验证
     * @param tqm
     * @param code
     * @param gsdm
     * @return
     */
    @RequestMapping(value = "/tqyz")
    @ResponseBody
    public Map<String,Object> tqyz(String tqm,String code,String gsdm) {
        String sessionCode = (String) session.getAttribute("rand");

        String opendid = (String) session.getAttribute("openid");
        Map<String, Object> result = new HashMap<String, Object>();
        if (code != null && sessionCode != null && code.equals(sessionCode)) {
            Map resultMap = new HashMap();
            Map map = new HashMap<>();
            List<Kpls> list = new ArrayList<>();
            map.put("gsdm", gsdm);
            if(null!=gsdm &&"cmsc".equals(gsdm)){
                map.put("khh",tqm);
            }else {
                map.put("tqm", tqm);
            }
            if(null!= gsdm && "cmsc".equals(gsdm)){
                list = jylsService.findBykhh(map);
            }else {
                list = jylsService.findByTqm(map);
            }
            Jyls jyls = jylsService.findOne(map);
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
                request.getSession().setAttribute("djh",list.get(0).getDjh());
                request.getSession().setAttribute("serialorder",list.get(0).getSerialorder());
                for (Kpls kpls2: list) {
                    pdfdzs += kpls2.getPdfurl().replace(".pdf",".jpg") + ",";
                }
                if(pdfdzs.length() > 0){
                    result.put("pdfdzs",pdfdzs.substring(0,pdfdzs.length() - 1));
                    request.getSession().setAttribute("pdfdzs",pdfdzs.substring(0,pdfdzs.length() - 1));
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
                //表示已经开过票 --领取发票类型
                if(WeixinUtils.isWeiXinBrowser(request)){
                    WxFpxx wxFpxxByTqm = wxfpxxJpaDao.selsetByOrderNo(tqm);
                    if(null==wxFpxxByTqm){
                        WxFpxx wFpxx = new WxFpxx();
                        wFpxx.setTqm(tqm);
                        wFpxx.setGsdm(gsdm);
                        wFpxx.setOrderNo(tqm);
                        wFpxx.setQ("");
                        wFpxx.setWxtype("2");
                        wFpxx.setOpenId(opendid);
                        wFpxx.setKplsh(list.get(0).getKplsh().toString());
                        try {
                            wxfpxxJpaDao.save(wFpxx);
                        }catch (Exception e){
                            e.printStackTrace();
                            logger.info("交易信息保存失败1");
                            return null;
                        }
                    }else {
                        wxFpxxByTqm.setTqm(tqm);
                        wxFpxxByTqm.setGsdm(gsdm);
                        wxFpxxByTqm.setQ("");
                        wxFpxxByTqm.setOpenId(opendid);
                        wxFpxxByTqm.setOrderNo(tqm);
                        wxFpxxByTqm.setWxtype("2");//1:申请开票2：领取发票
                        wxFpxxByTqm.setKplsh(list.get(0).getKplsh().toString());
                        if(wxFpxxByTqm.getCode()!=null||!"".equals(wxFpxxByTqm.getCode())){
                            String notNullCode= wxFpxxByTqm.getCode();
                            wxFpxxByTqm.setCode(notNullCode);
                        }
                        try {
                            wxfpxxJpaDao.save(wxFpxxByTqm);
                        }catch (Exception e){
                            e.printStackTrace();
                            logger.info("交易信息保存失败2");
                            return null;
                        }
                    }
                }
            }
            else if(null != jyls && null !=jyls.getDjh()){
                result.put("num","6");
            }else {
                Cszb zb1 = cszbService.getSpbmbbh(gsdm, null,null, "sfdyjkhqkp");
                if(list.size()== 0 && null!=zb1.getCsz()&& zb1.getCsz().equals("是")){
                    //需要调用接口获取开票信息,并跳转发票提取页面
                   logger.info("start+++++++++++调用接口获取开票如：绿地优鲜");
                    //全家调用接口 解析xml
                    if(gsdm.equals("ldyx")){
                        logger.info("绿地优鲜拉取数据---------------------");
                        //第一次请求url获取token 验证
                        resultMap=getDataService.getldyxFirData(tqm,gsdm);
                        String accessToken = (String) resultMap.get("accessToken");
                        if(null == accessToken || "".equals(accessToken)){
                            result.put("num","12");
                            result.put("msg","获取数据失败，请重试！");
                            return result;
                        }else{
                            resultMap = getDataService.getldyxSecData(tqm,gsdm,accessToken);
                        }
                        if(null!=resultMap.get("msg")){
                            result.put("num","12");
                            result.put("msg",resultMap.get("msg"));
                            return result;
                        }
                    }else if("bqw".equals(gsdm)){
                        logger.info("波奇网拉取数据--------------");
                        Cszb  csz =  cszbService.getSpbmbbh(gsdm, null,null, "sfhhurl");
                        resultMap = getDataService.getDataForBqw(tqm, gsdm,csz.getCsz());
                    }
                    List<Jyxxsq> jyxxsqList=(List)resultMap.get("jyxxsqList");
                    List<Jymxsq> jymxsqList=(List)resultMap.get("jymxsqList");
                    List<Jyzfmx> jyzfmxList = (List) resultMap.get("jyzfmxList");
                    if(null!=jyxxsqList){
                        Jyxxsq jyxxsq=jyxxsqList.get(0);
                        request.getSession().setAttribute(gsdm+tqm+"je",jyxxsq.getJshj());
                    }
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
                    result.put("num","5");
                    result.put("tqm",tqm);
                    result.put("gsdm",gsdm);
                }
                else{
                    //不用获取数据，数据为空
                    result.put("num","1");
                    result.put("tqm",tqm);
                    result.put("gsdm",gsdm);
                }
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
    public Map<String,Object> spmx(String tqm,String gsdm) {
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            if(gsdm==null){
                request.getSession().setAttribute("msg", "出现未知异常，请重试!");
                response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                return null;
            }else {
                if("ldyx".equals(gsdm)){
                    List<Jymxsq> jymxsqList = (List)request.getSession().getAttribute(gsdm+tqm+"jymxsqList");
                    List<Jyzfmx> jyzfmxList = (List)request.getSession().getAttribute(gsdm+tqm+"jyzfmxList");
                    Double zjedb = (Double) request.getSession().getAttribute(gsdm+tqm+"je");

                    BigDecimal zje = new BigDecimal(zjedb.toString());
                    logger.info("总金额"+zje);
                    BigDecimal bkpje = new BigDecimal("0");
                    BigDecimal sjkpje = new BigDecimal("0");
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
                                bkpje=bkpje.add(new BigDecimal(jyzfmx2.getZfje().toString()));
                            }
                            System.out.println("不开票金额为"+bkpje);
                        }
                        sjkpje = zje.subtract(bkpje);
                        System.out.println("实际开票金额"+sjkpje);
                    }
                    int b = sjkpje.compareTo(new BigDecimal("0"));
                    if(b == 0){
                        logger.info("不开票金额为0");
                        result.put("num","12");
                    }
                    request.getSession().setAttribute(gsdm+tqm+"sjkpje",sjkpje);
                    result.put("jymxsqList",jymxsqList);
                    result.put("jyzfmxList",jyzfmxList);
                    result.put("sjkpje",sjkpje);
                    return result;
                }
                if("bqw".equals(gsdm)){
                    Map resultMap = (Map) request.getSession().getAttribute(gsdm + tqm + "resultMap");
                    if(resultMap == null){
                        request.getSession().setAttribute("msg", "该请求已过期，请重试!");
                        response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                        return null;
                    }
                    List<Jyxxsq>  jyxxsqList=(List)resultMap.get("jyxxsqList");
                    List<Jymxsq>  jymxsqList=(List)resultMap.get("jymxsqList");
                    result.put("jymxsqList",jymxsqList);
                    result.put("sjkpje",jyxxsqList.get(0).getJshj());
                    request.getSession().setAttribute(gsdm+tqm+"sjkpje",jyxxsqList.get(0).getJshj());
                    return result;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        Map gsMap = new HashMap();
        gsMap.put("gsdm",jyxxsq.getGsdm());
        Gsxx gsxx = gsxxservice.findOneByGsdm(gsMap);
        try {
            String xml = GetXmlUtil.getFpkjXml(jyxxsq, jymxsqList, jyzfmxList);
            logger.info("secretKey------" + gsxx.getSecretKey());
            logger.info("appKey------" + gsxx.getAppKey());
            String resultxml = HttpUtils.HttpUrlPost(xml, gsxx.getAppKey(), gsxx.getSecretKey());
            logger.info("-------返回值---------" + resultxml);
            Document document = DocumentHelper.parseText(resultxml);
            Element root = document.getRootElement();
            List<Element> childElements = root.elements();
            Map xmlMap = new HashMap();
            for (Element child : childElements) {
                xmlMap.put(child.getName(), child.getText());
            }
            String returncode = (String) xmlMap.get("ReturnCode");
            String ReturnMessage = (String) xmlMap.get("ReturnMessage");
            if (returncode.equals("9999")) {
                logger.info("发送客户端失败----msg--"+ReturnMessage);
                result.put("msg", ReturnMessage);
                return result;
            } else if(returncode.equals("0000")){
                logger.info("发送客户端成功----");
            Tqmtq tqmtq1 = new Tqmtq();
            tqmtq1.setDdh(jyxxsq.getTqm());
            tqmtq1.setLrsj(new Date());
            if("bqw".equals(gsdm)){
                tqmtq1.setZje(Double.valueOf(String.valueOf(request.getSession().getAttribute(gsdm + tqm + "sjkpje"))));
            }else {
                tqmtq1.setZje(Double.valueOf(String.valueOf(request.getSession().getAttribute(gsdm + tqm + "je"))));
            }
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
            if (openid != null && !"null".equals(openid)) {
                tqmtq1.setOpenid(openid);
            }
            tqmtqService.save(tqmtq1);
            result.put("msg", "1");
        }
        }catch(Exception e){
            e.printStackTrace();
        }
        return  result;
    }

    @RequestMapping(value = "/fpsession")
    @ResponseBody
    public Map fpsession(String tqm,String gsdm) {
        Map<String, Object> result = new HashMap<String, Object>();
        Map csmap = new HashMap<>();
        csmap.put("tqm", tqm);
        csmap.put("gsdm", gsdm);
        List<Kpls> jylslist = jylsService.findByTqm(csmap);
        Map map2 = new HashMap();
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
        //request.getSession().setAttribute("msg", "请重新扫描二维码");
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        result.put("kprq",sdf.format(kplsList.get(0).getKprq()));
        result.put("price",kplsList.get(0).getJshj());
        Jyls jyls = new Jyls();
        jyls.setGsdm(kplsList.get(0).getGsdm());
        jyls.setDjh((Integer) request.getSession().getAttribute("djh"));
        jyls.setJylsh(kplsList.get(0).getJylsh());
        Jyls jyls1 = jylsService.findOneByParams(jyls);
        if(jyls1.getGsdm().equals("hdsc")||jyls1.getGsdm().equals("cmsc")){
            result.put("orderNo",jyls1.getKhh());
        }else {
            result.put("orderNo",jyls1.getTqm());
        }
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

        //System.out.println(""+session.getAttribute(gsdm+tqm+"fptt").toString());
        result.put("price",request.getSession().getAttribute("price"));
        result.put("orderTime",request.getSession().getAttribute("orderTime"));
        result.put("orderNo",request.getSession().getAttribute("orderNo"));
        result.put("order",request.getSession().getAttribute("order"));
        result.put("tqm",request.getSession().getAttribute("tqm"));
        result.put("error",request.getSession().getAttribute("error"));
        result.put("msg",request.getSession().getAttribute("msg"));
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