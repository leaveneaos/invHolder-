package com.rjxx.taxeasy.controller;

import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.comm.SigCheck;
import com.rjxx.taxeasy.domains.Csb;
import com.rjxx.taxeasy.domains.Fpj;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.domains.Tqjl;
import com.rjxx.taxeasy.service.CsbService;
import com.rjxx.taxeasy.service.FpjService;
import com.rjxx.taxeasy.service.JylsService;
import com.rjxx.taxeasy.service.JyspmxService;
import com.rjxx.taxeasy.service.KplsService;
import com.rjxx.taxeasy.service.TqjlService;
import com.rjxx.utils.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/dzfp/rjxx")

public class RjxxController extends BaseController {
	@Autowired
	private JylsService jylsService;
	@Autowired
	private KplsService kplsService;
	@Autowired
	private JyspmxService jyspmxService;
	@Autowired
	private TqjlService tqjlService;
	@Autowired
	private FpjService fpjService;
	@Autowired
	private CsbService csbService;

	@RequestMapping
	@ResponseBody
	public String index() throws IOException {
		response.sendRedirect(request.getContextPath() + "/rjxx.html?_t=" + System.currentTimeMillis());
		return null;
		// return "redirect:/zydc.html";
	}

	@RequestMapping(value = "/zydc")
	@ResponseBody
	public Map Fptq(String tqm, String code) {
		String sessionCode = (String) session.getAttribute("rand");
		String openid = (String) session.getAttribute("openid");
		Map<String, Object> result = new HashMap<String, Object>();
		if (code != null && sessionCode != null && code.equals(sessionCode)) {
			Map map = new HashMap<>();
			map.put("tqm", tqm);
			map.put("gsdm", "rjxx");
			List<Kpls> list = jylsService.findByTqm(map);
			if (list.size() > 0) {
				
				String pdfdzs = "";
				request.getSession().setAttribute("djh", list.get(0).getDjh());
				for (Kpls kpls2 : list) {
					pdfdzs += kpls2.getPdfurl().replace(".pdf", ".jpg") + ",";
				}
				if (pdfdzs.length() > 0) {
					result.put("pdfdzs", pdfdzs.substring(0, pdfdzs.length() - 1));
					request.getSession().setAttribute("pdfdzs", pdfdzs.substring(0, pdfdzs.length() - 1));
				}
				if (openid != null && !openid.equals("null")) {
			        Map<String, Object> params = new HashMap<>();
			        params.put("djh", list.get(0).getDjh());
			        params.put("unionid", openid);
			        Fpj fpj = fpjService.findOneByParams(params);
			        if (fpj == null) {
			        	fpj = new Fpj();
						fpj.setDjh(list.get(0).getDjh());
						fpj.setUnionid(openid);
						fpj.setYxbz("1");
						fpj.setLrsj(new Date());
						fpj.setXgsj(new Date());
						fpjService.save(fpj);
					}
				}
				result.put("num", "2");
				Tqjl tqjl = new Tqjl();
				tqjl.setDjh(String.valueOf(list.get(0).getDjh()));
				tqjl.setJlly("1");
				tqjl.setTqsj(new Date());
				String visiterIP;
				if (request.getHeader("x-forwarded-for") == null) {
					visiterIP = request.getRemoteAddr();// 访问者IP
				} else {
					visiterIP = request.getHeader("x-forwarded-for");
				}
				tqjl.setIp(visiterIP);
				String llqxx = request.getHeader("User-Agent");
				tqjl.setLlqxx(llqxx);
				tqjlService.save(tqjl);
			} else {
				result.put("num", "3");
			}
		} else {
			result.put("num", "4");
		}
		return result;
	}

	@RequestMapping(value = "/getFp")
	@ResponseBody
	public void getFp(Integer djh) throws IOException {
		Map<String, Object> result = new HashMap<String, Object>();
		Kpls kpls = new Kpls();
		kpls.setDjh(djh);
		List<Kpls> list = kplsService.findByDjh(kpls);
		if (list.size() > 0) {
			String pdfdzs = "";
			request.getSession().setAttribute("djh", list.get(0).getDjh());
			for (Kpls kpls2 : list) {
				pdfdzs += kpls2.getPdfurl().replace(".pdf", ".jpg") + ",";
			}
			if (pdfdzs.length() > 0) {
				result.put("pdfdzs", pdfdzs.substring(0, pdfdzs.length() - 1));
				request.getSession().setAttribute("pdfdzs", pdfdzs.substring(0, pdfdzs.length() - 1));
			}
			result.put("num", "2");
			Tqjl tqjl = new Tqjl();
			tqjl.setDjh(String.valueOf(list.get(0).getDjh()));
			tqjl.setJlly("1");
			tqjl.setTqsj(new Date());
			String visiterIP;
			if (request.getHeader("x-forwarded-for") == null) {
				visiterIP = request.getRemoteAddr();// 访问者IP
			} else {
				visiterIP = request.getHeader("x-forwarded-for");
			}
			tqjl.setIp(visiterIP);
			String llqxx = request.getHeader("User-Agent");
			tqjl.setLlqxx(llqxx);
			tqjlService.save(tqjl);
		} else {
			result.put("num", "3");
		}
		response.sendRedirect(request.getContextPath() + "/fp.html?_t=" + System.currentTimeMillis());

	}

	@RequestMapping(value = "/token")
	@ResponseBody
	public void getMsg() throws IOException {
		String sign = request.getParameter("signature");
		String times = request.getParameter("timestamp");
		String nonce = request.getParameter("nonce");
		String echo = request.getParameter("echostr");
		if (SigCheck.checkSignature(sign, times, nonce)) {
			response.getOutputStream().print(request.getParameter("echostr"));
			logger.error("isSuccess:" + echo);
		}
	}

	@RequestMapping(value = "/getUrl")
	@ResponseBody
	public Map<String, Object> getUrl(){
		Map<String, Object> result = new HashMap<String, Object>();
		Object djh = session.getAttribute("djh");
		result.put("djh", djh);

		Map<String, Object> params = new HashMap<>();
		params.put("csm", "mbxxurl");
		Csb cs = csbService.findOneByParams(params);
		if (djh == null) {
			result.put("success", false);
			return result;
		}
		if (cs != null) {
			String url = cs.getMrz();
			url = url.substring(0, url.lastIndexOf("/")+1)+"saveFpj?djh="+djh;
			result.put("url", url);
			result.put("success", true);
		}
		
		return result;
		
	}

	@RequestMapping(value = "/saveFpj")
	@ResponseBody
	public void saveFpj(Integer djh) throws IOException{
		session.setAttribute("djh", djh);
		response.sendRedirect(request.getContextPath() + "/sccg.html?_t=" + System.currentTimeMillis());
	}

}
