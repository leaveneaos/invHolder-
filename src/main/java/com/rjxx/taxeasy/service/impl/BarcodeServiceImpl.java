package com.rjxx.taxeasy.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.rjxx.taxeasy.bizcomm.utils.GetXmlUtil;
import com.rjxx.taxeasy.bizcomm.utils.HttpUtils;
import com.rjxx.taxeasy.dao.*;
import com.rjxx.taxeasy.domains.*;
import com.rjxx.taxeasy.service.BarcodeService;
import com.rjxx.taxeasy.service.CszbService;
import com.rjxx.taxeasy.service.SpvoService;
import com.rjxx.taxeasy.vo.Spvo;
import com.rjxx.taxeasy.wechat.util.TaxUtil;
import com.rjxx.utils.RJCheckUtil;
import com.rjxx.utils.XmlUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by wangyahui on 2017/8/9 0009
 */
@Service
@Transactional
public class BarcodeServiceImpl implements BarcodeService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private CszbService cszbService;//参数主表
    @Autowired
    private XfJpaDao xfJpaDao;
    @Autowired
    private PpJpaDao ppJpaDao;
    @Autowired
    private SpvoService spvoService;
    @Autowired
    private GsxxJpaDao gsxxJpaDao;
    @Autowired
    private KplsJpaDao kplsJpaDao;
    @Autowired
    private SkpJpaDao skpJpaDao;
    @Autowired
    private JylsJpaDao jylsJpaDao;

    @Override
    public Map sm(String gsdm, String q) {
        Gsxx gsxx = gsxxJpaDao.findOneByGsdm(gsdm);
        String secretKey = gsxx.getSecretKey();
        Boolean b = RJCheckUtil.checkMD5(secretKey, q);
        if (b) {
            Map decode = RJCheckUtil.decode(q);
            String storeNo = decode.get("storeNo").toString();
            String orderNo = decode.get("orderNo").toString();
            Skp skp = skpJpaDao.findOneByKpddmAndGsdm(storeNo,gsdm);
            Integer pid = skp.getPid();
            Integer xfid = skp.getXfid(); //销方id
            Integer kpdid = skp.getId();//税控盘id(开票点id)
            String ppdm = "";
            String ppurl = "";
            if (pid != null) {
                Pp pp = ppJpaDao.findOne(pid);
                ppdm = pp.getPpdm();
                ppurl = pp.getPpurl();
            } else {
                Cszb cszb = cszbService.getSpbmbbh(gsdm, xfid, kpdid, "mrmburlid");
                ppurl = cszb.getCsz();
            }
            Map map = new HashMap();
            map.put("ppdm", ppdm);
            map.put("ppurl", ppurl);
            map.put("orderNo", orderNo);
            return map;
        } else {
            return null;
        }
    }

    @Override
    public String getSpxx(String gsdm, String q) {
        Map decode = RJCheckUtil.decode(q);
        String orderNo = decode.get("orderNo").toString();
        String orderTime = decode.get("orderTime").toString();
        String price = decode.get("price").toString();
        String storeNo = decode.get("storeNo").toString();
        if (StringUtils.isNotBlank(orderNo) &&
                StringUtils.isNotBlank(orderTime) &&
                StringUtils.isNotBlank(price) &&
                StringUtils.isNotBlank(storeNo)) {
            try {
                Skp skp = skpJpaDao.findOneByKpddmAndGsdm(storeNo,gsdm);
                Integer xfid = skp.getXfid(); //销方id
                Integer kpdid = skp.getId();//税控盘id(开票点id)
                Cszb cszb = cszbService.getSpbmbbh(gsdm, xfid, kpdid, "dyspbmb");
                Map map = new HashMap();
                map.put("gsdm", gsdm);
                map.put("spdm", cszb.getCsz());
                Spvo oneSpvo = spvoService.findOneSpvo(map);
                Double spsl = oneSpvo.getSl();
                String spmc = oneSpvo.getSpmc();
                Map result = new HashMap();
                result.put("orderNo", orderNo);
                result.put("orderTime", orderTime);
                result.put("storeNo", storeNo);
                result.put("price", price);
                result.put("spsl", spsl);
                result.put("spmc", spmc);
                return JSONObject.toJSONString(result);
            } catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }else{
            return null;
        }
    }



    @Override
    public String makeInvoice(String gsdm, String q, String gfmc, String gfsh, String email,
                              String gfyh, String gfyhzh, String gfdz, String gfdh) {
        Map decode = RJCheckUtil.decode(q);
        String orderNo = decode.get("orderNo").toString();
        String orderTime = decode.get("orderTime").toString();
        String price = decode.get("price").toString();
        String storeNo = decode.get("storeNo").toString();
        if (StringUtils.isNotBlank(orderNo) &&
                StringUtils.isNotBlank(orderTime) &&
                StringUtils.isNotBlank(price) &&
                StringUtils.isNotBlank(storeNo) &&
                StringUtils.isNotBlank(gfmc) &&
                StringUtils.isNotBlank(email)) {
            try {
                Skp skp = skpJpaDao.findOneByKpddmAndGsdm(storeNo,gsdm);
                Integer xfid = skp.getXfid(); //销方id
                Xf xf = xfJpaDao.findOne(xfid);
                Integer kpdid = skp.getId();//税控盘id(开票点id)
                Jyxxsq jyxxsq = new Jyxxsq();
                jyxxsq.setDdh(orderNo);
                jyxxsq.setGsdm(gsdm);
                jyxxsq.setKpddm(storeNo);
                jyxxsq.setXfmc(xf.getXfmc());
                jyxxsq.setKpr(skp.getKpr());
                jyxxsq.setFhr(skp.getFhr());
                jyxxsq.setSkr(skp.getSkr());
                jyxxsq.setXfid(xfid);
                jyxxsq.setXfsh(xf.getXfsh());
                jyxxsq.setXfyhzh(skp.getYhzh());
                jyxxsq.setXfyh(skp.getKhyh());
                jyxxsq.setJshj(Double.valueOf(price));
                jyxxsq.setXfdh(skp.getLxdh());//销方电话
                jyxxsq.setXfdz(skp.getLxdz());//销方地址
                jyxxsq.setXflxr(xf.getXflxr());//销方联系人
                jyxxsq.setXfyb(xf.getXfyb());//销方邮编
                jyxxsq.setGfmc(gfmc);
                jyxxsq.setGfsh(gfsh);
                jyxxsq.setGfemail(email);
                jyxxsq.setGfdz(gfdz);
                jyxxsq.setGfdh(gfdh);
                jyxxsq.setGfyhzh(gfyhzh);
                jyxxsq.setGfyh(gfyh);
                jyxxsq.setJylsh(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
                jyxxsq.setFpzldm("12");
                jyxxsq.setFpczlxdm("11");
                jyxxsq.setSffsyj("1");
                jyxxsq.setZsfs("0");
                jyxxsq.setHsbz("1");
                jyxxsq.setSjly("5");
                jyxxsq.setLrsj(new Date());
                jyxxsq.setXgsj(new Date());
                jyxxsq.setDdrq(new SimpleDateFormat("yyyyMMddHHmmss").parse(orderTime));
                jyxxsq.setTqm(orderNo);

                List<Jymxsq> jymxsqList = new ArrayList<>();
                Jymxsq jymxsq = new Jymxsq();
                Cszb cszb = cszbService.getSpbmbbh(gsdm, xfid, kpdid, "dyspbmb");
                Map map = new HashMap();
                map.put("gsdm", gsdm);
                map.put("spdm", cszb.getCsz());
                Spvo spvo = spvoService.findOneSpvo(map);
                jymxsq.setSpdm(spvo.getSpbm());
                jymxsq.setYhzcmc(spvo.getYhzcmc());
                jymxsq.setYhzcbs(spvo.getYhzcbs());
                jymxsq.setLslbz(spvo.getLslbz());
                jymxsq.setJshj(Double.valueOf(price));
                jymxsq.setFphxz("0");
                jymxsq.setSpmc(spvo.getSpmc());
                jymxsq.setLrsj(new Date());
                jymxsq.setXgsj(new Date());
                jymxsq.setSpsl(spvo.getSl());
                jymxsq.setSpje(Double.valueOf(price));
                jymxsqList.add(jymxsq);
                List<Jymxsq> jymxsqs = TaxUtil.separatePrice(jymxsqList);

                List<Jyzfmx> jyzfmxList = new ArrayList<>();

                String xml = GetXmlUtil.getFpkjXml(jyxxsq, jymxsqs,jyzfmxList);
                Gsxx oneByGsdm = gsxxJpaDao.findOneByGsdm(gsdm);
                String appid = oneByGsdm.getAppKey();
                String key = oneByGsdm.getSecretKey();
                String resultxml = HttpUtils.HttpUrlPost(xml, appid, key);
                String json = "";
                try {
                    Map<String, Object> resultMap = XmlUtil.xml2Map(resultxml);
                    String returnMsg=resultMap.get("ReturnMessage").toString();
                    String returnCode=resultMap.get("ReturnCode").toString();
                    Map map2 = new HashMap();
                    map2.put("returnMsg", returnMsg);
                    map2.put("returnCode", returnCode);
                    json=JSONObject.toJSONString(map2);
                }catch (Exception e){
                    String serialorder=resultxml;
                    Kpls oneBySerialorder = kplsJpaDao.findOneBySerialorder(serialorder);
                    String fphm=oneBySerialorder.getFphm();
                    String fpdm = oneBySerialorder.getFpdm();
                    if(fphm==null || fpdm==null){
                        return "-1";
                    }
                    Map map3 = new HashMap();
                    map3.put("fphm", fphm);
                    map3.put("fpdm", fpdm);
                    json=JSONObject.toJSONString(map3);
                }
                return json;
            } catch (Exception e) {
                e.printStackTrace();
                return "-1";
            }
        } else {
            return "0";
        }
    }

    @Override
    public String checkStatus(String tqm, String gsdm) {
        Integer djh = jylsJpaDao.findDjhByTqmAndGsdm(tqm, gsdm);
        if(djh!=null){
            Kpls kpls = kplsJpaDao.findOneByDjh(djh);
            String fpztdm = kpls.getFpztdm();
            String pdfurl = kpls.getPdfurl();
            String fphm = kpls.getFphm();
            if("00".equals(fpztdm)&& StringUtils.isNotBlank(pdfurl)&&StringUtils.isNotBlank(fphm)){
                return pdfurl;
            }else{
                return "开具中";
            }
        }else{
            return "可开具";
        }
    }
}
