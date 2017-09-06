package com.rjxx.taxeasy.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.dao.WxfpxxJpaDao;
import com.rjxx.taxeasy.domains.WxFpxx;
import com.rjxx.taxeasy.service.BarcodeService;
import com.rjxx.taxeasy.utils.alipay.AlipayConstants;
import com.rjxx.taxeasy.wechat.dto.Result;
import com.rjxx.taxeasy.wechat.util.HttpClientUtil;
import com.rjxx.taxeasy.wechat.util.ResultUtil;
import com.rjxx.utils.weixin.WeiXinConstants;
import com.rjxx.utils.weixin.WeixinUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
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
            WxFpxx wxFpxxByTqm = wxfpxxJpaDao.selsetByOrderNo(orderNo);
            if(WeixinUtils.isWeiXinBrowser(request)){
                logger.info("----微信扫码保存交易信息----");
                if(null==wxFpxxByTqm){
                    WxFpxx wxFpxx = new WxFpxx();
                    wxFpxx.setTqm(tqm);
                    wxFpxx.setGsdm(gsdm);
                    wxFpxx.setQ(q);
                    wxFpxx.setOpenId(openid);
                    wxFpxx.setOrderNo(orderNo);
                    wxFpxx.setWxtype("1");//1:申请开票2：领取发票
                    try {
                        wxfpxxJpaDao.save(wxFpxx);
                    }catch (Exception e){
                        return ResultUtil.error("交易信息保存失败");
                    }
                }else {
                    wxFpxxByTqm.setTqm(tqm);
                    wxFpxxByTqm.setGsdm(gsdm);
                    wxFpxxByTqm.setQ(q);
                    wxFpxxByTqm.setOpenId(openid);
                    wxFpxxByTqm.setOrderNo(orderNo);
                    wxFpxxByTqm.setWxtype("1");//1:申请开票2：领取发票
                    if(wxFpxxByTqm.getCode()!=null||!"".equals(wxFpxxByTqm.getCode())){
                        String notNullCode= wxFpxxByTqm.getCode();
                        wxFpxxByTqm.setCode(notNullCode);
                    }
                    try {
                        wxfpxxJpaDao.save(wxFpxxByTqm);
                    }catch (Exception e){
                        logger.info("交易信息保存失败");
                        return ResultUtil.error("交易信息保存失败");
                    }
                }
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

        String status = barcodeService.makeInvoice(gsdm, q, gfmc, gfsh, email, gfyh, gfyhzh, gfdz, gfdh, tqm,userId,"5");
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
        logger.info("获取openid的时候得到的access_token"+JSON.toJSONString(resultObject));
        String access_token = resultObject.getString("access_token");
        if(null!= access_token){
            session.setAttribute("access_token", access_token);
        }
        String openid = resultObject.getString("openid");
        logger.info("存入session时候的openid"+openid);
        if (openid != null) {
            session.setAttribute("openid", openid);
        }
//        int index = state.indexOf("$");
//        String gsdm = state.substring(0, index);
//        String q = state.substring(index+1, state.length());
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
                String status = barcodeService.checkStatus(ppdm+orderNo, gsdm);
                if(status!=null){
                    switch (status) {
                        case "可开具":
                            if (StringUtils.isNotBlank(ppdm)) {
                                //有品牌代码对应的url
                                response.sendRedirect(request.getContextPath() + ppurl + "?t="+ System.currentTimeMillis()+ "=" +ppdm );
                            } else {
                                //无品牌对应的url
                                response.sendRedirect(request.getContextPath() + ppurl + "?t=" + System.currentTimeMillis()+"=no");
                            }
                            break;
                        case "开具中":
                            //开具中对应的url
                            response.sendRedirect(request.getContextPath() + "/QR/zzkj.html?t=" + System.currentTimeMillis());
                            break;
                        default://已经开过票的
                            if(status.indexOf("pdf")!=-1){
                                String pdf = status.split("[+]")[0];
                                String je = status.split("[+]")[1];
                                String orderTime = status.split("[+]")[2];
                                String kplsh = status.split("[+]")[3];
                                String img = pdf.replace("pdf", "jpg");
                                if(WeixinUtils.isWeiXinBrowser(request)){
                                    WxFpxx wxFpxxByTqm = wxfpxxJpaDao.selsetByOrderNo(orderNo);
                                    if(null == wxFpxxByTqm){
                                        logger.info("已经开过票的存入微信发票详情--");
                                        WxFpxx Wxfpxx = new WxFpxx();
                                        Wxfpxx.setTqm(ppdm+orderNo);
                                        Wxfpxx.setGsdm(gsdm);
                                        Wxfpxx.setQ(q);
                                        Wxfpxx.setOpenId(openid);
                                        Wxfpxx.setOrderNo(orderNo);
                                        Wxfpxx.setWxtype("2");
                                        Wxfpxx.setKplsh(kplsh);
                                        logger.info("已完成开票之后，微信扫码直接归入卡包--信息"+Wxfpxx.getTqm()+"----公司代码"+gsdm+"----q值"+
                                                q+
                                                "------订单编号"+Wxfpxx.getOrderNo()+"------发票类型"+Wxfpxx.getWxtype());
                                        try {
                                            wxfpxxJpaDao.save(Wxfpxx);
                                        }catch (Exception e){
                                            logger.info("交易信息保存失败");
                                            return ;
                                        }
                                    }else {
                                        wxFpxxByTqm.setTqm(ppdm+orderNo);
                                        wxFpxxByTqm.setGsdm(gsdm);
                                        wxFpxxByTqm.setQ(q);
                                        wxFpxxByTqm.setOpenId(openid);
                                        wxFpxxByTqm.setOrderNo(orderNo);
                                        wxFpxxByTqm.setWxtype("2");//1:申请开票2：领取发票
                                        wxFpxxByTqm.setKplsh(kplsh);
                                        if(wxFpxxByTqm.getCode()!=null||!"".equals(wxFpxxByTqm.getCode())){
                                            String notNullCode= wxFpxxByTqm.getCode();
                                            wxFpxxByTqm.setCode(notNullCode);
                                        }
                                        try {
                                            wxfpxxJpaDao.save(wxFpxxByTqm);
                                        }catch (Exception e){
                                            logger.info("交易信息保存失败");
                                            return ;
                                        }
                                    }

                                }
                                logger.info("跳转的url--"+"/QR/scan.html?t=" + System.currentTimeMillis()
                                        + "=" + img + "=" + orderNo + "=" +je + "=" + orderTime);
                                //有pdf对应的url
                                response.sendRedirect(request.getContextPath() + "/QR/scan.html?t=" + System.currentTimeMillis()
                                        + "=" + img + "=" + orderNo + "=" +je + "=" + orderTime);
                            }else{
                                //无pdf对应的url
                                response.sendRedirect(request.getContextPath() + "/QR/error.html?t=" + System.currentTimeMillis() + "=GET_PDF_ERROR");
                            }
                    }
                }else{
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
