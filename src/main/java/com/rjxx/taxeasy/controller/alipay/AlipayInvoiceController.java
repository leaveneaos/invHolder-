package com.rjxx.taxeasy.controller.alipay;

import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.dto.alipay.AlipayReceiveApplyDto;
import com.rjxx.taxeasy.dto.alipay.AlipayResult;
import com.rjxx.utils.alipay.AlipayRSAUtil;
import com.rjxx.utils.alipay.AlipayResultUtil;
import org.springframework.web.bind.annotation.*;

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
public class AlipayInvoiceController extends BaseController{

    private static final String ALIPAY_PUBLIC_KEY = "";
    private static final String ALIPAY_PRIVATE_KEY = "";
    private static final int MESSAGE_CACHE_SIZE = 1000;
    private static List<String> cacheList = new ArrayList<>(MESSAGE_CACHE_SIZE);


    @RequestMapping(value = "receiveApply",method = RequestMethod.POST)
    public AlipayResult receiveApply(@RequestBody AlipayReceiveApplyDto data){
        String applyId = data.getApplyId();
        String userId = data.getUserId();
        String subShortName = data.getSubShortName();
        String mShortName = data.getmShortName();
        String orderNo = data.getOrderNo();
        String payerName = data.getPayerName();
        String payerRegisterNo = data.getPayerRegisterNo();
        String invoiceAmount = data.getInvoiceAmount();
        String payerAddressPhone = data.getPayerAddressPhone();
        String payerBankNameAccount = data.getPayerBankNameAccount();
        String sign = data.getSign();
        boolean distinct = distinct(applyId, orderNo, userId);
        if(!distinct){
            return null;
        }
        Map<String, String> params = new HashMap<>();
        params.put("applyId", applyId);
        params.put("userId", userId);
        params.put("subShortName", subShortName);
        params.put("mShortName", mShortName);
        params.put("orderNo", orderNo);
        params.put("payerName", payerName);
        params.put("payerRegisterNo", payerRegisterNo);
        params.put("invoiceAmount", invoiceAmount);
        params.put("payerAddressPhone", payerAddressPhone);
        params.put("payerBankNameAccount", payerBankNameAccount);
        String signature = AlipayRSAUtil.toSign(params, ALIPAY_PRIVATE_KEY);
        try {
            boolean b = AlipayRSAUtil.toVerify(signature, ALIPAY_PUBLIC_KEY, sign);
            if(!b){
                return AlipayResultUtil.result(AlipayResultUtil.INVOICE_PARAM_ILLEGAL,"开票参数非法");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return AlipayResultUtil.result(AlipayResultUtil.SYSTEM_ERROR,"系统错误");
        }
        return null;
    }

    private synchronized boolean distinct(String applyId,String orderNo,String userId){
        String flag = applyId + orderNo + userId;
        if (cacheList.contains(flag)) {
            logger.info("cacheList里已存在"+flag);
            return false;
        }
        if(cacheList.size()>=MESSAGE_CACHE_SIZE){
            cacheList.remove(0);
        }
        cacheList.add(flag);
        return true;
    }
}
