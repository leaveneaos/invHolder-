package com.rjxx.taxeasy.controller;

import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.dao.WxfpxxJpaDao;
import com.rjxx.taxeasy.service.BarcodeService;
import com.rjxx.utils.HtmlUtils;
import com.rjxx.utils.weixin.WeiXinConstants;
import com.rjxx.utils.weixin.WeixinUtils;
import com.rjxx.utils.weixin.wechatFpxxServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by wangyahui on 2017/8/9 0009
 */
@RestController
@RequestMapping("/barcode")
public class BarcodeController extends BaseController {
    @Autowired
    private BarcodeService barcodeService;

    @Autowired
    private WxfpxxJpaDao wxfpxxJpaDao;
    @Autowired
    private wechatFpxxServiceImpl wechateFpxxService;
    /**
     * 扫码请求,如果验签成功,进入扫码信息确认页面
     *
     * @param gsdm 公司代码
     * @param q    约定参数
     */
    @RequestMapping("/{gsdm}")
    public void sm(@PathVariable(value = "gsdm") String gsdm, @RequestParam String q) {
        String ua = request.getHeader("user-agent").toLowerCase();
        String type = request.getParameter("t");
        session.setAttribute("q", q);
        session.setAttribute("gsdm", gsdm);
        session.setAttribute("type", type);
        //判断是否是微信浏览器
        if (ua.indexOf("micromessenger") > 0) {
            //session.setAttribute("gsdm", gsdm);
            //session.setAttribute("q", q);
            if(type!=null){
                session.setAttribute("type", type);
            }
            String url = HtmlUtils.getBasePath(request);
                String ul = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WeiXinConstants.APP_ID + "&redirect_uri="
                        + url + "scan/getOpenid&" + "response_type=code&scope=snsapi_base&state=" +"state"
                        + "#wechat_redirect";
                try {
                    response.sendRedirect(ul);
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        if(type!=null&&type.equals("test")){
            try {
                logger.info("进入测试盘开票----");
                String redirectUrl = request.getContextPath() + "/dicos/ddqr.html?_t=" + System.currentTimeMillis()
                        + "=ycyz";
                logger.info("---测试跳转地址"+redirectUrl);
                response.sendRedirect(redirectUrl);
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //非微信浏览器
        Map result = barcodeService.sm(gsdm, q);
        try {
            if (result != null) {
                //session.setAttribute("gsdm", gsdm);
                //session.setAttribute("q", q);
                String ppdm = result.get("ppdm").toString();
                String ppurl = result.get("ppurl").toString();
                String orderNo = result.get("orderNo").toString();
                session.setAttribute("orderNo", orderNo);
                session.setAttribute("tqm", ppdm+orderNo);
                List<String> status = barcodeService.checkStatus(ppdm+orderNo, gsdm);
                if (status != null) {
                    if(status.contains("可开具")){
                        if (StringUtils.isNotBlank(ppdm)) {
                            //有品牌代码对应的url
                            response.sendRedirect(request.getContextPath() + ppurl + "?t=" + System.currentTimeMillis() + "=" + ppdm);
                            return;
                        } else {
                            //无品牌对应的url
                            response.sendRedirect(request.getContextPath() + ppurl + "?t=" + System.currentTimeMillis() + "=no");
                            return;
                        }
                    }else if(status.contains("开具中")){
                        //开具中对应的url
                        response.sendRedirect(request.getContextPath() + "/QR/zzkj.html?t=" + System.currentTimeMillis());
                        return;
                    }else {
                        StringBuilder sb = new StringBuilder();
                        for(String str:status){
                            if(str.indexOf("pdf")!=-1){
                                String pdf = str.split("[+]")[0];
                                String img = pdf.replace("pdf", "jpg");
                                sb.append("&"+img);
                            }
                        }
                        String serialOrder = status.get(0).split("[+]")[4];
                        session.setAttribute("serialorder", serialOrder);
                        //response.sendRedirect(request.getContextPath() + "/QR/scan.html?t=" + System.currentTimeMillis() + "="+sb.toString());
                        //response.sendRedirect(request.getContextPath() + "/CO/smfpxq.html?serialOrder="+serialOrder+"&&_t=" + System.currentTimeMillis());
                        response.sendRedirect(request.getContextPath() + "/CO/dzfpxq.html?_t=" + System.currentTimeMillis());
                        return;
                    }
                } else {
                    //获取pdf状态码失败的url
                    response.sendRedirect(request.getContextPath() + "/QR/error.html?t=" + System.currentTimeMillis() + "=GET_PDF_STATE_ERROR");
                    return;
                }
            } else {
                //验签失败url
                response.sendRedirect(request.getContextPath() + "/QR/error.html?t=" + System.currentTimeMillis() + "=QRCODE_VALIDATION_FAILED");
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                //重定向报错url
                response.sendRedirect(request.getContextPath() + "/QR/error.html?t=" + System.currentTimeMillis() + "=REDIRECT_ERROR");
                return;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
