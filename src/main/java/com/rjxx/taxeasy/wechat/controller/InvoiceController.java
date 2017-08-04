package com.rjxx.taxeasy.wechat.controller;

import com.alibaba.fastjson.JSONObject;
import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.wechat.dto.Result;
import com.rjxx.taxeasy.wechat.service.InvoiceService;
import com.rjxx.taxeasy.wechat.util.HttpClientUtil;
import com.rjxx.taxeasy.wechat.util.ResultUtil;
import com.rjxx.utils.HtmlUtils;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * Created by Administrator on 2017/7/31 0031.
 */
@RestController
@RequestMapping("/wechat")
public class InvoiceController extends BaseController {

    @Autowired
    private InvoiceService invoiceService;

    @Value("${rjxx.appid}")
    private String APP_ID;
    @Value("${rjxx.secret}")
    private String SECRET ;

    @RequestMapping(value = "/invoice", method = RequestMethod.POST)
    @ApiOperation(value = "接收抬头")
    public Result send(@RequestParam String purchaserName,
                       @RequestParam String purchaserTaxNo,
                       @RequestParam String email,
                       @RequestParam Double amount,
                       @RequestParam String user,
                       @RequestParam String id) {
        String username = "";
        String openid = "";
        //如果前台传值
        if (StringUtils.isNotBlank(user) && StringUtils.isNotBlank(id)
                &&!"undefined".equals(user) && !"undefined".equals(id)) {
            username =user;
            openid = id;

            //如果不传值
        } else {
            //如果session中没有
//            if (session.getAttribute("username") == null || session.getAttribute("openid") == null) {
//            return ResultUtil.error("redirect");
//        }else{
                username = (String) session.getAttribute("username");
                openid =
//                    (String) session.getAttribute("openid");
                        "openid";
//            }
        }
        String status = invoiceService.send(purchaserName, purchaserTaxNo, email, amount, username, openid);
        if ("-1".equals(status)) {
            return ResultUtil.error("发送失败");
        } else if ("0".equals(status)) {
            return ResultUtil.error("所需信息为空");
        } else {
            return ResultUtil.success(status);
        }
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ApiOperation(value = "登录")
    public Result login(@RequestParam String user,
                        @RequestParam String pass) {
        String status = invoiceService.login(user, pass);
        if ("1".equals(status)) {
            session.setMaxInactiveInterval(45 * 60);
            session.setAttribute("username", user);
            session.setAttribute("password", pass);
            return ResultUtil.success();
        } else {
            return ResultUtil.error("用户名不存在或密码错误");
        }
    }


    @RequestMapping(method = RequestMethod.GET)
    public String index() {
        String ua = request.getHeader("user-agent").toLowerCase();
        //判断是否是微信浏览器
        if (ua.indexOf("micromessenger") > 0) {
            String url = HtmlUtils.getBasePath(request);
            String openid = String.valueOf(session.getAttribute("openid"));
            if (openid == null || "null".equals(openid)) {
                String ul = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + APP_ID + "&redirect_uri="
                        + url + "wechat/getOpenid&" + "response_type=code&scope=snsapi_base&state=" + "wljqr"
                        + "#wechat_redirect";
                try {
                    response.sendRedirect(ul);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            } else {
                try {
                    response.sendRedirect(request.getContextPath() + "/Akey/login.html");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }
        //不是的话重定向到登录页面
        try {
            response.sendRedirect(request.getContextPath() + "/Akey/isnotwx.html");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    @RequestMapping(value="/getOpenid",method = RequestMethod.GET)
    public void getOpenId(String state, String code) {
        String turl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + APP_ID + "&secret="
                + SECRET + "&code=" + code + "&grant_type=authorization_code";
        String resultJson = HttpClientUtil.doGet(turl);
        JSONObject resultObject = JSONObject.parseObject(resultJson);
        String openid = resultObject.getString("openid");
        if (openid != null) {
            session.setAttribute("openid", openid);
        }
        try {
            response.sendRedirect(request.getContextPath() + "/Akey/login.html");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
