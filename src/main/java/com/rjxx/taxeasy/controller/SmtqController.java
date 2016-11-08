package com.rjxx.taxeasy.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.rjxx.comm.utils.MD5Util;
import com.rjxx.comm.web.BaseController;
import com.rjxx.taxeasy.domains.Gsxx;
import com.rjxx.taxeasy.domains.Jyls;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.domains.Smtq;
import com.rjxx.taxeasy.domains.Tqjl;
import com.rjxx.taxeasy.service.GsxxService;
import com.rjxx.taxeasy.service.JylsService;
import com.rjxx.taxeasy.service.KplsService;
import com.rjxx.taxeasy.service.SmtqService;
import com.rjxx.taxeasy.service.TqjlService;

@Controller
@RequestMapping("/dzfp_sqj")
public class SmtqController extends BaseController {

	@Autowired
	private GsxxService gsxxservice;
	@Autowired
	private SmtqService smtqService;
	@Autowired
	private JylsService jylsService;
	@Autowired
	private KplsService kplsService;
	@Autowired
	private TqjlService tqjlService;
	@RequestMapping
	public String index() throws Exception {
		String str = request.getParameter("q");
		byte[] bt = null;
		try {
			sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();
			bt = decoder.decodeBuffer(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String csc = new String(bt);
		String[] cssz = csc.split("&");
		String orderNo = cssz[0].substring(cssz[0].lastIndexOf("=") + 1);
		String orderTime = cssz[1].substring(cssz[1].lastIndexOf("=") + 1);
		String price = cssz[2].substring(cssz[2].lastIndexOf("=") + 1);
		String sign = cssz[3].substring(cssz[3].lastIndexOf("=") + 1);
		String dbs = csc.substring(0,csc.lastIndexOf("&")+1);
		Map params = new HashMap<>();
		params.put("gsdm", "sqj");
		Gsxx gsxx = gsxxservice.findOneByParams(params);
		if (null==gsxx) {
			return "smtq/demo";
		}
		dbs+="key="+gsxx.getSecretKey();
		String key1 = MD5Util.generatePassword(dbs);

		if (!sign.equals(key1.toLowerCase())) {
			return "smtq/demo";
		}
		if (null==orderNo||"".equals(orderNo)) {
			return "smtq/demo";
		}
		if (null==orderTime||"".equals(orderTime)) {
			return "smtq/demo";
		}
		if (null==price||"".equals(price)) {
			return "smtq/demo";
		}
		request.getSession().setAttribute("orderNo", orderNo);
		request.getSession().setAttribute("orderTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new SimpleDateFormat("yyyyMMddHHmmss").parse(orderTime)));
		request.getSession().setAttribute("price", price);
    	String ddh = (String) request.getSession().getAttribute("orderNo"); 
		Map map = new HashMap<>();
		map.put("ddh",ddh);
		Smtq smtq1 = smtqService.findOneByParams(map);
		if (null!=smtq1&&smtq1.getFpzt().equals("07")) {
			  return "redirect:dzfp_sqj/smtq3"; 
		}
		if (null!=smtq1&&smtq1.getFpzt().equals("04")) {
			 Map map2 = new HashMap<>();
	         map.put("ddh", ddh);
	         Jyls jyls = jylsService.findByTqm(map);
			  Kpls kpls = new Kpls();
              kpls.setDjh(jyls.getDjh());
              List<Kpls> list = kplsService.findByDjh(kpls);
              String pdfdzs = "";
              boolean falg = false;
              String msg="";
              for (Kpls kpls2 : list) {
              	if (kpls2.getFpztdm().equals("01")) {
              		msg="您提取的发票存在部分红冲情况!";
						falg=true;
						break;
					}
              	if (kpls2.getFpztdm().equals("02")) {
              		msg="您提取的发票含有已红冲发票!";
						falg=true;
						break;
					}
              	if (kpls2.getFpztdm().equals("03")) {
              		msg="您提取的发票含有已换开发票!";
						falg=true;
						break;
					}
              	if (kpls2.getFpztdm().equals("05")) {
              		msg="您提取的发票开具失败!";
						falg=true;
						break;
					}
                  pdfdzs += kpls2.getPdfurl().replace(".pdf", ".jpg") + ",";
              }
              if (falg) {
            	  return "smtq/demo";
				}
              if (pdfdzs.length() > 0) {
            	  request.getSession().setAttribute("djh", jyls.getDjh());
                  request.getSession().setAttribute("pdfdzs",  pdfdzs.substring(0, pdfdzs.length() - 1));
              }
              Tqjl tqjl = new Tqjl();
              tqjl.setDjh(String.valueOf(jyls.getDjh()));
              tqjl.setTqsj(new Date());
              String visiterIP=request.getRemoteAddr();//访问者IP  
              tqjl.setIp(visiterIP);
              String llqxx =request.getHeader("User-Agent");
              tqjl.setLlqxx(llqxx);
              tqjlService.save(tqjl);
			return "smtq/fp";
		}
		return "smtq/smtq1";
	}
	 @RequestMapping(value = "/smtq3")
	 public String tztz(){
		 return "smtq/smtq3"; 
	 }
	 @RequestMapping(value = "/smtq2")
	 public String smtq2(){
		 return "smtq/smtq2"; 
	 }
    @RequestMapping(value = "/getSmsj")
    @ResponseBody
	public Map getSmsj(){
    	Map<String, Object> result = new HashMap<String, Object>();
		result.put("orderNo", request.getSession().getAttribute("orderNo"));
		result.put("orderTime", request.getSession().getAttribute("orderTime"));
		result.put("price", request.getSession().getAttribute("price"));
		return result;
	}
    @RequestMapping(value = "/save")
    @ResponseBody
    public Map save(String fptt,String nsrsbh,String dz,String dh,String khh,String khhzh,String yx,String sj) throws Exception{
    	Map<String , Object> result = new HashMap<>();
    	String ddh = (String) request.getSession().getAttribute("orderNo"); 
    	if ("".equals(ddh)) {
			result.put("msg", "1");
			return result;
		}
		Map params = new HashMap<>();
		params.put("gsdm", "sqj");
		Gsxx gsxx = gsxxservice.findOneByParams(params);
		Map map = new HashMap<>();
		map.put("ddh",ddh);
		Smtq smtq1 = smtqService.findOneByParams(map);
		Smtq smtq;
		if (null!=smtq1) {
			 smtq = smtq1;
		}else{
			smtq = new Smtq();	
		}
		smtq.setDdh((String) request.getSession().getAttribute("orderNo"));
		smtq.setJylssj(new SimpleDateFormat("yyyyMMddHHmmss").parse((String) request.getSession().getAttribute("orderTime")));
		smtq.setZje(Double.parseDouble((String) request.getSession().getAttribute("price")));
		smtq.setGfmc(fptt);
		smtq.setNsrsbh(nsrsbh);
		smtq.setDz(dz);
		smtq.setDh(ddh);
		smtq.setKhh(khh);
		smtq.setKhhzh(khhzh);
		smtq.setYx(yx);
		smtq.setSj(sj);
		smtq.setFpzt("07");
		smtq.setYxbz("1");
		smtq.setGsdm(gsxx.getGsdm());
		smtq.setLrsj(new Date());
		smtqService.save(smtq);
		result.put("msg", "2");
		return result;
    }
    @RequestMapping(value ="/getZje")
    @ResponseBody
    public Map getZje(){
    	Map<String , Object> result = new HashMap<>();
    	result.put("zje", request.getSession().getAttribute("price"));
    	return result;
    }
	@RequestMapping(value = "/fpsession")
	@ResponseBody
	public Map fpsession(){
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("djh", request.getSession().getAttribute("djh"));
		result.put("pdfdz", request.getSession().getAttribute("pdfdzs"));
		return result;
	}
	public static void main(String[] args) {
		String str = "b3JkZXJObz0yMDE2MTAxMzEyNTUxMTEyMzQmb3JkZXJUaW1lPTIwMTYxMDEzMTI1NTExJnByaWNlPTIzJnNpZ249YjBjODdjY2U4NmE0ZGZlYmVkYzA1ZDgzZTdmNzY3OTA=";
		byte[] bt = null;
		try {
			sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();
			bt = decoder.decodeBuffer(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String csc = new String(bt);
		System.out.println(csc);
	}

}
