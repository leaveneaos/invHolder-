package com.rjxx.taxeasy.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.rjxx.comm.utils.MailUtil;
import com.rjxx.taxeasy.domains.Fphkyj;
import com.rjxx.taxeasy.domains.Jyls;
import com.rjxx.taxeasy.domains.Jyspmx;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.domains.Wxkb;
import com.rjxx.taxeasy.service.FphkyjService;
import com.rjxx.taxeasy.service.JylsService;
import com.rjxx.taxeasy.service.JyspmxService;
import com.rjxx.taxeasy.service.KplsService;
import com.rjxx.taxeasy.service.WxkbService;

@Controller
@RequestMapping("/tijiao")
public class TijiaoController {
	@Autowired
	private JyspmxService jyspmxService;
	@Autowired
	private WxkbService wxkbService;
	@Autowired
	private FphkyjService fphkyjService;
	@Autowired
	private JylsService jylsService;
	@Autowired
	private KplsService kplsService;
	@Value("${emailHost}")
	private  String emailHost;
	@Value("${emailUserName}")
	private  String emailUserName;
	@Value("${emailPwd}")
	private  String emailPwd;
	@Value("${emailForm}")
	private  String emailForm;
	@Value("${emailTitle}")
	private static String emailTitle;
	@RequestMapping(value = "/hqmx")
	@ResponseBody
	public Map hqmx(String djh) {
		Map<String, Object> result = new HashMap<String, Object>();
		Map params = new HashMap<>();
		if (null == djh || "".equals(djh)) {
			result.put("mxList", null);
			return result;
		}
		params.put("djh", djh);
		List<Jyspmx> mxList = jyspmxService.findAllByParams(params);
		result.put("mxList", mxList);
		return result;
	}

	@RequestMapping(value = "/fpxx")
	@ResponseBody
	public Map fpxx(String fptt, String nsrsbh, String dz, String dh, String khh, String yhzh, String djh) {
		Map<String, Object> result = new HashMap<String, Object>();
		Map map = new HashMap<>();
		map.put("djh", djh);
		Jyls jyls = jylsService.findOneByParams(map);
		if (!"00".equals(jyls.getClztdm())) {
			result.put("msg", "1");
			return result;
		}
		jyls.setGfmc(fptt);
		jyls.setGfsh(nsrsbh);
		jyls.setGfdz(dz);
		jyls.setGfdh(dh);
		jyls.setGfyh(khh);
		jyls.setGfyhzh(yhzh);
		jyls.setClztdm("01");
		jylsService.save(jyls);
		result.put("msg", "2");
		return result;
	}

	@RequestMapping(value = "/fpzt")
	@ResponseBody
	public Map fpzt(String djh) {
		Map<String, Object> result = new HashMap<String, Object>();
		
		Map params = new HashMap<>();
		params.put("djh", djh);
		result.put("djh", djh);
		Jyls jyls = jylsService.findOneByParams(params);
		if ("91".equals(jyls.getClztdm())) {
			List<Kpls> kplsList = kplsService.findAllByParams(params);
			String pdfdzs = "";
			for (Kpls kpls2 : kplsList) {
				pdfdzs += kpls2.getPdfurl().replace(".pdf", ".jpg") + ",";
			}
			if (pdfdzs.length() > 0) {
				result.put("pdfdzs", pdfdzs.substring(0, pdfdzs.length() - 1));
			}
			result.put("msg", true);
		} else {
			result.put("msg", false);
		}
		return result;
	}

	@RequestMapping(value = "/yxfs")
	@ResponseBody
	public Map yxfs(String yx, String djh) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		Map params = new HashMap<>();
		params.put("djh", djh);
		boolean flag = false;
		Jyls jyls = jylsService.findOneByParams(params);
		List<Kpls> kplsList = kplsService.findAllByParams(params);
		List<String> pdfUrlList = new ArrayList<>();
		for (Kpls kpls : kplsList) {
			pdfUrlList.add(kpls.getPdfurl());
		}
		if (kplsList.size() > 0) {
			sendMail(jyls.getDdh(), yx, pdfUrlList, jyls.getXfmc());
			flag = true;
		}
		result.put("msg", flag);
		return result;
	}

	/**
	 * A发送邮件的内容
	 *
	 * @param ddh
	 *            订单号
	 * @return
	 * @throws Exception
	 */
	private static String getAFMailContent(String ddh, List<String> pdfUrlList, String gsdm) throws Exception {
		StringBuffer sb = new StringBuffer();
		// sb.append(null2Wz(iurb.get("BUYER_NAME")));
		sb.append(" 先生/小姐您好：<br/>");
		sb.append("<br/>");
		sb.append("您的订单号码： ");
		sb.append(ddh).append("的电子发票已开具成功，电子发票下载地址：<br>");
		for (String pdfUrl : pdfUrlList) {
			sb.append("<a href='" + pdfUrl + "'>" + null2Wz(pdfUrl) + "</a><br>");
		}
		sb.append("请及时下载您的发票。");
		sb.append("<br/><br/>");
		sb.append(gsdm);
		sb.append("<br/>");
		sb.append("<br/>");
		Date d = new Date();
		sb.append(1900 + d.getYear()).append("年").append(d.getMonth() + 1).append("月").append(d.getDate()).append("日");
		return sb.toString();
	}

	// 判空
	private static Object null2Wz(Object s) {
		return s == null || "".equals(s) ? "未知" : s;
	}

	/**
	 * 发送邮件
	 *
	 * @param ddh
	 * @param email
	 * @param pdfUrlList
	 * @param gsdm
	 * @throws Exception
	 */
	public void sendMail(String ddh, String email, List<String> pdfUrlList, String gsdm) throws Exception {
		MailUtil sendmail = new MailUtil();
		sendmail.setHost(emailHost);
		sendmail.setUserName(emailUserName);
		sendmail.setPassWord(emailPwd);
		sendmail.setTo(email);

		sendmail.setFrom(emailForm);
		sendmail.setSubject(emailTitle);
		sendmail.setContent(getAFMailContent(ddh, pdfUrlList, gsdm));
		// TODO 这里需要根据邮件摸板内容进行调整。

		// XXX 先生/小姐您好：
		//
		// 订单号码： XXXXXXXX, 您的发票信息如下：
		//
		//
		// 发票将邮寄至地址（即订单收货地址）： XXXXXXX 收件人（即定单收货人）： xxxxxx
		// 上述资料是您在个人基本资料中所登陆的地址，并已输入发票系统，为避免退货情况产生，
		// 请您再次确认住址是否正确，若有需要修改邮寄资料请联络客服中心进行修改。
		//
		// 在此提醒若您是在收到此邮件后才修改个人基本资料，则新登陆的邮寄资料将会在下次发票开立时生效
		//
		//
		//
		// 爱芙趣商贸（上海）有限公司
		// 20xx年x月x日

		sendmail.sendMail();

		Thread.sleep(5000);
	}

	@RequestMapping(value = "/sqyj")
	@ResponseBody
	public Map sqyj(Fphkyj fphkyj) {
		Map<String, Object> result = new HashMap<String, Object>();
		Map params = new HashMap<>();
		params.put("djh", fphkyj.getDjh());
		Fphkyj fphkyj2 = fphkyjService.findOneByParams(params);
		if (null != fphkyj2) {
			result.put("msg", false);
			result.put("sh", fphkyj2.getSfyj());
			return result;
		}
		Jyls jyls = jylsService.findOneByParams(params);
		List<Kpls> list = kplsService.findAllByParams(params);
		for (Kpls kpls : list) {
			fphkyj.setYxbz("0");
			fphkyj.setLrsj(new Date());
			fphkyj.setXgsj(new Date());
			fphkyj.setDdh(jyls.getDdh());
			;
			fphkyj.setFpdm(kpls.getFpdm());
			fphkyj.setFphm(kpls.getFphm());
			fphkyj.setGfmc(kpls.getGfmc());
			fphkyj.setSfyj("0");
			fphkyj.setXfid(kpls.getXfid());
			fphkyj.setKplsh(String.valueOf(kpls.getKplsh()));
			fphkyj.setKprq(kpls.getKprq());
			fphkyjService.save(fphkyj);
			fphkyj.setId(null);
		}

		result.put("msg", true);
		return result;
	}

	@RequestMapping(value = "/wxkb")
	@ResponseBody
	public Map wxkb(Wxkb wxkb) {
		Map<String, Object> result = new HashMap<String, Object>();
		wxkb.setScsj(new Date());
		wxkbService.save(wxkb);
		result.put("msg", true);
		return result;

	}

	@RequestMapping(value = "/kbcx")
	@ResponseBody
	public Map kbcx() {
		Map<String, Object> result = new HashMap<String, Object>();
		List<Wxkb> list = wxkbService.findAllByParams(null);
		if (list.size() > 0) {
			Wxkb wxkb = list.get(0);
			result.put("accessToken", wxkb.getAccessToken());
			Date old = wxkb.getScsj();
			Date current = new Date();
			long interval = (current.getTime() - old.getTime()) / 1000;
			if (interval > Long.parseLong(wxkb.getExpiresIn())) {
				result.put("bz", "0");
			} else {
				result.put("bz", "1");
			}
		}else{
			result.put("bz", "0");
		}
		result.put("msg", true);
		return result;

	}

}
