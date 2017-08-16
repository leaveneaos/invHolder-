package com.rjxx.taxeasy.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.service.BarcodeService;
import com.rjxx.taxeasy.wechat.dto.Result;
import com.rjxx.taxeasy.wechat.util.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by wangyahui on 2017/8/9 0009
 */
@RestController
@RequestMapping("/scan")
public class ScanController extends BaseController {

    @Autowired
    private BarcodeService barcodeService;

    /**
     * 确认扫码中传递的信息
     */
    @RequestMapping("/scanConfirm")
    public Result smConfirm() {
        String gsdm = (String) session.getAttribute("gsdm");
        String q = (String) session.getAttribute("q");
        if (gsdm == null || q == null) {
            return ResultUtil.error("session过期,请重新扫码");
        }
        String jsonData = barcodeService.getSpxx(gsdm, q);
        if (jsonData != null) {
            return ResultUtil.success(jsonData);//订单号,订单时间,门店号,金额,商品名,商品税率
        } else {
            return ResultUtil.error("二维码信息获取失败");
        }
    }

    /**
     * 提交抬头
     *
     * @param gfmc  购方名称
     * @param gfsh  购方税号
     * @param email 购方邮箱
     */
    @RequestMapping("/submit")
    public Result submit(@RequestParam String gfmc, @RequestParam String gfsh, @RequestParam String email) {
        String gsdm = (String) session.getAttribute("gsdm");
        String q = (String) session.getAttribute("q");
        if (gsdm == null || q == null) {
            return ResultUtil.error("redirect");
        }
        //非必须参数
        String gfdz = request.getParameter("gfdz");
        String gfdh = request.getParameter("gfdh");
        String gfyhzh = request.getParameter("gfyhzh");
        String gfyh = request.getParameter("gfyh");
        String tqm = request.getParameter("tqm");

        //开票
        String status = barcodeService.makeInvoice(gsdm, q, gfmc, gfsh, email, gfyh, gfyhzh, gfdz, gfdh, tqm);
        if ("-1".equals(status)) {
            return ResultUtil.error("开具失败");
        } else if ("0".equals(status)) {
            return ResultUtil.error("所需信息为空");
        } else {
            JSONObject jsonObject = JSON.parseObject(status);
            session.setAttribute("serialorder", jsonObject.getString("serialorder"));
            return ResultUtil.success(status);
        }
    }
}
