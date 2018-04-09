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
import com.rjxx.taxeasy.dto.AdapterDataOrderBuyer;
import com.rjxx.taxeasy.service.AdapterService;
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
    private AdapterService adapterService;
    @Autowired
    private XfJpaDao xfJpaDao;
    @Autowired
    private SkpJpaDao skpJpaDao;
    @Autowired
    private wechatFpxxServiceImpl wechateFpxxService;
    @Autowired
    private CommonController commonController;
    @Autowired
    private wechatFpxxServiceImpl wechatFpxxService;

    private static final String TYPE_ONE_CALLBACKURL = "kptService/getOpenidForOne";
    private static final String TYPE_TWO_CALLBACKURL = "kptService/getOpenid";
    private static final String TYPE_THREE_CALLBACKURL = "kptService/getOpenid";


    @RequestMapping(value = "/{gsdm}/{q}", method = RequestMethod.GET)
    public void get(@PathVariable String gsdm, @PathVariable String q) {
        String ua = request.getHeader("user-agent").toLowerCase();
        Gsxx gsxx = gsxxJpaDao.findOneByGsdm(gsdm);
        if(gsxx==null){
//            errorRedirect("COMPANY_MSG_ERROR");
            errorRedirect("未知的公司代码");
            return;
        }
        Boolean checkResult = RJCheckUtil.checkMD5ForAll(gsxx.getSecretKey(), q);
        if (!checkResult) {
//            errorRedirect("QRCODE_VALIDATION_FAILED");
            errorRedirect("二维码验签失败");
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
//            errorRedirect("MISSING_TYPE");
            errorRedirect("开票类型缺失");
            return;
        }
        switch (type) {
            case "1":
                if (!StringUtil.isNotBlankList(on, ot, pr)) {
//                    errorRedirect("TYPE_ONE_REQUIRED_PARAMETER_MISSING");
                    errorRedirect("必要参数缺失");
                    return;
                }
                session.setAttribute("gsdm", gsdm);
                session.setAttribute("q", q);
                String grantOne = isWechat(ua, TYPE_ONE_CALLBACKURL);
                try {
                    if (grantOne != null) {
                        response.sendRedirect(grantOne);
                        return;
                    } else {
                        response.sendRedirect(
                                request.getContextPath() + "/qrcode/input.html?" +
                                        "t=" + System.currentTimeMillis() +
                                        "=" + on +
                                        "=" + ot +
                                        "=" + pr);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
//                    errorRedirect("REDIRECT_ERROR");
                    errorRedirect("重定向失败");
                    return;
                }
            case "2":
                if (!StringUtil.isNotBlankList(on, ot, pr)) {
//                    errorRedirect("TYPE_TWO_REQUIRED_PARAMETER_MISSING");
                    errorRedirect("必要参数缺失");
                    return;
                }
                session.setAttribute("q", q);
                session.setAttribute("gsdm", gsdm);
                String grant = isWechat(ua, TYPE_TWO_CALLBACKURL);
                //如果是微信浏览器，则拉取授权
                if (grant != null) {
                    try {
                        response.sendRedirect(grant);
                    } catch (IOException e) {
                        e.printStackTrace();
//                        errorRedirect("REDIRECT_ERROR");
                        errorRedirect("重定向失败");
                    }
                    return;
                } else {
                    Map result = adapterService.getGrandMsg(gsdm, q);
                    deal(result, gsdm);
                }
                break;
            case "3":
                if (StringUtil.isBlankList(on, tq)) {
//                    errorRedirect("TYPE_THREE_REQUIRED_PARAMETER_MISSING");
                    errorRedirect("必要参数缺失");
                    return;
                }
                String orderNo = "";
                String extractCode = "";
                String storeNo = "";
                if (StringUtil.isNotBlankList(on)) {
                    orderNo = on;
                } else {
                    orderNo = tq;
                }

                if (StringUtil.isNotBlankList(tq)) {
                    extractCode = tq;
                } else {
                    extractCode = on;
                }

                if (StringUtil.isNotBlankList(sn)) {
                    storeNo = sn;
                } else {
                    try {
                        Xf xf = xfJpaDao.findOneByGsdm(gsdm);
                        Skp skp = skpJpaDao.findOneByGsdmAndXfsh(gsdm, xf.getId());
                        storeNo = skp.getKpddm();
                    } catch (Exception e) {
//                        errorRedirect("TYPE_THREE_SN_PARAMETER_MISSING");
                        errorRedirect("确认订单时门店号缺失");
                        return;
                    }
                }
                session.setAttribute("gsdm", gsdm);
                session.setAttribute("on", orderNo);
                session.setAttribute("sn", storeNo);
                session.setAttribute("tq", extractCode);
                //跳转不需要Q，微信发票信息需要
                session.setAttribute("q", q);
                String grantThree = isWechat(ua, TYPE_THREE_CALLBACKURL);
                //如果是微信浏览器，则拉取授权
                if (grantThree != null) {
                    try {
                        response.sendRedirect(grantThree);
                    } catch (IOException e) {
                        e.printStackTrace();
//                        errorRedirect("REDIRECT_ERROR");
                        errorRedirect("重定向失败");
                        return;
                    }
                    return;
                } else {
                    Map result = adapterService.getGrandMsg(gsdm, orderNo, storeNo);
                    deal(result, gsdm);
                }
                break;
            default:
//                errorRedirect("UNKNOWN_TYPE");
                errorRedirect("未知的开票类型");
                return;
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
//            errorRedirect("GET_WECHAT_AUTHORIZED_FAILED");
            errorRedirect("获取微信授权失败");
            return;
        }
        String gsdm = session.getAttribute("gsdm").toString();
        String q = session.getAttribute("q").toString();
        String on = (String) session.getAttribute("on");
        String sn = (String) session.getAttribute("sn");
        Map result;
        if (StringUtil.isNotBlankList(q)&&StringUtil.isBlankList(on,sn)) {
            result = adapterService.getGrandMsg(gsdm, q);
        } else {
            result = adapterService.getGrandMsg(gsdm, on, sn);
        }
        deal(result, gsdm);
    }

    @RequestMapping("/getOpenidForOne")
    public void getOpenidForOne(String state, String code) {
        String turl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + WeiXinConstants.APP_ID + "&secret="
                + WeiXinConstants.APP_SECRET + "&code=" + code + "&grant_type=authorization_code";
        String resultJson = HttpClientUtil.doGet(turl);
        JSONObject resultObject = JSONObject.parseObject(resultJson);
        String openid = resultObject.getString("openid");
        if (openid != null) {
            session.setAttribute("openid", openid);
        }
        if (session.getAttribute("gsdm") == null) {
//            errorRedirect("GET_WECHAT_AUTHORIZED_FAILED");
            errorRedirect("获取微信授权失败");
            return;
        }
        String gsdm = (String) session.getAttribute("gsdm");
        String q = (String) session.getAttribute("q");
        Map map = RJCheckUtil.decodeForAll(q);
        String data = (String) map.get("A0");
        JSONObject jsonData = JSON.parseObject(data);
        String on = jsonData.getString("on");
        String ot = jsonData.getString("ot");
        String pr = jsonData.getString("pr");
        String sn = jsonData.getString("sn");
        String storeNoOne = "";
        if (StringUtil.isNotBlankList(sn)) {
            storeNoOne = sn;
        } else {
            try {
                Xf xf = xfJpaDao.findOneByGsdm(gsdm);
                Skp skp = skpJpaDao.findOneByGsdmAndXfsh(gsdm, xf.getId());
                storeNoOne = skp.getKpddm();
            } catch (Exception e) {
//                errorRedirect("TYPE_ONE_SN_PARAMETER_MISSING");
                errorRedirect("获取OPENID时门店号缺失");
                return;
            }
        }
        boolean b = wechatFpxxService.InFapxx(null, gsdm, on, q, "1", openid, "", null, request,"1");
        if (!b) {
            errorRedirect("保存发票信息失败，请重试");
            return;
        }
        commonController.isWeiXin(storeNoOne, on, ot, pr, gsdm);
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
        String apiType;
        if (StringUtil.isNotBlankList(q)&&StringUtil.isBlankList(on,sn)) {
            jsonData = adapterService.getSpxx(gsdm, q);
            apiType = "2";
        } else {
            if (!StringUtil.isNotBlankList(on, sn, tq)) {
                return ResultUtil.error("交易信息获取失败");
            }
            jsonData = adapterService.getSpxx(gsdm, on, sn, tq);
            apiType = "3";
        }
        if (jsonData != null) {
            JSONObject jsonObject = JSON.parseObject(jsonData);
            String tqm = jsonObject.getString("tqm");
            String orderNo = jsonObject.getString("orderNo");
            boolean b = wechateFpxxService.InFapxx(tqm, gsdm, orderNo, q, "1", openid,
                    (String) request.getSession().getAttribute(AlipayConstants.ALIPAY_USER_ID), "", request,apiType);
            if (!b) {
                return ResultUtil.error("保存发票信息失败，请重试！");
            }
            return ResultUtil.success(jsonData);//订单号,订单时间,门店号,金额,商品名,商品税率
        } else {
            if ("1".equals(jsonData)) {
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
        if (StringUtil.isNotBlankList(q)&&StringUtil.isBlankList(on,sn)) {
            status = adapterService.makeInvoice(gsdm, q, gfmc, gfsh, email, gfyh, gfyhzh, gfdz, gfdh, tqm, userId, "5", "", "");
        } else {
            status = adapterService.makeInvoice(gsdm, on, sn, tq, gfmc, gfsh, email, gfyh, gfyhzh, gfdz, gfdh, tqm, userId, "5", "", "");
        }
        //开票
        if ("-1".equals(status)) {
            return ResultUtil.error("开具失败");
        } else if ("0".equals(status)) {
            return ResultUtil.error("所需信息为空");
        } else if("-2".equals(status)){
            return ResultUtil.error("交易数据上传中");
        }else {
            JSONObject jsonObject = JSON.parseObject(status);
            session.setAttribute("serialorder", jsonObject.getString("serialorder"));
            return ResultUtil.success(status);
        }
    }

    @RequestMapping(value = "/input", method = RequestMethod.GET)
    public Result input(@RequestParam String gfmc, @RequestParam String gfsh, @RequestParam String email,
                        String gfdz, String gfdh, String gfyhzh, String gfyh) {
        String gsdm = (String) session.getAttribute("gsdm");
        String q = (String) session.getAttribute("q");
        Map map = RJCheckUtil.decodeForAll(q);
        String data = (String) map.get("A0");
        JSONObject jsonData = JSON.parseObject(data);
        String sn = jsonData.getString("sn");
        String storeNoOne = "";
        if (StringUtil.isNotBlankList(sn)) {
            storeNoOne = sn;
        } else {
            try {
                Xf xf = xfJpaDao.findOneByGsdm(gsdm);
                Skp skp = skpJpaDao.findOneByGsdmAndXfsh(gsdm, xf.getId());
                storeNoOne = skp.getKpddm();
            } catch (Exception e) {
//                errorRedirect("TYPE_ONE_SN_PARAMETER_MISSING");
                errorRedirect("发送抬头时门店号缺失");
                return null;
            }
        }
        AdapterDataOrderBuyer buyer = new AdapterDataOrderBuyer();
        buyer.setName(gfmc);
        buyer.setIdentifier(gfsh);
        buyer.setEmail(email);
        buyer.setAddress(gfdz);
        buyer.setTelephoneNo(gfdh);
        buyer.setBankAcc(gfyhzh);
        buyer.setBank(gfyh);
        boolean b = adapterService.sendBuyer(gsdm, storeNoOne, buyer);
        if (!b) {
            return ResultUtil.error("发送失败");
        }
        return ResultUtil.success();
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
                session.setAttribute("tqm",tqm);
                session.setAttribute("orderNo",orderNo);
                List<String> status = adapterService.checkStatus(tqm, gsdm);
                if (!status.isEmpty()) {
                    if (status.contains("可开具")) {
                        if (StringUtils.isNotBlank(ppdm)) {
                            //有品牌代码对应的url
                            response.sendRedirect(request.getContextPath() + ppurl + "?t=" + System.currentTimeMillis() + "=" + ppdm + "=" + headcolor + "=" + bodycolor);
                            return;
                        } else {
                            //无品牌
//                            errorRedirect("GET_GRAND_ERROR");
                            errorRedirect("获取品牌失败");
                            return;
                        }
                    } else if (status.contains("开具中")) {
                        //开具中对应的url
                        response.sendRedirect(request.getContextPath() + "/QR/zzkj.html?t=" + System.currentTimeMillis());
                        return;
                    }else if(status.contains("纸票")){
                        errorRedirect("该订单已开具纸质发票，不可重复开票");
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
//                    errorRedirect("GET_PDF_STATE_ERROR");
                    errorRedirect("获取PDF文件失败");
                    return;
                }
            } else {
//                errorRedirect("STORE_BRAND_MISSING");
                errorRedirect("门店号或品牌缺失");
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
//            errorRedirect("REDIRECT_ERROR");
            errorRedirect("重定向错误");
            return;
        }
    }

    private String isWechat(String ua, String callbackurl) {
        //判断是否是微信浏览器
        if (ua.indexOf("micromessenger") > 0) {
            String url = HtmlUtils.getBasePath(request);
            String ul = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinConstants.APP_ID + "&redirect_uri="
                    + url + callbackurl + "&" + "response_type=code&scope=snsapi_base&state=" + "state"
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
    }
}
