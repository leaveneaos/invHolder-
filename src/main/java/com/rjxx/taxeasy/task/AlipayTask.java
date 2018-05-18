package com.rjxx.taxeasy.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rjxx.taxeasy.dao.SkpJpaDao;
import com.rjxx.taxeasy.dao.XfJpaDao;
import com.rjxx.taxeasy.domains.Skp;
import com.rjxx.taxeasy.domains.WxFpxx;
import com.rjxx.taxeasy.domains.Xf;
import com.rjxx.taxeasy.dto.AdapterDataOrderBuyer;
import com.rjxx.taxeasy.service.adapter.AdapterService;
import com.rjxx.utils.RJCheckUtil;
import com.rjxx.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author wangyahui
 * @email wangyahui@datarj.com
 * @company 上海容津信息技术有限公司
 * @date 2018/5/16
 */
public class AlipayTask implements Runnable{
    private static Logger logger = LoggerFactory.getLogger(AlipayTask.class);

    private AdapterService adapterService;
    private XfJpaDao xfJpaDao;
    private SkpJpaDao skpJpaDao;
    private WxFpxx wxFpxx;
    private Map alipayResultMap;

    @Override
    public void run() {
        if(wxFpxx.getApitype()!=null){
            String apiTpye = wxFpxx.getApitype();
            String gfyh=(String) alipayResultMap.get("payerBankName");
            String gfyhzh=(String) alipayResultMap.get("payerBankAccount");
            String gfdh=(String) alipayResultMap.get("payerTelphone");
            String gfdz=(String) alipayResultMap.get("payerAddress");
            String gfsh=(String) alipayResultMap.get("payerRegisterNo");
            String gfmc=(String) alipayResultMap.get("payerName");
            //FIXME
//            String email=(String) alipayResultMap.get("email");
            //购方信息获取
            if("1".equals(apiTpye)){
                Map map = RJCheckUtil.decodeForAll(wxFpxx.getQ());
                String data = (String) map.get("A0");
                JSONObject jsonData = JSON.parseObject(data);
                String on = jsonData.getString("on");
                AdapterDataOrderBuyer buyer = new AdapterDataOrderBuyer();
                buyer.setBank(gfyh);
                buyer.setBankAcc(gfyhzh);
                buyer.setTelephoneNo(gfdh);
                buyer.setAddress(gfdz);
//                buyer.setEmail(email);
                buyer.setIdentifier(gfsh);
                buyer.setName(gfmc);
                boolean b = adapterService.sendBuyer(wxFpxx.getGsdm(), on, buyer);
                if(!b){
                    String re = "抬头发送失败，请重试！";
//                    weixinUtils.jujuekp(SuccOrderId, re, access_token);
                    return;
                }
                //离线开票
            }else {
                String re;
                String status="";
                if("2".equals(apiTpye)){
                    status=adapterService.makeInvoice(wxFpxx.getGsdm(), wxFpxx.getQ(), gfmc, gfsh, null, gfyh, gfyhzh, gfdz, gfdh, wxFpxx.getTqm(), wxFpxx.getOpenId(), "5", null, null);
                    //抽数据开票
                }else if("3".equals(apiTpye)){
                    logger.info("apiType="+apiTpye);
                    Map map = RJCheckUtil.decodeForAll(wxFpxx.getQ());
                    String data = (String) map.get("A0");
                    JSONObject jsonData = JSON.parseObject(data);
                    String on = jsonData.getString("on");
                    String sn = jsonData.getString("sn");
                    String tq = jsonData.getString("tq");
                    String orderNo = "";
                    String extractCode = "";
                    String storeNo = "";
                    if (StringUtil.isNotBlankList(on)) {
                        orderNo = on;
                    } else {
                        orderNo = tq;
                    }

                    if (StringUtil.isNotBlankList(tq)) {
                        extractCode = tq;
                    } else {
                        extractCode = on;
                    }

                    if (StringUtil.isNotBlankList(sn)) {
                        storeNo = sn;
                    } else {
                        try {
                            Xf xf = xfJpaDao.findOneByGsdm(wxFpxx.getGsdm());
                            Skp skp = skpJpaDao.findOneByGsdmAndXfsh(wxFpxx.getGsdm(), xf.getId());
                            storeNo = skp.getKpddm();
                        } catch (Exception e) {
                            re = "解析门店号失败，请重试！";
//                            weixinUtils.jujuekp(SuccOrderId, re, access_token);
                            e.printStackTrace();
                            return;
                        }
                    }
                    logger.info("makeInvoiceForApiType=3");
                    status = adapterService.makeInvoice(wxFpxx.getGsdm(), orderNo, storeNo, extractCode, gfmc, gfsh, null, gfyh, gfyhzh, gfdz, gfdh, wxFpxx.getTqm(), wxFpxx.getOpenId(), "5", null, null);
                }else if("4".equals(apiTpye)){
                    String yx;
//                    if(email!=null){
//                        yx = email;
//                    }else{
                        yx = wxFpxx.getTqm();//该字段临时存放邮箱
//                    }
                    status=adapterService.makeInvoiceForFour(wxFpxx.getGsdm(),wxFpxx.getQ(),gfmc,gfsh,gfdz,gfdh,gfyhzh,gfyh,yx,wxFpxx.getOpenId(),"5",null,null);
                }
                if ("-1".equals(status)) {
                    re="开具失败";
//                    weixinUtils.jujuekp(SuccOrderId, re, access_token);
                    return;
                } else if ("0".equals(status)) {
                    re="所需信息为空";
//                    weixinUtils.jujuekp(SuccOrderId, re, access_token);
                    return;
                } else if("-2".equals(status)){
                    re="交易数据上传中";
//                    weixinUtils.jujuekp(SuccOrderId, re, access_token);
                    return;
                }else{
                    try {
                        JSONObject  jsonObject = JSON.parseObject(status);
                    }catch (Exception e){
                        e.printStackTrace();
                        re = status;
//                        weixinUtils.jujuekp(SuccOrderId, re, access_token);
                    }
                    //成功
                    return;
                }
            }
        }
        return;
    }

    public AdapterService getAdapterService() {
        return adapterService;
    }

    public void setAdapterService(AdapterService adapterService) {
        this.adapterService = adapterService;
    }

    public WxFpxx getWxFpxx() {
        return wxFpxx;
    }

    public void setWxFpxx(WxFpxx wxFpxx) {
        this.wxFpxx = wxFpxx;
    }

    public Map getAlipayResultMap() {
        return alipayResultMap;
    }

    public void setAlipayResultMap(Map alipayResultMap) {
        this.alipayResultMap = alipayResultMap;
    }

    public XfJpaDao getXfJpaDao() {
        return xfJpaDao;
    }

    public void setXfJpaDao(XfJpaDao xfJpaDao) {
        this.xfJpaDao = xfJpaDao;
    }

    public SkpJpaDao getSkpJpaDao() {
        return skpJpaDao;
    }

    public void setSkpJpaDao(SkpJpaDao skpJpaDao) {
        this.skpJpaDao = skpJpaDao;
    }
}
