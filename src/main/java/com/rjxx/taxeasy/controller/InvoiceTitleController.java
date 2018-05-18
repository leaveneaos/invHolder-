package com.rjxx.taxeasy.controller;

import com.alibaba.fastjson.JSON;
import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.utils.alipay.AlipayUtils;
import com.rjxx.taxeasy.vo.InvoiceTitleVo;
import com.rjxx.web.JsonStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by Administrator on 2017-07-05.
 */
@Controller
@CrossOrigin
public class InvoiceTitleController extends BaseController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/getInvoiceTitle", method = {RequestMethod.POST})
    @ResponseBody
    public JsonStatus getInvoiceTitle() throws Exception {
         JsonStatus jsonStatus = new JsonStatus();
         jsonStatus.setSuccess(false);
        logger.info(JSON.toJSONString(jsonStatus)+"start  application");
       if (AlipayUtils.isAlipayBrowser(request) && AlipayUtils.isAlipayAuthorized(session)) {
           logger.info(JSON.toJSONString(jsonStatus)+"middle  application");

           List<InvoiceTitleVo> voList = AlipayUtils.getAlipayInvoiceTitleList(session);
           logger.info(JSON.toJSONString(jsonStatus)+"middle2  application");

           if (voList != null && !voList.isEmpty()) {
               logger.info(JSON.toJSONString(jsonStatus)+"end  application");
               jsonStatus.setSuccess(true);
                 jsonStatus.setData(voList);
                 return jsonStatus;
             }
         }
        logger.info(JSON.toJSONString(jsonStatus)+"result  application");

       // JsonStatus jsonStatus = new JsonStatus();
       // jsonStatus.setSuccess(true);
       // List<InvoiceTitleVo> voList=new ArrayList<>();
       // InvoiceTitleVo invoiceTitleVo=new InvoiceTitleVo();
       // invoiceTitleVo.setDefault(true);
       // invoiceTitleVo.setOpenBankAccount("7899588558445");
       // invoiceTitleVo.setOpenBankName("中国银行");
       // invoiceTitleVo.setTaxRegisterNo("9102365478945632102");
       // invoiceTitleVo.setTitleName("张强");
       // invoiceTitleVo.setUserAddress("上海徐汇");
       // invoiceTitleVo.setUserEmail("zhangqiang@datarj.com");
       // invoiceTitleVo.setUserMobile("2345678945");
       // InvoiceTitleVo invoiceTitleVo2=new InvoiceTitleVo();
       // invoiceTitleVo2.setDefault(false);
       // invoiceTitleVo2.setOpenBankAccount("7899588558445-1");
       // invoiceTitleVo2.setOpenBankName("中国银行2");
       // invoiceTitleVo2.setTaxRegisterNo("9102365478945632102");
       // invoiceTitleVo2.setTitleName("张强2");
       // invoiceTitleVo2.setUserAddress("上海徐汇2");
       // invoiceTitleVo2.setUserEmail("zhangqiang@datarj.com");
       // invoiceTitleVo2.setUserMobile("2345678945");
       // voList.add(invoiceTitleVo);
       // voList.add(invoiceTitleVo2);
       // jsonStatus.setData(voList);
       return jsonStatus;
    }

//   public static void main(String[] args) {
//       JsonStatus jsonStatus = new JsonStatus();
//       jsonStatus.setSuccess(true);
//       List<InvoiceTitleVo> voList=new ArrayList<>();
//       InvoiceTitleVo invoiceTitleVo=new InvoiceTitleVo();
//       invoiceTitleVo.setDefault(true);
//       invoiceTitleVo.setOpenBankAccount("7899588558445");
//       invoiceTitleVo.setOpenBankName("中国银行");
//       invoiceTitleVo.setTaxRegisterNo("9102365478945632102");
//       invoiceTitleVo.setTitleName("张强");
//       invoiceTitleVo.setUserAddress("上海徐汇");
//       invoiceTitleVo.setUserEmail("zhangqiang@datarj.com");
//       invoiceTitleVo.setUserMobile("2345678945");
//       InvoiceTitleVo invoiceTitleVo2=new InvoiceTitleVo();
//       invoiceTitleVo2.setDefault(false);
//       invoiceTitleVo2.setOpenBankAccount("7899588558445-1");
//       invoiceTitleVo2.setOpenBankName("中国银行2");
//       invoiceTitleVo2.setTaxRegisterNo("9102365478945632102");
//       invoiceTitleVo2.setTitleName("张强2");
//       invoiceTitleVo2.setUserAddress("上海徐汇2");
//       invoiceTitleVo2.setUserEmail("zhangqiang@datarj.com");
//       invoiceTitleVo2.setUserMobile("2345678945");
//       voList.add(invoiceTitleVo);
//       voList.add(invoiceTitleVo2);
//       jsonStatus.setData(voList);
//       System.out.println(JSON.toJSONString(jsonStatus));
//   }

}
