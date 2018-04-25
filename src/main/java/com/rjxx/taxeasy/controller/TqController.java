package com.rjxx.taxeasy.controller;
import com.alibaba.fastjson.JSON;
import com.rjxx.taxeasy.bizcomm.utils.InvoiceQueryUtil;
import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.service.GsxxService;
import com.rjxx.taxeasy.service.JylsService;
import com.rjxx.taxeasy.service.KplsService;
import com.rjxx.taxeasy.vo.Fpcxvo;
import com.rjxx.utils.weixin.wechatFpxxServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by zsq on 2017-11-02.
 * 公用扫描提取详情页，归入卡包和发票管家
 */
@Controller
@RequestMapping("/tq")
public class TqController extends BaseController{


    @Autowired
    private InvoiceQueryUtil invoiceQueryUtil;
    @Autowired
    private KplsService kplsService;
    @Autowired
    private JylsService jylsService;
    @Autowired
    private wechatFpxxServiceImpl wechatFpxxService;
    @Autowired
    private GsxxService gsxxService;

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


    /**
     * 获取发票信息
     * @return
     */
    @RequestMapping(value = "/fpxx")
    @ResponseBody
    public Map tjsession() {
        Map<String, Object> result = new HashMap<String, Object>();
//        if(null!=request.getSession().getAttribute("khh")&&(request.getSession().getAttribute("gsdm").equals("cmsc")
//          || request.getSession().getAttribute("gsdm").equals("hdsc")||request.getSession().getAttribute("gsdm").equals("shssts"))){
        if(session.getAttribute("khh")!=null&&session.getAttribute("gsdm")!=null){
            String khh= request.getSession().getAttribute("khh").toString();
            String gsdm= request.getSession().getAttribute("gsdm").toString();
            List<Fpcxvo> invoiceListByKhh = invoiceQueryUtil.getInvoiceListByKhh(gsdm,khh);
            logger.info("获取到的khh"+khh+"和公司代码"+gsdm);
            request.getSession().setAttribute("kplsList",invoiceListByKhh);
            if(invoiceListByKhh.size()==0){
                result.put("msg","0");
                return result;
            }
            result.put("khh", request.getSession().getAttribute("khh"));
            result.put("kplsList",invoiceListByKhh);
        }else if(null!=request.getSession().getAttribute("tqm")){
            String tqm = request.getSession().getAttribute("tqm").toString();
            String gsdm= request.getSession().getAttribute("gsdm").toString();
            logger.info("获取到的tqm"+tqm+"和公司代码"+gsdm);
            List<Fpcxvo> invoiceListBytqm = invoiceQueryUtil.getInvoiceListBytqm(gsdm, tqm);
            request.getSession().setAttribute("kplsList",invoiceListBytqm);
            if(invoiceListBytqm.size()==0){
                result.put("msg","0");
                return result;
            }
            result.put("kplsList",invoiceListBytqm);
            result.put("tqm", request.getSession().getAttribute("tqm"));
        }else {
            result.put("msg","0");
        }
        return result;
    }

    /**
     * 获取发票信息PDF
     * @param i
     * @return
     */
    @RequestMapping(value = "/dzxfpxx")
    @ResponseBody
    public Map dzxfpxx(Integer i){
        Map<String, Object> result = new HashMap<String, Object>();
        if(i == null){
            result.put("msg","0");
            return result;
        }else {
            String openid = (String) session.getAttribute("openid");
            List<Fpcxvo> kplsList = (List) request.getSession().getAttribute("kplsList");
            if(request.getSession().getAttribute("kplsList")==null){
                result.put("msg","0");
                return result;
            }
            if(i>kplsList.size()){
                result.put("msg","2");
                return result;
            }
            String pdfdz = kplsList.get(i).getPdfurl().replace(".pdf",".jpg");
            result.put("pdfdzs",pdfdz);
            result.put("kpls",kplsList);
//            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            result.put("kprq",kplsList.get(i).getKprq());
            result.put("price",kplsList.get(i).getPrice());
            result.put("gsdm",kplsList.get(i).getGsdm());
            result.put("serialorder",kplsList.get(i).getSerialorder());
            request.getSession().setAttribute("serialorder",kplsList.get(i).getSerialorder());
            result.put("orderTime",kplsList.get(i).getKprq2());
            String tqm="";
            if(kplsList.get(i).getTqm()!=null && !kplsList.get(i).getTqm().equals("")){
                tqm=kplsList.get(i).getTqm();
            }else if(kplsList.get(i).getKhh()!=null && !kplsList.get(i).getKhh().equals("")){
                tqm=kplsList.get(i).getKhh();
            }else {
                result.put("msg","2");
                return result;
            }
            String orderNo="";
           //德克士、一茶一坐     订单号(提取码 = 品牌+订单号)
           if(request.getSession().getAttribute("gsdm").equals("dicos")
                    ||request.getSession().getAttribute("gsdm").equals("chamate")){
                if(null==request.getSession().getAttribute("orderNo")){
                    result.put("msg","2");
                    return result;
                }
                if(kplsList.get(i).getKplsh2().indexOf(",")<0){
                    orderNo = request.getSession().getAttribute("orderNo").toString();
                }else {
                    orderNo = kplsList.get(i).getOrderNo();
                }
            }
            //上师大、崇明水厂、瀚达水厂   客户号
            else if(kplsList.get(i).getGsdm().equals("shssts") ||
                    kplsList.get(i).getGsdm().equals("cmsc") ||
                    kplsList.get(i).getGsdm().equals("hdsc")){
                orderNo = kplsList.get(i).getOrderNo();
                logger.info(JSON.toJSONString(kplsList.get(i)));
            }
            // 其他  提取码
            else {
                if(kplsList.get(i).getKplsh2().indexOf(",")<0){
                    orderNo=tqm;
                }else {
                    orderNo = kplsList.get(i).getOrderNo();
                }
            }
            boolean b = wechatFpxxService.InFapxx(tqm, kplsList.get(i).getGsdm(), orderNo,
                    "", "2", openid, "", kplsList.get(i).getKplsh2(), request);
            if(!b){
                result.put("msg","1");
                return result;
            }
            result.put("orderNo",orderNo);
            result.put("gsdm",kplsList.get(i).getGsdm());
        }
        return result;
    }
}
