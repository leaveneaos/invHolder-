package com.rjxx.taxeasy.wechat.service;

import com.alibaba.fastjson.JSONObject;
import com.rjxx.taxeasy.bizcomm.utils.GetXmlUtil;
import com.rjxx.taxeasy.bizcomm.utils.HttpUtils;
import com.rjxx.taxeasy.dao.*;
import com.rjxx.taxeasy.domains.*;
import com.rjxx.taxeasy.service.CszbService;
import com.rjxx.taxeasy.service.SpvoService;
import com.rjxx.taxeasy.vo.Spvo;
import com.rjxx.taxeasy.wechat.util.TaxUtil;
import com.rjxx.utils.XmlUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2017/7/31 0031.
 */
@Service
public class InvoiceService {

    @Autowired
    private YhJpaDao yhJpaDao;
    @Autowired
    private XfJpaDao xfJpaDao;
    @Autowired
    private SkpJpaDao skpJpaDao;
    @Autowired
    private CszbService cszbService;
    @Autowired
    private KplsJpaDao kplsJpaDao;
    @Autowired
    private GsxxJpaDao gsxxJpaDao;
    @Autowired
    private SpvoService spvoService;

    public String send(String purchaserName, String purchaserTaxNo,
                       String email, Double amount, String username, String openid) {
        if (StringUtils.isNotBlank(purchaserName) &&
                StringUtils.isNotBlank(purchaserTaxNo) &&
                StringUtils.isNotBlank(email) &&
                amount != null &&
                StringUtils.isNotBlank(username) &&
                StringUtils.isNotBlank(openid)) {
            try {
                //调接口
                String gsdm = yhJpaDao.findGsdmByDlyhid(username);
                Xf xf = xfJpaDao.findOneByGsdm(gsdm);
                String kpddm = skpJpaDao.findKpddmByGsdm(gsdm);
                Jyxxsq jyxxsq = new Jyxxsq();
                jyxxsq.setDdh(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
                jyxxsq.setJylsh(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
                jyxxsq.setGsdm(gsdm);
                jyxxsq.setKpddm(kpddm);
                jyxxsq.setXfmc(xf.getXfmc());
                jyxxsq.setXfdh(xf.getXfdh());
                jyxxsq.setXfdz(xf.getXfdz());
                jyxxsq.setXfid(xf.getId());
                jyxxsq.setXflxr(xf.getXflxr());
                jyxxsq.setXfsh(xf.getXfsh());
                jyxxsq.setXfyb(xf.getXfyb());
                jyxxsq.setXfyhzh(xf.getXfyhzh());
                jyxxsq.setXfyh(xf.getXfyh());
                jyxxsq.setDdrq(new Date());
                jyxxsq.setFpzldm("12");
                jyxxsq.setFpczlxdm("11");
                jyxxsq.setGfmc(purchaserName);
                jyxxsq.setGfsh(purchaserTaxNo);
                jyxxsq.setGfemail(email);
                jyxxsq.setSffsyj("1");
                jyxxsq.setZsfs("0");
                jyxxsq.setJshj(amount);
                jyxxsq.setHsbz("1");
                jyxxsq.setLrsj(new Date());
                jyxxsq.setXgsj(new Date());
                jyxxsq.setOpenid(openid);
                jyxxsq.setSjly("4");
                jyxxsq.setKpr(xf.getKpr());
                jyxxsq.setFhr(xf.getFhr());
                jyxxsq.setSkr(xf.getSkr());

                Integer kpdid = skpJpaDao.findIdByKpddm(kpddm);
                Cszb cszb = cszbService.getSpbmbbh(gsdm, jyxxsq.getXfid(), kpdid, "dyspbmb");
                Map map = new HashMap();
                map.put("gsdm", gsdm);
                map.put("spdm", cszb.getCsz());
                Spvo oneSpvo = spvoService.findOneSpvo(map);
                Jymxsq jymxsq = new Jymxsq();
                jymxsq.setSpdm(oneSpvo.getSpbm());
//                jymxsq.setSps(1d);
                jymxsq.setYhzcmc(oneSpvo.getYhzcmc());
                jymxsq.setYhzcbs(oneSpvo.getYhzcbs());
                jymxsq.setLslbz(oneSpvo.getLslbz());
//                jymxsq.setSpdj(100d);
                jymxsq.setJshj(amount);
                jymxsq.setFphxz("0");
                jymxsq.setSpmc(oneSpvo.getSpmc());
                jymxsq.setLrsj(new Date());
                jymxsq.setXgsj(new Date());
                jymxsq.setSpsl(oneSpvo.getSl());
                jymxsq.setSpje(amount);
                List<Jymxsq> jymxsqList = new ArrayList<>();
                jymxsqList.add(jymxsq);
                List<Jymxsq> jymxsqs = TaxUtil.separatePrice(jymxsqList);

                List<Jyzfmx> jyzfmxList = new ArrayList<>();
                Jyzfmx jyzfmx = new Jyzfmx();

                String xml = GetXmlUtil.getFpkjXml(jyxxsq, jymxsqs,jyzfmxList);
                Gsxx oneByGsdm = gsxxJpaDao.findOneByGsdm(gsdm);
                String appid = oneByGsdm.getAppKey();
                String key = oneByGsdm.getSecretKey();
                String resultxml = HttpUtils.HttpUrlPost(xml, appid, key);
                Map<String, Object> resultMap = XmlUtil.xml2Map(resultxml);

                String json = "";
                if(resultMap.get("ReturnCode")!=null){
                    String returnMsg=resultMap.get("ReturnMessage").toString();
                    String returnCode=resultMap.get("ReturnCode").toString();
                    Map map2 = new HashMap();
                    map2.put("returnMsg", returnMsg);
                    map2.put("returnCode", returnCode);
                    map2.put("purchaserName", purchaserName);
                    map2.put("purchaserTaxNo", purchaserTaxNo);
                    map2.put("email", email);
                    map2.put("amount", amount);
                    map2.put("users", username);
                    map2.put("id", openid);
                    json=JSONObject.toJSONString(map2);
                }else{
                    String serialorder=resultMap.get("Serialorder").toString();
                    Kpls oneBySerialorder = kplsJpaDao.findOneBySerialorder(serialorder);
                    String fphm=oneBySerialorder.getFphm();
                    String fpdm = oneBySerialorder.getFpdm();
                    Date kprq = oneBySerialorder.getKprq();
                    Map map3 = new HashMap();
                    map3.put("fphm", fphm);
                    map3.put("fpdm", fpdm);
                    map3.put("purchaserName", purchaserName);
                    map3.put("amount", amount);
                    map3.put("kprq", kprq);
                    map3.put("users", username);
                    map3.put("id", openid);
                    json=JSONObject.toJSONString(map3);
                }
                System.out.println(json);
                return json;
            } catch (Exception e) {
                e.printStackTrace();
                return "-1";
            }
        } else {
            return "0";
        }
    }

    public String login(String username, String password) {
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            String pass = yhJpaDao.findYhmmByDlyhid(username);
            if (password.equals(pass)) {
                return "1";
            } else {
                return "0";
            }
        } else {
            return "0";
        }
    }
}
