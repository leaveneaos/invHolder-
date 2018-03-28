package com.rjxx.taxeasy.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.dao.GsxxJpaDao;
import com.rjxx.taxeasy.dao.SkpJpaDao;
import com.rjxx.taxeasy.dao.XfJpaDao;
import com.rjxx.taxeasy.domains.Gsxx;
import com.rjxx.taxeasy.domains.Skp;
import com.rjxx.taxeasy.domains.Xf;
import com.rjxx.taxeasy.service.AdapterService;
import com.rjxx.taxeasy.service.jkpz.JkpzService;
import com.rjxx.taxeasy.utils.alipay.AlipayConstants;
import com.rjxx.taxeasy.wechat.dto.Result;
import com.rjxx.taxeasy.wechat.util.ResultUtil;
import com.rjxx.utils.HtmlUtils;
import com.rjxx.utils.RJCheckUtil;
import com.rjxx.utils.StringUtil;
import com.rjxx.utils.weixin.HttpClientUtil;
import com.rjxx.utils.weixin.WeiXinConstants;
import com.rjxx.utils.weixin.wechatFpxxServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author wangyahui
 * @email wangyahui@datarj.com
 * @company 上海容津信息技术有限公司
 * @date 2018/3/20
 */

@RestController
@RequestMapping("/kptService")
public class AdapterController extends BaseController {
    @Autowired
    private GsxxJpaDao gsxxJpaDao;
    @Autowired
    private JkpzService jkpzService;
    @Autowired
    private AdapterService adapterService;
    @Autowired
    private XfJpaDao xfJpaDao;
    @Autowired
    private SkpJpaDao skpJpaDao;
    @Autowired
    private wechatFpxxServiceImpl wechateFpxxService;

    private static final String TYPE_ONE_CALLBACKURL = "";
    private static final String TYPE_TWO_CALLBACKURL = "adapter/getOpenid";
    private static final String TYPE_THREE_CALLBACKURL = "adapter/getOpenid";



    @RequestMapping(value = "/{gsdm}", method = RequestMethod.GET)
    public void get(@PathVariable String gsdm, @RequestParam String q) {
        String ua = request.getHeader("user-agent").toLowerCase();
        Gsxx gsxx = gsxxJpaDao.findOneByGsdm(gsdm);
        Boolean checkResult = RJCheckUtil.checkMD5ForAll(gsxx.getSecretKey(), q);
        if (!checkResult) {
            errorRedirect("QRCODE_VALIDATION_FAILED");
            return;
        }
        Map map = RJCheckUtil.decodeForAll(q);
        String data = (String) map.get("A0");
        JSONObject jsonData = JSON.parseObject(data);
        String on = jsonData.getString("on");
        String ot = jsonData.getString("ot");
        String pr = jsonData.getString("pr");
        String sn = jsonData.getString("sn");
        String sp = jsonData.getString("sp");
        String tq = jsonData.getString("tq");
        String type = jsonData.getString("type");
        if (!StringUtil.isNotBlankList(type)) {
            errorRedirect("MISSING_TYPE");
        }
        switch (type) {
            case "1":
                if (!StringUtil.isNotBlankList(on, ot, pr)) {
                    errorRedirect("TYPE_ONE_REQUIRED_PARAMETER_MISSING");
                }
                break;
            case "2":
                if (!StringUtil.isNotBlankList(on, ot, pr)) {
                    errorRedirect("TYPE_TWO_REQUIRED_PARAMETER_MISSING");
                }
                session.setAttribute("q", q);
                session.setAttribute("gsdm", gsdm);
                String grant = isWechat(ua,TYPE_TWO_CALLBACKURL);
                //如果是微信浏览器，则拉取授权
                if (grant != null) {
                    try {
                        response.sendRedirect(grant);
                    } catch (IOException e) {
                        e.printStackTrace();
                        errorRedirect("REDIRECT_ERROR");
                    }
                    return;
                } else {
                    Map result = adapterService.getGrandMsg(gsdm, q);
                    deal(result, gsdm);
                }
                break;
            case "3":
                if (StringUtil.isBlankList(on,tq)) {
                    errorRedirect("TYPE_THREE_REQUIRED_PARAMETER_MISSING");
                }
                String orderNo="";
                String extractCode="";
                String storeNo ="";
                if(StringUtil.isNotBlankList(on)){
                    orderNo = on;
                }else{
                    orderNo = tq;
                }

                if(StringUtil.isNotBlankList(tq)){
                    extractCode = tq;
                }else{
                    extractCode = on;
                }

                if(StringUtil.isNotBlankList(sn)){
                    storeNo = sn;
                }else{
                    try {
                        Xf xf = xfJpaDao.findOneByGsdm(gsdm);
                        Skp skp = skpJpaDao.findOneByGsdmAndXfsh(gsdm, xf.getId());
                        storeNo = skp.getKpddm();
                    }catch (Exception e){
                        errorRedirect("TYPE_THREE_SN_PARAMETER_MISSING");
                    }
                }
                session.setAttribute("gsdm", gsdm);
                session.setAttribute("on", orderNo);
                session.setAttribute("sn", storeNo);
                session.setAttribute("tq", extractCode);
                String grantThree = isWechat(ua,TYPE_THREE_CALLBACKURL);
                //如果是微信浏览器，则拉取授权
                if (grantThree != null) {
                    try {
                        response.sendRedirect(grantThree);
                    } catch (IOException e) {
                        e.printStackTrace();
                        errorRedirect("REDIRECT_ERROR");
                    }
                    return;
                } else {
                    Map result = adapterService.getGrandMsg(gsdm, orderNo,storeNo);
                    deal(result, gsdm);
                }
                break;
            default:
                errorRedirect("UNKNOWN_TYPE");
                break;
        }
    }

    @RequestMapping("/getOpenid")
    public void getOpenid(String state, String code) {
        String turl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + WeiXinConstants.APP_ID + "&secret="
                + WeiXinConstants.APP_SECRET + "&code=" + code + "&grant_type=authorization_code";
        String resultJson = HttpClientUtil.doGet(turl);
        JSONObject resultObject = JSONObject.parseObject(resultJson);
        String openid = resultObject.getString("openid");
        if (openid != null) {
            session.setAttribute("openid", openid);
        }
        if (session.getAttribute("gsdm") == null) {
            errorRedirect("GET_WECHAT_AUTHORIZED_FAILED");
        }
        String gsdm = session.getAttribute("gsdm").toString();
        String q = session.getAttribute("q").toString();
        String on = (String) session.getAttribute("on");
        String sn = (String) session.getAttribute("sn");
        Map result;
        if(StringUtil.isNotBlankList(q)){
            result = adapterService.getGrandMsg(gsdm, q);
        }else{
            result = adapterService.getGrandMsg(gsdm, on,sn);
        }
        deal(result, gsdm);
    }

    @RequestMapping("/scanConfirm")
    public Result smConfirm() {
        String q = (String) session.getAttribute("q");
        String gsdm = (String) session.getAttribute("gsdm");
        String openid = (String) session.getAttribute("openid");
        String on = (String) session.getAttribute("on");
        String sn = (String) session.getAttribute("sn");
        String tq = (String) session.getAttribute("tq");
        if (!StringUtil.isNotBlankList(gsdm)) {
            return ResultUtil.error("session过期,请重新扫码");
        }
        String jsonData;
        if(StringUtil.isNotBlankList(q)){
            jsonData = adapterService.getSpxx(gsdm, q);
        }else{
            if(!StringUtil.isNotBlankList(on,sn,tq)){
                return ResultUtil.error("交易信息获取失败");
            }
            jsonData = adapterService.getSpxx(gsdm, on, sn, tq);
        }
        if (jsonData != null) {
            JSONObject jsonObject = JSON.parseObject(jsonData);
            String tqm = jsonObject.getString("tqm");
            String orderNo = jsonObject.getString("orderNo");
            boolean b = wechateFpxxService.InFapxx(tqm, gsdm, orderNo, q, "1", openid,
                    (String) request.getSession().getAttribute(AlipayConstants.ALIPAY_USER_ID), "", request);
            if (!b) {
                return ResultUtil.error("保存发票信息失败，请重试！");
            }
            return ResultUtil.success(jsonData);//订单号,订单时间,门店号,金额,商品名,商品税率
        } else {
            if("1".equals(jsonData)){
                return ResultUtil.error("交易数据上传中");
            }
            return ResultUtil.error("二维码信息获取失败");
        }
    }

    @RequestMapping("/submit")
    public Result submit(@RequestParam String gfmc, @RequestParam String gfsh, @RequestParam String email,
                         String gfdz, String gfdh, String gfyhzh, String gfyh, String tqm) {
        String gsdm = (String) session.getAttribute("gsdm");
        String q = (String) session.getAttribute("q");
        String on = (String) session.getAttribute("on");
        String sn = (String) session.getAttribute("sn");
        String tq = (String) session.getAttribute("tq");
        String userId = (String) request.getSession().getAttribute(AlipayConstants.ALIPAY_USER_ID);
        if (gsdm == null) {
            return ResultUtil.error("redirect");
        }
        String status;
        if(StringUtil.isNotBlankList(q)) {
            status = adapterService.makeInvoice(gsdm, q, gfmc, gfsh, email, gfyh, gfyhzh, gfdz, gfdh, tqm, userId, "5", "", "");
        }else {
            status = adapterService.makeInvoice(gsdm,on,sn,tq,gfmc, gfsh, email, gfyh, gfyhzh, gfdz, gfdh, tqm, userId, "5", "", "");
        }
        //开票
        if ("-1".equals(status)) {
            return ResultUtil.error("开具失败");
        } else if ("0".equals(status)) {
            return ResultUtil.error("所需信息为空");
        } else {
            JSONObject jsonObject = JSON.parseObject(status);
            session.setAttribute("serialorder", jsonObject.getString("serialorder"));
            return ResultUtil.success(status);
        }
    }

    private void deal(Map result, String gsdm) {
        try {
            if (result != null) {
                String ppdm = result.get("ppdm").toString();
                String ppurl = result.get("ppurl").toString();
                String headcolor = result.get("headcolor").toString();
                String bodycolor = result.get("bodycolor").toString();
                String orderNo = result.get("orderNo").toString();
                String tqm = ppdm + orderNo;
                List<String> status = adapterService.checkStatus(tqm, gsdm);
                if (!status.isEmpty()) {
                    if (status.contains("可开具")) {
                        if (StringUtils.isNotBlank(ppdm)) {
                            //有品牌代码对应的url
                            response.sendRedirect(request.getContextPath() + ppurl + "?t=" + System.currentTimeMillis() + "=" + ppdm + "=" + headcolor + "=" + bodycolor);
                            return;
                        } else {
                            //无品牌
                            errorRedirect("GET_GRAND_ERROR");
                        }
                    } else if (status.contains("开具中")) {
                        //开具中对应的url
                        response.sendRedirect(request.getContextPath() + "/QR/zzkj.html?t=" + System.currentTimeMillis());
                        return;
                    } else {
                        StringBuilder sb = new StringBuilder();
                        for (String str : status) {
                            if (str.indexOf("pdf") != -1) {
                                String pdf = str.split("[+]")[0];
                                String img = pdf.replace("pdf", "jpg");
                                sb.append("&" + img);
                            }
                        }
                        String serialOrder = status.get(0).split("[+]")[4];
                        session.setAttribute("serialorder", serialOrder);
                        response.sendRedirect(request.getContextPath() + "/CO/dzfpxq.html?_t=" + System.currentTimeMillis());
                        return;
                    }
                } else {
                    //获取pdf状态码失败的url
                    errorRedirect("GET_PDF_STATE_ERROR");
                }
            } else {
                errorRedirect("STORE_BRAND_MISSING");
            }
        } catch (IOException e) {
            e.printStackTrace();
            errorRedirect("REDIRECT_ERROR");
        }
    }

    private String isWechat(String ua,String callbackurl) {
        //判断是否是微信浏览器
        if (ua.indexOf("micromessenger") > 0) {
            String url = HtmlUtils.getBasePath(request);
            String ul = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinConstants.APP_ID + "&redirect_uri="
                    + url + callbackurl+"&" + "response_type=code&scope=snsapi_base&state=" + "state"
                    + "#wechat_redirect";
            return ul;
        } else {
            return null;
        }
    }

    private void errorRedirect(String errorName) {
        try {
            response.sendRedirect(request.getContextPath() + "/QR/error.html?t=" + System.currentTimeMillis() + "=" + errorName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }
}
