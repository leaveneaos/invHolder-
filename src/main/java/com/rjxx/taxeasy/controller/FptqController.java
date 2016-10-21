package com.rjxx.taxeasy.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.rjxx.taxeasy.service.JylsService;
import com.rjxx.taxeasy.service.JyspmxService;
import com.rjxx.taxeasy.service.KplsService;

@Controller
@RequestMapping("/extractInvoice")

public class FptqController extends BaseController {
    @Autowired
    private JylsService jylsService;
    @Autowired
    private KplsService kplsService;
    @Autowired
    private JyspmxService jyspmxService;

    @RequestMapping
    public String index() {
        return "index.html";
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
                Kpls kpls = new Kpls();
                kpls.setDjh(jyls.getDjh());
                List<Kpls> list = kplsService.findByDjh(kpls);
                if (list.size() == 0) {
                    result.put("num", "1");
                } else {
                    String pdfdzs = "";
                    for (Kpls kpls2 : list) {
                        pdfdzs += kpls2.getPdfurl().replace(".pdf", ".jpg") + ",";
                    }
                    if (pdfdzs.length() > 0) {
                        result.put("pdfdzs", pdfdzs.substring(0, pdfdzs.length() - 1));
                    }
                    result.put("num", "2");
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
