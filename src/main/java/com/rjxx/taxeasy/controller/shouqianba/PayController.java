package com.rjxx.taxeasy.controller.shouqianba;

import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.dto.shouqianba.PayResult;
import com.rjxx.taxeasy.dto.shouqianba.QueryResult;
import com.rjxx.taxeasy.wechat.dto.Result;
import com.rjxx.taxeasy.wechat.util.ResultUtil;
import com.rjxx.utils.shouqianba.PayUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;

/**
 * @author wangyahui
 * @email wangyahui@datarj.com
 * @company 上海容津信息技术有限公司
 * @date 2018/6/4
 */
@RestController
@RequestMapping("/pay")
public class PayController extends BaseController{

    @Value("${web.url.error}")
    private String errorUrl;

    private String succUrl = "";

    @RequestMapping
    public Result payIn(String terminal_sn, String terminal_key, String client_sn, String total_amount,
                        String subject, String oprator) {
        Map payResult = PayUtil.payIn(terminal_sn, terminal_key, client_sn, total_amount, subject, oprator,
                request.getContextPath() + "/receive");
        if(payResult!=null){
            return ResultUtil.success(payResult);
        }else{
            return ResultUtil.error("获取跳转链接失败,请重试");
        }
    }

    @RequestMapping("/receive")
    public void receive(PayResult payResult) {
        String is_success = payResult.getIs_success();
        if("SUCCESS".equals(is_success)){
            String terminal_sn = payResult.getTerminal_sn();
            String client_sn = payResult.getClient_sn();
            String sn = payResult.getSn();
            //FIXME
            String terminal_key = "select terminal_key from t where client_sn=?1";
            //开始轮询
            boolean flag = false;
            QueryResult query = null;
            BigInteger initTime = BigInteger.valueOf(System.currentTimeMillis());
            x:if(!flag){
                QueryResult q = PayUtil.query(terminal_sn, terminal_key, client_sn, sn);
                if(q!=null) {
                    flag = true;
                    query = q;
                }else{
                    try {
                        BigInteger curTime = BigInteger.valueOf(System.currentTimeMillis());
                        if(curTime.subtract(initTime).compareTo(new BigInteger("120000"))==1){
                            errorRedirect("查询不到该订单的状态");
                            return;
                        }
                        if(curTime.subtract(initTime).compareTo(new BigInteger("30000"))==1){
                            Thread.sleep(5000);
                        }else{
                            Thread.sleep(2000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                break x;
            }else{
                String result_code = query.getResult_code();
                if("SUCCESS".equals(result_code)){
                    try {
                        response.sendRedirect(succUrl);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                    String error_code = query.getError_code();
                    String error_message = query.getError_message();
                    if(StringUtils.isNotBlank(error_code)){
                        errorRedirect(error_message);
                    }else{
                        errorRedirect("查询时，未收到支付商返回");
                    }
                }
            }
        }else{
            String error_code = payResult.getError_code();
            String error_message = payResult.getError_message();
            if(StringUtils.isNotBlank(error_code)){
                errorRedirect(error_message);
            }else{
                errorRedirect("回调时，未收到支付商返回");
            }
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
