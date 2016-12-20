package com.rjxx.taxeasy.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;

import com.rjxx.taxeasy.comm.SigCheck;

public class CoreServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	 public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {  
	        // 微信加密签名  
	        String signature = request.getParameter("signature");  
	        // 时间戳  
	        String timestamp = request.getParameter("timestamp");  
	        // 随机数  
	        String nonce = request.getParameter("nonce");  
	        // 随机字符串  
	        String echostr = request.getParameter("echostr");  
	  
	        PrintWriter out = response.getWriter();  
	        // 通过检验signature对请求进行校验，若校验成功则原样返回echostr，表示接入成功，否则接入失败  
	        if (SigCheck.checkSignature(signature, timestamp, nonce)) {  
	            response.getOutputStream().print(request.getParameter("echostr"));
	            LoggerFactory.getLogger(this.getClass()).error("isSuccess:" + echostr);
	        }  
	        out.close();  
	        out = null;  
	    }  
	  
	    /** 
	     * 处理微信服务器发来的消息 
	     */  
	    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {  
	        doGet(request, response);  
	    }  
	

}
