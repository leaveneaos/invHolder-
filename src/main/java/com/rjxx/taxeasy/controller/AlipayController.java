package com.rjxx.taxeasy.controller;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.domains.Kpspmx;
import com.rjxx.taxeasy.domains.Pp;
import com.rjxx.taxeasy.service.KplsService;
import com.rjxx.taxeasy.service.KpspmxService;
import com.rjxx.taxeasy.service.PpService;
import com.rjxx.taxeasy.utils.alipay.AlipayConstants;
import com.rjxx.taxeasy.utils.alipay.AlipayUtils;
import com.rjxx.utils.HtmlUtils;
import com.rjxx.utils.StringUtils;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017-06-26.
 */
@Controller
public class AlipayController extends BaseController {

    @Autowired
    private KplsService kplsService;

    @Autowired
    private KpspmxService kpspmxService;

    @Autowired
    private PpService ppService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 获取阿里授权
     */
    @RequestMapping(value = AlipayConstants.AFTER_ALIPAY_AUTHORIZED_REDIRECT_URL,method = RequestMethod.GET)
    @ResponseBody
    public String getAlipay(String state, String auth_code) throws Exception {
        //获取access_token
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConstants.GATEWAY_URL, AlipayConstants.APP_ID, AlipayConstants.PRIVATE_KEY, AlipayConstants.FORMAT, AlipayConstants.CHARSET, AlipayConstants.ALIPAY_PUBLIC_KEY, AlipayConstants.SIGN_TYPE);
        AlipaySystemOauthTokenRequest alipaySystemOauthTokenRequest = new AlipaySystemOauthTokenRequest();
        alipaySystemOauthTokenRequest.setCode(auth_code);
        logger.info(JSON.toJSONString(auth_code+"-----start application-------"));
        alipaySystemOauthTokenRequest.setGrantType("authorization_code");
        try {
            AlipaySystemOauthTokenResponse oauthTokenResponse = alipayClient.execute(alipaySystemOauthTokenRequest);
            session.setAttribute(AlipayConstants.ALIPAY_ACCESS_TOKEN, oauthTokenResponse.getAccessToken());//访问令牌
            session.setAttribute(AlipayConstants.ALIPAY_USER_ID, oauthTokenResponse.getUserId());//用户id
            logger.info("--------Aipay_User_Id-----------"+oauthTokenResponse.getUserId());
            session.setAttribute("refresh_token",oauthTokenResponse.getRefreshToken());//刷新令牌
            session.setAttribute("expires_in",oauthTokenResponse.getExpiresIn());//过期时间
            session.setAttribute("re_expires_in",oauthTokenResponse.getReExpiresIn());//刷新令牌时间
            //refreshToken(oauthTokenResponse.getRefreshToken());
            String returnUrl = new String(Base64.decodeBase64(state), "UTF-8");
            String redirectUrl = HtmlUtils.finishedUrl(request, returnUrl);
            logger.info(JSON.toJSONString(oauthTokenResponse)+"-------end application---------");
            response.sendRedirect(redirectUrl);
            logger.info("-------URL---------"+redirectUrl+"-------URL---------");
            return null;
        } catch (Exception e) {
            //处理异常
            logger.error("Get Ali Access_token error", e);
            request.getSession().setAttribute("msg", "获取支付宝授权出现异常!");
            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            return null;
        }

    }

    private void refreshToken(String refreshToken) throws AlipayApiException {
        logger.info("--------刷新令牌----Start application---------");
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConstants.GATEWAY_URL, AlipayConstants.APP_ID, AlipayConstants.PRIVATE_KEY, AlipayConstants.FORMAT, AlipayConstants.CHARSET, AlipayConstants.ALIPAY_PUBLIC_KEY, AlipayConstants.SIGN_TYPE);
        AlipaySystemOauthTokenRequest alipaySystemOauthTokenRequest = new AlipaySystemOauthTokenRequest();
        alipaySystemOauthTokenRequest.setRefreshToken(refreshToken);
        alipaySystemOauthTokenRequest.setGrantType("refresh_token");
        AlipaySystemOauthTokenResponse oauthTokenResponse = alipayClient.execute(alipaySystemOauthTokenRequest);
        session.setAttribute(AlipayConstants.ALIPAY_ACCESS_TOKEN, oauthTokenResponse.getAccessToken());
        session.setAttribute(AlipayConstants.ALIPAY_USER_ID, oauthTokenResponse.getUserId());
        session.setAttribute("refresh_token",oauthTokenResponse.getRefreshToken());
        logger.info(JSON.toJSONString(oauthTokenResponse)+"--------刷新令牌----end application---------");
    }

    /**
     * 将发票信息同步到支付宝
     *
     * @return
     */
    @RequestMapping(value = "/syncAlipay")
    @ResponseBody
    public String syncAlipay(@RequestParam(required = false) String serialorder) throws Exception {
        if (serialorder == null) {
            Object serialorderObject = session.getAttribute("serialorder");
            if (serialorderObject == null) {
                request.getSession().setAttribute("msg", "会话超时，请重新开始操作!");
                response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                return null;
            }
            serialorder = serialorderObject.toString();
        }
        //判断是否是支付宝内
        if (!AlipayUtils.isAlipayBrowser(request)) {
            request.getSession().setAttribute("msg", "请使用支付宝进行该操作");
            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            return null;
        }
        if (!AlipayUtils.isAlipayAuthorized(session)) {
            AlipayUtils.initAlipayAuthorization(request, response, "/syncAlipay");
            return null;
        }
        Map params = new HashMap();
        params.put("serialorder", serialorder);
        List<Kpls> kplsList = kplsService.findAll(params);
        String redirectUrl = null;
        for (Kpls kpls : kplsList) {
            int kplsh = kpls.getKplsh();
            Map params2 = new HashMap();
            params2.put("kplsh", kplsh);
            List<Kpspmx> kpspmxList = kpspmxService.findMxNewList(params2);
            Pp pp = ppService.findOnePpByGsdmSkpid(kpls.getSkpid(), kpls.getGsdm());
            if (pp == null || StringUtils.isBlank(pp.getAliMShortName()) || StringUtils.isBlank(pp.getAliSubMShortName())) {
                request.getSession().setAttribute("msg", "该商户没有注册到支付宝发票管家");
                response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                return null;
            }
            redirectUrl = AlipayUtils.syncInvoice2Alipay(session, kpls, kpspmxList, pp.getAliMShortName(), pp.getAliSubMShortName());
            if (redirectUrl == null) {
                request.getSession().setAttribute("msg", "将发票归集到支付宝发票管家出现异常");
                response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
                return null;
            }
        }
        if (redirectUrl == null) {
            request.getSession().setAttribute("msg", "将发票归集到支付宝发票管家出现异常");
            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            return null;
        } else {
            response.sendRedirect(redirectUrl);
            return null;
        }
    }
}
