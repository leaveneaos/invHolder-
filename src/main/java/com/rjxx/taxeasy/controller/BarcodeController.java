package com.rjxx.taxeasy.controller;

import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.dao.WxfpxxJpaDao;
import com.rjxx.taxeasy.service.BarcodeService;
import com.rjxx.utils.HtmlUtils;
import com.rjxx.utils.weixin.WeiXinConstants;
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
    /**
     * 扫码请求,如果验签成功,进入扫码信息确认页面
     *
     * @param gsdm 公司代码
     * @param q    约定参数
     */
    @RequestMapping("/{gsdm}")
    public void sm(@PathVariable(value = "gsdm") String gsdm, @RequestParam String q) {
        String ua = request.getHeader("user-agent").toLowerCase();
        //判断是否是微信浏览器
        if (ua.indexOf("micromessenger") > 0) {
            session.setAttribute("gsdm", gsdm);
            session.setAttribute("q", q);
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

        Map result = barcodeService.sm(gsdm, q);
        try {
            if (result != null) {
                session.setAttribute("gsdm", gsdm);
                session.setAttribute("q", q);
                String ppdm = result.get("ppdm").toString();
                String ppurl = result.get("ppurl").toString();
                String orderNo = result.get("orderNo").toString();
                session.setAttribute("orderNo", orderNo);
                List<String> status = barcodeService.checkStatus(ppdm+orderNo, gsdm);
                if (status != null) {
                    if(status.contains("可开具")){
                        if (StringUtils.isNotBlank(ppdm)) {
                            //有品牌代码对应的url
                            response.sendRedirect(request.getContextPath() + ppurl + "?t=" + System.currentTimeMillis() + "=" + ppdm);
                        } else {
                            //无品牌对应的url
                            response.sendRedirect(request.getContextPath() + ppurl + "?t=" + System.currentTimeMillis() + "=no");
                        }
                    }else if(status.contains("开具中")){
                        //开具中对应的url
                        response.sendRedirect(request.getContextPath() + "/QR/zzkj.html?t=" + System.currentTimeMillis());
                    }else {
                        StringBuilder sb = new StringBuilder();
                        for(String str:status){
                            if(str.indexOf("pdf")!=-1){
                                String pdf = str.split("[+]")[0];
                                String img = pdf.replace("pdf", "jpg");
                                sb.append("&"+img);
                            }
                        }
                        session.setAttribute("serialorder", status.get(0).split("[+]")[4]);
                        response.sendRedirect(request.getContextPath() + "/QR/scan.html?t=" + System.currentTimeMillis() + "="+sb.toString());
                    }
//                    switch (status) {
//                        case "可开具":
//                            if (StringUtils.isNotBlank(ppdm)) {
//                                //有品牌代码对应的url
//                                response.sendRedirect(request.getContextPath() + ppurl + "?t=" + System.currentTimeMillis() + "=" + ppdm);
//                            } else {
//                                //无品牌对应的url
//                                response.sendRedirect(request.getContextPath() + ppurl + "?t=" + System.currentTimeMillis() + "=no");
//                            }
//                            break;
//                        case "开具中":
//                            //开具中对应的url
//                            response.sendRedirect(request.getContextPath() + "/QR/zzkj.html?t=" + System.currentTimeMillis());
//                            break;
//                        default:
//                            if (status.indexOf("pdf") != -1) {
//                                logger.info(status);
//                                String pdf = status.split("[+]")[0];
//                                //String je = status.split("[+]")[1];
//                               // String orderTime = status.split("[+]")[2];
//                                //String kplsh = status.split("[+]")[3];
//                                String img = pdf.replace("pdf", "jpg");
//                                response.sendRedirect(request.getContextPath() + "/QR/scan.html?t=" + System.currentTimeMillis() + "="+img);
//                            } else {
//                                //无pdf对应的url
//                                response.sendRedirect(request.getContextPath() + "/QR/error.html?t=" + System.currentTimeMillis() + "=GET_PDF_ERROR");
//                            }
//                    }
                } else {
                    //获取pdf状态码失败的url
                    response.sendRedirect(request.getContextPath() + "/QR/error.html?t=" + System.currentTimeMillis() + "=GET_PDF_STATE_ERROR");
                }
            } else {
                //验签失败url
                response.sendRedirect(request.getContextPath() + "/QR/error.html?t=" + System.currentTimeMillis() + "=QRCODE_VALIDATION_FAILED");
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                //重定向报错url
                response.sendRedirect(request.getContextPath() + "/QR/error.html?t=" + System.currentTimeMillis() + "=REDIRECT_ERROR");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
