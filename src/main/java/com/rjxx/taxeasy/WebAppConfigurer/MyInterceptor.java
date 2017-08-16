package com.rjxx.taxeasy.WebAppConfigurer;

import com.alibaba.fastjson.JSON;
import com.rjxx.taxeasy.utils.alipay.AlipayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Created by xlm on 2017/8/16.
 */
public class MyInterceptor implements HandlerInterceptor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HttpSession session=request.getSession();
        String[] url=request.getServletPath().split("/");
        logger.info("-----初始化URL----end-----"+JSON.toJSONString(url));
        if(url.length<3){
            if (AlipayUtils.isAlipayBrowser(request)) {
                logger.info("---------判断是否是支付宝浏览器------");
                if (!AlipayUtils.isAlipayAuthorized(session)) {
                    logger.info("-----初始化支付宝授权----strat-----");
                    AlipayUtils.initAlipayAuthorization(request, response, request.getServletPath());
                    logger.info("-----初始化支付宝授权----end------");
                    logger.info("-----初始化URL----end-----"+request.getServletPath());
                    return;
                }
            }
        }
        logger.info("-----初始化URL----end-----"+request.getServletPath());
    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
