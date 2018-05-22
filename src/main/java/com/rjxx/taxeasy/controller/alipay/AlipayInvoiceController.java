package com.rjxx.taxeasy.controller.alipay;

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
import com.rjxx.utils.alipay.AlipayRSAUtil;
import com.rjxx.utils.alipay.AlipayResultUtil;
import com.rjxx.utils.alipay.AlipayUtils;
import com.rjxx.utils.weixin.wechatFpxxServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
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

    public static final String APPLY_SUCCESS = "APPLY_SUCCESS";
    public static final String INVOICE_SUCCESS = "INVOICE_SUCCESS";
    public static final String INVOICE_ERROR = "INVOICE_ERROR";
    public static final String INVOICE_IS_APPLIED = "INVOICE_IS_APPLIED";
    public static final String INVOICE_PARAM_ILLEGAL = "INVOICE_PARAM_ILLEGAL";
    public static final String SYSTEM_ERROR = "SYSTEM_ERROR";

    private static final String ALIPAY_PUBLIC_KEY = "";
    private static final String ALIPAY_PRIVATE_KEY = "";
    private static final int MESSAGE_CACHE_SIZE = 1000;
    private static List<String> cacheList = new ArrayList<>(MESSAGE_CACHE_SIZE);


    @RequestMapping(value = "/receiveApply", method = RequestMethod.POST)
    public AlipayResult receiveApply(@RequestBody AlipayReceiveApplyDto data) {
        String applyId = data.getApplyId();
        String userId = data.getUserId();
        String subShortName = data.getSubShortName();
        String mShortName = data.getmShortName();
        String orderNo = data.getOrderNo();
        String payerName = data.getPayerName();
        String payerRegisterNo = data.getPayerRegisterNo();
        String invoiceAmount = data.getInvoiceAmount();
        String payerAddress = data.getPayerAddress();
        String payerTelphone = data.getPayerTelphone();
        String payerBankName = data.getPayerBankName();
        String payerBankAccount = data.getPayerBankAccount();
        String sign = data.getSign();
        boolean distinct = distinct(applyId, orderNo, userId);
        if (!distinct) {
            return null;
        }
        Map<String, String> alipayResultMap = new HashMap<>();
        alipayResultMap.put("applyId", applyId);
        alipayResultMap.put("userId", userId);
        alipayResultMap.put("subShortName", subShortName);
        alipayResultMap.put("mShortName", mShortName);
        alipayResultMap.put("orderNo", orderNo);
        alipayResultMap.put("payerName", payerName);
        alipayResultMap.put("payerRegisterNo", payerRegisterNo);
        alipayResultMap.put("invoiceAmount", invoiceAmount);
        alipayResultMap.put("payerAddress", payerAddress);
        alipayResultMap.put("payerTelphone", payerTelphone);
        alipayResultMap.put("payerBankName", payerBankName);
        alipayResultMap.put("payerBankAccount", payerBankAccount);
        String signature = AlipayRSAUtil.toSign(alipayResultMap, ALIPAY_PRIVATE_KEY);
        try {
            boolean b = AlipayRSAUtil.toVerify(signature, ALIPAY_PUBLIC_KEY, sign);
            if (!b) {
                return AlipayResultUtil.result(INVOICE_PARAM_ILLEGAL, "开票参数非法");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return AlipayResultUtil.result(SYSTEM_ERROR, "系统错误");
        }

        logger.info("拿到支付宝回传的订单编号为" + orderNo);
        WxFpxx wxFpxx = wxfpxxJpaDao.selectByWeiXinOrderNo(orderNo);
        if (null == wxFpxx) {
            return AlipayResultUtil.result(SYSTEM_ERROR, "根据订单号未查到该笔订单");
        }
        AlipayTask alipayTask = new AlipayTask();
        alipayTask.setAdapterService(adapterService);
        alipayTask.setAlipayResultMap(alipayResultMap);
        alipayTask.setWxFpxx(wxFpxx);
        alipayTask.setSkpJpaDao(skpJpaDao);
        alipayTask.setXfJpaDao(xfJpaDao);
        Thread t = new Thread(alipayTask);
        t.start();
        return null;
    }

    /**
     * 排重
     * @param applyId
     * @param orderNo
     * @param userId
     * @return
     */
    private synchronized boolean distinct(String applyId, String orderNo, String userId) {
        String flag = applyId + orderNo + userId;
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
     * 开始拉取支付宝授权
     * @param storeNo
     * @param orderNo
     * @param price
     * @param gsdm
     * @param type
     */
    @RequestMapping(value = "/isAlipay", method = RequestMethod.POST)
    public void isAlipay(String storeNo, String orderNo, String price, String gsdm, String type) {
        String redirectUrl = "";
        Map resultMap = new HashMap();
        if (AlipayUtils.isAlipayBrowser(request)) {
            try {
                logger.info("------orderNo---------" + orderNo);
                if (null == orderNo || "".equals(orderNo)) {
                    request.getSession().setAttribute("msg", "订单号为空,拉取支付宝授权页失败!请重试!");
                    response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                    return;
                }
                if (null == price || "".equals(price)) {
                    request.getSession().setAttribute("msg", "金额为空,拉取支付宝授权页失败!请重试!");
                    response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                    return;
                }
                if ("0.0".equals(price)) {
                    request.getSession().setAttribute("msg", "该订单可开票金额为0");
                    response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                    return;
                }
                WxFpxx wxFpxx = wxfpxxJpaDao.selsetByOrderNo(orderNo, gsdm);
                if (null == wxFpxx) {
                    request.getSession().setAttribute("msg", "根据支付宝回传订单号未找到该订单!请重试!");
                    response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
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
                            request.getSession().setAttribute("msg", "获取支付宝授权页URL失败!请重试!");
                            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                            return;
                        } else {
                            //成功跳转
                            response.sendRedirect(redirectUrl);
                            return;
                        }
                    } else if (status.contains("纸票")) {
                        request.getSession().setAttribute("msg", "该订单已开具纸质发票，不能重复开具");
                        response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                        return;
                    } else if (status.contains("红冲")) {
                        request.getSession().setAttribute("msg", "该订单已红冲");
                        response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                        return;
                    } else {
                        request.getSession().setAttribute("msg", "获取授权失败!请重试!");
                        response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            request.getSession().setAttribute("msg", "不是支付宝浏览器!请重试!");
            try {
                response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        return;
    }

    /**
     * 重定向到支付宝授权页
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
        String sign = "";
        Map sendParam = new HashMap();
        sendParam.put("invoiceAmount", doumoney);
        sendParam.put("orderNo", orderNo);
        sendParam.put("mShortName", mShortName);
        sendParam.put("subShortName", subShortName);
        sendParam.put("resultUrl", redirect_url);
        sendParam.put("sign", sign);
        return null;
    }

    /**
     * 拒绝开票
     * @param applyId
     * @param reason
     */
    public void refuse(String applyId, String reason) {
        //FIXME
    }
}
