package com.rjxx.taxeasy.controller;

import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.domains.Tqjl;
import com.rjxx.taxeasy.service.JylsService;
import com.rjxx.taxeasy.service.JyspmxService;
import com.rjxx.taxeasy.service.KplsService;
import com.rjxx.taxeasy.service.TqjlService;
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
