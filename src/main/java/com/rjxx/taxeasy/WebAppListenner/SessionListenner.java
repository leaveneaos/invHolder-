package com.rjxx.taxeasy.WebAppListenner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionListener;
import javax.servlet.http.HttpSessionEvent;

/**
 * Created by Administrator on 2017-09-28.
 */

public class SessionListenner implements HttpSessionListener {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static int activeSessions =0;
    /* Session创建事件 */
    public void sessionCreated(HttpSessionEvent se) {
        ServletContext ctx = se.getSession().getServletContext( );
        Integer numSessions = (Integer) ctx.getAttribute("numSessions");
        if (numSessions == null) {
            numSessions = new Integer(1);
        }
        else {
            int count = numSessions.intValue( );
            numSessions = new Integer(count + 1);
        }
        ctx.setAttribute("numSessions", numSessions);
    }
    /* Session失效事件 */
    public void sessionDestroyed(HttpSessionEvent se) {
        ServletContext ctx = se.getSession().getServletContext();
        Integer numSessions = (Integer)ctx.getAttribute("numSessions");
        if(numSessions == null){
            numSessions = new Integer(0);
        }else{
            int count = numSessions.intValue( );
            numSessions = new Integer(count - 1);
        }
        ctx.setAttribute("numSessions", numSessions);


}
}