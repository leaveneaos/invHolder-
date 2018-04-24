package com.rjxx.taxeasy.service.buildqr;

import com.alibaba.fastjson.JSON;
import com.rjxx.taxeasy.dao.GsxxJpaDao;
import com.rjxx.taxeasy.dao.YhJpaDao;
import com.rjxx.taxeasy.domains.Gsxx;
import com.rjxx.taxeasy.domains.Skp;
import com.rjxx.taxeasy.domains.Yh;
import com.rjxx.taxeasy.dto.AdapterGet;
import com.rjxx.taxeasy.service.SkpService;
import com.rjxx.utils.Base64Util;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wangyahui
 * @email wangyahui@datarj.com
 * @company 上海容津信息技术有限公司
 * @date 2018/4/24
 */
@Service
public class BuildQRService {
    @Autowired
    private YhJpaDao yhJpaDao;
    @Autowired
    private SkpService skpService;
    @Autowired
    private GsxxJpaDao gsxxJpaDao;

    public String login(String username, String password) {
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            String pass = yhJpaDao.findYhmmByDlyhid(username);
            if (password.equals(pass)) {
                Yh yh = yhJpaDao.findOne(yhJpaDao.findIdByDlyhid(username));
                String roleids = yh.getRoleids();
                List<Skp> skpListByYhId = skpService.getSkpListByYhId(yh.getId());
                if(skpListByYhId.size()!=1){
                    return "-2";
                }
                String kpddm = skpListByYhId.get(0).getKpddm();
                String kpdmc = skpListByYhId.get(0).getKpdmc();
                String gsdm = skpListByYhId.get(0).getGsdm();
                Map map = new HashMap<>();
                map.put("kpddm", kpddm);
                map.put("kpdmc", kpdmc);
                map.put("gsdm", gsdm);
                if("1".equals(roleids)){
                    return "-1";
                }
                return JSON.toJSONString(map);
            } else {
                return "0";
            }
        } else {
            return "0";
        }
    }

    public String create(String gsdm,String orderNo, String orderTime,
                         String storeNo, String price){
        AdapterGet adapterGet = new AdapterGet();
        adapterGet.setType("2");
        adapterGet.setOt(orderNo);
        adapterGet.setSn(storeNo);
        adapterGet.setOt(orderTime);
        adapterGet.setPr(price);
        String dataJson = JSON.toJSONString(adapterGet);
        Gsxx gsxx = gsxxJpaDao.findOneByGsdm(gsdm);
        String key = gsxx.getSecretKey();
        String sign = DigestUtils.md5Hex("data=" + dataJson + "&key=" + key);
        String str = "data=" + dataJson + "&si=" + sign;
        String encode = null;
        try {
            encode = Base64Util.encode(str);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encode;
    }
}
