package com.rjxx.taxeasy.controller.adapter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.controller.CommonController;
import com.rjxx.taxeasy.dao.GsxxJpaDao;
import com.rjxx.taxeasy.dao.PpJpaDao;
import com.rjxx.taxeasy.dao.SkpJpaDao;
import com.rjxx.taxeasy.dao.XfJpaDao;
import com.rjxx.taxeasy.domains.*;
import com.rjxx.taxeasy.dto.AdapterDataOrderBuyer;
import com.rjxx.taxeasy.dto.AdapterGet;
import com.rjxx.taxeasy.dto.AdapterPost;
import com.rjxx.taxeasy.service.CszbService;
import com.rjxx.taxeasy.service.adapter.AdapterService;
import com.rjxx.taxeasy.utils.alipay.AlipayConstants;
import com.rjxx.taxeasy.wechat.dto.Result;
import com.rjxx.taxeasy.wechat.util.ResultUtil;
import com.rjxx.utils.Base64Util;
import com.rjxx.utils.HtmlUtils;
import com.rjxx.utils.RJCheckUtil;
import com.rjxx.utils.StringUtil;
import com.rjxx.utils.weixin.HttpClientUtil;
import com.rjxx.utils.weixin.WeiXinConstants;
import com.rjxx.utils.weixin.wechatFpxxServiceImpl;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
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
@CrossOrigin
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
    @Autowired
    private CszbService cszbService;
    @Autowired
    private PpJpaDao ppJpaDao;

    private static final String TYPE_ONE_CALLBACKURL = "kptService/getOpenidForOne";
    private static final String TYPE_TWO_CALLBACKURL = "kptService/getOpenid";
    private static final String TYPE_THREE_CALLBACKURL = "kptService/getOpenid";
    private static final String TYPE_FOUR_CALLBACKURL = "kptService/getOpenidForFour";

    @ApiOperation(value = "手输提取码入口")
    @RequestMapping(value="/{ppdm}",method = RequestMethod.GET)
    public void extract(@PathVariable String ppdm) {
        Pp pp;
        try {
            pp = ppJpaDao.findOneByPpdm(ppdm);
            if(pp==null){
                errorRedirect("GET_GRAND_ERROR");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorRedirect("GET_GRAND_ERROR");
            return;
        }

        String gsdm = pp.getGsdm();
        String buttoncolor = "no";
        String bodycolor = "no";
        String headcolor = "no";
        if(pp.getPpbuttoncolor()!=null){
            buttoncolor = pp.getPpbuttoncolor();
        }
        if(pp.getPpbodycolor()!=null){
            bodycolor = pp.getPpbodycolor();
        }
        if(pp.getPpheadcolor()!=null){
            headcolor = pp.getPpheadcolor();
        }
        session.setAttribute("gsdm", gsdm);
        try {
            response.sendRedirect(
                    request.getContextPath() + "/qrcode/luru.html?t="
                            + System.currentTimeMillis() +"="+ppdm+ "=" + headcolor + "=" + bodycolor + "=" + buttoncolor);
            return;
        } catch (IOException e) {
            e.printStackTrace();
            errorRedirect("REDIRECT_ERROR");
            return;
        }
    }

    @ApiOperation(value = "手输提取码界面提交接口")
    @RequestMapping(value = "/lrqr",method = RequestMethod.POST)
    public Result lrqr(@RequestParam String tq,@RequestParam String code){
        String sessionCode = (String) session.getAttribute("rand");
        if (code != null && sessionCode != null && code.equals(sessionCode)) {
            if(session.getAttribute("gsdm")==null){
                return ResultUtil.error("会话超时");
            }
            String gsdm=(String) session.getAttribute("gsdm");
            Map apiMsg = adapterService.getApiMsg(gsdm, tq);
            if(apiMsg==null){
                return ResultUtil.error("开票数据未上传，请稍后再试");
            }
            if(apiMsg.get("msg")!=null){
                return ResultUtil.error((String) apiMsg.get("msg"));
            }
            AdapterGet adapterGet = new AdapterGet();
            adapterGet.setType("3");
            if(apiMsg.get("jyxxsq")!=null){
                Jyxxsq jyxxsq = (Jyxxsq)apiMsg.get("jyxxsq");
                adapterGet.setOn(jyxxsq.getDdh());
                adapterGet.setSn(jyxxsq.getKpddm());
            }else{
                AdapterPost post = (AdapterPost)apiMsg.get("post");
                adapterGet.setOn(post.getData().getOrder().getOrderNo());
                adapterGet.setSn(post.getClientNo());
            }

            String dataJson = JSON.toJSONString(adapterGet);
            Gsxx gsxx = gsxxJpaDao.findOneByGsdm(gsdm);
            String key = gsxx.getSecretKey();
            String sign = DigestUtils.md5Hex("data=" + dataJson + "&key=" + key);
            String str = "data=" + dataJson + "&si=" + sign;
            String q = null;
            try {
                q = Base64Util.encode(str);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            Map map = new HashMap();
            map.put("url", HtmlUtils.getBasePath(request) + "kptService/" + gsdm + "/" + q);
            return ResultUtil.success(map);
        }else{
            return ResultUtil.error("验证码输入错误");
        }
    }

    @ApiOperation(value = "通用提取接口总入口")
    @RequestMapping(value = "/{gsdm}/{q}", method = RequestMethod.GET)
    public void get(@PathVariable String gsdm, @PathVariable String q) {
        Gsxx gsxx = gsxxJpaDao.findOneByGsdm(gsdm);
        if (gsxx == null) {
            errorRedirect("COMPANY_MSG_ERROR");
            return;
        }
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
        String mi = jsonData.getString("mi");
        if (!StringUtil.isNotBlankList(type)) {
            errorRedirect("MISSING_TYPE");
            return;
        }
        switch (type) {
            case "1":
                if (!StringUtil.isNotBlankList(on, ot, pr)) {
                    errorRedirect("TYPE_ONE_REQUIRED_PARAMETER_MISSING");
                    return;
                }
                session.setAttribute("gsdm", gsdm);
                session.setAttribute("q", q);
                String grantOne = isWechat(TYPE_ONE_CALLBACKURL);
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
                    errorRedirect("REDIRECT_ERROR");
                    return;
                }
            case "2":
                if (!StringUtil.isNotBlankList(on, ot, pr)) {
                    errorRedirect("TYPE_TWO_REQUIRED_PARAMETER_MISSING");
                    return;
                }
                session.setAttribute("q", q);
                session.setAttribute("gsdm", gsdm);
                String grant = isWechat(TYPE_TWO_CALLBACKURL);
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
                if (StringUtil.isBlankList(on, tq)) {
                    errorRedirect("TYPE_THREE_REQUIRED_PARAMETER_MISSING");
                    return;
                }
                String orderNo;
                String extractCode;
                String storeNo;
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
                        errorRedirect("TYPE_THREE_SN_PARAMETER_MISSING");
                        return;
                    }
                }
                session.setAttribute("gsdm", gsdm);
                session.setAttribute("on", orderNo);
                session.setAttribute("sn", storeNo);
                session.setAttribute("tq", extractCode);
                //跳转不需要Q，微信发票信息需要
                session.setAttribute("q", q);
                String grantThree = isWechat(TYPE_THREE_CALLBACKURL);
                //如果是微信浏览器，则拉取授权
                if (grantThree != null) {
                    try {
                        response.sendRedirect(grantThree);
                    } catch (IOException e) {
                        e.printStackTrace();
                        errorRedirect("REDIRECT_ERROR");
                        return;
                    }
                    return;
                } else {
                    Map result = adapterService.getGrandMsg(gsdm, orderNo, storeNo);
                    deal(result, gsdm);
                }
                break;
            case "4":
                if (!StringUtil.isNotBlankList(mi, ot)) {
                    errorRedirect("TYPE_FOUR_REQUIRED_PARAMETER_MISSING");
                    return;
                }
                String confirmMsg = adapterService.getConfirmMsg(gsdm, q);
                if ("error".equals(confirmMsg)) {
                    errorRedirect("GET_CONFIRM_MSG_ERROR");
                    return;
                } else if ("sn".equals(confirmMsg)) {
                    errorRedirect("GET_STORENO_ERROR");
                    return;
                } else if ("jyxxsq".equals(confirmMsg)) {
                    errorRedirect("GET_ORDER_ERROR");
                    return;
                } else if ("pp".equals(confirmMsg)) {
                    errorRedirect("GET_GRAND_ERROR");
                    return;
                } else {
                    JSONObject msgJson = JSON.parseObject(confirmMsg);
                    String ppdm = msgJson.getString("ppdm");
                    String ppurl = msgJson.getString("ppurl");
                    session.setAttribute("gsdm", gsdm);
                    session.setAttribute("q", q);
                    try {
                        response.sendRedirect(ppurl + "?t=" + System.currentTimeMillis() + "=" + ppdm);
                        return;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            default:
                errorRedirect("UNKNOWN_TYPE");
                return;
        }
    }
    @ApiIgnore
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
            errorRedirect("GET_WECHAT_AUTHORIZED_FAILED");
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
        boolean b = wechatFpxxService.InFapxx(null, gsdm, on, q, "1", openid, "", null, request, "1");
        if (!b) {
            errorRedirect("SAVE_WECAHT_ERROR");
            return;
        }
        commonController.isWeiXin(null, on, ot, pr, gsdm);
    }

    @ApiOperation(value = "type1抬头页面提交接口")
    @RequestMapping(value = "/input", method = RequestMethod.GET)
    public Result submitForOne(@RequestParam String gfmc, @RequestParam String gfsh, @RequestParam String email,
                               String gfdz, String gfdh, String gfyhzh, String gfyh) {
        String gsdm = (String) session.getAttribute("gsdm");
        String q = (String) session.getAttribute("q");
        Map map = RJCheckUtil.decodeForAll(q);
        String data = (String) map.get("A0");
        JSONObject jsonData = JSON.parseObject(data);
        String on = jsonData.getString("on");
        AdapterDataOrderBuyer buyer = new AdapterDataOrderBuyer();
        buyer.setName(gfmc);
        buyer.setIdentifier(gfsh);
        buyer.setEmail(email);
        buyer.setAddress(gfdz);
        buyer.setTelephoneNo(gfdh);
        buyer.setBankAcc(gfyhzh);
        buyer.setBank(gfyh);
        boolean b = adapterService.sendBuyer(gsdm, on, buyer);
        if (!b) {
            return ResultUtil.error("发送失败");
        }
        return ResultUtil.success();
    }

    @ApiIgnore
    @RequestMapping("/getOpenid")
    public void getOpenidForTwoAndThree(String state, String code) {
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
            return;
        }
        String gsdm = session.getAttribute("gsdm").toString();
        String q = session.getAttribute("q").toString();
        String on = (String) session.getAttribute("on");
        String sn = (String) session.getAttribute("sn");
        Map result;
        if (StringUtil.isNotBlankList(q) && StringUtil.isBlankList(on, sn)) {
            result = adapterService.getGrandMsg(gsdm, q);//type2
        } else {
            result = adapterService.getGrandMsg(gsdm, on, sn);//type3
        }
        deal(result, gsdm);
    }

    @ApiOperation(value = "type2和3获取确认页面参数接口")
    @RequestMapping(value = "/scanConfirm",method = RequestMethod.POST)
    public Result getConfirmMsgForTwoAndThree() {
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
        if (StringUtil.isNotBlankList(q) && StringUtil.isBlankList(on, sn)) {
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
            if ("1".equals(jsonData)) {
                return ResultUtil.error("开票数据未上传，请稍后再试");
            }
            JSONObject jsonObject;
            try {
                jsonObject = JSON.parseObject(jsonData);
            }catch (Exception e){
                e.printStackTrace();
                return ResultUtil.error(jsonData);
            }
            String orderTime = jsonObject.getString("orderTime");
            //开票限期判断
            Boolean isInvoiceDateRestriction = adapterService.isInvoiceDateRestriction(gsdm, null, null, orderTime);
            if (isInvoiceDateRestriction == null) {
                return ResultUtil.error("开票期限格式错误");
            } else {
                if (isInvoiceDateRestriction) {
                    logger.info("超过开票期限");
                    return ResultUtil.error("已超过开票截止日期，请联系商家");
                }
            }
            String tqm = jsonObject.getString("tqm");
            String orderNo = jsonObject.getString("orderNo");
            boolean b = wechateFpxxService.InFapxx(tqm, gsdm, orderNo, q, "1", openid,
                    (String) request.getSession().getAttribute(AlipayConstants.ALIPAY_USER_ID), "", request, apiType);
            if (!b) {
                return ResultUtil.error("保存发票信息失败，请重试！");
            }
            return ResultUtil.success(jsonData);//订单号,订单时间,门店号,金额,商品名,商品税率
        } else {
            return ResultUtil.error("二维码信息获取失败");
        }
    }

    @ApiOperation(value = "type2和3抬头页面提交接口")
    @RequestMapping(value = "/submit",method = RequestMethod.POST)
    public Result submitForTwoAndThree(@RequestParam String gfmc, @RequestParam String gfsh, @RequestParam String email,
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
        if (StringUtil.isNotBlankList(q) && StringUtil.isBlankList(on, sn)) {
            status = adapterService.makeInvoice(gsdm, q, gfmc, gfsh, email, gfyh, gfyhzh, gfdz, gfdh, tqm, userId, "5", "", "");
        } else {
            status = adapterService.makeInvoice(gsdm, on, sn, tq, gfmc, gfsh, email, gfyh, gfyhzh, gfdz, gfdh, tqm, userId, "5", "", "");
        }
        //开票
        if ("-1".equals(status)) {
            return ResultUtil.error("开具失败");
        } else if ("0".equals(status)) {
            return ResultUtil.error("所需信息为空");
        } else if ("-2".equals(status)) {
            return ResultUtil.error("开票数据未上传，请稍后再试");
        } else {
            JSONObject jsonObject;
            try {
                jsonObject = JSON.parseObject(status);
            }catch (Exception e){
                e.printStackTrace();
                return ResultUtil.error(status);
            }
            session.setAttribute("serialorder", jsonObject.getString("serialorder"));
            return ResultUtil.success(status);
        }
    }

    @ApiOperation(value = "type4获取确认页面信息接口")
    @RequestMapping(value = "/getConfirmMsg",method = RequestMethod.POST)
    public Result getConfirmMsgForFour() {
        String q = (String) session.getAttribute("q");
        String gsdm = (String) session.getAttribute("gsdm");
        if (!StringUtil.isNotBlankList(q, gsdm)) {
            return ResultUtil.error("会话超时，请重新扫码");
        }
        return ResultUtil.success(adapterService.getConfirmMsg(gsdm, q));
    }

    @ApiOperation(value = "type4获取订单列表接口")
    @RequestMapping(value = "/getInvoiceList",method = RequestMethod.POST)
    public Result getInvoiceList(String gsdm, String khh) {
        session.setAttribute("khh",khh);
        String invoiceList = adapterService.getInvoiceList(gsdm, khh);
        if (invoiceList == null) {
            return ResultUtil.error("开票数据未上传，请稍后再试");
        }
        return ResultUtil.success(invoiceList);
    }

    @ApiOperation(value = "已开票跳转查看发票接口")
    @RequestMapping(value = "/getPDFList",method = RequestMethod.POST)
    public Result getPDFList(String serialorder) {
        session.setAttribute("serialorder", serialorder);
        Map map = new HashMap();
        map.put("url", request.getContextPath() + "/CO/dzfpxq.html?_t=" + System.currentTimeMillis());
        return ResultUtil.success(JSON.toJSONString(map));
    }

    @ApiOperation(value = "type4抬头页面提交接口")
    @RequestMapping(value = "/submitForFour",method = RequestMethod.POST)
    public Result submitForFour(String gfmc, String gfsh, String gfdz,
                                String gfdh, String gfyhzh, String gfyh,
                                String gsdm, String email, String jylsh,
                                String ddh, String ddrq, String je, String kpddm) {
        String wechat = isWechat(TYPE_FOUR_CALLBACKURL);
        if (wechat != null) {
            session.setAttribute("ddh", ddh);
            session.setAttribute("ddrq", ddrq);
            session.setAttribute("je", je);
            session.setAttribute("kpddm", kpddm);
            session.setAttribute("jylsh", jylsh);//其实是sqlsh
            session.setAttribute("email", email);
            Map map = new HashMap();
            map.put("url", wechat);
            return ResultUtil.success(JSON.toJSONString(map));
        } else {
            String userId = (String) request.getSession().getAttribute(AlipayConstants.ALIPAY_USER_ID);
            String status = adapterService.makeInvoiceForFour(gsdm, jylsh, gfmc, gfsh, gfdz,
                    gfdh, gfyhzh, gfyh, email, userId, "5", "", "");
            //开票
            if ("-1".equals(status)) {
                return ResultUtil.error("开具失败");
            } else {
                JSONObject jsonObject = JSON.parseObject(status);
                session.setAttribute("serialorder", jsonObject.getString("serialorder"));
                return ResultUtil.success(status);
            }
        }
    }
    @ApiIgnore
    @RequestMapping("/getOpenidForFour")
    public void getOpenidForFour(String state, String code) {
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
            return;
        }
        if (session.getAttribute("gsdm") == null || session.getAttribute("jylsh") == null) {
            errorRedirect("SESSION_MISSING");
            return;
        }
        String gsdm = (String) session.getAttribute("gsdm");
        String ddh = (String) session.getAttribute("ddh");
        String ddrq = (String) session.getAttribute("ddrq");
        String je = (String) session.getAttribute("je");
        String kpddm = (String) session.getAttribute("kpddm");
        String jylsh = (String) session.getAttribute("jylsh");
        String email = (String) session.getAttribute("email");
        Boolean b = wechatFpxxService.InFapxx(email, gsdm, ddh, jylsh, "1", openid, "", "", request, "4");
        if (!b) {
            errorRedirect("SAVE_WECAHT_ERROR");
            return;
        }
        Cszb cszb = cszbService.getSpbmbbh(gsdm, null, null, "sfsycdtt");//是否使用C端抬头
        String isC = cszb.getCsz();
        String wechatAuthType;
        if("是".equals(isC)){
            wechatAuthType = "1";
        }else if("否".equals(isC)){
            wechatAuthType = "0";
        }else{
            errorRedirect("UNKNOW_2C_TYPE");
            return;
        }
        commonController.isWeiXin(kpddm, ddh, ddrq, je, gsdm, wechatAuthType);
    }

    @ApiOperation(value = "所有页面根据ppdm获取定制化信息接口（headcolor、bodycolor、buttoncolor等）")
    @RequestMapping(value = "/getShowMsg",method = RequestMethod.POST)
    public Result getShowMsg(@RequestParam String ppdm){
        String showMsg = adapterService.getShowMsg(ppdm);
        if(showMsg!=null){
            return ResultUtil.success(showMsg);
        }else{
            return ResultUtil.error("获取品牌信息失败");
        }
    }

    private void deal(Map result, String gsdm) {
        try {
            if (result != null) {
                String ppdm = result.get("ppdm").toString();
                String ppurl = result.get("ppurl").toString();
//                String headcolor = (String)result.get("headcolor");
//                String bodycolor = (String)result.get("bodycolor");
//                String buttoncolor = (String)result.get("buttoncolor");
                String orderNo = result.get("orderNo").toString();
                String tqm = ppdm + orderNo;
                session.setAttribute("tqm", tqm);
                session.setAttribute("orderNo", orderNo);
                List<String> status = adapterService.checkStatus(tqm, gsdm);
                if (!status.isEmpty()) {
                    if (status.contains("可开具")) {
                        if (StringUtils.isNotBlank(ppdm)) {
                            //有品牌代码对应的url
                            response.sendRedirect(request.getContextPath() + ppurl + "?t=" + System.currentTimeMillis() + "=" + ppdm);
                            return;
                        } else {
                            //无品牌
                            errorRedirect("GET_GRAND_ERROR");
                            return;
                        }
                    } else if (status.contains("开具中")) {
                        //开具中对应的url
                        response.sendRedirect(request.getContextPath() + "/QR/zzkj.html?t=" + System.currentTimeMillis());
                        return;
                    } else if (status.contains("红冲")) {
                        errorRedirect("ORDER_CANCELLED");
                        return;
                    }else if (status.contains("纸票")) {
                        errorRedirect("该订单已开具纸质发票，不能重复开具");
                        return;
                    } else {
                        String serialOrder = status.get(0).split("[+]")[4];
                        session.setAttribute("serialorder", serialOrder);
                        response.sendRedirect(request.getContextPath() + "/CO/dzfpxq.html?_t=" + System.currentTimeMillis());
                        return;
                    }
                } else {
                    //获取pdf状态码失败的url
                    errorRedirect("GET_PDF_STATE_ERROR");
                    return;
                }
            } else {
                errorRedirect("STORE_BRAND_MISSING");
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            errorRedirect("REDIRECT_ERROR");
            return;
        }
    }

    private String isWechat(String callbackurl) {
        String ua = request.getHeader("user-agent").toLowerCase();
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

    public void errorRedirect(String errorName) {
        try {
            response.sendRedirect(request.getContextPath() + "/QR/error.html?t=" + System.currentTimeMillis() + "=" + errorName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}