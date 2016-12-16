package com.rjxx.taxeasy.controller;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.domains.Fpj;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.domains.Tqjl;
import com.rjxx.taxeasy.service.FpjService;
import com.rjxx.taxeasy.service.JylsService;
import com.rjxx.taxeasy.service.TqjlService;
@Controller
@RequestMapping("dzfp_wljqr")
public class WljqrController extends BaseController{

    @Autowired
    private JylsService jylsService;
    @Autowired
    private TqjlService tqjlService;
    @Autowired
    private FpjService fpjService;

    @RequestMapping
    @ResponseBody
    public String index() throws IOException {
        response.sendRedirect(request.getContextPath() + "/wljqr.html?_t=" + System.currentTimeMillis());
        return null;
        // return "redirect:/zydc.html";
    }
    
    @RequestMapping(value = "/{tqm}",method = RequestMethod.GET)
    @ResponseBody
    public String tqm(@PathVariable ("tqm")String tqm) throws IOException {
        response.sendRedirect(request.getContextPath() + "/wljqr.html?tqm="+tqm+"&_t=" + System.currentTimeMillis());
        return null;
        // return "redirect:/zydc.html";
    }
    
    @RequestMapping(value = "/fptq")
    @ResponseBody
    public Map Fptq(String tqm, String code) {
        String sessionCode = (String) session.getAttribute("rand");
        String openid = (String)session.getAttribute("openid");
        Map<String, Object> result = new HashMap<String, Object>();
        if (code != null && sessionCode != null && code.equals(sessionCode)) {
            Map map = new HashMap<>();
            map.put("tqm", tqm);
            map.put("gsdm", "wljqr");
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
                	Fpj fpj = new Fpj();
					fpj.setDjh(list.get(0).getDjh());
					fpj.setUnionid(openid);
					fpj.setYxbz("1");
					fpj.setLrsj(new Date());
					fpj.setXgsj(new Date());
					fpjService.save(fpj);
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
