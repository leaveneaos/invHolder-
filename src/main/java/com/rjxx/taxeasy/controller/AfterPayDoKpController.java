package com.rjxx.taxeasy.controller;

import com.alibaba.fastjson.JSON;
import com.rjxx.taxeasy.bizcomm.utils.GetXmlUtil;
import com.rjxx.taxeasy.bizcomm.utils.HttpUtils;
import com.rjxx.taxeasy.domains.Gsxx;
import com.rjxx.taxeasy.domains.Jymxsq;
import com.rjxx.taxeasy.domains.Jyxxsq;
import com.rjxx.taxeasy.domains.Jyzfmx;
import com.rjxx.taxeasy.service.GsxxService;
import com.rjxx.utils.StringUtils;
import com.rjxx.utils.weixin.WechatBatchCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Administrator on 2017-12-27.
 */
@Controller
@RequestMapping("/service")
public class AfterPayDoKpController {

    @Autowired
    protected HttpServletRequest request;
    @Autowired
    private WechatBatchCard wechatBatchCard;
    @Autowired
    private GsxxService gsxxService;
    private static Logger logger = LoggerFactory.getLogger(AfterPayDoKpController.class);

    /**
     * 支付后开票,收到开票请求，获取开票信息
     *
     * @return
     */
    @RequestMapping(value = "/afterPayDoKp", method = RequestMethod.POST)
    public String AfterPayDokp() throws Exception {
        logger.info("收到开票请求");
        String result = "";
        try {
            Map requestMap = wechatBatchCard.parseXml(request);
            logger.info("取到的数据---为" + JSON.toJSONString(requestMap));
            String tqm = requestMap.get("").toString();//提取码
            String gfmc = requestMap.get("title").toString();//名称
            String gfsh = requestMap.get("tax_no").toString();//税号
            String gfdz = requestMap.get("addr").toString();//地址
            String gfdh = requestMap.get("phone").toString();//电话
            String gfyh = requestMap.get("bank_type").toString();//银行类型
            String gfyhzh = requestMap.get("bank_no").toString();//银行账号
            String gfEmali = requestMap.get("Email").toString();//邮箱

            List<Jymxsq> jymxsqList = new ArrayList<>();
            List<Jyzfmx> jyzfmxList = new ArrayList<>();
            //调用接口获取交易数据

            //封装购方信息
            Jyxxsq jyxxsq = new Jyxxsq();
            jyxxsq.setTqm(tqm);
            jyxxsq.setGfmc(gfmc);
            jyxxsq.setGfemail(gfEmali);
            if (StringUtils.isNotBlank(jyxxsq.getGfemail())) {
                jyxxsq.setSffsyj("1");
            }
            jyxxsq.setGfsh(gfsh);
            jyxxsq.setGfdz(gfdz);
            jyxxsq.setGfdh(gfdh);
            jyxxsq.setGfyh(gfyh);
            jyxxsq.setGfyhzh(gfyhzh);
            jyxxsq.setSjly("4");//数据来源--微信
            if(null!=gfsh.trim()&&!"".equals(gfsh.trim())){
                jyxxsq.setGflx("1");
            }else {
                jyxxsq.setGflx("0");
            }
            Map gsMap = new HashMap();
            gsMap.put("gsdm",jyxxsq.getGsdm());
            Gsxx gsxx = gsxxService.findOneByGsdm(gsMap);
            String xml = GetXmlUtil.getFpkjXml(jyxxsq, jymxsqList, jyzfmxList);
            String resultxml = HttpUtils.HttpUrlPost(xml, gsxx.getAppKey(), gsxx.getSecretKey());
            //Map<String, Object> resultMap = XmlUtil.xml2Map(resultxml);
            //String returnMsg=resultMap.get("ReturnMessage").toString();
            //String returnCode=resultMap.get("ReturnCode").toString();
            //if(returnCode!=null &&returnCode.equals("0000")){

            //}else {
            //    result = resultxml;
                return resultxml;
            //}
        } catch (Exception e) {
            e.printStackTrace();
            result = "<Responese>\n  <ReturnCode>9999</ReturnCode>\n  <ReturnMessage>发票开具失败</ReturnMessage>\n<Responese>";
            return result;
        }
    }


}
