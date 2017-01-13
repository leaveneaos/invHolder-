package com.rjxx.taxeasy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.domains.*;
import com.rjxx.taxeasy.service.*;
import com.rjxx.utils.HtmlUtils;
import com.rjxx.utils.MD5Util;
import com.rjxx.utils.StringUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/dzfp_sqj")
public class SqjController extends BaseController {

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
	private JyxxService jyxxservice;
	@Autowired
	private JyspmxService jyspmxService;
	@Autowired
	private TqmtqService tqmtqService;
	@Autowired
	private PpService ppService;
	@Autowired
	private SpService spService;
	@Autowired
	private FpjService fpjService;

	public static final String APP_ID = "wx9abc729e2b4637ee";

	public static final String GET_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";// 获取access

	public static final String SECRET = "6415ee7a53601b6a0e8b4ac194b382eb";

	@RequestMapping
	@ResponseBody
	public void index() throws Exception {
		String str = request.getParameter("q");
		Map<String, Object> params = new HashMap<>();
		params.put("gsdm", "sqj");
		Gsxx gsxx = gsxxservice.findOneByParams(params);
    	if (gsxx.getWxappid() == null || gsxx.getWxsecret() == null) {
			gsxx.setWxappid(APP_ID);
			gsxx.setWxsecret(SECRET);
		}
		String ua = request.getHeader("user-agent").toLowerCase();
		if (ua.indexOf("micromessenger") > 0) {
			String url = HtmlUtils.getBasePath(request);
			String openid = String.valueOf(session.getAttribute("openid"));
			if (openid == null || "null".equals(openid)) {
				String ul = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + gsxx.getWxappid() + "&redirect_uri="
						+ url + "/dzfp_sqj/getWx&" + "response_type=code&scope=snsapi_base&state=" + str
						+ "#wechat_redirect";
				response.sendRedirect(ul);
				return;
			}else{
				sendHtml(str, gsxx);
			}
		}else{
			sendHtml(str, gsxx);
		}
	}

	@RequestMapping(value = "/getWx")
	@ResponseBody
	public void getWx(String state, String code) throws IOException {
		Map params = new HashMap<>();
		params.put("gsdm", "sqj");
		Gsxx gsxx = gsxxservice.findOneByParams(params);
    	if (gsxx.getWxappid() == null || gsxx.getWxsecret() == null) {
			gsxx.setWxappid(APP_ID);
			gsxx.setWxsecret(SECRET);
		}
		String turl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + gsxx.getWxappid() + "&secret="
				+ gsxx.getWxsecret() + "&code=" + code + "&grant_type=authorization_code";
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
					
				} else {// 正常情况下{"access_token":"ACCESS_TOKEN","expires_in":7200}
					session.setAttribute("access_token", map.get("access_token"));
					session.setAttribute("openid", map.get("openid"));
					logger.info(session.getAttribute("openid").toString());
					map.put("success", true);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 关闭连接 ,释放资源
			client.getConnectionManager().shutdown();
		}
		if (state.equals("sqj")) {
			response.sendRedirect(request.getContextPath() + "/smtq/sqj.html?_t=" + System.currentTimeMillis());
			return;
		}else if (state.equals("wdm")) {
			response.sendRedirect(request.getContextPath() + "/smtq/sqjwdm.html?_t=" + System.currentTimeMillis());
			return;
		}else if (state.equals("bss")) {
			response.sendRedirect(request.getContextPath() + "/smtq/sqjss.html?_t=" + System.currentTimeMillis());
			return;
		}else{
			sendHtml(state, gsxx);
		}
	}

	@RequestMapping(value = "/sendHtml")
	@ResponseBody
	public void sendHtml(String state, Gsxx gsxx) throws IOException{
		try {
			logger.info(session.getAttribute("openid").toString());
			byte[] bytes = Base64.decodeBase64(state);
			String csc = new String(bytes);
			String[] cssz = csc.split("&");
			String orderNo = cssz[0].substring(cssz[0].lastIndexOf("=") + 1);
			String orderTime = cssz[1].substring(cssz[1].lastIndexOf("=") + 1);
			String price = cssz[2].substring(cssz[2].lastIndexOf("=") + 1);
			String sn = cssz[3].substring(cssz[3].lastIndexOf("=") + 1);
			String sign = cssz[4].substring(cssz[4].lastIndexOf("=") + 1);
			String dbs = csc.substring(0, csc.lastIndexOf("&") + 1);
			if (null == gsxx) {
				request.getSession().setAttribute("msg", "公司信息不正确!");
				response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
				return;
			}
			dbs += "key=" + gsxx.getSecretKey();
			String key1 = MD5Util.generatePassword(dbs);

			if (!sign.equals(key1.toLowerCase())) {
				request.getSession().setAttribute("msg", "秘钥不匹配!");
				response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
				return;
			}
			if (null == orderNo || "".equals(orderNo)) {
				request.getSession().setAttribute("msg", "未包含流水号!");
				response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
				return;
			}
			if (null == sn || "".equals(sn)) {
				request.getSession().setAttribute("msg", "未包含门店信息!");
				response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
				return;
			}
			if (null == orderTime || "".equals(orderTime)) {
				request.getSession().setAttribute("msg", "未包含流水时间!");
				response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
				return;
			}
			if (null == price || "".equals(price)) {
				request.getSession().setAttribute("msg", "未包含金额!");
				response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
				return;
			}
			Map map2 = new HashMap<>();
			map2.put("gsdm", "sqj");
			map2.put("kpddm", sn);
			Skp skp = skpService.findOneByParams(map2);
			if (null != skp) {
				Xf xf = xfService.findOne(skp.getXfid());
				if (null == xf) {
					request.getSession().setAttribute("msg", "未查询到销方!");
					response.sendRedirect(
							request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
					return;
				}
			} else {
				request.getSession().setAttribute("msg", "未查询到门店号!");
				response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
				return;
			}
			request.getSession().setAttribute("orderNo", orderNo);
			request.getSession().setAttribute("orderTime1", orderTime);
			request.getSession().setAttribute("orderTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.format(new SimpleDateFormat("yyyyMMddHHmmss").parse(orderTime)));
			request.getSession().setAttribute("price", price);
			request.getSession().setAttribute("sn", sn);
			String ddh = (String) request.getSession().getAttribute("orderNo");
			String openid = (String) session.getAttribute("openid");
			Map map = new HashMap<>();
			map.put("ddh", ddh);
			map.put("gsdm", "sqj");
			Smtq smtq1 = smtqService.findOneByParams(map);
			Map mapo = new HashMap<>();
			mapo.put("tqm", ddh);
			mapo.put("je", String.valueOf(request.getSession().getAttribute("price")));
			mapo.put("gsdm", "sqj");
			Tqmtq tqmtq = tqmtqService.findOneByParams(mapo);
			if (null != smtq1 && null != smtq1.getId()) {
				List<Kpls> list = jylsService.findByTqm(map);
				if (list.size() > 0) {
					if (openid != null && !"null".equals(openid)) {
						Map<String, Object> param = new HashMap<>();
						param.put("djh", list.get(0).getDjh());
						param.put("unionid", openid);
						Fpj fpj = fpjService.findOneByParams(param);
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
					String pdfdzs = "";
					for (Kpls kpls2 : list) {
						pdfdzs += kpls2.getPdfurl().replace(".pdf", ".jpg") + ",";
					}
					if (pdfdzs.length() > 0) {
						request.getSession().setAttribute("djh", list.get(0).getDjh());
						request.getSession().setAttribute("pdfdzs", pdfdzs.substring(0, pdfdzs.length() - 1));
					}
					Tqjl tqjl = new Tqjl();
					tqjl.setDjh(String.valueOf(list.get(0).getDjh()));
					tqjl.setTqsj(new Date());
					String visiterIP;
					if (request.getHeader("x-forwarded-for") == null) {
						visiterIP = request.getRemoteAddr();// 访问者IP
					} else {
						visiterIP = request.getHeader("x-forwarded-for");
					}
					tqjl.setIp(visiterIP);
					tqjl.setJlly("1");
					String llqxx = request.getHeader("User-Agent");
					tqjl.setLlqxx(llqxx);
					tqjlService.save(tqjl);
					response.sendRedirect(request.getContextPath() + "/fp.html?_t=" + System.currentTimeMillis());
					return;
				}
				response.sendRedirect(request.getContextPath() + "/smtq/smtq3.html?_t=" + System.currentTimeMillis());
				return;
			} else if (null != tqmtq && null != tqmtq.getId()) {
				response.sendRedirect(request.getContextPath() + "/smtq/smtq3.html?_t=" + System.currentTimeMillis());
				return;
			} else {
				if (null != skp.getPid()) {
					Pp pp = ppService.findOne(skp.getPid());
					if (null != pp.getPpurl()) {
						response.sendRedirect(
								request.getContextPath() + pp.getPpurl() + "?_t" + System.currentTimeMillis());
						return;
					}
				} else {
					response.sendRedirect(
							request.getContextPath() + "/smtq/smtq1.html?_t=" + System.currentTimeMillis());
					return;
				}
			}
		} catch (Exception e) {
			request.getSession().setAttribute("msg", e.getMessage());
			response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
			return;
		}
	}

	@RequestMapping(value = "/smtq3")
	@ResponseBody
	public void tztz() throws IOException {
		response.sendRedirect(request.getContextPath() + "/smtq/smtq3.html?_t=" + System.currentTimeMillis());
	}

	@RequestMapping(value = "/demo")
	@ResponseBody
	public void demo() throws IOException {
		response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
	}

	@RequestMapping(value = "/smtq2")
	@ResponseBody
	public void smtq2(String openid) throws IOException {
		session.setAttribute("openid", openid);
		response.sendRedirect(request.getContextPath() + "/smtq/smtq2.html?_t=" + System.currentTimeMillis());
	}

	@RequestMapping(value = "/bangzhu")
	@ResponseBody
	public void bangzhu() throws IOException {
		response.sendRedirect(request.getContextPath() + "/smtq/smtq2.html?_t=" + System.currentTimeMillis());
	}

	@RequestMapping(value = "/yxxg")
	@ResponseBody
	public void yxxg() throws IOException {
		response.sendRedirect(request.getContextPath() + "/smtq/xgyx.html?_t=" + System.currentTimeMillis());
	}

	@RequestMapping(value = "/fp")
	@ResponseBody
	public void fp() throws IOException {
		response.sendRedirect(request.getContextPath() + "/fp.html?_t=" + System.currentTimeMillis());
	}

	@RequestMapping(value = "/getSmsj")
	@ResponseBody
	public Map getSmsj() {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("orderNo", request.getSession().getAttribute("orderNo"));
		result.put("orderTime", request.getSession().getAttribute("orderTime"));
		result.put("price", request.getSession().getAttribute("price"));
		result.put("sn", request.getSession().getAttribute("sn"));
		String llqxx = request.getHeader("User-Agent");
		if (llqxx.contains("MicroMessenger")) {
			result.put("isWx", true);
		} else {
			result.put("isWx", false);
		}
		return result;
	}

	@RequestMapping(value = "/xgyx")
	@ResponseBody
	public Map xgyx(String yx) {
		Map<String, Object> result = new HashMap<String, Object>();
		Map params = new HashMap<>();
		params.put("ddh", (String) request.getSession().getAttribute("orderNo"));
		params.put("tqm", (String) request.getSession().getAttribute("tqm"));
		Smtq smtq = smtqService.findOneByParams(params);
		Tqmtq tqmtq = tqmtqService.findOneByParams(params);
		if (null!=params.get("ddh")) {
			smtq.setYx(yx);
			smtqService.save(smtq);
		}else if (null!=params.get("tqm")) {
			tqmtq.setGfemail(yx);
			tqmtqService.save(tqmtq);
		}
		result.put("num", "1");
		return result;
	}

	@RequestMapping(value = "/save")
	@ResponseBody
	@Transactional
	public Map save(String fptt, String nsrsbh, String dz, String dh, String khh, String khhzh, String yx, String sj)
			throws Exception {
		Map<String, Object> result = new HashMap<>();
		String ddh = (String) request.getSession().getAttribute("orderNo");
		String openid = (String) request.getSession().getAttribute("openid");

		logger.info(openid);
		if ("".equals(ddh)) {
			result.put("msg", "1");
			return result;
		}
		Map params = new HashMap<>();
		params.put("gsdm", "sqj");
		Gsxx gsxx = gsxxservice.findOneByParams(params);
		Map map = new HashMap<>();
		map.put("ddh", ddh);
		Smtq smtq1 = smtqService.findOneByParams(map);
		Smtq smtq;
		if (null != smtq1) {
			smtq = smtq1;
		} else {
			smtq = new Smtq();
		}
		smtq.setDdh((String) request.getSession().getAttribute("orderNo"));
		smtq.setKpddm((String) request.getSession().getAttribute("sn"));
		smtq.setJylssj(
				new SimpleDateFormat("yyyyMMddHHmmss").parse((String) request.getSession().getAttribute("orderTime1")));
		smtq.setZje(Double.parseDouble((String) request.getSession().getAttribute("price")));
		smtq.setGfmc(fptt);
		smtq.setNsrsbh(nsrsbh);
		smtq.setDz(dz);
		smtq.setDh(dh);
		smtq.setKhh(khh);
		smtq.setKhhzh(khhzh);
		smtq.setYx(yx);
		smtq.setSj(sj);
		smtq.setFpzt("07");
		smtq.setYxbz("1");
		smtq.setGsdm(gsxx.getGsdm());
		if (openid != null && !"null".equals(openid)) {
			smtq.setOpenid(openid);
		}
		smtq.setLrsj(new Date());
		smtqService.save(smtq);
		/*
		 * Cszb cszb = this.getCszb("sqj", null, null, "sflxkp"); if (null !=
		 * cszb && "是".equals(cszb.getCsz())) { Map map2 = new HashMap<>();
		 * map2.put("gsdm", "sqj"); map2.put("kpddm", (String)
		 * request.getSession().getAttribute("sn")); Skp skp =
		 * skpService.findOneByParams(map2); if (null != skp &&
		 * !"".equals(skp.getXfid())) { Xf xf =
		 * xfService.findOne(skp.getXfid()); if (null != xf) { Jyls jyls = new
		 * Jyls(); jyls.setClztdm("03"); jyls.setDdh((String)
		 * request.getSession().getAttribute("orderNo")); jyls.setGfsh(nsrsbh);
		 * 
		 * jyls.setGfmc(fptt); jyls.setGfyh(khh); jyls.setGfyhzh(khhzh);
		 * jyls.setGflxr(""); jyls.setBz(""); jyls.setGfemail(yx);
		 * jyls.setGfdz(dz); String tqm = (String)
		 * request.getSession().getAttribute("orderNo");
		 * 
		 * if (StringUtils.isNotBlank(tqm)) { Map params2 = new HashMap<>();
		 * params2.put("gsdm", "sqj"); params2.put("tqm", tqm); Jyls tmp =
		 * jylsService.findByTqm(params2); if (tmp != null) {
		 * result.put("failure", true); result.put("xx", "离线开票失败,提取码已经存在");
		 * return result; } }
		 * 
		 * jyls.setTqm(tqm); jyls.setJylsh("SQJ" + new
		 * SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));
		 * jyls.setJshj(Double.parseDouble((String)
		 * request.getSession().getAttribute("price"))); jyls.setYkpjshj(0.00);
		 * jyls.setHsbz("1"); jyls.setXfid(xf.getId());
		 * jyls.setXfsh(xf.getXfsh()); jyls.setXfmc(xf.getXfmc()); //
		 * jyls.setXfid(331); // jyls.setXfmc("上海百旺测试盘");
		 * jyls.setXfsh(xf.getXfsh()); jyls.setLrsj(new Date());
		 * jyls.setXgsj(new Date()); jyls.setJylssj(new Date());
		 * jyls.setFpzldm("12"); jyls.setFpczlxdm("11");
		 * jyls.setXfyh(xf.getXfyh()); jyls.setXfyhzh(xf.getXfyhzh());
		 * jyls.setXfdz(xf.getXfdz()); jyls.setXfdh(xf.getXfdh()); if
		 * (StringUtils. isNotBlank(jyls.getGfemail())) { jyls.setSffsyj("1"); }
		 * jyls.setKpr(xf.getKpr()); jyls.setFhr(xf.getFhr());
		 * jyls.setSkr(xf.getSkr()); jyls.setSsyf(new
		 * SimpleDateFormat("yyyyMM").format(new Date())); Map map3 = new
		 * HashMap<>(); map3.put("gsdm", "zydc"); Yh yh =
		 * yhService.findOneByParams(map3); jyls.setLrry(yh.getId());
		 * jyls.setXgry(yh.getId()); jyls.setYxbz("1"); jyls.setGsdm("sqj");
		 * jyls.setSkpid(skp.getId()); Jyspmx jyspmx = new Jyspmx();
		 * jyspmx.setSpmxxh(1); jyspmx.setFphxz("0"); jyspmx.setSpmc("餐饮费");
		 * jyspmx.setSpdm("1010101070000000000");
		 * jyspmx.setSpje(Double.parseDouble((String)
		 * request.getSession().getAttribute("price"))); jyspmx.setSpsl(0.06);
		 * jyspmx.setLrsj(new Date()); jyspmx.setXgsj(new Date());
		 * jyspmx.setLrry(yh.getId()); jyspmx.setXgry(yh.getId());
		 * jyspmx.setGsdm("sqj"); jyspmx.setSkpid(skp.getId());
		 * jylsService.save(jyls); jyspmx.setDjh(jyls.getDjh());
		 * jyspmx.setJshj(Double.parseDouble((String)
		 * request.getSession().getAttribute("price")));
		 * jyspmxService.save(jyspmx); smtq.setFpzt("08");
		 * smtqService.save(smtq); } else { result.put("failure", true);
		 * result.put("xx", "离线开票失败,无销方信息"); } } else { result.put("failure",
		 * true); result.put("xx", "离线开票失败,无税控盘信息"); } }
		 */
		result.put("failure", false);
		result.put("msg", "2");
		return result;
	}

	@RequestMapping(value = "/getZje")
	@ResponseBody
	public Map getZje() {
		Map<String, Object> result = new HashMap<>();
		result.put("zje", request.getSession().getAttribute("price"));
		return result;
	}

	@RequestMapping(value = "/getmsg")
	@ResponseBody
	public Map getMsg() {
		Map<String, Object> result = new HashMap<>();
		result.put("msg", request.getSession().getAttribute("msg"));
		result.put("clztdm", request.getSession().getAttribute("clztdm"));
		return result;
	}

	@RequestMapping(value = "/fpsession")
	@ResponseBody
	public Map fpsession() {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("djh", request.getSession().getAttribute("djh"));
		result.put("pdfdz", request.getSession().getAttribute("pdfdzs"));
		return result;
	}

	@RequestMapping(value = "/saveOpenid")
	@ResponseBody
	public Map saveOpenid(String openid) {
		Map<String, Object> result = new HashMap<String, Object>();
		session.setAttribute("openid", openid);
		Integer djh = (Integer) session.getAttribute("djh");
		if (djh != null && openid != null && !openid.equals("null")) {
			Map<String, Object> param = new HashMap<>();
			param.put("djh", djh);
			param.put("unionid", openid);
			Fpj fpj = fpjService.findOneByParams(param);
			if (fpj == null) {
				fpj = new Fpj();
				fpj.setDjh(djh);
				fpj.setUnionid(openid);
				fpj.setYxbz("1");
				fpj.setLrsj(new Date());
				fpj.setXgsj(new Date());
				fpjService.save(fpj);
			}
		}
		return result;
	}

	public Cszb getCszb(String gsdm, Integer xfid, Integer kpdid, String csm) {
		Map params = new HashMap<>();
		params.put("gsdm", gsdm);
		params.put("xfid", xfid);
		params.put("kpdid", kpdid);
		params.put("csm", csm);
		List<Cszb> list = new ArrayList<>();
		list = cszbService.findAllByParams(params);
		if (list.size() == 1) {
			return list.get(0);
		} else if (list.size() == 0) {
			return new Cszb();
		} else {
			return list.get(0);
		}
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

	// 食其家 采用提取码提取方式
	// 跳转到sqj提取码提取页面
	@RequestMapping(value = "/sqj")
	@ResponseBody
	public void tqmtq() throws Exception {
		Map<String, Object> params = new HashMap<>();
		params.put("gsdm", "sqj");
		String str = "sqj";
		Gsxx gsxx = gsxxservice.findOneByParams(params);
    	if (gsxx.getWxappid() == null || gsxx.getWxsecret() == null) {
			gsxx.setWxappid(APP_ID);
			gsxx.setWxsecret(SECRET);
		}
		String ua = request.getHeader("user-agent").toLowerCase();
		if (ua.indexOf("micromessenger") > 0) {
			String url = HtmlUtils.getBasePath(request);
			String openid = String.valueOf(session.getAttribute("openid"));
			if (openid == null || "null".equals(openid)) {
				String ul = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + gsxx.getWxappid() + "&redirect_uri="
						+ url + "/dzfp_sqj/getWx&" + "response_type=code&scope=snsapi_base&state=" + str
						+ "#wechat_redirect";
				response.sendRedirect(ul);
				return;
			}else{
				response.sendRedirect(request.getContextPath() + "/smtq/sqj.html?_t=" + System.currentTimeMillis());
				return;
			}
		}else{
			response.sendRedirect(request.getContextPath() + "/smtq/sqj.html?_t=" + System.currentTimeMillis());
			return;
		}
		
	}

	// 食其家 寿司采用提取码提取方式
	// 跳转到sqj提取码提取页面
	@RequestMapping(value = "/ss")
	@ResponseBody
	public void tqmsstq() throws Exception {
		Map<String, Object> params = new HashMap<>();
		params.put("gsdm", "sqj");
		String str = "bss";
		Gsxx gsxx = gsxxservice.findOneByParams(params);
    	if (gsxx.getWxappid() == null || gsxx.getWxsecret() == null) {
			gsxx.setWxappid(APP_ID);
			gsxx.setWxsecret(SECRET);
		}
		String ua = request.getHeader("user-agent").toLowerCase();
		if (ua.indexOf("micromessenger") > 0) {
			String url = HtmlUtils.getBasePath(request);
			String openid = String.valueOf(session.getAttribute("openid"));
			if (openid == null || "null".equals(openid)) {
				String ul = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + gsxx.getWxappid() + "&redirect_uri="
						+ url + "/dzfp_sqj/getWx&" + "response_type=code&scope=snsapi_base&state=" + str
						+ "#wechat_redirect";
				response.sendRedirect(ul);
				return;
			}else{
				response.sendRedirect(request.getContextPath() + "/smtq/sqjss.html?_t=" + System.currentTimeMillis());
				return;
			}
		}else{
			response.sendRedirect(request.getContextPath() + "/smtq/sqjss.html?_t=" + System.currentTimeMillis());
			return;
		}
		
	}

	// 食其家 寿司采用提取码提取方式
	// 跳转到sqj提取码提取页面
	@RequestMapping(value = "/wdm")
	@ResponseBody
	public void tqmwdmtq() throws Exception {
		Map<String, Object> params = new HashMap<>();
		params.put("gsdm", "sqj");
		String str = "wdm";
		Gsxx gsxx = gsxxservice.findOneByParams(params);
    	if (gsxx.getWxappid() == null || gsxx.getWxsecret() == null) {
			gsxx.setWxappid(APP_ID);
			gsxx.setWxsecret(SECRET);
		}
		String ua = request.getHeader("user-agent").toLowerCase();
		if (ua.indexOf("micromessenger") > 0) {
			String url = HtmlUtils.getBasePath(request);
			String openid = String.valueOf(session.getAttribute("openid"));
			if (openid == null || "null".equals(openid)) {
				String ul = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + gsxx.getWxappid() + "&redirect_uri="
						+ url + "/dzfp_sqj/getWx&" + "response_type=code&scope=snsapi_base&state=" + str
						+ "#wechat_redirect";
				response.sendRedirect(ul);
				return;
			}else{
				response.sendRedirect(request.getContextPath() + "/smtq/sqjwdm.html?_t=" + System.currentTimeMillis());
				return;
			}
		}else{
			response.sendRedirect(request.getContextPath() + "/smtq/sqjwdm.html?_t=" + System.currentTimeMillis());
			return;
		}
		
	}

	// 校验提取码是否正确
	@RequestMapping(value = "/tqyz")
	@ResponseBody
	public Map<String, Object> tqyz(String tqm, String code, String je) {
		String sessionCode = (String) session.getAttribute("rand");
		String openid = (String) session.getAttribute("openid");
		Map<String, Object> result = new HashMap<String, Object>();
		if (code != null && sessionCode != null && code.equals(sessionCode)) {
			Map mapo = new HashMap<>();
			mapo.put("ddh", tqm);
			mapo.put("gsdm", "sqj");
			Smtq smtq1 = smtqService.findOneByParams(mapo);
			Map map = new HashMap<>();
			map.put("tqm", tqm);
			Jyxx jyxxtq = jyxxservice.findOneByParams(map);
			map.put("je", je);
			map.put("gsdm", "sqj");
			Jyxx jyxx = jyxxservice.findOneByParams(map);
			Tqmtq tqmtq = tqmtqService.findOneByParams(map);
			Jyls jyls = jylsService.findOne(map);
			List<Kpls> list = jylsService.findByTqm(map);
			if (list.size() > 0) {
				if (openid != null && !"null".equals(openid)) {
					Map<String, Object> params = new HashMap<>();
					params.put("djh", jyls.getDjh());
					params.put("unionid", openid);
					Fpj fpj = fpjService.findOneByParams(params);
					if (fpj == null) {
						fpj = new Fpj();
						fpj.setDjh(jyls.getDjh());
						fpj.setUnionid(openid);
						fpj.setYxbz("1");
						fpj.setLrsj(new Date());
						fpj.setXgsj(new Date());
						fpjService.save(fpj);
					}
				}
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
			} else if (null != jyls && null != jyls.getDjh()) {
				result.put("num", "6");
			} else if (null != tqmtq && null != tqmtq.getId()) {
				result.put("num", "7");
			} else if (null != smtq1 && null != smtq1.getId()) {
				result.put("num", "6");
			} else if (null != jyxxtq && null == jyxx) {
				result.put("num", "9");
			} else if (null != jyxx && null != jyxx.getId()) {
				request.getSession().setAttribute("tqm", tqm);
				request.getSession().setAttribute("je", je);
				request.getSession().setAttribute("jyxx", jyxx);
				request.getSession().setAttribute("ppjg", "1");
				result.put("num", "5");
			} else {
				request.getSession().setAttribute("tqm", tqm);
				request.getSession().setAttribute("je", je);
				request.getSession().setAttribute("ppjg", "0");
				result.put("num", "5");
			}
		} else {
			result.put("num", "4");
		}
		return result;
	}

	// 获取购方信息,保存到交易流水
	@RequestMapping(value = "/saveLs")
	@ResponseBody
	@Transactional
	public Map<String, Object> saveLs(String fptt, String nsrsbh, String dz, String dh, String khh, String khhzh,
			String yx, String sj) {
		Map<String, Object> result = new HashMap<String, Object>();
		String tqm = String.valueOf(request.getSession().getAttribute("tqm"));
		String openid = String.valueOf(request.getSession().getAttribute("openid"));
		if (null == tqm || "".equals(tqm)) {
			result.put("msg", "1");
			return result;
		}
		Map map = new HashMap<>();
		map.put("tqm", tqm);
		map.put("je", String.valueOf(request.getSession().getAttribute("je")));
		map.put("gsdm", "sqj");
		Tqmtq tqmtq = tqmtqService.findOneByParams(map);
		Jyls jyls1 = jylsService.findOne(map);
		if (null != tqmtq && null != tqmtq.getId()) {
			result.put("msg", "该提取码已提交过申请!");
			return result;
		}
		String ppjg = String.valueOf(request.getSession().getAttribute("ppjg"));
		if ("1".equals(ppjg)) {
			Jyxx jyxx = (Jyxx) request.getSession().getAttribute("jyxx");
			Map map2 = new HashMap<>();
			map2.put("gsdm", "sqj");
			map2.put("kpddm", jyxx.getStoreNo());
			Skp skp = skpService.findOneByParams(map2);
			if (null != skp && !"".equals(skp.getXfid())) {
				Xf xf = xfService.findOne(skp.getXfid());
				if (null != xf) {
					Jyls jyls = new Jyls();
					jyls.setClztdm("03");
					jyls.setDdh(jyxx.getOrderNo());
					jyls.setGfsh(nsrsbh);
					jyls.setGfsjh(sj);
					jyls.setGfmc(fptt);
					jyls.setGfyh(khh);
					jyls.setGfyhzh(khhzh);
					jyls.setGflxr("");
					// jyls.setBz("");
					jyls.setGfemail(yx);
					jyls.setGfdz(dz);
					jyls.setTqm(jyxx.getOrderNo());
					jyls.setJylsh("SQJ" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));
					jyls.setJshj(jyxx.getPrice());
					jyls.setYkpjshj(0.00);
					jyls.setHsbz("1");
					jyls.setXfid(xf.getId());
					jyls.setXfsh(xf.getXfsh());
					jyls.setXfmc(xf.getXfmc());
					// jyls.setXfid(331);
					// jyls.setXfmc("上海百旺测试盘");
					jyls.setXfsh(xf.getXfsh());
					jyls.setLrsj(new Date());
					jyls.setXgsj(new Date());
					jyls.setJylssj(new Date());
					jyls.setFpzldm("12");
					jyls.setFpczlxdm("11");
					jyls.setBz(jyxx.getStoreNo());
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
					Sp sp1 = new Sp();
					sp1.setGsdm("sqj");
					Sp sp = spService.findOneByParams(sp1);
					if (null != sp) {
						jyspmx.setSpmc(sp.getSpmc());
					} else {
						jyspmx.setSpmc("餐饮服务");
					}
					jyspmx.setSpdm("1010101070000000000");
					jyspmx.setSpje(jyxx.getPrice());
					jyspmx.setSpsl(0.06);
					jyspmx.setLrsj(new Date());
					jyspmx.setXgsj(new Date());
					jyspmx.setLrry(yh.getId());
					jyspmx.setXgry(yh.getId());
					jyspmx.setGsdm("sqj");
					jyspmx.setSkpid(skp.getId());
					jylsService.save(jyls);
					if (openid != null && !"null".equals(openid)) {
						Fpj fpj = new Fpj();
						fpj.setDjh(jyls.getDjh());
						fpj.setUnionid(openid);
						fpj.setYxbz("1");
						fpj.setLrsj(new Date());
						fpj.setXgsj(new Date());
						fpjService.save(fpj);
					}
					jyspmx.setDjh(jyls.getDjh());
					jyspmx.setJshj(jyxx.getPrice());
					jyspmxService.save(jyspmx);
					result.put("msg", "1");
				} else {
					result.put("msg", "门店号为查询到销方!");
				}

			} else {
				result.put("msg", "未查询到门店号!");
			}
		} else {
			Tqmtq tqmtq1 = new Tqmtq();
			tqmtq1.setDdh(tqm);
			tqmtq1.setLrsj(new Date());
			tqmtq1.setZje(Double.valueOf(String.valueOf(request.getSession().getAttribute("je"))));
			tqmtq1.setGfmc(fptt);
			tqmtq1.setNsrsbh(nsrsbh);
			tqmtq1.setDz(dz);
			tqmtq1.setDh(dh);
			tqmtq1.setKhh(khh);
			tqmtq1.setKhhzh(khhzh);
			tqmtq1.setFpzt("0");
			tqmtq1.setYxbz("1");
			tqmtq1.setGfsjh(sj);
			tqmtq1.setGfemail(yx);
			tqmtq1.setGsdm("sqj");
			if (openid != null && !"null".equals(openid)) {
				tqmtq1.setOpenid(openid);
			}
			tqmtqService.save(tqmtq1);
			result.put("msg", "1");
		}
		/*
		 * Map map2 = new HashMap<>(); map2.put("gsdm", "sqj");
		 * map2.put("kpddm", jyxx.getStoreNo()); Skp skp =
		 * skpService.findOneByParams(map2); if (null != skp &&
		 * !"".equals(skp.getXfid())) { Xf xf =
		 * xfService.findOne(skp.getXfid()); if (null != xf) { Jyls jyls = new
		 * Jyls(); jyls.setClztdm("03"); jyls.setDdh(jyxx.getOrderNo());
		 * jyls.setGfsh(nsrsbh);
		 * 
		 * jyls.setGfmc(fptt); jyls.setGfyh(khh); jyls.setGfyhzh(khhzh);
		 * jyls.setGflxr(""); jyls.setBz(""); jyls.setGfemail(yx);
		 * jyls.setGfdz(dz); String tqm = jyxx.getOrderNo();
		 * 
		 * if (StringUtils.isNotBlank(tqm)) { Map params2 = new HashMap<>();
		 * params2.put("gsdm", "sqj"); params2.put("tqm", tqm); Jyls tmp =
		 * jylsService.findByTqm(params2); if (tmp != null) {
		 * result.put("failure", true); result.put("xx", "离线开票失败,提取码已经存在");
		 * return result; } }
		 * 
		 * jyls.setTqm(tqm); jyls.setJylsh("SQJ" + new
		 * SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));
		 * jyls.setJshj(jyxx.getPrice()); jyls.setYkpjshj(0.00);
		 * jyls.setHsbz("1"); jyls.setXfid(xf.getId());
		 * jyls.setXfsh(xf.getXfsh()); jyls.setXfmc(xf.getXfmc()); //
		 * jyls.setXfid(331); // jyls.setXfmc("上海百旺测试盘");
		 * jyls.setXfsh(xf.getXfsh()); jyls.setLrsj(new Date());
		 * jyls.setXgsj(new Date()); jyls.setJylssj(new Date());
		 * jyls.setFpzldm("12"); jyls.setFpczlxdm("11");
		 * jyls.setXfyh(xf.getXfyh()); jyls.setXfyhzh(xf.getXfyhzh());
		 * jyls.setXfdz(xf.getXfdz()); jyls.setXfdh(xf.getXfdh()); if
		 * (StringUtils.isNotBlank(jyls.getGfemail())) { jyls.setSffsyj("1"); }
		 * jyls.setKpr(xf.getKpr()); jyls.setFhr(xf.getFhr());
		 * jyls.setSkr(xf.getSkr()); jyls.setSsyf(new
		 * SimpleDateFormat("yyyyMM").format(new Date())); Map map3 = new
		 * HashMap<>(); map3.put("gsdm", "sqj"); Yh yh =
		 * yhService.findOneByParams(map3); jyls.setLrry(yh.getId());
		 * jyls.setXgry(yh.getId()); jyls.setYxbz("1"); jyls.setGsdm("sqj");
		 * jyls.setSkpid(skp.getId()); Jyspmx jyspmx = new Jyspmx();
		 * jyspmx.setSpmxxh(1); jyspmx.setFphxz("0"); jyspmx.setSpmc("餐饮费");
		 * jyspmx.setSpdm("1010101070000000000");
		 * jyspmx.setSpje(jyxx.getPrice()); jyspmx.setSpsl(0.06);
		 * jyspmx.setLrsj(new Date()); jyspmx.setXgsj(new Date());
		 * jyspmx.setLrry(yh.getId()); jyspmx.setXgry(yh.getId());
		 * jyspmx.setGsdm("sqj"); jyspmx.setSkpid(skp.getId());
		 * jylsService.save(jyls); jyspmx.setDjh(jyls.getDjh());
		 * jyspmx.setJshj(jyxx.getPrice()); jyspmxService.save(jyspmx);
		 * result.put("msg", "1"); }else{ result.put("msg", "门店号为查询到销方!"); }
		 * 
		 * }else{ result.put("msg", "未查询到门店号!"); }
		 */
		return result;
	}
}
