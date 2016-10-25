package com.rjxx.comm.utils;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
/**
 * 读取配置文件的信息
 * @author k
 */
public class ReadProperties {
	public static String read(String s) throws IOException{
		URL url =Thread.currentThread().getContextClassLoader().getResource("url.properties");
		Properties p = new Properties(); 
		p.load(url.openStream());		
		String rs = (String) p.get(s); 					
		return rs; 
	}	
}
