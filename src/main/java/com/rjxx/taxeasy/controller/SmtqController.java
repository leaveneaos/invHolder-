package com.rjxx.taxeasy.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.rjxx.comm.utils.MD5Util;
import com.rjxx.comm.utils.StringUtils;
import com.rjxx.comm.web.BaseController;
import com.rjxx.taxeasy.domains.Cszb;
import com.rjxx.taxeasy.domains.Gsxx;
import com.rjxx.taxeasy.domains.Jyls;
import com.rjxx.taxeasy.domains.Jyspmx;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.domains.Skp;
import com.rjxx.taxeasy.domains.Smtq;
import com.rjxx.taxeasy.domains.Tqjl;
import com.rjxx.taxeasy.domains.Xf;
import com.rjxx.taxeasy.domains.Yh;
import com.rjxx.taxeasy.service.CszbService;
import com.rjxx.taxeasy.service.GsxxService;
import com.rjxx.taxeasy.service.JylsService;
import com.rjxx.taxeasy.service.JyspmxService;
import com.rjxx.taxeasy.service.KplsService;
import com.rjxx.taxeasy.service.SkpService;
import com.rjxx.taxeasy.service.SmtqService;
import com.rjxx.taxeasy.service.TqjlService;
import com.rjxx.taxeasy.service.XfService;
import com.rjxx.taxeasy.service.YhService;


@Controller
@RequestMapping("/dzfp_sqj")
public class SmtqController extends BaseController{

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
	@Autowired
	private CszbService cszbService;
	@Autowired
	private SkpService skpService;
	@Autowired
	private XfService xfService;
	@Autowired
	private YhService yhService;
	@Autowired
	private JyspmxService jyspmxService;
	@RequestMapping
	public String index() throws Exception {
		String str = request.getParameter("q");
		byte[] bt = null;
		try {
			sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();
			bt = decoder.decodeBuffer(str);
		
		String csc = new String(bt);
		String[] cssz = csc.split("&");
		String orderNo = cssz[0].substring(cssz[0].lastIndexOf("=") + 1);
		String orderTime = cssz[1].substring(cssz[1].lastIndexOf("=") + 1);
		String price = cssz[2].substring(cssz[2].lastIndexOf("=") + 1);
		String sn = cssz[3].substring(cssz[3].lastIndexOf("=") + 1);
		String sign = cssz[4].substring(cssz[4].lastIndexOf("=") + 1);
		String dbs = csc.substring(0,csc.lastIndexOf("&")+1);
		Map params = new HashMap<>();
		params.put("gsdm", "sqj");
		Gsxx gsxx = gsxxservice.findOneByParams(params);
		if (null==gsxx) {
			request.getSession().setAttribute("msg", "公司信息不正确!");
			return "redirect:/smtq/demo.html";
		}
		dbs+="key="+gsxx.getSecretKey();
		String key1 = MD5Util.generatePassword(dbs);

		if (!sign.equals(key1.toLowerCase())) {
			request.getSession().setAttribute("msg", "秘钥不匹配!");
			return "redirect:/smtq/demo.html";
		}
		if (null==orderNo||"".equals(orderNo)) {
			request.getSession().setAttribute("msg", "未包含流水号!");
			return "redirect:/smtq/demo.html";
		}
		if (null==sn||"".equals(sn)) {
			request.getSession().setAttribute("msg", "未包含门店信息!");
			return "redirect:/smtq/demo.html";
		}
		if (null==orderTime||"".equals(orderTime)) {
			request.getSession().setAttribute("msg", "未包含流水时间!");
			return "redirect:/smtq/demo.html";
		}
		if (null==price||"".equals(price)) {
			request.getSession().setAttribute("msg", "未包含金额!");
			return "redirect:/smtq/demo.html";
		}
		request.getSession().setAttribute("orderNo", orderNo);
		request.getSession().setAttribute("orderTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new SimpleDateFormat("yyyyMMddHHmmss").parse(orderTime)));
		request.getSession().setAttribute("price", price);
		request.getSession().setAttribute("sn", sn);
    	String ddh = (String) request.getSession().getAttribute("orderNo"); 
		Map map = new HashMap<>();
		map.put("ddh",ddh);
		Smtq smtq1 = smtqService.findOneByParams(map);
		if (null!=smtq1&&null!=smtq1.getId()) {
			  Jyls jyls = jylsService.findByTqm(map);
			  if (null!=jyls&&null!=jyls.getClztdm()) {
				if ("91".equals(jyls.getClztdm())) {
					Kpls kpls = new Kpls();
		              kpls.setDjh(jyls.getDjh());
		              List<Kpls> list = kplsService.findByDjh(kpls);
		              String pdfdzs = "";
		              boolean falg = false;
		              String msg="";
		              for (Kpls kpls2 : list) {
		                  pdfdzs += kpls2.getPdfurl().replace(".pdf", ".jpg") + ",";
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
		              return "redirect:/fp.html"; 
				}else{
					  request.getSession().setAttribute("clztdm", jyls.getClztdm());
				}
			}
			  return "redirect:dzfp_sqj/smtq3"; 
		}
		} catch (IOException e) {
			request.getSession().setAttribute("msg", e.getMessage());
			return "redirect:/smtq/demo.html";
		}
		 return "redirect:/smtq/smtq1.html"; 
	}
	 @RequestMapping(value = "/smtq3")
	 public String tztz(){
		 return "redirect:/smtq/smtq3.html"; 
	 }
	 @RequestMapping(value = "/demo")
	 public String demo(){
		 return "redirect:/smtq/demo.html"; 
	 }
	 @RequestMapping(value = "/smtq2")
	 public String smtq2(){
		 return "redirect:/smtq/smtq2.html"; 
	 }
	 @RequestMapping(value = "/bangzhu")
	 public String bangzhu(){
		 return "redirect:/bangzhu.html"; 
	 }
	 @RequestMapping(value = "/yxxg")
	 public String yxxg(){
		 return "redirect:/smtq/xgyx.html"; 
	 }
	 @RequestMapping(value = "/fp")
	 public String fp(){
		 return "redirect:/fp.html"; 
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
    @RequestMapping(value = "/xgyx")
    @ResponseBody
	public Map xgyx(String yx){
    	Map<String, Object> result = new HashMap<String, Object>();
    	Map params = new HashMap<>();
    	params.put("ddh", (String) request.getSession().getAttribute("orderNo"));
    	Smtq smtq = smtqService.findOneByParams(params);
    	smtq.setYx(yx);
    	smtqService.save(smtq);
    	result.put("num", "1");
		return result;
	}
    @RequestMapping(value = "/save")
    @ResponseBody
    @Transactional
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
		smtq.setKpddm((String) request.getSession().getAttribute("sn"));
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
		Cszb cszb = this.getCszb("sqj", null, null, "sflxkp");
		if (null!=cszb&&"是".equals(cszb.getCsz())) {
			Map map2  = new HashMap<>();
			map2.put("gsdm", "sqj");
			map2.put("kpddm", (String) request.getSession().getAttribute("sn"));
			Skp skp = skpService.findOneByParams(map2);
			if (null!=skp&&!"".equals(skp.getXfid())) {
				Xf xf = xfService.findOne(skp.getXfid());
				if (null!=xf) {
					Jyls jyls = new Jyls();
					jyls.setClztdm("01");
					jyls.setDdh((String) request.getSession().getAttribute("orderNo"));
					jyls.setGfsh(nsrsbh);

					jyls.setGfmc(fptt);
					jyls.setGfyh(khh);
					jyls.setGfyhzh(khhzh);
					jyls.setGflxr("");
					jyls.setBz("");
					jyls.setGfemail(yx);
					jyls.setGfdz(dz);
					String tqm = (String) request.getSession().getAttribute("orderNo");
					if (StringUtils.isNotBlank(tqm)) {
						Map params2 = new HashMap<>();
						params2.put("gsdm", "sqj");
						params2.put("tqm", tqm);
						Jyls tmp = jylsService.findByTqm(params2);
						if (tmp != null) {
							result.put("failure", true);
							result.put("xx", "离线开票失败,提取码已经存在");
							return result;
						}
					}
					jyls.setTqm(tqm);
					jyls.setJylsh("SQJ" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));
					jyls.setJshj(Double.parseDouble((String) request.getSession().getAttribute("price")));
					jyls.setYkpjshj(0.00);
					jyls.setHsbz("1");
					jyls.setXfid(xf.getId());
					jyls.setXfsh(xf.getXfsh());
					jyls.setXfmc(xf.getXfmc());
					jyls.setLrsj(new Date());
					jyls.setXgsj(new Date());
					jyls.setJylssj(new Date());
					jyls.setFpzldm("12");
					jyls.setFpczlxdm("11");
					jyls.setXfyh(xf.getXfyh());
					jyls.setXfyhzh(xf.getXfyhzh());
					jyls.setXfdz(xf.getXfdz());
					jyls.setXfdh(xf.getXfdh());
					if (StringUtils.isNotBlank(jyls.getGfemail())) {
						jyls.setSffsyj("1");
					}
					jyls.setKpr(xf.getKpr());
					jyls.setFhr(xf.getFhr());
					jyls.setSkr(xf.getSkr());
					jyls.setSsyf(new SimpleDateFormat("yyyyMM").format(new Date()));
					Map map3 = new HashMap<>();
					map3.put("gsdm", "sqj");
					Yh yh = yhService.findOneByParams(map3);
					jyls.setLrry(yh.getId());
					jyls.setXgry(yh.getId());
					jyls.setYxbz("1");
					jyls.setGsdm("sqj");
					jyls.setSkpid(skp.getId());
					Jyspmx jyspmx = new Jyspmx();
					jyspmx.setSpmxxh(1);
					jyspmx.setFphxz("0");
					jyspmx.setSpmc("餐饮费");
					jyspmx.setSpdm("1010101070000000000");
					jyspmx.setSpje(Double.parseDouble((String) request.getSession().getAttribute("price")));
					jyspmx.setSpsl(0.06);
					jyspmx.setLrsj(new Date());
					jyspmx.setXgsj(new Date());
					jyspmx.setLrry(yh.getId());
					jyspmx.setXgry(yh.getId());
					jyspmx.setGsdm("sqj");
					jyspmx.setSkpid(skp.getId());
					jylsService.save(jyls);
					jyspmx.setDjh(jyls.getDjh());
					jyspmx.setJshj(Double.parseDouble((String) request.getSession().getAttribute("price")));
					jyspmxService.save(jyspmx);
				}else {
					result.put("failure", true);
					result.put("xx", "离线开票失败,无销方信息");
				}
			}else {
				result.put("failure", true);
				result.put("xx", "离线开票失败,无税控盘信息");
			}
		}
		result.put("failure", false);
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
    @RequestMapping(value ="/getmsg")
    @ResponseBody
    public Map getMsg(){
    	Map<String , Object> result = new HashMap<>();
    	result.put("msg", request.getSession().getAttribute("msg"));
    	result.put("clztdm", request.getSession().getAttribute("clztdm"));
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
	public Cszb getCszb(String gsdm,Integer xfid,Integer kpdid,String csm){
		Map params = new HashMap<>();
		params.put("gsdm", gsdm);
		params.put("xfid", xfid);
		params.put("kpdid", kpdid);
		params.put("csm", csm);
		List<Cszb> list = new ArrayList<>(); 
		list = cszbService.findAllByParams(params);
		if (list.size()==1) {
		return list.get(0);
		}else if (list.size()==0) {
			return new Cszb();
		}else{
			return list.get(0);
		}
	}
	/*
	 * 定时获取流水状态
	 * */
	public void getJylsxx(){
	     Runnable runnable = new Runnable() {  
	            public void run() {  
	            	 Map map2 = new HashMap<>();
	            	String ddh = (String) request.getSession().getAttribute("orderNo"); 
	    	         map2.put("ddh", ddh);
	    	         Jyls jyls = jylsService.findByTqm(map2);
	    	         if (null!=jyls) {
						
					}
	            }  
	        }; 
	        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
	        service.scheduleAtFixedRate(runnable, 10, 1, TimeUnit.SECONDS); 
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
