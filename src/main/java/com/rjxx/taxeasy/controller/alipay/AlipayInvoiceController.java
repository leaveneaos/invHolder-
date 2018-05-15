//package com.rjxx.taxeasy.controller.alipay;
//
//import com.rjxx.taxeasy.comm.BaseController;
//import com.rjxx.taxeasy.controller.AdapterController;
//import com.rjxx.taxeasy.domains.Kpls;
//import com.rjxx.taxeasy.domains.Kpspmx;
//import com.rjxx.taxeasy.domains.Pp;
//import com.rjxx.taxeasy.utils.alipay.AlipayUtils;
//import com.rjxx.utils.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.io.IOException;
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
//
//    /**
//     * 将发票信息同步到支付宝
//     *
//     * @return
//     */
//    @RequestMapping(value = "/sync")
//    @ResponseBody
//    public String syncAlipay(@RequestParam(required = false) String serialorder) throws Exception {
//        if (serialorder == null) {
//            Object serialorderObject = session.getAttribute("serialorder");
//            if (serialorderObject == null) {
//                request.getSession().setAttribute("msg", "会话超时，请重新开始操作!");
//                response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
//                return null;
//            }
//            serialorder = serialorderObject.toString();
//        }
//        //判断是否是支付宝内
//        if (!AlipayUtils.isAlipayBrowser(request)) {
//            request.getSession().setAttribute("msg", "请使用支付宝进行该操作");
//            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
//            return null;
//        }
//        if (!AlipayUtils.isAlipayAuthorized(session)) {
//            AlipayUtils.initAlipayAuthorization(request, response, "/syncAlipay");
//            return null;
//        }
//        Map params = new HashMap();
//        params.put("serialorder", serialorder);
//        List<Kpls> kplsList = kplsService.findAll(params);
//        String redirectUrl = null;
//        for (Kpls kpls : kplsList) {
//            int kplsh = kpls.getKplsh();
//            Map params2 = new HashMap();
//            params2.put("kplsh", kplsh);
//            List<Kpspmx> kpspmxList = kpspmxService.findMxNewList(params2);
//            Pp pp = ppService.findOnePpByGsdmSkpid(kpls.getSkpid(), kpls.getGsdm());
//            if (pp == null || StringUtils.isBlank(pp.getAliMShortName()) || StringUtils.isBlank(pp.getAliSubMShortName())) {
//                request.getSession().setAttribute("msg", "该商户没有注册到支付宝发票管家");
//                response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
//                return null;
//            }
//            redirectUrl = AlipayUtils.syncInvoice2Alipay(session, kpls, kpspmxList, pp.getAliMShortName(), pp.getAliSubMShortName());
//            if (redirectUrl == null) {
//                request.getSession().setAttribute("msg", "将发票归集到支付宝发票管家出现异常");
//                response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
//                return null;
//            }
//        }
//        if (redirectUrl == null) {
//            request.getSession().setAttribute("msg", "将发票归集到支付宝发票管家出现异常");
//            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
//            return null;
//        } else {
//            response.sendRedirect(redirectUrl);
//            return null;
//        }
//    }
//}
