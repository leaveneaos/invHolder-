package com.rjxx.taxeasy.service.impl;

import com.rjxx.taxeasy.dao.*;
import com.rjxx.taxeasy.domains.*;
import com.rjxx.taxeasy.service.InitCompanyService;
import com.rjxx.utils.AppKeySecretUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wangyahui
 * @email wangyahui@datarj.com
 * @company 上海容津信息技术有限公司
 * @date 2018/7/9
 */
@Service
public class InitCompanyServiceImpl implements InitCompanyService {

    private Logger logger = LoggerFactory.getLogger(InitCompanyServiceImpl.class);

    @Autowired
    private GsxxJpaDao gsxxJpaDao;
    @Autowired
    private XfJpaDao xfJpaDao;
    @Autowired
    private SkpJpaDao skpJpaDao;
    @Autowired
    private YhJpaDao yhJpaDao;
    @Autowired
    private GroupJpaDao groupJpaDao;
    @Autowired
    private CszbJpaDao cszbJpaDao;

    @Override
    public Map initGsxx(String gsdm) {
        Map errorMsg = new HashMap();
        Gsxx oneByGsdm = gsxxJpaDao.findOneByGsdm(gsdm);
        if(oneByGsdm!=null){
            errorMsg.put("errorMsg", "该公司已存在");
            return errorMsg;
        }
        try {
            Gsxx gsxx = new Gsxx();
            gsxx.setGsmc(gsdm);
            gsxx.setGsdm(gsdm);
            String[] arr = AppKeySecretUtils.generate();
            String appId = "RJ" + arr[0].substring(0, 12);
            String key = arr[1];
            gsxx.setAppKey(appId);
            gsxx.setSecretKey(key);
            gsxx.setGsjc(gsdm);
            gsxx.setYjmbDm(4);
            gsxx.setXfnum(100);
            gsxx.setKpdnum(1000);
            gsxx.setYhnum(1000);
            gsxx.setKpnum(120000);
            gsxx.setYxqsrq(new Date());
            gsxx.setYxjzrq(new Date());
            gsxxJpaDao.save(gsxx);

            Xf xf = new Xf();
            xf.setLrsj(new Date());
            xf.setXgsj(new Date());
            xf.setLrry(1);
            xf.setXgry(1);
            xf.setYxbz("1");
            xf.setGsdm(gsdm);
            xf.setXfsh("500102010003643");
            xf.setXfmc("上海百旺测试3643");
            xf.setXfdh("测试电话");
            xf.setXfdz("测试地址");
            xf.setXfyh("测试银行");
            xf.setXfyhzh("测试银行账号");
            xf.setKpr("测试");
            Xf saveXf = xfJpaDao.save(xf);

            Skp skp = new Skp();
            skp.setYxbz("1");
            skp.setLrsj(new Date());
            skp.setXgsj(new Date());
            skp.setLrry(1);
            skp.setXgry(1);
            skp.setDpmax(900d);
            skp.setFpfz(900d);
            skp.setPpmax(0d);
            skp.setPpfz(0d);
            skp.setZpmax(0d);
            skp.setZpfz(0d);
            skp.setKpddm(gsdm+"_01");
            skp.setSbcs("1");
            skp.setXfid(saveXf.getId());
            skp.setLxdh("联系电话");
            skp.setLxdz("联系地址");
            skp.setKhyh("开户银行");
            skp.setYhzh("银行账号");
            skp.setKpr("开票人");
            skp.setGsdm(gsdm);
            skp.setKplx("12");
            skp.setWrzs("1");
            skp.setKpdmc("测试店");
            skp.setSkph("499000134531");
            Skp saveSkp = skpJpaDao.save(skp);

            Yh yh = new Yh();
            yh.setYxbz("1");
            yh.setLrsj(new Date());
            yh.setXgsj(new Date());
            yh.setXgry(1);
            yh.setLrry(1);
            yh.setRoleids("1");
            yh.setZtbz("1");
            yh.setAdmin("1");
            yh.setZhlxdm("03");
            yh.setYhmc(gsdm);
            yh.setDlyhid(gsdm+"_test");
            yh.setYhmm("88e65665a998784d0eb4dd3ab112a87b");
            yh.setGsdm(gsdm);
            yh.setXb("0");
            Yh saveYh = yhJpaDao.save(yh);

            Group group = new Group();
            group.setYxbz("1");
            group.setLrsj(new Date());
            group.setXgsj(new Date());
            group.setLrry(1);
            group.setXgry(1);
            group.setYhid(saveYh.getId());
            group.setXfid(saveXf.getId());
            groupJpaDao.save(group);
            group.setSkpid(saveSkp.getId());
            groupJpaDao.save(group);

            Cszb cszb = new Cszb();
            cszb.setYxbz("1");
            cszb.setLrry(1);
            cszb.setXgry(1);
            cszb.setLrsj(new Date());
            cszb.setXgsj(new Date());
            cszb.setGsdm(gsdm);
            cszb.setCsid(15);
            cszb.setCsz("03");
            cszbJpaDao.save(cszb);

            Cszb cszb2 = new Cszb();
            cszb2.setYxbz("1");
            cszb2.setLrry(1);
            cszb2.setXgry(1);
            cszb2.setLrsj(new Date());
            cszb2.setXgsj(new Date());
            cszb2.setGsdm(gsdm);
            cszb2.setCsid(38);
            cszb2.setCsz("http://datarj.imwork.net:24825/SKServer/SKDo");
            cszbJpaDao.save(cszb2);

            Map succMap = new HashMap();
            succMap.put("appId", appId);
            succMap.put("key", key);
            succMap.put("公司代码", gsdm);
            succMap.put("开票点代码", gsdm+"_01");
            succMap.put("销方名称", "上海百旺测试3643");
            succMap.put("销方税号", "500102010003643");
            succMap.put("平台账号", gsdm+"_test");
            return succMap;
        } catch (Exception e) {
            e.printStackTrace();
            errorMsg.put("errorMsg", e.getMessage());
            return errorMsg;
        }
    }
}
