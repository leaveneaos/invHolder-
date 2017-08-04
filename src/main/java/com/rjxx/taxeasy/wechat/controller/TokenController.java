package com.rjxx.taxeasy.wechat.controller;

import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.comm.SigCheck;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * Created by Administrator on 2017/8/4 0004.
 */
@RestController
@RequestMapping("/wechat")
public class TokenController extends BaseController {

    @RequestMapping(value = "/token",method = RequestMethod.GET)
    public void getToken() {
        String sign = request.getParameter("signature");
        String times = request.getParameter("timestamp");
        String nonce = request.getParameter("nonce");
        String echo = request.getParameter("echostr");
        if (SigCheck.checkSignature(sign, times, nonce)) {
            try {
                response.getOutputStream().print(request.getParameter("echostr"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            logger.info("isSuccess:" + echo);
        }
    }
}
