package com.rjxx.comm.utils;

/**
 * Created by Administrator on 2016/9/23.
 */
public class HtmlUtils {

    /**
     * 忽略html标签
     *
     * @param htmlContent
     * @return
     */
    public static String ignoreHtmlTag(String htmlContent) {
        return htmlContent.replaceAll("<(!|/)?(.|\\n)*?>", "").replaceAll("&nbsp;", "");
    }

}
