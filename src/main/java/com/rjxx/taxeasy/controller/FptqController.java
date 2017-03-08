package com.rjxx.taxeasy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.domains.Fpj;
import com.rjxx.taxeasy.domains.Gsxx;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.domains.Tqjl;
import com.rjxx.taxeasy.service.FpjService;
import com.rjxx.taxeasy.service.GsxxService;
import com.rjxx.taxeasy.service.JylsService;
import com.rjxx.taxeasy.service.TqjlService;
import com.rjxx.utils.HtmlUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/extractInvoice/zydc")

public class FptqController extends BaseController {
    @Autowired
    private JylsService jylsService;
    @Autowired
    private TqjlService tqjlService;
    @Autowired
    private FpjService fpjService;
    @Autowired
    private GsxxService gsxxService;

	public static final String APP_ID = "wx9abc729e2b4637ee";

	public static final String SECRET = "6415ee7a53601b6a0e8b4ac194b382eb";

    @RequestMapping
    @ResponseBody
    public String index() throws IOException {
    	Map<String, Object> params = new HashMap<>();
		params.put("gsdm", "zydc");
		String str = "zydc";
		Gsxx gsxx = gsxxService.findOneByParams(params);
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
						+ url + "extractInvoice/zydc/getWx&" + "response_type=code&scope=snsapi_base&state=" + str
						+ "#wechat_redirect";
				logger.info(ul);
				response.sendRedirect(ul);
				return null;
			}else{
				response.sendRedirect(request.getContextPath() + "/zydc.html?_t=" + System.currentTimeMillis());
		        return null;
			}
		}
		response.sendRedirect(request.getContextPath() + "/zydc.html?_t=" + System.currentTimeMillis());
        
        return null;
        // return "redirect:/zydc.html";
    }
    
    @RequestMapping(value = "/getWx")
   	@ResponseBody
   	public void getWx(String state, String code) throws IOException {
   		Map params = new HashMap<>();
   		params.put("gsdm", "wljqr");
   		Gsxx gsxx = gsxxService.findOneByParams(params);
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
   					map.put("success", true);
   				}
   			}
   		} catch (Exception e) {
   			e.printStackTrace();
   		} finally {
   			// 关闭连接 ,释放资源
   			client.getConnectionManager().shutdown();
   		}
   		response.sendRedirect(request.getContextPath() + "/zydc.html?_t=" + System.currentTimeMillis());
   		return;
   	}
    
    @RequestMapping(value = "/{tqm}",method = RequestMethod.GET)
    @ResponseBody
    public String tqm(@PathVariable ("tqm")String tqm) throws IOException {
        response.sendRedirect(request.getContextPath() + "/zydc.html?tqm="+tqm+"&_t=" + System.currentTimeMillis());
        return null;
        // return "redirect:/zydc.html";
    }
    
    @RequestMapping(value = "/zydc")
    @ResponseBody
    public Map Fptq(String tqm, String code) {
        String sessionCode = (String) session.getAttribute("rand");
        String openid = (String)session.getAttribute("openid");
        Map<String, Object> result = new HashMap<String, Object>();
        if (code != null && sessionCode != null && code.equals(sessionCode)) {
            Map map = new HashMap<>();
            map.put("tqm", tqm);
            map.put("gsdm", "zydc");
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
                if (openid != null && !"null".equals(openid)) {
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

}
