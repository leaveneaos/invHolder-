package com.rjxx.comm.utils;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Administrator on 2016/4/10.
 */
public class RequestUtils {

    /**
     * 生成完全路径的请求上下文
     *
     * @param request
     * @return
     */
    public static String getContextPath(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }

    /**
     * 获取域路径
     *
     * @param request
     * @return
     */
    public static String getDomainPath(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
    }

}
