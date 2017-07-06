package com.rjxx.taxeasy.controller;

import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.utils.alipay.AlipayUtils;
import com.rjxx.taxeasy.vo.InvoiceTitleVo;
import com.rjxx.web.JsonStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by Administrator on 2017-07-05.
 */
@Controller
public class InvoiceTitleController extends BaseController {


    @RequestMapping(value = "/getInvoiceTitle", method = {RequestMethod.POST})
    @ResponseBody
    public JsonStatus getInvoiceTitle() throws Exception {
        JsonStatus jsonStatus = new JsonStatus();
        jsonStatus.setSuccess(false);
        if (AlipayUtils.isAlipayBrowser(request) && AlipayUtils.isAlipayAuthorized(session)) {
            List<InvoiceTitleVo> voList = AlipayUtils.getAlipayInvoiceTitleList(session);
            if (voList != null && !voList.isEmpty()) {
                jsonStatus.setSuccess(true);
                jsonStatus.setData(voList);
                return jsonStatus;
            }
        }
        return jsonStatus;
    }

}
