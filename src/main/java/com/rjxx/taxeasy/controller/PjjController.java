package com.rjxx.taxeasy.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rjxx.comm.utils.MailUtil;
import com.rjxx.comm.web.BaseController;
import com.rjxx.taxeasy.domains.Fpj;
import com.rjxx.taxeasy.domains.Jyls;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.service.FpjService;
import com.rjxx.taxeasy.service.JylsService;
import com.rjxx.taxeasy.service.KplsService;
import com.rjxx.taxeasy.vo.FpjVo;

@SuppressWarnings("deprecation")
@Controller
@RequestMapping("/pjj")
public class PjjController extends BaseController {

	public static final String APP_ID = "wx9abc729e2b4637ee";

	public static final String GET_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";// 获取access

	public static final String SECRET = "6415ee7a53601b6a0e8b4ac194b382eb";

	@Autowired
	private FpjService fpjService;

	@Autowired
	private KplsService kplsService;

	@Autowired
	private JylsService jylsService;

	@Value("${emailHost}")
	private String emailHost;
	@Value("${emailUserName}")
	private String emailUserName;
	@Value("${emailPwd}")
	private String emailPwd;
	@Value("${emailForm}")
	private String emailForm;
	@Value("${emailTitle}")
	private static String emailTitle;

	@RequestMapping
	public String index() {
		return "pjj/index";
	}

	/**
	 * 获取交易信息
	 * 
	 * @return
	 */
	@RequestMapping(value = "/getKhjy")
	@ResponseBody
	public Map getKhjy() {
		Map<String, Object> result = new HashMap<>();
		Map<String, Object> params = new HashMap<>();
		String unionid = (String) session.getAttribute("unionid");
		params.put("unionid", unionid);
		List<FpjVo> list = fpjService.findAllByParam(params);
		List<Kpls> kps = null;
		Kpls kpls = new Kpls();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
		for (FpjVo fpjVo : list) {
			kpls.setDjh(fpjVo.getDjh());
			kps = kplsService.findByDjh(kpls);
			if (!kps.isEmpty()) {
				fpjVo.setKprq(sdf.format(kps.get(0).getKprq()));
			}
		}
		result.put("fps", list);
		return result;
	}

	/**
	 * 获取发票信息
	 * 
	 * @param djh
	 * @return
	 */
	@RequestMapping(value = "/saveFp")
	public String getFp(Integer djh) {
		Map<String, Object> params = new HashMap<>();
		if (djh == null) {
			djh = -1;
		}
		params.put("djh", djh);
		List<Kpls> list = kplsService.findAllByParams(params);
		for (Kpls kpls : list) {
			if (kpls.getPdfurl() != null && !"".equals(kpls.getPdfurl())) {
				kpls.getPdfurl().replace(".pdf", ".jpg");
			}
		}
		session.setAttribute("djh", djh);
		session.setAttribute("fps", list);
		return "pjj/imageviewer";
	}

	/**
	 * 发票预览
	 * 
	 * @return
	 */
	@RequestMapping(value = "/getFp")
	@ResponseBody
	public Map getFp() {
		Map<String, Object> result = new HashMap<>();
		result.put("fps", session.getAttribute("fps"));
		return result;
	}

	/**
	 * 跳转到邮箱页面
	 * 
	 * @return
	 */
	@RequestMapping(value = "/youxiong")
	public String youxiong() {
		return "pjj/youxiang";
	}

	/**
	 * 跳转到首页
	 */
	@RequestMapping(value = "/first")
	public String back() {
		return "pjj/index";
	}

	/**
	 * 跳转到错误页面
	 */
	@RequestMapping(value = "/error")
	public String error() {
		return "redirect:/smtq/demo.html";
	}

	/**
	 * 添加到发票夹
	 * 
	 * @param unionid
	 * @return
	 */
	@RequestMapping(value = "/saveFpj")
	@ResponseBody
	public Map save(String unionid) {
		Map<String, Object> result = new HashMap<>();
		Integer djh = (Integer) session.getAttribute("djh");
		if (djh == null) {
			result.put("error", true);
			return result;
		}
		Map<String, Object> params = new HashMap<>();
		params.put("djh", djh);
		params.put("unionid", unionid);
		Fpj fpj = fpjService.findOneByParams(params);
		if (fpj != null) {
			result.put("nopeat", true);
			result.put("msg", "该订单已添加发票夹");
			return result;
		}
		fpj = new Fpj();
		fpj.setDjh(djh);
		fpj.setUnionid(unionid);
		fpj.setYxbz("1");
		fpj.setLrsj(new Date());
		fpj.setXgsj(new Date());
		fpjService.save(fpj);
		result.put("success", true);
		return result;
	}

	/**
	 * 判断unionID是否存在
	 * 
	 * @return
	 */
	@RequestMapping(value = "/getUnionid")
	@ResponseBody
	public Map getUnionid() {
		Map<String, Object> result = new HashMap<>();
		String unionid = (String) session.getAttribute("unionid");
		if (unionid != null) {
			result.put("success", true);
		} else {
			result.put("success", false);
		}
		return result;
	}

	/**
	 * 判断access_token是否存在
	 * 
	 * @return
	 */
	@RequestMapping(value = "/CheckToken")
	@ResponseBody
	public Map CheckToken() {
		Map<String, Object> result = new HashMap<>();
		String access_token = (String) session.getAttribute("access_token");
		if (access_token != null) {
			result.put("success", true);
		} else {
			result.put("success", false);
		}
		return result;
	}

	/**
	 * 发送邮件
	 * 
	 * @param yx
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/sendEmail")
	@ResponseBody
	public Map sendMail(String yx) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		Integer djh = (Integer) session.getAttribute("djh");
		if (djh == null) {
			result.put("success", false);
			result.put("msg", "会话已过期");
			return result;
		}
		Map<String, Object> params = new HashMap<>();
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
		result.put("success", flag);
		return result;
	}

	/**
	 * 获取授权code
	 * 
	 * @param apiUrl
	 * @param appId
	 * @param url
	 * @return
	 */
	public Map getCode(String apiUrl, String appId, String url) {
		Map<String, Object> result = new HashMap<>();
		String codeUrl = String.format(
				"%s?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_userinfo&state=STATE#wechat_redirect",
				apiUrl, appId, url);
		return result;
	}

	/**
	 * 获取access_token
	 * 
	 * @param apiurl
	 * @param appid
	 * @param code
	 * @return
	 */
	@RequestMapping(value = "/getToken")
	@ResponseBody
	public Map hqtk(String apiurl, String appid, String code) {
		Map<String, Object> result = new HashMap<String, Object>();
		// 获取token
		String turl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + APP_ID + "&secret=" + SECRET
				+ "&code=" + code + "&grant_type=authorization_code";
		// https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(turl);
		ObjectMapper jsonparer = new ObjectMapper();// 初始化解析json格式的对象
		try {
			HttpResponse res = client.execute(get);
			String responseContent = null; // 响应内容
			HttpEntity entity = res.getEntity();
			responseContent = EntityUtils.toString(entity, "UTF-8");
			Map map = jsonparer.readValue(responseContent, Map.class);
			// 将json字符串转换为json对象
			if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				if (map.get("errcode") != null) {// 错误时微信会返回错误码等信息，{"errcode":40013,"errmsg":"invalid
					result.put("success", false);
					result.put("msg", "获取微信token失败,错误代码为" + map.get("errcode"));
					return result;
				} else {// 正常情况下{"access_token":"ACCESS_TOKEN","expires_in":7200}
					session.setAttribute("access_token", map.get("access_token"));
					map.put("success", true);
					return map;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.put("success", false);
			result.put("msg", "获取微信token失败" + e.getMessage());
		} finally {
			// 关闭连接 ,释放资源
			client.getConnectionManager().shutdown();
		}
		return result;
	}

	/**
	 * 刷新access_token
	 * 
	 * @param apiurl
	 * @param appid
	 * @param code
	 * @param refresh_token
	 * @return
	 */
	@RequestMapping(value = "/getRefresh")
	@ResponseBody
	public Map getRefresh(String apiurl, String appid, String code, String refresh_token) {
		Map<String, Object> result = new HashMap<String, Object>();
		// 获取token
		String turl = String.format("%s?grant_type=refresh_token&appid=%s&refresh_token=%s", apiurl, APP_ID,
				refresh_token);
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(turl);
		ObjectMapper jsonparer = new ObjectMapper();// 初始化解析json格式的对象
		try {
			HttpResponse res = client.execute(get);
			String responseContent = null; // 响应内容
			HttpEntity entity = res.getEntity();
			responseContent = EntityUtils.toString(entity, "UTF-8");
			Map map = jsonparer.readValue(responseContent, Map.class);
			// 将json字符串转换为json对象
			if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				if (map.get("errcode") != null) {// 错误时微信会返回错误码等信息，{"errcode":40013,"errmsg":"invalid
					result.put("success", false);
					result.put("msg", "获取微信token失败,错误代码为" + map.get("errcode"));
					return result;
				} else {// 正常情况下{"access_token":"ACCESS_TOKEN","expires_in":7200}
					map.put("success", true);
					return map;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.put("success", false);
			result.put("msg", "获取微信token失败" + e.getMessage());
		} finally {
			// 关闭连接 ,释放资源
			client.getConnectionManager().shutdown();
		}
		return result;
	}

	/**
	 * 获取微信用户信息
	 * 
	 * @param apiurl
	 * @param openid
	 * @param access_token
	 * @return
	 */
	@RequestMapping(value = "/getUserMsg")
	@ResponseBody
	public Map getUserMsg(String openid, String access_token) {
		Map<String, Object> result = new HashMap<String, Object>();
		// 获取token
		String turl = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=" + access_token + 
				"&openid=" + openid + "&lang=zh_CN";
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(turl);
		ObjectMapper jsonparer = new ObjectMapper();// 初始化解析json格式的对象
		try {
			HttpResponse res = client.execute(get);
			String responseContent = null; // 响应内容
			HttpEntity entity = res.getEntity();
			responseContent = EntityUtils.toString(entity, "UTF-8");
			Map map = jsonparer.readValue(responseContent, Map.class);
			// 将json字符串转换为json对象
			if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				if (map.get("errcode") != null) {// 错误时微信会返回错误码等信息，{"errcode":40013,"errmsg":"invalid
					result.put("success", false);
					result.put("msg", "获取微信用户信息失败,错误代码为" + map.get("errcode"));
					return result;
				} else {// 正常情况下{"access_token":"ACCESS_TOKEN","expires_in":7200}
					map.put("success", true);
					logger.info("unionid" + map.get("unionid"));
					session.setAttribute("unionid", map.get("unionid"));
					return map;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.put("success", false);
			result.put("msg", "获取微信token失败" + e.getMessage());
		} finally {
			// 关闭连接 ,释放资源
			client.getConnectionManager().shutdown();
		}
		return result;
	}

	/**
	 * 生成邮件内容
	 * 
	 * @param ddh
	 * @param pdfUrlList
	 * @param gsdm
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

		Thread.sleep(2000);
	}
}
