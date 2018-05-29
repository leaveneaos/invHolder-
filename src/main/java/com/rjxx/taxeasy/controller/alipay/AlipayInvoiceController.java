package com.rjxx.taxeasy.controller.alipay;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayEbppInvoiceApplyResultSyncRequest;
import com.alipay.api.response.AlipayEbppInvoiceApplyResultSyncResponse;
import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.dao.PpJpaDao;
import com.rjxx.taxeasy.dao.SkpJpaDao;
import com.rjxx.taxeasy.dao.WxfpxxJpaDao;
import com.rjxx.taxeasy.dao.XfJpaDao;
import com.rjxx.taxeasy.domains.Pp;
import com.rjxx.taxeasy.domains.Skp;
import com.rjxx.taxeasy.domains.WxFpxx;
import com.rjxx.taxeasy.dto.alipay.AlipayReceiveApplyDto;
import com.rjxx.taxeasy.dto.alipay.AlipayResult;
import com.rjxx.taxeasy.service.SkpService;
import com.rjxx.taxeasy.service.adapter.AdapterService;
import com.rjxx.taxeasy.task.AlipayTask;
import com.rjxx.utils.HtmlUtils;
import com.rjxx.utils.alipay.AlipayConstant;
import com.rjxx.utils.alipay.AlipayResultUtil;
import com.rjxx.utils.alipay.AlipaySignUtil;
import com.rjxx.utils.alipay.AlipayUtils;
import com.rjxx.utils.weixin.wechatFpxxServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wangyahui
 * @email wangyahui@datarj.com
 * @company 上海容津信息技术有限公司
 * @date 2018/5/15
 */
@CrossOrigin
@RestController
@RequestMapping("/alipayInvoice")
public class AlipayInvoiceController extends BaseController {
    @Autowired
    private AdapterService adapterService;
    @Autowired
    private WxfpxxJpaDao wxfpxxJpaDao;
    @Autowired
    private SkpJpaDao skpJpaDao;
    @Autowired
    private XfJpaDao xfJpaDao;
    @Autowired
    private wechatFpxxServiceImpl wechatFpxxService;
    @Autowired
    private SkpService skpService;
    @Autowired
    private PpJpaDao ppJpaDao;

    @Value("${web.url.error}")
    private String errorUrl;

    public static final String APPLY_SUCCESS = "APPLY_SUCCESS";
    public static final String INVOICE_IS_APPLIED = "INVOICE_IS_APPLIED";
    public static final String INVOICE_PARAM_ILLEGAL = "INVOICE_PARAM_ILLEGAL";
    public static final String UNKNOW_ORDER = "UNKNOW_ORDER";

    private static final int MESSAGE_CACHE_SIZE = 1000;
    private static List<String> cacheList = new ArrayList<>(MESSAGE_CACHE_SIZE);


    /**
     * 此处使用支付宝提供的公钥私钥
     *
     * @param data
     * @return
     */
    @RequestMapping(value = "/receiveApply", method = RequestMethod.POST)
    public AlipayResult receiveApply(@RequestBody AlipayReceiveApplyDto data) {
        String applyId = data.getApplyId();
//        String userId = data.getUserId();
        String subShortName = data.getSubShortName();
        String mShortName = data.getmShortName();
        String orderNo = data.getOrderNo();
        String payerName = data.getPayerName();
        String payerRegisterNo = data.getPayerRegisterNo();
        String invoiceAmount = data.getInvoiceAmount();
        String payerAddress = data.getPayerAddress();
        String payerTelPhone = data.getPayerTelPhone();
        String payerBankName = data.getPayerBankName();
        String payerBankAccount = data.getPayerBankAccount();
        String sign = data.getSign();
        boolean distinct = distinct(applyId, orderNo);
        if (!distinct) {
            return AlipayResultUtil.result(APPLY_SUCCESS, "开票申请的信息校验无误，已提交开票");
        }
        Map<String, String> alipayResultMap = new HashMap<>();
        alipayResultMap.put("applyId", applyId);
//        alipayResultMap.put("userId", userId);
        alipayResultMap.put("subShortName", subShortName);
        alipayResultMap.put("mShortName", mShortName);
        alipayResultMap.put("orderNo", orderNo);
        alipayResultMap.put("payerName", payerName);
        alipayResultMap.put("payerRegisterNo", payerRegisterNo);
        alipayResultMap.put("invoiceAmount", invoiceAmount);
        alipayResultMap.put("payerAddress", payerAddress);
        alipayResultMap.put("payerTelPhone", payerTelPhone);
        alipayResultMap.put("payerBankName", payerBankName);
        alipayResultMap.put("payerBankAccount", payerBankAccount);
        alipayResultMap.put("sign", sign);

        try {
            boolean verify = AlipaySignUtil.getVerify(alipayResultMap, AlipaySignUtil.getPublickey(AlipaySignUtil.PUBKEY));
            if (!verify) {
                return AlipayResultUtil.result(INVOICE_PARAM_ILLEGAL, "开票参数非法");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("发生未知错误");
            return AlipayResultUtil.result(INVOICE_PARAM_ILLEGAL, "发生未知错误,验签不通过");
        }

        logger.info("拿到支付宝回传的订单编号为" + orderNo);
        WxFpxx wxFpxx = wxfpxxJpaDao.selectByWeiXinOrderNo(orderNo);
        if (null == wxFpxx) {
            logger.info("根据支付宝回传订单号未找到wxfpxx");
            return AlipayResultUtil.result(UNKNOW_ORDER, "未知的订单");
        }
        AlipayTask alipayTask = new AlipayTask();
        alipayTask.setAdapterService(adapterService);
        alipayTask.setAlipayResultMap(alipayResultMap);
        alipayTask.setWxFpxx(wxFpxx);
        alipayTask.setSkpJpaDao(skpJpaDao);
        alipayTask.setXfJpaDao(xfJpaDao);
        Thread t = new Thread(alipayTask);
        t.start();
        return AlipayResultUtil.result(APPLY_SUCCESS, "开票申请的信息校验无误，已提交开票");
    }

    /**
     * 排重
     *
     * @param applyId
     * @param orderNo
     * @return
     */
    private synchronized boolean distinct(String applyId, String orderNo) {
        String flag = applyId + orderNo;
        if (cacheList.contains(flag)) {
            logger.info("cacheList里已存在" + flag);
            return false;
        }
        if (cacheList.size() >= MESSAGE_CACHE_SIZE) {
            cacheList.remove(0);
        }
        cacheList.add(flag);
        return true;
    }


    /**
     * 重定向到支付宝授权页,此处使用我们公司自己的公钥私钥
     *
     * @param gsdm
     * @param orderNo
     * @param price
     * @param storeNo
     * @param type
     * @return
     */
    public String redirectAlipay(String gsdm, String orderNo, String price, String storeNo, String type) {
        logger.info("拉取授权订单编号" + orderNo + "金额" + price + "门店号" + storeNo + "--------------" + gsdm);
        if (null == orderNo || null == price || storeNo == null) {
            return null;
        }
        BigDecimal big = new BigDecimal(price);
        BigDecimal newbig = big.multiply(new BigDecimal(100));
        Double doumoney = new Double(newbig.toString());

        String redirect_url = "";
        String mShortName = "";
        String subShortName = "";
        Map skpParam = new HashMap();
        skpParam.put("kpddm", storeNo);
        skpParam.put("gsdm", gsdm);
        try {
            Skp skp = skpService.findOneByParams(skpParam);
            if (skp == null) {
                logger.info("根据门店号，获取门店失败!");
                return null;
            } else {
                Pp pp = ppJpaDao.findOneById(skp.getPid());
                if (pp == null) {
                    logger.info("根据开票点，获取品牌失败!");
                    return null;
                } else {
                    redirect_url = HtmlUtils.getBasePath(request) + "qrcode/witting.html"
                            + "?t=" + System.currentTimeMillis() + "&ppdm=" + pp.getPpdm();
                    mShortName = pp.getAliMShortName();
                    subShortName = pp.getAliSubMShortName();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("发生未知错误，跳转授权页失败!");
            return null;
        }
        String url = "";
        try {
            Map sendParam = new HashMap();
            sendParam.put("invoiceAmount", doumoney);
            sendParam.put("orderNo", orderNo);
            sendParam.put("mShortName", mShortName);
            sendParam.put("subShortName", subShortName);
            sendParam.put("resultUrl", URLEncoder.encode(redirect_url, "utf-8"));
            String params = AlipaySignUtil.getSignContent(sendParam, AlipaySignUtil.getPrivateKey(AlipaySignUtil.PRIKEY));
            url = URLEncoder.encode("/www/route.htm?scene=STANDARD_INVOICE&invoiceParams=" + URLEncoder.encode(params,"utf-8"),"utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        String redirectUrl = "alipays://platformapi/startapp?" +
                "appId=20000920&startMultApp=YES&appClearTop=false&url=" + url;
        System.out.println(redirectUrl);
        return url;
    }

    /**
     * 拒绝开票
     *
     * @param applyId
     * @param reason
     */
    public void refuse(String orderNo, String applyId, String reason) {
        String serverUrl = AlipayConstant.GATEWAY_URL;
        String appId = AlipayConstant.APP_ID;
        String privateKey = AlipayConstant.PRIVATE_KEY;
        String format = AlipayConstant.FORMAT;
        String charset = AlipayConstant.CHARSET;
        String alipayPulicKey = AlipayConstant.ALIPAY_PUBLIC_KEY;
        String signType = AlipayConstant.SIGN_TYPE;

        AlipayClient alipayClient = new DefaultAlipayClient(serverUrl, appId, privateKey,
                format, charset, alipayPulicKey, signType);
        AlipayEbppInvoiceApplyResultSyncRequest request = new AlipayEbppInvoiceApplyResultSyncRequest();

        Map param = new HashMap();
        param.put("apply_id", applyId);
        param.put("result", "失败");
        param.put("result_code", "fail");
        param.put("result_msg", reason);

        String bizContent = JSON.toJSONString(param);
        request.setBizContent(bizContent);
        AlipayEbppInvoiceApplyResultSyncResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()) {
            logger.info("------支付宝拒绝开票成功------");
            WxFpxx wxFpxx = wxfpxxJpaDao.selectByWeiXinOrderNo(orderNo);
            int coun = wxFpxx.getCount() + 1;
            wxFpxx.setCount(coun);
            wxfpxxJpaDao.save(wxFpxx);
            logger.info("拒绝开票----更新计数" + coun);
        } else {
            logger.info("------支付宝拒绝开票失败-------");
            logger.info(response.getCode() + "--------" + response.getMsg());
            logger.info(response.getSubCode() + "--------" + response.getSubMsg());
        }
    }

    /**
     * 开始拉取支付宝授权
     *
     * @param storeNo
     * @param orderNo
     * @param price
     * @param gsdm
     * @param type
     */
    @RequestMapping(value = "/isAlipay", method = RequestMethod.POST)
    public void isAlipay(String storeNo, String orderNo, String price, String gsdm, String type) {
        String redirectUrl = "";
        if (AlipayUtils.isAlipayBrowser(request)) {
            try {
                logger.info("------orderNo---------" + orderNo);
                if (null == orderNo || "".equals(orderNo)) {
                    errorRedirect("订单号为空,拉取支付宝授权页失败!请重试!");
                    return;
                }
                if (null == price || "".equals(price)) {
                    errorRedirect("金额为空,拉取支付宝授权页失败!请重试!");
                    return;
                }
                if ("0.0".equals(price)) {
                    errorRedirect("该订单可开票金额为0");
                    return;
                }
                WxFpxx wxFpxx = wxfpxxJpaDao.selsetByOrderNo(orderNo, gsdm);
                if (null == wxFpxx) {
                    errorRedirect("根据该订单号未找到该订单!请重试!");
                    return;
                }
                List<String> status = adapterService.checkStatus(wxFpxx.getTqm(), wxFpxx.getGsdm());
                if (status != null) {
                    if (status.contains("开具中")) {
                        //开具中对应的url
                        response.sendRedirect(request.getContextPath() + "/QR/zzkj.html?t=" + System.currentTimeMillis());
                        return;
                    } else if (status.contains("可开具")) {
                        String weixinOrderNo = wechatFpxxService.getweixinOrderNo(orderNo, gsdm);
                        logger.info("orderNo---" + orderNo + "传给支付宝的OrderNo" + weixinOrderNo);
                        //可开具 跳转微信授权链接
                        redirectUrl = redirectAlipay(gsdm, weixinOrderNo, price, storeNo, type);
                        if (null == redirectUrl || redirectUrl.equals("")) {
                            //获取授权失败
                            errorRedirect("获取支付宝授权页URL失败!请重试!");
                            return;
                        } else {
                            //成功跳转
                            response.sendRedirect(redirectUrl);
                            return;
                        }
                    } else if (status.contains("纸票")) {
                        errorRedirect("该订单已开具纸质发票，不能重复开具");
                        return;
                    } else if (status.contains("红冲")) {
                        errorRedirect("该订单已红冲");
                        return;
                    } else {
                        errorRedirect("获取授权失败!请重试!");
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorRedirect("发生未知错误！获取授权失败!请重试!");
                return;
            }
        } else {
            errorRedirect("不是支付宝浏览器!请重试!");
            return;
        }
        return;
    }

    public void errorRedirect(String errorName) {
        try {
            response.sendRedirect(errorUrl + "/" + errorName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
