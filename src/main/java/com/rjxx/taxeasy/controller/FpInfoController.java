package com.rjxx.taxeasy.controller;

import com.rjxx.taxeasy.comm.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by Administrator on 2017-12-07.
 */
@Controller
@RequestMapping("/info")
public class FpInfoController extends BaseController {

    @RequestMapping
    @ResponseBody
    public void index() throws Exception{
        String gsdm = request.getParameter("g");
        String str = request.getParameter("q");
        if(gsdm==null||str==null){
            //获取授权失败
            request.getSession().setAttribute("msg", "扫码信息有误!请重试!");
            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            return;
        }
        request.getSession().setAttribute("gsdm",gsdm);
        if(gsdm.equals("cmsc")||gsdm.equals("hdsc")||gsdm.equals("shssts")){
            request.getSession().setAttribute("khh",str);
        }else {
            request.getSession().setAttribute("tqm",str);
        }
        response.sendRedirect(request.getContextPath() + "/CO/dzfpxq.html?_t=" + System.currentTimeMillis());
        return;
    }

}
