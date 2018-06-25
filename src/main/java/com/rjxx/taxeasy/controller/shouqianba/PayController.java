package com.rjxx.taxeasy.controller.shouqianba;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.dao.GsxxJpaDao;
import com.rjxx.taxeasy.domains.Cszb;
import com.rjxx.taxeasy.domains.Gsxx;
import com.rjxx.taxeasy.dto.shouqianba.PayResult;
import com.rjxx.taxeasy.service.CszbService;
import com.rjxx.taxeasy.service.shouqianba.PayService;
import com.rjxx.taxeasy.wechat.dto.Result;
import com.rjxx.taxeasy.wechat.util.ResultUtil;
import com.rjxx.utils.AESUtil;
import com.rjxx.utils.HtmlUtils;
import com.rjxx.utils.RandomValidateCodeUtil;
import com.rjxx.utils.dwz.ShortUrlUtil;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wangyahui
 * @email wangyahui@datarj.com
 * @company 上海容津信息技术有限公司
 * @date 2018/6/4
 */
@RestController
@RequestMapping("/pay")
@CrossOrigin
public class PayController extends BaseController {


    @Value("${pay.url.success}")
    private String succUrl;

    @Value("${pay.url.error}")
    private String errorUrl;

    @Autowired
    private PayService payService;
    @Autowired
    private GsxxJpaDao gsxxJpaDao;
    @Autowired
    private CszbService cszbService;

    @RequestMapping(value = "/start", method = RequestMethod.POST)
    @ApiOperation("获取收款码URL")
    public Result getPayUrl(@RequestParam String terminal_sn, @RequestParam String terminal_key,
                            @RequestParam String total_amount, @RequestParam String subject, @RequestParam String oprator,
                            @RequestParam String orderNo, @RequestParam String gsdm, @RequestParam String storeNo) {
        Map param = new HashMap();
        param.put("tn", terminal_sn);
        param.put("tk", terminal_key);
        param.put("pr", total_amount);
        param.put("sj", subject);
        param.put("op", oprator);
        param.put("on", orderNo);
        param.put("sn", storeNo);
        String dataJson = JSON.toJSONString(param);
        Gsxx gsxx = gsxxJpaDao.findOneByGsdm(gsdm);
        String q = null;
        try {
            q = AESUtil.encrypt(dataJson, gsxx.getSecretKey());
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.error("生成二维码失败PAY[URL]");
        }
        Map map = new HashMap<>();
        Cszb cszb = cszbService.getSpbmbbh(gsdm, null, null, "isStartDWZ");
        if ("是".equals(cszb.getCsz())) {
            try {
                String dwz = ShortUrlUtil.dwz(HtmlUtils.getBasePath(request) + "pay/" + gsdm + "/" + q);
                map.put("url", dwz);
            } catch (Exception e) {
                e.printStackTrace();
                return ResultUtil.error("短网址服务错误PAY[URL]");
            }
        } else {
            map.put("url", HtmlUtils.getBasePath(request) + "pay/" + gsdm + "/" + q);
        }
        return ResultUtil.success(map);
    }

    @RequestMapping(value = "/{gsdm}/{q}", method = RequestMethod.GET)
    @ApiIgnore
    public void payIn(@PathVariable String gsdm, @PathVariable String q) {
        Gsxx gsxx = gsxxJpaDao.findOneByGsdm(gsdm);
        if (gsxx == null) {
            errorRedirect("该公司不存在PAY[IN]");
            return;
        }
        String data = "";
        try {
            data = AESUtil.decrypt(q, gsxx.getSecretKey());
        } catch (Exception e) {
            e.printStackTrace();
            errorRedirect("验签失败PAY[IN]");
            return;
        }
        JSONObject jsonData = JSON.parseObject(data);
        String terminal_sn = jsonData.getString("tn");
        String terminal_key = jsonData.getString("tk");
        String total_amount = jsonData.getString("pr");
        String subject = jsonData.getString("sj");
        String oprator = jsonData.getString("op");
        String orderNo = jsonData.getString("on");
        String storeNo = jsonData.getString("sn");
        String returnUtl = HtmlUtils.getBasePath(request) + "pay/receive";
        Map payResult = payService.payIn(terminal_sn, terminal_key, total_amount, subject, oprator,
                returnUtl, orderNo, gsdm, storeNo);
        String errorMsg = (String) payResult.get("errorMsg");
        String url = (String) payResult.get("url");
        if (StringUtils.isNotBlank(errorMsg)) {
            errorRedirect(errorMsg);
            return;
        }
        try {
            response.sendRedirect(url);
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/receive", method = RequestMethod.GET)
    @ApiIgnore
    public void receive(PayResult payResult) {
        Map receive = payService.receive(payResult);
        String errorMsg = (String) receive.get("errorMsg");
        String gsdm = (String) receive.get("gsdm");
        String orderNo = (String) receive.get("orderNo");
        if (errorMsg != null) {
            errorRedirect(errorMsg);
            return;
        } else {
            try {
                response.sendRedirect(succUrl + "?gsdm=" + gsdm + "&orderNo=" + orderNo + "&t=" + System.currentTimeMillis());
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @RequestMapping(value = "/getPayOut", method = RequestMethod.POST)
    @ApiOperation("查询结果表")
    public Result getPayOut(String gsdm, String orderNo) {
        Map payOut = payService.getPayOut(gsdm, orderNo);
        if (payOut == null) {
            return ResultUtil.error("未查询到数据PAY[GET]");
        }
        return ResultUtil.success(payOut);
    }


    @RequestMapping(value = "/extract", method = RequestMethod.POST)
    @ApiOperation("根据商户号提票")
    public Result extract(String tradeNo, @RequestParam String code) {
        String sessionCode = (String) session.getAttribute(RandomValidateCodeUtil.RANDOMCODEKEY);
        if(StringUtils.isBlank(sessionCode)){
            return ResultUtil.error("会话过期请重试PAY[EX]");
        }
        if (code.equals(sessionCode)) {
            Map receive = payService.extract(tradeNo);
            String errorMsg = (String) receive.get("errorMsg");
            if (errorMsg != null) {
                return ResultUtil.error(errorMsg);
            } else {
                return ResultUtil.success(receive);
            }
        }else{
            return ResultUtil.error("验证码错误PAY[EX]");
        }
    }

    public void errorRedirect(String errorName) {
        try {
            response.sendRedirect(errorUrl + "/" + URLEncoder.encode(errorName) + "?t=" + System.currentTimeMillis());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
