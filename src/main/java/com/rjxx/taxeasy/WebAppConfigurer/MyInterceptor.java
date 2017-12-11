package com.rjxx.taxeasy.WebAppConfigurer;

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
        HttpSession session=request.getSession();
//        long l1 = System.currentTimeMillis();
//        long l2 = session.getCreationTime();
//        if((System.currentTimeMillis()-session.getCreationTime())/1000 > 30*60){
//            logger.info("session过期了");
//            response.sendRedirect(request.getContextPath() + "/QR/error.html?t=" + System.currentTimeMillis() + "=session_out");
//            return true;
//        }
        String url=request.getServletPath();
        logger.info("-----初始化URL----Start-----"+url);
        if(url.equals("/fm")||url.equals("/af")||url.equals("/barcode/chamate")||url.equals("/barcode/dicos")||
                url.equals("/mb") || url.equals("/dzfp_sqj")||url.equals("/tq")){
            if (AlipayUtils.isAlipayBrowser(request)) {
                logger.info("---------判断是否是支付宝浏览器------");
                if (!AlipayUtils.isAlipayAuthorized(session)) {
                    logger.info("-----初始化支付宝授权----strat-----");
                    String q=request.getParameter("q");
                    String g = request.getParameter("g");
                    String t = request.getParameter("t");

                    if(q!= null && g!= null && t==null ){
                        AlipayUtils.initAlipayAuthorization(request, response, request.getServletPath()+"?g="+g+"&q="+q);
                    }else if(q!= null && (g==null &&t==null)){
                        AlipayUtils.initAlipayAuthorization(request, response, request.getServletPath()+"?q="+q);
                    }else if(g != null && (q==null && t==null)){
                        AlipayUtils.initAlipayAuthorization(request, response, request.getServletPath()+"?g="+g);
                    }else if(t != null && (q==null && g==null)){
                        AlipayUtils.initAlipayAuthorization(request, response, request.getServletPath()+"?t="+t);
                    }else if(t!= null && q!=null && g==null){
                        AlipayUtils.initAlipayAuthorization(request, response, request.getServletPath()+"?t="+t+"&q="+q);
                    }
                    logger.info("-----初始化支付宝授权----end------");
                    logger.info("-----初始化URL----end-----"+request.getServletPath());
                    return false;
                }
            }
        }
        logger.info("-----初始化URL----end-----"+request.getServletPath());
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
