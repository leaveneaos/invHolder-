package com.rjxx.taxeasy.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpRequest;
import org.hibernate.loader.custom.Return;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.rjxx.comm.web.BaseController;
import com.rjxx.taxeasy.domains.Jyls;
import com.rjxx.taxeasy.domains.Jyspmx;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.domains.Tqjl;
import com.rjxx.taxeasy.service.JylsService;
import com.rjxx.taxeasy.service.JyspmxService;
import com.rjxx.taxeasy.service.KplsService;
import com.rjxx.taxeasy.service.TqjlService;

@Controller
@RequestMapping("/extractInvoice/zydc")

public class FptqController extends BaseController {
    @Autowired
    private JylsService jylsService;
    @Autowired
    private KplsService kplsService;
    @Autowired
    private JyspmxService jyspmxService;
    @Autowired
    private TqjlService tqjlService;
    @RequestMapping
    public String index() {
        return "zydc";
    }

    @RequestMapping(value = "/zydc")
    @ResponseBody
    public Map Fptq(String tqm, String code) {
        String sessionCode = (String) session.getAttribute("rand");
        Map<String, Object> result = new HashMap<String, Object>();
        if (code != null && sessionCode != null && code.equals(sessionCode)) {
            Map map = new HashMap<>();
            map.put("tqm", tqm);
            Jyls jyls = jylsService.findByTqm(map);
            if (null != jyls) {
                result.put("djh", jyls.getDjh());
                result.put("clztdm", jyls.getClztdm());
                request.getSession().setAttribute("djh", jyls.getDjh());
                Kpls kpls = new Kpls();
                kpls.setDjh(jyls.getDjh());
                List<Kpls> list = kplsService.findByDjh(kpls);
                if (list.size() == 0||null==list.get(0).getFpdm()||"".equals(list.get(0).getFpdm())) {
                    result.put("num", "1");              
                }
                else {
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
                    	 result.put("num", "5");
                    	 result.put("msg", msg);
                    	 return result;
					}
                    if (pdfdzs.length() > 0) {
                        result.put("pdfdzs", pdfdzs.substring(0, pdfdzs.length() - 1));
                        request.getSession().setAttribute("pdfdzs",  pdfdzs.substring(0, pdfdzs.length() - 1));
                    }
                    result.put("num", "2");
                    Tqjl tqjl = new Tqjl();
                    tqjl.setDjh(String.valueOf(jyls.getDjh()));
                    tqjl.setTqsj(new Date());
                    String visiterIP=request.getRemoteAddr();//访问者IP  
                    tqjl.setIp(visiterIP);
                    String llqxx =request.getHeader("User-Agent");
                    tqjl.setLlqxx(llqxx);
                    tqjlService.save(tqjl);
                }
            } else {
                result.put("num", "3");
            }
        } else {
            result.put("num", "4");
        }
        return result;
    }

}
