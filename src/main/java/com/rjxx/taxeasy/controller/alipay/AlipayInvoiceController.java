//package com.rjxx.taxeasy.controller.alipay;
//
//import com.rjxx.taxeasy.comm.BaseController;
//import com.rjxx.taxeasy.dao.SkpJpaDao;
//import com.rjxx.taxeasy.dao.WxfpxxJpaDao;
//import com.rjxx.taxeasy.dao.XfJpaDao;
//import com.rjxx.taxeasy.domains.WxFpxx;
//import com.rjxx.taxeasy.domains.WxToken;
//import com.rjxx.taxeasy.dto.alipay.AlipayReceiveApplyDto;
//import com.rjxx.taxeasy.dto.alipay.AlipayResult;
//import com.rjxx.taxeasy.service.adapter.AdapterService;
//import com.rjxx.taxeasy.task.AlipayTask;
//import com.rjxx.utils.alipay.AlipayRSAUtil;
//import com.rjxx.utils.alipay.AlipayResultUtil;
//import com.rjxx.utils.alipay.AlipayUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * @author wangyahui
// * @email wangyahui@datarj.com
// * @company 上海容津信息技术有限公司
// * @date 2018/5/15
// */
//@CrossOrigin
//@RestController
//@RequestMapping("/alipayInvoice")
//public class AlipayInvoiceController extends BaseController{
//    @Autowired
//    private AdapterService adapterService;
//    @Autowired
//    private WxfpxxJpaDao wxfpxxJpaDao;
//    @Autowired
//    private SkpJpaDao skpJpaDao;
//    @Autowired
//    private XfJpaDao xfJpaDao;
//
//    public static final String APPLY_SUCCESS = "APPLY_SUCCESS";
//    public static final String INVOICE_SUCCESS = "INVOICE_SUCCESS";
//    public static final String INVOICE_ERROR = "INVOICE_ERROR";
//    public static final String INVOICE_IS_APPLIED = "INVOICE_IS_APPLIED";
//    public static final String INVOICE_PARAM_ILLEGAL = "INVOICE_PARAM_ILLEGAL";
//    public static final String SYSTEM_ERROR = "SYSTEM_ERROR";
//
//    private static final String ALIPAY_PUBLIC_KEY = "";
//    private static final String ALIPAY_PRIVATE_KEY = "";
//    private static final int MESSAGE_CACHE_SIZE = 1000;
//    private static List<String> cacheList = new ArrayList<>(MESSAGE_CACHE_SIZE);
//
//
//    @RequestMapping(value = "/receiveApply",method = RequestMethod.POST)
//    public AlipayResult receiveApply(@RequestBody AlipayReceiveApplyDto data){
//        String applyId = data.getApplyId();
//        String userId = data.getUserId();
//        String subShortName = data.getSubShortName();
//        String mShortName = data.getmShortName();
//        String orderNo = data.getOrderNo();
//        String payerName = data.getPayerName();
//        String payerRegisterNo = data.getPayerRegisterNo();
//        String invoiceAmount = data.getInvoiceAmount();
//        String payerAddress = data.getPayerAddress();
//        String payerTelphone = data.getPayerTelphone();
//        String payerBankName = data.getPayerBankName();
//        String payerBankAccount = data.getPayerBankAccount();
//        String sign = data.getSign();
//        boolean distinct = distinct(applyId, orderNo, userId);
//        if(!distinct){
//            return null;
//        }
//        Map<String, String> alipayResultMap = new HashMap<>();
//        alipayResultMap.put("applyId", applyId);
//        alipayResultMap.put("userId", userId);
//        alipayResultMap.put("subShortName", subShortName);
//        alipayResultMap.put("mShortName", mShortName);
//        alipayResultMap.put("orderNo", orderNo);
//        alipayResultMap.put("payerName", payerName);
//        alipayResultMap.put("payerRegisterNo", payerRegisterNo);
//        alipayResultMap.put("invoiceAmount", invoiceAmount);
//        alipayResultMap.put("payerAddress", payerAddress);
//        alipayResultMap.put("payerTelphone", payerTelphone);
//        alipayResultMap.put("payerBankName", payerBankName);
//        alipayResultMap.put("payerBankAccount", payerBankAccount);
//        String signature = AlipayRSAUtil.toSign(alipayResultMap, ALIPAY_PRIVATE_KEY);
//        try {
//            boolean b = AlipayRSAUtil.toVerify(signature, ALIPAY_PUBLIC_KEY, sign);
//            if(!b){
//                return AlipayResultUtil.result(INVOICE_PARAM_ILLEGAL,"开票参数非法");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return AlipayResultUtil.result(SYSTEM_ERROR,"系统错误");
//        }
//
//        logger.info("拿到支付宝回传的订单编号为"+orderNo);
//        WxFpxx wxFpxx = wxfpxxJpaDao.selectByWeiXinOrderNo(orderNo);
//        if(null==wxFpxx){
//            return AlipayResultUtil.result(SYSTEM_ERROR, "根据订单号未查到该笔订单");
//        }
//        AlipayTask alipayTask = new AlipayTask();
//        alipayTask.setAdapterService(adapterService);
//        alipayTask.setAlipayResultMap(alipayResultMap);
//        alipayTask.setWxFpxx(wxFpxx);
//        alipayTask.setSkpJpaDao(skpJpaDao);
//        alipayTask.setXfJpaDao(xfJpaDao);
//        Thread t = new Thread(alipayTask);
//        t.start();
//        return null;
//    }
//
//    private synchronized boolean distinct(String applyId,String orderNo,String userId){
//        String flag = applyId + orderNo + userId;
//        if (cacheList.contains(flag)) {
//            logger.info("cacheList里已存在"+flag);
//            return false;
//        }
//        if(cacheList.size()>=MESSAGE_CACHE_SIZE){
//            cacheList.remove(0);
//        }
//        cacheList.add(flag);
//        return true;
//    }
//
//
//    @RequestMapping(value = "/isAlipay",method = RequestMethod.POST)
//    public String isAlipay(String storeNo, String orderNo, String orderTime, String price,String gsdm,String type){
//            String redirectUrl ="";
//            Map resultMap = new HashMap();
//            if(AlipayUtils.isAlipayBrowser(request)){
//                try {
//                    logger.info("------orderNo---------"+orderNo);
//                    if(null==orderNo || "".equals(orderNo)){
//                        request.getSession().setAttribute("msg", "获取微信授权失败!请重试!");
//                        response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
//                        return null;
//                    }
//                    if(null == orderTime || "".equals(orderTime)){
//                        request.getSession().setAttribute("msg", "获取微信授权失败!请重试!");
//                        response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
//                        return null;
//                    }
//                    if(null == price || "".equals(price)){
//                        request.getSession().setAttribute("msg", "获取微信授权失败!请重试!");
//                        response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
//                        return null;
//                    }
//                    if("0.0".equals(price)){
//                        request.getSession().setAttribute("msg", "该订单可开票金额为0");
//                        response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
//                        return null;
//                    }
//                    WxFpxx wxFpxx = wxfpxxJpaDao.selsetByOrderNo(orderNo,gsdm);
//                    if(null==wxFpxx){
//                        request.getSession().setAttribute("msg", "获取微信授权失败!请重试!");
//                        response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
//                        return null;
//                    }
//                    List<String> status = adapterService.checkStatus(wxFpxx.getTqm(),wxFpxx.getGsdm());
//                    if(status!=null){
//                        if(status.contains("开具中")){
//                            //开具中对应的url
//                            response.sendRedirect(request.getContextPath() + "/QR/zzkj.html?t=" + System.currentTimeMillis());
//                            return null;
//                        }else if(status.contains("可开具")){
//                            String access_token ="";
//                            String ticket = "";
//                            WxToken wxToken = wxTokenJpaDao.findByFlag("01");
//                            if(wxToken==null){
//                                access_token= (String) weixinUtils.hqtk().get("access_token");
//                                ticket = weixinUtils.getTicket(access_token);
//                            }else {
//                                access_token = wxToken.getAccessToken();
//                                ticket= wxToken.getTicket();
//                            }
//                            if(ticket==null){
//                                //获取授权失败
//                                request.getSession().setAttribute("msg", "获取微信授权失败!请重试!");
//                                response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
//                                return null;
//                            }
//                            String spappid = weixinUtils.getSpappid(access_token);//获取平台开票信息
//                            if(null==spappid ||"".equals(spappid)){
//                                //获取授权失败
//                                request.getSession().setAttribute("msg", "获取微信授权失败!请重试!");
//                                response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
//                                return null;
//                            }
//                            String weixinOrderNo = wechatFpxxService.getweixinOrderNo(orderNo,gsdm);
//                            logger.info("orderNo---"+orderNo+"传给微信的weixinOrderNo"+weixinOrderNo);
////                        String gsdm = wxFpxx.getGsdm();
//                            //可开具 跳转微信授权链接
//                            redirectUrl = weixinUtils.getTiaoURL(gsdm,weixinOrderNo,price,orderTime, storeNo,type,access_token,ticket,spappid);
//                            if(null==redirectUrl||redirectUrl.equals("")){
//                                //获取授权失败
//                                request.getSession().setAttribute("msg", "获取微信授权失败!请重试!");
//                                response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
//                                return null;
//                            }else {
//                                //成功跳转
//                                response.sendRedirect(redirectUrl);
//                                return null;
//                            }
//                        }else if(status.contains("纸票")){
//                            request.getSession().setAttribute("msg", "该订单已开具纸质发票，不能重复开具");
//                            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
//                            return null;
//                        }else if(status.contains("红冲")){
//                            request.getSession().setAttribute("msg", "该订单已红冲");
//                            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
//                            return null;
//                        }else {
//                            request.getSession().setAttribute("msg", "获取微信授权失败!请重试!");
//                            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
//                            return null;
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }else {
//                logger.info("不是微信浏览器-------------");
//                resultMap.put("num" ,"1");
//            }
//            return resultMap;
//        }
//
//    public void errorRedirect(String errorName) {
//        try {
//            response.sendRedirect(request.getContextPath() + "/QR/error.html?t=" + System.currentTimeMillis() + "=" + errorName);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
