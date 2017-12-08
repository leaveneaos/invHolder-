package com.rjxx.taxeasy.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.dao.WxfpxxJpaDao;
import com.rjxx.taxeasy.service.BarcodeService;
import com.rjxx.taxeasy.utils.alipay.AlipayConstants;
import com.rjxx.taxeasy.wechat.dto.Result;
import com.rjxx.taxeasy.wechat.util.HttpClientUtil;
import com.rjxx.taxeasy.wechat.util.ResultUtil;
import com.rjxx.utils.weixin.WeiXinConstants;
import com.rjxx.utils.weixin.wechatFpxxServiceImpl;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/scan")
public class ScanController extends BaseController {
    @Autowired
    private BarcodeService barcodeService;
    @Autowired
    private WxfpxxJpaDao wxfpxxJpaDao;
    @Autowired
    private wechatFpxxServiceImpl wechateFpxxService;

    /**
     * 确认扫码中传递的信息
     */
    @RequestMapping("/scanConfirm")
    public Result smConfirm() {
        String gsdm = (String) session.getAttribute("gsdm");
        logger.warn("存入数据库时候gsdm="+gsdm);
        String q = (String) session.getAttribute("q");
        logger.warn("存入数据库时候q="+q);
        String openid = (String) session.getAttribute("openid");
        logger.warn("存入数据库时候openid ="+openid );
        String orderNo = (String) session.getAttribute("orderNo");
        logger.warn("存入数据库时候orderNo="+orderNo);
        if (gsdm == null || q == null ) {
            return ResultUtil.error("session过期,请重新扫码");
        }
        String jsonData = barcodeService.getSpxx(gsdm, q);
        if (jsonData != null) {
            JSONObject jsonObject = JSON.parseObject(jsonData);
            String tqm=jsonObject.getString("tqm");
            session.setAttribute("tqm",tqm);
            boolean b = wechateFpxxService.InFapxx(tqm, gsdm, orderNo, q, "1", openid,
                    (String) request.getSession().getAttribute(AlipayConstants.ALIPAY_USER_ID), "", request);
            if(!b){
                return ResultUtil.error("保存发票信息失败，请重试！");
            }
            return ResultUtil.success(jsonData);//订单号,订单时间,门店号,金额,商品名,商品税率
        } else {
            return ResultUtil.error("二维码信息获取失败");
        }
    }

    /**
     * 提交抬头
     *
     * @param gfmc  购方名称
     * @param gfsh  购方税号
     * @param email 购方邮箱
     */
    @RequestMapping("/submit")
    public Result submit(@RequestParam String gfmc, @RequestParam String gfsh, @RequestParam String email) {
        String gsdm = (String) session.getAttribute("gsdm");
        String q = (String) session.getAttribute("q");
        if (gsdm == null || q == null) {
            return ResultUtil.error("redirect");
        }
        String userId = (String) request.getSession().getAttribute(AlipayConstants.ALIPAY_USER_ID);
        //非必须参数
        String gfdz = request.getParameter("gfdz");
        String gfdh = request.getParameter("gfdh");
        String gfyhzh = request.getParameter("gfyhzh");
        String gfyh = request.getParameter("gfyh");
        String tqm = request.getParameter("tqm");

        String status = barcodeService.makeInvoice(gsdm, q, gfmc, gfsh, email, gfyh, gfyhzh, gfdz, gfdh, tqm,userId,"5","","");
        //开票
        if ("-1".equals(status)) {
            return ResultUtil.error("开具失败");
        } else if ("0".equals(status)) {
            return ResultUtil.error("所需信息为空");
        } else {
            JSONObject jsonObject = JSON.parseObject(status);
            session.setAttribute("serialorder", jsonObject.getString("serialorder"));
            return ResultUtil.success(status);
        }
    }



    @RequestMapping(value = "/getOpenid")
    public void getOpenId(String state, String code) {
        String turl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + WeiXinConstants.APP_ID + "&secret="
                + WeiXinConstants.APP_SECRET + "&code=" + code + "&grant_type=authorization_code";
        String resultJson = HttpClientUtil.doGet(turl);
        JSONObject resultObject = JSONObject.parseObject(resultJson);
        String openid = resultObject.getString("openid");
        String access_token = resultObject.getString("access_token");
        logger.info("存入的access_token"+access_token);
        logger.info("存入session时候的openid"+openid);
        if (openid != null) {
            session.setAttribute("openid", openid);
        }

        if(session.getAttribute("gsdm")==null|| session.getAttribute("q")==null){
            try {
                response.sendRedirect(request.getContextPath() + "/QR/error.html?t=" + System.currentTimeMillis() + "=GET_WECHAT_AUTHORIZED_FAILED");
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String type = request.getSession().getAttribute("type").toString();
        if(type!=null&&type.equals("test")){
            logger.info("进入测试盘开票----");
        }
        String gsdm = session.getAttribute("gsdm").toString();
        String q = session.getAttribute("q").toString();
        logger.info("存入session时候的gsdm"+gsdm);
        logger.info("存入session时候的q"+q);
        Map result = barcodeService.sm(gsdm, q);
        try {
            if (result != null) {
                session.setAttribute("gsdm", gsdm);
                session.setAttribute("q", q);
                String ppdm = result.get("ppdm").toString();
                String ppurl = result.get("ppurl").toString();
                String orderNo = result.get("orderNo").toString();
                logger.warn("存入session时候orderNo="+orderNo);
                session.setAttribute("orderNo", orderNo);
                session.setAttribute("tqm", ppdm+orderNo);
                List<String> status = barcodeService.checkStatus(ppdm+orderNo, gsdm);
                logger.info("status=",status);
                if(status!=null&&status.size()>0){
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
                        String je = status.get(0).split("[+]")[1];
                        String orderTime = status.get(0).split("[+]")[2];
                        String kplsh = status.get(0).split("[+]")[3];
                        session.setAttribute("serialorder", status.get(0).split("[+]")[4]);
                        boolean b = wechateFpxxService.InFapxx(ppdm + orderNo, gsdm, orderNo, q, "2", openid,
                                (String) request.getSession().getAttribute(AlipayConstants.ALIPAY_USER_ID), "", request);
                        if(!b){
                            request.getSession().setAttribute("msg", "发票信息保存失败，请重试!");
                            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                            return;
                        }
                        String serialOrder = status.get(0).split("[+]")[4];
                        //logger.info("跳转的url--"+request.getContextPath() + "/CO/smfpxq.html?serialOrder="+serialOrder+"&&_t=" + System.currentTimeMillis());
                        //有pdf对应的url
                        //response.sendRedirect(request.getContextPath() + "/QR/scan.html?t=" + System.currentTimeMillis()
                        //        + "=" + sb.toString() + "=" + orderNo + "=" +je + "=" + orderTime);
                        //response.sendRedirect(request.getContextPath() + "/CO/smfpxq.html?serialOrder="+serialOrder+"&&_t=" + System.currentTimeMillis());
                        response.sendRedirect(request.getContextPath() + "/CO/dzfpxq.html?_t=" + System.currentTimeMillis());
                        return;
                    }
                }else{
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
