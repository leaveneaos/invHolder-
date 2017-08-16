package com.rjxx.taxeasy.utils.alipay;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.InvoiceTitleModel;
import com.alipay.api.request.AlipayEbppInvoiceSycnRequest;
import com.alipay.api.request.AlipayEbppInvoiceTitleListGetRequest;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.response.AlipayEbppInvoiceSycnResponse;
import com.alipay.api.response.AlipayEbppInvoiceTitleListGetResponse;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.domains.Kpspmx;
import com.rjxx.taxeasy.vo.InvoiceTitleVo;
import com.rjxx.utils.HtmlUtils;
import com.rjxx.utils.StringUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 支付宝工具类
 * Created by ZhangBing on 2017-06-26.
 */
public class AlipayUtils {

    private  static Logger logger = LoggerFactory.getLogger(AlipayUtils.class);

    /**
     * 判断是不是支付宝浏览器
     *
     * @param request
     * @return
     */
    public static boolean isAlipayBrowser(HttpServletRequest request) {
        String ua = request.getHeader("user-agent").toLowerCase();
        boolean res = ua.contains("alipay");
        return res;
    }

    /**
     * 判断是否已经进行支付宝授权
     *
     * @param session
     * @return
     */
    public static boolean isAlipayAuthorized(HttpSession session) throws AlipayApiException{
        String userId = (String) session.getAttribute(AlipayConstants.ALIPAY_USER_ID);
        String expires_in = (String) session.getAttribute("expires_in");
        String re_expires_in = (String) session.getAttribute("re_expires_in");
        if (StringUtils.isNotBlank(userId)) {
            refreshToken(session);
            return true;
        }
        return false;
    }
    public static  void refreshToken(HttpSession session) throws AlipayApiException {
        String refreshToken = (String) session.getAttribute("refresh_token");
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConstants.GATEWAY_URL, AlipayConstants.APP_ID, AlipayConstants.PRIVATE_KEY, AlipayConstants.FORMAT, AlipayConstants.CHARSET, AlipayConstants.ALIPAY_PUBLIC_KEY, AlipayConstants.SIGN_TYPE);
        AlipaySystemOauthTokenRequest alipaySystemOauthTokenRequest = new AlipaySystemOauthTokenRequest();
        alipaySystemOauthTokenRequest.setRefreshToken(refreshToken);
        alipaySystemOauthTokenRequest.setGrantType("refresh_token");
        AlipaySystemOauthTokenResponse oauthTokenResponse = alipayClient.execute(alipaySystemOauthTokenRequest);
        session.setAttribute(AlipayConstants.ALIPAY_ACCESS_TOKEN, oauthTokenResponse.getAccessToken());
        session.setAttribute(AlipayConstants.ALIPAY_USER_ID, oauthTokenResponse.getUserId());
        session.setAttribute("refresh_token",oauthTokenResponse.getRefreshToken());
        logger.info(JSON.toJSONString(oauthTokenResponse)+"end application");
    }
    /**
     * 初始化支付宝授权
     *
     * @param request
     * @param response
     * @param returnUrl
     */
    public static void initAlipayAuthorization(HttpServletRequest request, HttpServletResponse response, String returnUrl) throws Exception {
        String redirectUrl = AlipayConstants.AUTH_URL.replace("ENCODED_URL", java.net.URLEncoder.encode(HtmlUtils.finishedUrl(request, AlipayConstants.AFTER_ALIPAY_AUTHORIZED_REDIRECT_URL), "UTF-8"));
        redirectUrl += "&state=" + Base64.encodeBase64String(returnUrl.getBytes("UTF-8"));
        logger.info(redirectUrl);
        response.sendRedirect(redirectUrl);
    }

    /**
     * 获取支付宝发票抬头
     *
     * @param session
     * @return
     */
    public static List<InvoiceTitleVo> getAlipayInvoiceTitleList(HttpSession session) throws Exception {
        String accessToken = (String) session.getAttribute(AlipayConstants.ALIPAY_ACCESS_TOKEN);
        if (StringUtils.isBlank(accessToken)) {
            throw new Exception("alipay not authorized!!!");
        }
        System.out.println(accessToken);
        String userId = (String) session.getAttribute(AlipayConstants.ALIPAY_USER_ID);
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConstants.GATEWAY_URL, AlipayConstants.APP_ID, AlipayConstants.PRIVATE_KEY, AlipayConstants.FORMAT, AlipayConstants.CHARSET, AlipayConstants.ALIPAY_PUBLIC_KEY, AlipayConstants.SIGN_TYPE);
        AlipayEbppInvoiceTitleListGetRequest request = new AlipayEbppInvoiceTitleListGetRequest();
        request.setBizContent("{" +
                "\"user_id\":\"" + userId + "\"" +
                "  }");
        AlipayEbppInvoiceTitleListGetResponse response = alipayClient.execute(request, accessToken);
        if (response.isSuccess()) {
            List<InvoiceTitleModel> invoiceTitleModelList = response.getTitleList();
            if (invoiceTitleModelList != null && !invoiceTitleModelList.isEmpty()) {
                List<InvoiceTitleVo> voList = new ArrayList<>();
                for (InvoiceTitleModel model : invoiceTitleModelList) {
                    InvoiceTitleVo vo = new InvoiceTitleVo();
                    vo.setDefault(model.getIsDefault());
                    vo.setOpenBankAccount(model.getOpenBankAccount());
                    vo.setOpenBankName(model.getOpenBankName());
                    vo.setTaxRegisterNo(model.getTaxRegisterNo());
                    vo.setTitleName(model.getTitleName());
                    vo.setUserAddress(model.getUserAddress());
                    vo.setUserEmail(model.getUserEmail());
                    vo.setUserMobile(model.getUserMobile());
                    voList.add(vo);
                }
                return voList;
            }
        }
        return null;
    }

    /**
     * 同步发票到支付宝发票管家，成功返回支付宝的url，失败则返回null
     *
     * @param kpls
     * @param kpspmxList
     * @param mShortName
     * @param subMShortName
     * @return
     */
    public static String syncInvoice2Alipay(HttpSession session, Kpls kpls, List<Kpspmx> kpspmxList, String mShortName, String subMShortName) throws Exception {
        String accessToken = (String) session.getAttribute(AlipayConstants.ALIPAY_ACCESS_TOKEN);
        if (StringUtils.isBlank(accessToken)) {
            throw new Exception("alipay not authorized!!!");
        }
        String userId = (String) session.getAttribute(AlipayConstants.ALIPAY_USER_ID);
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConstants.GATEWAY_URL, AlipayConstants.APP_ID, AlipayConstants.PRIVATE_KEY, AlipayConstants.FORMAT, AlipayConstants.CHARSET, AlipayConstants.ALIPAY_PUBLIC_KEY, AlipayConstants.SIGN_TYPE);
        AlipayEbppInvoiceSycnRequest alipayEbppInvoiceSycnRequest = new AlipayEbppInvoiceSycnRequest();
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        AlipayBizObject alipayBizObject = new AlipayBizObject();
        alipayBizObject.setM_short_name(mShortName);
        alipayBizObject.setSub_m_short_name(subMShortName);
        InvoiceInfo invoiceInfo = new InvoiceInfo();
        List<InvoiceInfo> invoiceInfoList = new ArrayList<>();
        invoiceInfoList.add(invoiceInfo);
        alipayBizObject.setInvoice_info(invoiceInfoList);
        invoiceInfo.setUser_id(userId);
        invoiceInfo.setInvoice_code(kpls.getFpdm());
        invoiceInfo.setInvoice_no(kpls.getFphm());
        invoiceInfo.setRegister_no(kpls.getXfsh());
        invoiceInfo.setInvoice_amount(decimalFormat.format(kpls.getJshj()));
        invoiceInfo.setInvoice_date(DateFormatUtils.format(kpls.getKprq(), "yyyy-MM-dd"));
        List<InvoiceContent> invoiceContentList = new ArrayList<>();
        invoiceInfo.setInvoice_content(invoiceContentList);

        for (Kpspmx kpspmx : kpspmxList) {
            InvoiceContent invoiceContent = new InvoiceContent();
            invoiceContentList.add(invoiceContent);
            invoiceContent.setItem_name(kpspmx.getSpmc());
            invoiceContent.setItem_no(kpspmx.getSpdm());
            if (kpspmx.getSpdj() != null) {
                invoiceContent.setItem_price(decimalFormat.format(kpspmx.getSpdj()));
            }
            if (kpspmx.getSps() != null) {
                invoiceContent.setItem_quantity(kpspmx.getSps());
            }
            invoiceContent.setRow_type(Integer.valueOf(kpspmx.getFphxz()));
            invoiceContent.setItem_sum_price(decimalFormat.format(kpspmx.getSpje()));
            invoiceContent.setItem_tax_price(decimalFormat.format(kpspmx.getSpse()));
            invoiceContent.setItem_tax_rate(decimalFormat.format(kpspmx.getSpsl()));
            invoiceContent.setItem_unit(kpspmx.getSpdw());
            invoiceContent.setItem_amount(decimalFormat.format((kpspmx.getSpje() + kpspmx.getSpse())));
        }
        invoiceInfo.setOut_biz_no(kpls.getFpdm() + kpls.getFphm());
        invoiceInfo.setInvoice_type("blue");
        String pdfUrl = kpls.getPdfurl();
        String imgUrl = pdfUrl.replace(".pdf", ".jpg");
        invoiceInfo.setInvoice_img_url(imgUrl);
        InvoiceTitle invoiceTitle = new InvoiceTitle();
        invoiceInfo.setInvoice_title(invoiceTitle);
        invoiceTitle.setUser_id(userId);
        invoiceTitle.setTitle_name(kpls.getGfmc());
        if (StringUtils.isNotBlank(kpls.getGfsh())) {
            invoiceTitle.setTitle_type("CORPORATION");
        } else {
            invoiceTitle.setTitle_type("PERSONAL");
        }
        invoiceTitle.setUser_mobile(kpls.getGfdh());
        invoiceTitle.setLogon_id("");
        invoiceTitle.setUser_email("");
        invoiceTitle.setIs_default(false);
        invoiceTitle.setTax_register_no(kpls.getGfsh());
        invoiceTitle.setUser_address(kpls.getGfdz());
        invoiceTitle.setOpen_bank_name(kpls.getGfyh());
        invoiceTitle.setOpen_bank_account(kpls.getGfyhzh());
        invoiceInfo.setInvoice_file_data("");
        invoiceInfo.setInvoice_fake_code(kpls.getJym());
        invoiceInfo.setOut_invoice_id(kpls.getFpdm() + kpls.getFphm());
        invoiceInfo.setFile_download_type("pdf");
        invoiceInfo.setOriginal_blue_invoice_code("");
        invoiceInfo.setOriginal_blue_invoice_no("");
        invoiceInfo.setRegister_name(kpls.getXfmc());
        invoiceInfo.setRegister_phone_no(kpls.getXfdh());
        invoiceInfo.setRegister_address(kpls.getXfdz());
        invoiceInfo.setExtend_fields("");
        invoiceInfo.setInvoice_operator(kpls.getKpr());
        invoiceInfo.setFile_download_url(pdfUrl);
        invoiceInfo.setTax_amount(decimalFormat.format(kpls.getHjse()));
        invoiceInfo.setSum_amount(decimalFormat.format(kpls.getJshj()));
        if ("12".equals(kpls.getFpzldm())) {
            invoiceInfo.setTax_type("PLAIN");
        } else if ("01".equals(kpls.getFpzldm())) {
            invoiceInfo.setTax_type("SPECIAL");
        } else if ("02".equals(kpls.getFpzldm())) {
            invoiceInfo.setTax_type("PLAIN_INVOICE");
        }
        invoiceInfo.setRegister_bank_name(kpls.getXfyh());
        invoiceInfo.setRegister_bank_account(kpls.getXfyhzh());
        ObjectMapper objectMapper = new ObjectMapper();
        String result = objectMapper.writeValueAsString(alipayBizObject);
        result = result.replace("_default", "is_default");
        alipayEbppInvoiceSycnRequest.setBizContent(result);
        AlipayEbppInvoiceSycnResponse response = alipayClient.execute(alipayEbppInvoiceSycnRequest);
        if (response.isSuccess()) {
            return response.getUrl();
        } else {
            return null;
        }
    }

}
