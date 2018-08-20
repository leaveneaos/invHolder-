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
import com.rjxx.taxeasy.utils.alipay.AlipayUtils;
import com.rjxx.taxeasy.wechat.dto.Result;
import com.rjxx.taxeasy.wechat.util.ResultUtil;
import com.rjxx.utils.*;
import com.rjxx.utils.weixin.HttpClientUtil;
import com.rjxx.utils.weixin.WeiXinConstants;
import com.rjxx.utils.weixin.wechatFpxxServiceImpl;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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

    @Value("${web.url.error}")
    private String errorUrl;

    @Value("${web.url.success}")
    private String succesUrl;

    @Value("${web.url.ticketing}")
    private String ticketingUrl;

    @Value("${web.url.luru}")
    private String luruUrl;

    @Value("${web.url.maked}")
    private String makedUrl;

    private static final String TYPE_ONE_CALLBACKURL = "kptService/getOpenidForOne";
    private static final String TYPE_TWO_CALLBACKURL = "kptService/getOpenid";
    private static final String TYPE_THREE_CALLBACKURL = "kptService/getOpenid";
    private static final String TYPE_FOUR_CALLBACKURL = "kptService/getOpenidForFour";

    /**
     * 该品牌代码必须全平台唯一
     *
     * @param ppdm
     */
    @ApiOperation(value = "手输提取码入口")
    @RequestMapping(value = "/{ppdm}", method = RequestMethod.GET)
    public void extract(@PathVariable String ppdm) {
        Pp pp;
        try {
            pp = ppJpaDao.findOneByPpdm(ppdm);
            if (pp == null) {
                errorRedirect("未找到该品牌[TQ]");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorRedirect("该品牌不是唯一[TQ]");
            return;
        }

        String gsdm = pp.getGsdm();
        session.setAttribute("gsdm", gsdm);
        try {
            String url;
            if (pp.getPpurl().contains("http")) {
                url = luruUrl + "/?t=" + System.currentTimeMillis() + "=" + ppdm;
            } else {
                url = request.getContextPath() + "/qrcode/luru.html?t=" + System.currentTimeMillis() + "=" + ppdm;
            }
            response.sendRedirect(url);
            return;
        } catch (IOException e) {
            e.printStackTrace();
            errorRedirect("重定向到开票发生错误[TQ]");
            return;
        }
    }

    @ApiOperation(value = "手输提取码界面提交接口")
    @RequestMapping(value = "/lrqr", method = RequestMethod.POST)
    public Result lrqr(@RequestParam String tq, @RequestParam String code) {
        String sessionCode = "";
        if (session.getAttribute("rand") != null) {
            sessionCode = (String) session.getAttribute("rand");
        } else {
            sessionCode = (String) session.getAttribute(RandomValidateCodeUtil.RANDOMCODEKEY);
        }
        if (org.apache.commons.lang3.StringUtils.isBlank(sessionCode)) {
            return ResultUtil.error("会话过期，请重试[TQ]");
        }
        if (code.equals(sessionCode)) {
            if (session.getAttribute("gsdm") == null) {
                return ResultUtil.error("会话超时[TQ]");
            }
            String gsdm = (String) session.getAttribute("gsdm");
            Map apiMsg = adapterService.getApiMsg(gsdm, tq);
            if (apiMsg == null) {
                return ResultUtil.error("开票数据未上传，请稍后再试[TQ]");
            }
            String msg = (String) apiMsg.get("msg");
            if (msg != null) {
                if (!msg.contains("已处理")) {
                    return ResultUtil.error(msg);
                }
            }
            AdapterGet adapterGet = new AdapterGet();
            adapterGet.setType("3");
            if (apiMsg.get("jyxxsq") != null) {
                Jyxxsq jyxxsq = (Jyxxsq) apiMsg.get("jyxxsq");
                adapterGet.setOn(jyxxsq.getTqm());
                adapterGet.setSn(jyxxsq.getKpddm());
            } else {
                AdapterPost post = (AdapterPost) apiMsg.get("post");
                adapterGet.setOn(post.getData().getOrder().getExtractedCode());
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
        } else {
            return ResultUtil.error("验证码输入错误[TQ]");
        }
    }

    @ApiOperation(value = "通用提取接口总入口")
    @RequestMapping(value = "/{gsdm}/{q}", method = RequestMethod.GET)
    public void get(@PathVariable String gsdm, @PathVariable String q) {
        Gsxx gsxx = gsxxJpaDao.findOneByGsdm(gsdm);
        if (gsxx == null) {
            errorRedirect("未知的公司");
            return;
        }
        Boolean checkResult = RJCheckUtil.checkMD5ForAll(gsxx.getSecretKey(), q);
        if (!checkResult) {
            errorRedirect("验签失败");
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
            errorRedirect("获取开票类型失败");
            return;
        }
        switch (type) {
            case "1":
                if (!StringUtil.isNotBlankList(on, ot, pr)) {
                    errorRedirect("必须的参数为空TP[1]");
                    return;
                }
                session.setAttribute("gsdm", gsdm);
                session.setAttribute("q", q);
                Map confirmParamOne = new HashMap();
                confirmParamOne.put("orderNo", on);
                confirmParamOne.put("orderTime", ot);
                confirmParamOne.put("price", pr);
                confirmParamOne.put("isTitle", "1");
                deal(confirmParamOne, gsdm);
                break;
            case "2":
                if (!StringUtil.isNotBlankList(on, ot, pr)) {
                    errorRedirect("必须的参数为空TP[2]");
                    return;
                }
                session.setAttribute("q", q);
                session.setAttribute("gsdm", gsdm);
                logger.info("type2存入session的q--"+session.getAttribute("q"));
                String grant = isWechat(TYPE_TWO_CALLBACKURL);
                //如果是微信浏览器，则拉取授权
                if (grant != null) {
                    try {
                        response.sendRedirect(grant);
                    } catch (IOException e) {
                        e.printStackTrace();
                        errorRedirect("重定向开票页面发生错误TP[2]");
                    }
                    return;
                } else {
                    Map result = adapterService.getGrandMsg(gsdm, q);
                    deal(result, gsdm);
                }
                break;
            case "3":
                if (StringUtil.isBlankList(on, tq)) {
                    errorRedirect("必须的参数为空TP[3]");
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
                        errorRedirect("缺少开票点代码TP[3]");
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
                        errorRedirect("重定向开票页面发生错误TP[3]");
                        return;
                    }
                    return;
                } else {
                    Map result = adapterService.getGrandMsg(gsdm, orderNo,extractCode, storeNo);
                    deal(result, gsdm);
                }
                break;
            case "4":
                if (!StringUtil.isNotBlankList(mi, ot)) {
                    errorRedirect("必须的参数为空TP[4]");
                    return;
                }
                String confirmMsg = adapterService.getConfirmMsg(gsdm, q);
                if ("error".equals(confirmMsg)) {
                    errorRedirect("获取确认信息失败TP[4]");
                    return;
                } else if ("sn".equals(confirmMsg)) {
                    errorRedirect("获取开票点失败TP[4]");
                    return;
                } else if ("jyxxsq".equals(confirmMsg)) {
                    errorRedirect("获取订单失败TP[4]");
                    return;
                } else if ("pp".equals(confirmMsg)) {
                    errorRedirect("获取品牌失败TP[4]");
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
                errorRedirect("未知的开票类型");
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
            errorRedirect("会话已过期TP[1]");
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
            errorRedirect("保存发票信息失败TP[1]");
            return;
        }
        commonController.isWeiXin(null, on, ot, pr, gsdm);
    }

    @ApiOperation(value = "type1抬头页面提交接口")
    @RequestMapping(value = "/input", method = RequestMethod.POST)
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
            return ResultUtil.error("发送失败TP[1]");
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
            errorRedirect("会话已过期TP[2][3]");
            return;
        }
        String gsdm = session.getAttribute("gsdm").toString();
        String q = session.getAttribute("q").toString();
        String on = (String) session.getAttribute("on");
        String sn = (String) session.getAttribute("sn");
        String tq = (String) session.getAttribute("tq");
        Map result;
        if (StringUtil.isNotBlankList(q) && StringUtil.isBlankList(on, sn)) {
            result = adapterService.getGrandMsg(gsdm, q);//type2
        } else {
            result = adapterService.getGrandMsg(gsdm, on,tq, sn);//type3
        }
        deal(result, gsdm);
    }

    @ApiOperation(value = "type2和3获取确认页面参数接口")
    @RequestMapping(value = "/scanConfirm", method = RequestMethod.POST)
    public Result getConfirmMsgForTwoAndThree() {
        String q = (String) session.getAttribute("q");
        String gsdm = (String) session.getAttribute("gsdm");
        String openid = (String) session.getAttribute("openid");
        String on = (String) session.getAttribute("on");
        String sn = (String) session.getAttribute("sn");
        String tq = (String) session.getAttribute("tq");
        if (!StringUtil.isNotBlankList(gsdm)) {
            return ResultUtil.error("session过期,请重新扫码TP[2][3]");
        }
        String jsonData;
        String apiType;
        if (StringUtil.isNotBlankList(q) && StringUtil.isBlankList(on, sn)) {
            jsonData = adapterService.getSpxx(gsdm, q);
            apiType = "2";
        } else {
            if (StringUtil.isBlankList(on, sn, tq)) {
                return ResultUtil.error("交易信息获取失败TP[3]");
            }
            jsonData = adapterService.getSpxx(gsdm, on, sn, tq);
            apiType = "3";
        }
        if (jsonData != null) {
            if ("1".equals(jsonData)) {
                return ResultUtil.error("开票数据未上传，请稍后再试TP[3]");
            } else if ("2".equals(jsonData)) {
                return ResultUtil.error("金额传入有误，金额必须大于零TP[2]");
            } else if ("3".equals(jsonData)) {
                return ResultUtil.error("未找到默认开票点代码TP[2]");
            } else if ("4".equals(jsonData)) {
                return ResultUtil.error("金额与价格数量不一致TP[2]");
            } else if ("5".equals(jsonData)) {
                return ResultUtil.error("未找到默认商品代码TP[2]");
            } else if ("6".equals(jsonData)) {
                return ResultUtil.error("所需参数为空TP[2]");
            }
            JSONObject jsonObject;
            try {
                jsonObject = JSON.parseObject(jsonData);
            } catch (Exception e) {
                e.printStackTrace();
                return ResultUtil.error(jsonData);
            }
            String orderTime = jsonObject.getString("orderTime");
            //开票限期判断
            Boolean isInvoiceDateRestriction = adapterService.isInvoiceDateRestriction(gsdm, null, null, orderTime);
            if (isInvoiceDateRestriction == null) {
                return ResultUtil.error("开票期限格式错误TP[2][3]");
            } else {
                if (isInvoiceDateRestriction) {
                    logger.info("超过开票期限");
                    return ResultUtil.error("已超过开票截止日期，请联系商家TP[2][3]");
                }
            }
            String tqm = jsonObject.getString("tqm");
            String orderNo = jsonObject.getString("orderNo");
            boolean b = wechateFpxxService.InFapxx(tqm, gsdm, orderNo, q, "1", openid,
                    (String) request.getSession().getAttribute(AlipayConstants.ALIPAY_USER_ID), "", request, apiType);
            if (!b) {
                return ResultUtil.error("保存发票信息失败，请重试！TP[2][3]");
            }
            return ResultUtil.success(jsonData);//订单号,订单时间,门店号,金额,商品名,商品税率
        } else {
            return ResultUtil.error("二维码信息获取失败TP[2][3]");
        }
    }

    @ApiOperation(value = "type2和3抬头页面提交接口")
    @RequestMapping(value = "/submit", method = RequestMethod.POST)
    public Result submitForTwoAndThree(@RequestParam String gfmc, @RequestParam(required = false) String gfsh, @RequestParam String email,
                                       String gfdz, String gfdh, String gfyhzh, String gfyh, String tqm) {
        String gsdm = (String) session.getAttribute("gsdm");
        String q = (String) session.getAttribute("q");
        String on = (String) session.getAttribute("on");
        String sn = (String) session.getAttribute("sn");
        String tq = (String) session.getAttribute("tq");
        String userId = (String) request.getSession().getAttribute(AlipayConstants.ALIPAY_USER_ID);
        if (gsdm == null) {
            return ResultUtil.error("session过期,请重新扫码TP[2][3]");
        }
        String status;
        String sjly;
        if (AlipayUtils.isAlipayBrowser(request)) {
            sjly = "5";//支付宝
        } else {
            sjly = "6";//其他浏览器
        }
        if (StringUtil.isNotBlankList(q) && StringUtil.isBlankList(on, sn)) {
            status = adapterService.makeInvoice(gsdm, q, gfmc, gfsh, email, gfyh, gfyhzh, gfdz, gfdh, tqm, userId, sjly, "", "");
        } else {
            status = adapterService.makeInvoice(gsdm, on, sn, tq, gfmc, gfsh, email, gfyh, gfyhzh, gfdz, gfdh, tqm, userId, sjly, "", "");
        }
        //开票
        if ("-1".equals(status)) {
            return ResultUtil.error("开具失败TP[2][3]");
        } else if ("0".equals(status)) {
            return ResultUtil.error("所需信息为空TP[2][3]");
        } else if ("-2".equals(status)) {
            return ResultUtil.error("开票数据未上传，请稍后再试TP[3]");
        } else {
            JSONObject jsonObject;
            try {
                jsonObject = JSON.parseObject(status);
            } catch (Exception e) {
                e.printStackTrace();
                return ResultUtil.error(status);
            }
            session.setAttribute("serialorder", jsonObject.getString("serialorder"));
            return ResultUtil.success(status);
        }
    }

    @ApiOperation(value = "type4获取确认页面信息接口")
    @RequestMapping(value = "/getConfirmMsg", method = RequestMethod.POST)
    public Result getConfirmMsgForFour() {
        String q = (String) session.getAttribute("q");
        String gsdm = (String) session.getAttribute("gsdm");
        if (!StringUtil.isNotBlankList(q, gsdm)) {
            return ResultUtil.error("会话超时，请重新扫码TP[4]");
        }
        return ResultUtil.success(adapterService.getConfirmMsg(gsdm, q));
    }

    @ApiOperation(value = "type4获取订单列表接口")
    @RequestMapping(value = "/getInvoiceList", method = RequestMethod.POST)
    public Result getInvoiceList(String gsdm, String khh) {
        session.setAttribute("khh", khh);
        String invoiceList = adapterService.getInvoiceList(gsdm, khh);
        if (invoiceList == null) {
            return ResultUtil.error("开票数据未上传，请稍后再试TP[4]");
        }
        return ResultUtil.success(invoiceList);
    }

    @ApiOperation(value = "已开票跳转查看发票接口")
    @RequestMapping(value = "/getPDFList", method = RequestMethod.POST)
    public Result getPDFList(String serialorder) {
        session.setAttribute("serialorder", serialorder);
        Map map = new HashMap();
        map.put("url", makedUrl + "?serialorder=" + serialorder + "&t=" + System.currentTimeMillis());

        return ResultUtil.success(JSON.toJSONString(map));
    }

    @ApiOperation(value = "type4抬头页面提交接口（含微信授权页）")
    @RequestMapping(value = "/submitForFour", method = RequestMethod.POST)
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
            String sjly;
            if (AlipayUtils.isAlipayBrowser(request)) {
                sjly = "5";//支付宝
            } else {
                sjly = "6";//其他浏览器
            }
            String userId = (String) request.getSession().getAttribute(AlipayConstants.ALIPAY_USER_ID);
            String status = adapterService.makeInvoiceForFour(gsdm, jylsh, gfmc, gfsh, gfdz,
                    gfdh, gfyhzh, gfyh, email, userId, sjly, "", "");
            //开票
            if ("-1".equals(status)) {
                return ResultUtil.error("开具失败TP[4.1]");
            } else {
                JSONObject jsonObject = JSON.parseObject(status);
                session.setAttribute("serialorder", jsonObject.getString("serialorder"));
                return ResultUtil.success(status);
            }
        }
    }

    @ApiOperation(value = "type4抬头页面直接提交接口)")
    @RequestMapping(value = "/submitFour", method = RequestMethod.POST)
    public Result submitFour(String gfmc, String gfsh, String gfdz,
                             String gfdh, String gfyhzh, String gfyh,
                             String gsdm, String email, String jylsh,
                             String ddh, String ddrq, String je, String kpddm) {
        String sjly;
        if (AlipayUtils.isAlipayBrowser(request)) {
            sjly = "5";//支付宝
        } else {
            sjly = "6";//接口录入
        }
        String userId = (String) request.getSession().getAttribute(AlipayConstants.ALIPAY_USER_ID);
        String status = adapterService.makeInvoiceForFour(gsdm, jylsh, gfmc, gfsh, gfdz,
                gfdh, gfyhzh, gfyh, email, userId, sjly, "", "");
        //开票
        if ("-1".equals(status)) {
            return ResultUtil.error("开具失败TP[4.0]");
        } else {
            JSONObject jsonObject = JSON.parseObject(status);
            session.setAttribute("serialorder", jsonObject.getString("serialorder"));
            return ResultUtil.success(status);
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
        if (session.getAttribute("gsdm") == null || session.getAttribute("jylsh") == null) {
//            errorRedirect("SESSION_MISSING");
            errorRedirect("会话已过期TP[4]");
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
            errorRedirect("保存发票信息失败TP[4]");
            return;
        }
        Cszb cszb = cszbService.getSpbmbbh(gsdm, null, null, "sfsycdtt");//是否使用C端抬头
        String isC = cszb.getCsz();
        String wechatAuthType;
        if ("是".equals(isC)) {
            wechatAuthType = "1";
        } else if ("否".equals(isC)) {
            wechatAuthType = "0";
        } else {
            errorRedirect("未知的参数值TP[4]");
            return;
        }
        commonController.isWeiXin(kpddm, ddh, ddrq, je, gsdm, wechatAuthType);
    }

    @ApiOperation(value = "所有页面根据ppdm获取定制化信息接口（headcolor、bodycolor、buttoncolor等）")
    @RequestMapping(value = "/getShowMsg", method = RequestMethod.POST)
    public Result getShowMsg(@RequestParam String ppdm) {
        String showMsg = adapterService.getShowMsg(ppdm);
        if (showMsg != null) {
            return ResultUtil.success(showMsg);
        } else {
            return ResultUtil.error("获取品牌信息失败");
        }
    }

    private void deal(Map result, String gsdm) {
        try {
            if (result != null) {
                logger.info("---校验数据"+JSON.toJSONString(result));
                String ppdm = (String) result.get("ppdm");
                String ppurl = (String) result.get("ppurl");
                String orderNo = (String) result.get("orderNo");
                String extractCode = (String) result.get("extractCode");
                //isTitle==1是type1
                String isTitle = (String) result.get("isTitle");
                String orderTime = (String) result.get("orderTime");
                String price = (String) result.get("price");
                session.setAttribute("orderNo", orderNo);
                List<String> status = null;
                if(!"1".equals(isTitle)){
                    String tqm="";
                    if(org.apache.commons.lang3.StringUtils.isNotBlank(extractCode)){
                        tqm = extractCode;
                    }else{
                        tqm = ppdm + orderNo;
                    }
                    session.setAttribute("tqm", tqm);
                    status = adapterService.checkStatus(tqm, gsdm,null);
                }else{
                    status = adapterService.checkStatus(null, gsdm,orderNo);
                }
                if (!status.isEmpty()) {
                    if (status.contains("可开具")) {
                        if (StringUtils.isNotBlank(ppdm)) {
                            //有品牌代码对应的url
                            String sendUrl;
                            if (ppurl.contains("http")) {
                                sendUrl = ppurl + "?t=" + System.currentTimeMillis() + "=" + ppdm;
                            } else {
                                sendUrl = request.getContextPath() + ppurl + "?t=" + System.currentTimeMillis() + "=" + ppdm;
                            }
                            response.sendRedirect(sendUrl);
                            return;
                        } else {
                            if("1".equals(isTitle)) {
                                String grantOne = isWechat(TYPE_ONE_CALLBACKURL);
                                try {
                                    if (grantOne != null) {
                                        response.sendRedirect(grantOne);
                                        return;
                                    } else {
                                        response.sendRedirect(
                                                request.getContextPath() + "/qrcode/input.html?" +
                                                        "t=" + System.currentTimeMillis() +
                                                        "=" + orderNo +
                                                        "=" + orderTime +
                                                        "=" + price);
                                        return;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    errorRedirect("重定向开票页面发生错误TP[1]");
                                    return;
                                }
                            }else{
                                //无品牌
                                errorRedirect("未找到品牌");
                                return;
                            }
                        }
                    } else if (status.contains("开具中")) {
                        //开具中对应的url
                        response.sendRedirect(ticketingUrl);
                        return;
                    } else if (status.contains("红冲")) {
                        errorRedirect("该订单已红冲");
                        return;
                    } else if (status.contains("纸票")) {
                        errorRedirect("该订单已开具纸质发票，不能重复开具");
                        return;
                    } else {
                        String serialOrder = status.get(0).split("[+]")[4];
                        session.setAttribute("serialorder", serialOrder);
                        response.sendRedirect(makedUrl + "?serialorder=" + serialOrder + "&t=" + System.currentTimeMillis());
                        return;
                    }
                } else {
                    //获取pdf状态码失败的url
                    errorRedirect("获取开票状态失败");
                    return;
                }
            } else {
                errorRedirect("获取开票点或品牌失败");
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            errorRedirect("页面跳转错误");
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
            response.sendRedirect(errorUrl + "/" + URLEncoder.encode(errorName) + "?t=" + System.currentTimeMillis());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
