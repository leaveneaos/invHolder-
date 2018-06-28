package com.rjxx.taxeasy.comm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * Created by Administrator on 2016/8/18.
 */
public class BaseController {
    @Value("${web.url.error}")
    private String errorUrl;

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected HttpServletResponse response;

    @Autowired
    protected HttpSession session;


    public void errorRe(String errorName) {
        try {
            response.sendRedirect(errorUrl + "/" + URLEncoder.encode(errorName) + "?t=" + System.currentTimeMillis());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
