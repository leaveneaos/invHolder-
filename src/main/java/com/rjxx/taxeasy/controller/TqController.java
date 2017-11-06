package com.rjxx.taxeasy.controller;
import com.rjxx.taxeasy.comm.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * Created by zsq on 2017-11-02.
 * 公用扫描提取详情页，归入卡包和发票管家
 */
@Controller
@RequestMapping("/tq")
public class TqController extends BaseController{

    @RequestMapping
    @ResponseBody
    public void index() throws Exception{
        String serialOrder = request.getParameter("q");
        request.getSession().setAttribute("serialorder",serialOrder);
        if(serialOrder==null ||"".equals(serialOrder)){
            //获取授权失败
            request.getSession().setAttribute("msg", "扫码信息有误!请重试!");
            response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            return;
        }else {
            response.sendRedirect(request.getContextPath() + "/CO/smfpxq.html?serialOrder="+serialOrder+"&&_t=" + System.currentTimeMillis());
            return;
        }
    }
}
