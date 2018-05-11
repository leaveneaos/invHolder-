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
import com.rjxx.utils.NumberUtil;
import com.rjxx.utils.XmlUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by wangyahui on 2017/8/17 0017
 */
@Service
public class InvoiceService {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

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
    @Autowired
    private GroupJpaDao groupJpaDao;

    public String send(String purchaserName, String purchaserTaxNo,
                       String email, Double amount, String username, String openid) {
        if (StringUtils.isNotBlank(purchaserName) &&
                StringUtils.isNotBlank(email) &&
                amount != null &&
                StringUtils.isNotBlank(username)) {
            try {
                logger.info("service中的openid="+openid);
                //调接口
                Integer yhid=yhJpaDao.findIdByDlyhid(username);
                Group group = groupJpaDao.findOneByYhid(yhid);
                Integer skpid = group.getSkpid();
                Integer xfid = group.getXfid();
                Xf xf = xfJpaDao.findOneById(xfid);
                Skp skp = skpJpaDao.findOneById(skpid);
                String gsdm = yhJpaDao.findGsdmByDlyhid(username);
                Jyxxsq jyxxsq = new Jyxxsq();
                jyxxsq.setDdh(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())+ NumberUtil.getRandomLetter());
                jyxxsq.setJylsh(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())+NumberUtil.getRandomLetter());
                jyxxsq.setGsdm(gsdm);
                jyxxsq.setKpddm(skp.getKpddm());
                jyxxsq.setXfid(xfid);
                jyxxsq.setXflxr(xf.getXflxr());
                jyxxsq.setXfyb(xf.getXfyb());
                jyxxsq.setXfyhzh(xf.getXfyhzh());
                jyxxsq.setXfyh(xf.getXfyh());
                jyxxsq.setDdrq(new Date());
                jyxxsq.setFpzldm("12");
                jyxxsq.setFpczlxdm("11");
                jyxxsq.setGfmc(purchaserName);
                jyxxsq.setGfsh(purchaserTaxNo);
                if(null!=purchaserTaxNo && !"".equals(purchaserTaxNo)){
                    jyxxsq.setGflx("1");
                }else {
                    jyxxsq.setGflx("0");
                }
                jyxxsq.setGfemail(email);
                jyxxsq.setSffsyj("1");
                jyxxsq.setZsfs("0");
                jyxxsq.setJshj(amount);
                jyxxsq.setHsbz("1");
                jyxxsq.setLrsj(new Date());
                jyxxsq.setXgsj(new Date());
                jyxxsq.setOpenid(openid);
                jyxxsq.setSjly("4");
                jyxxsq.setXfsh(xf.getXfsh());
                jyxxsq.setXfmc(xf.getXfmc());
                jyxxsq.setKpr(skp.getKpr());
                jyxxsq.setFhr(skp.getFhr());
                jyxxsq.setSkr(skp.getSkr());
                jyxxsq.setXfdh(skp.getLxdh());
                jyxxsq.setXfdz(skp.getLxdz());

                Cszb cszb = cszbService.getSpbmbbh(gsdm, xfid, skpid, "dyspbmb");
                Map map = new HashMap();
                map.put("gsdm", gsdm);
                map.put("spdm", cszb.getCsz());
                Spvo oneSpvo = spvoService.findOneSpvo(map);
                Jymxsq jymxsq = new Jymxsq();
                jymxsq.setSpdm(oneSpvo.getSpbm());
                jymxsq.setYhzcmc(oneSpvo.getYhzcmc());
                jymxsq.setYhzcbs(oneSpvo.getYhzcbs());
                jymxsq.setLslbz(oneSpvo.getLslbz());
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
                    map2.put("purchaserName", purchaserName);
                    map2.put("purchaserTaxNo", purchaserTaxNo);
                    map2.put("email", email);
                    map2.put("amount", amount);
                    map2.put("users", username);
                    map2.put("id", openid);
                    json=JSONObject.toJSONString(map2);
                }catch (Exception e){
                    String serialorder=resultxml;
                    Kpls oneBySerialorder = kplsJpaDao.findOneBySerialorder(serialorder);
                    String fphm=oneBySerialorder.getFphm();
                    String fpdm = oneBySerialorder.getFpdm();
                    if(fphm==null || fpdm==null){
                        return "-1";
                    }
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
                Yh yh = yhJpaDao.findOne(yhJpaDao.findIdByDlyhid(username));
                String roleids = yh.getRoleids();
                if("1".equals(roleids)){
                    return "-1";
                }
                return "1";
            } else {
                return "0";
            }
        } else {
            return "0";
        }
    }
}
