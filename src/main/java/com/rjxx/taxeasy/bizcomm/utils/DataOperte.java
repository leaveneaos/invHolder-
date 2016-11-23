package com.rjxx.taxeasy.bizcomm.utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rjxx.taxeasy.domains.Ckhk;
import com.rjxx.taxeasy.domains.Dzfplog;
import com.rjxx.taxeasy.domains.Jyls;
import com.rjxx.taxeasy.domains.Jyspmx;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.domains.Kpspmx;
import com.rjxx.taxeasy.service.CkhkService;
import com.rjxx.taxeasy.service.DzfplogService;
import com.rjxx.taxeasy.service.JylsService;
import com.rjxx.taxeasy.service.JyspmxService;
import com.rjxx.taxeasy.service.KplsService;
import com.rjxx.taxeasy.service.KpspmxService;


@Service
public class DataOperte {
	
	@Autowired
	private KplsService kplsService;
	@Autowired
	private KpspmxService kpspmxService;
	@Autowired
	private JylsService jylsService;
	@Autowired
	private JyspmxService jymxService;
	@Autowired
	private DzfplogService logService;
	@Autowired
	private CkhkService ckService;
	

	
	/**
     * 保存日志记录
     *
     * @param djh
     * @param clztdm
     * @param cljgdm
     * @param ffcs
     * @param ycms
     * @param lrry
     */
	@Transactional
    public void saveLog(int djh, String clztdm, String cljgdm,
           String ffcs, String ycms, int lrry, String xfsh, String jylsh) {
        Dzfplog dl = new Dzfplog();
        dl.setDjh(djh);
        dl.setClztdm(clztdm);
        dl.setCljgdm(cljgdm);
        dl.setFfcs(ffcs);
        dl.setYcms(ycms);
        dl.setLrsj(new Date());
        dl.setLrry(lrry);
        dl.setXfsh(xfsh);
        dl.setJylsh(jylsh);
        try {
        	logService.save(dl);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
	/**
	 * 发票重开时保存负数据到交易流水
	 * 
	 * */
	@Transactional
	public void updateKpls(int kplsh,int djh,int yhid) throws Exception{
		Map<String, Object> params = new HashMap<>();
		DecimalFormat df=new DecimalFormat("#.00");
		params.put("kplsh", kplsh);
		Kpls kpls = kplsService.findOneByParams(params);
		params.put("djh", djh);
		Jyls jyls = new Jyls();
        Jyls old = jylsService.findOne(djh);
        jyls.setDdh(old.getDdh());
        String jylsh = "JY" + new SimpleDateFormat("yyyyMMddHHmmssSS").format(new Date());
        jyls.setJylsh(jylsh);
        jyls.setJylssj(new Date());
        jyls.setFpzldm("12");
        jyls.setClztdm("01");
        jyls.setFpczlxdm("12");
        jyls.setXfdh(kpls.getXfdh());
        jyls.setXfdz(kpls.getXfdz());
        jyls.setXflxr(kpls.getXflxr());
        jyls.setXfmc(kpls.getXfmc());
        jyls.setXfsh(kpls.getXfsh());
        jyls.setXfyb(kpls.getXfyb());
        jyls.setXfyh(kpls.getXfyh());
        jyls.setXfyhzh(kpls.getXfyhzh());
        jyls.setGfdh(kpls.getGfdh());
        jyls.setGfdz(kpls.getGfdz());
        jyls.setGfemail(kpls.getGfemail());
        jyls.setGflxr(kpls.getGflxr());
        jyls.setGfmc(kpls.getGfmc());
        jyls.setGfsh(kpls.getGfsh());
        jyls.setGfyb(kpls.getGfyb());
        jyls.setGfyh(kpls.getGfyh());
        jyls.setGfyhzh(kpls.getGfyhzh());
        jyls.setSsyf(kpls.getSsyf());
        jyls.setKpr(kpls.getKpr());
        jyls.setFhr(kpls.getFhr());
        jyls.setSkr(kpls.getSkr());
        jyls.setBz(kpls.getBz());
        jyls.setJshj(-kpls.getJshj());
        jyls.setHsbz(old.getHsbz());
        jyls.setYfpdm(kpls.getFpdm());
        jyls.setYfphm(kpls.getFphm());
        jyls.setYkpjshj(0d);
        jyls.setYxbz("1");
        jyls.setGsdm(kpls.getGsdm());
        jyls.setLrry(yhid);
        jyls.setLrsj(new Date());
        jyls.setXgry(yhid);
        jyls.setXgsj(new Date());
        jyls.setSkpid(kpls.getSkpid());
        jylsService.save(jyls);
        List<Kpspmx> mxList = kpspmxService.findMxList(params);
        djh = jyls.getDjh();
        if(mxList !=null){
        	for(int i=0;i<mxList.size();i++){
        		Kpspmx mxItem = mxList.get(i);
        		Jyspmx jyspmx = new Jyspmx();
                jyspmx.setDjh(jyls.getDjh());
                jyspmx.setSpmxxh(mxItem.getSpmxxh());
                jyspmx.setSpdm(mxItem.getSpdm());
                jyspmx.setSpmc(mxItem.getSpmc());
                jyspmx.setSps(mxItem.getSps()==null?null:-mxItem.getSps());
                jyspmx.setSpdj(mxItem.getSpdj());
                jyspmx.setSpdw(mxItem.getSpdw());
                jyspmx.setSpggxh(mxItem.getSpggxh());
                jyspmx.setFphxz(mxItem.getFphxz());
                jyspmx.setSpje(-mxItem.getSpje());
                jyspmx.setSpse(-mxItem.getSpse());
                jyspmx.setSpsl(mxItem.getSpsl());
                jyspmx.setJshj(Double.valueOf(df.format(jyspmx.getSpje() + jyspmx.getSpse())));
                jyspmx.setYkphj(0d);
                jyspmx.setGsdm(mxItem.getGsdm());
                jyspmx.setLrsj(new Date());
                jyspmx.setLrry(yhid);
                jyspmx.setXgsj(new Date());
                jyspmx.setXgry(yhid);
                jymxService.save(jyspmx);
        	}
        }
        //保存负交易流水结束后修改开票流水表状态
        Map param2 = new HashMap<>();
		param2.put("kplsh", kplsh);
		param2.put("fpztdm", "06");  //06重新开具
		kplsService.updateFpczlx(param2);
		//重新保存一笔新的交易流水
		Ckhk ck = ckService.findOneByParams(params);
		Jyls ls = new Jyls();
		ls.setDdh(old.getDdh());
        jylsh = "JY" + new SimpleDateFormat("yyyyMMddHHmmssSS").format(new Date());
        ls.setJylsh(jylsh);
        ls.setJylssj(new Date());
        ls.setFpzldm("12");
        ls.setClztdm("01");
        ls.setFpczlxdm("11");
        ls.setXfdh(kpls.getXfdh());
        ls.setXfdz(kpls.getXfdz());
        ls.setXflxr(kpls.getXflxr());
        ls.setXfmc(kpls.getXfmc());
        ls.setXfsh(kpls.getXfsh());
        ls.setXfyb(kpls.getXfyb());
        ls.setXfyh(kpls.getXfyh());
        ls.setXfyhzh(kpls.getXfyhzh());
        ls.setGfdh(ck.getGfdh());
        ls.setGfdz(ck.getGfdz());
        ls.setGfemail(ck.getGfyx());
        ls.setGfmc(ck.getGfmc());
        ls.setGfsh(ck.getGfsh());
        ls.setGfyb(kpls.getGfyb());
        ls.setGfyh(ck.getGfyh());
        ls.setGfyhzh(ck.getGfyhzh());
        ls.setSsyf(kpls.getSsyf());
        ls.setKpr(kpls.getKpr());
        ls.setFhr(kpls.getFhr());
        ls.setSkr(kpls.getSkr());
        ls.setBz(kpls.getBz());
        ls.setJshj(kpls.getJshj());
        ls.setHsbz(old.getHsbz());        
        ls.setYkpjshj(0d);
        ls.setYxbz("1");
        ls.setGsdm(kpls.getGsdm());
        ls.setLrry(yhid);
        ls.setLrsj(new Date());
        ls.setXgry(yhid);
        ls.setXgsj(new Date());
        ls.setSkpid(kpls.getSkpid());
        jylsService.save(ls);
        mxList = kpspmxService.findMxList(params);
        djh = ls.getDjh();
        if(mxList !=null){
        	for(int i=0;i<mxList.size();i++){
        		Kpspmx mxItem = mxList.get(i);
        		Jyspmx jyspmx = new Jyspmx();
                jyspmx.setDjh(djh);
                jyspmx.setSpmxxh(mxItem.getSpmxxh());
                jyspmx.setSpdm(mxItem.getSpdm());
                jyspmx.setSpmc(mxItem.getSpmc());
                jyspmx.setSps(mxItem.getSps());
                jyspmx.setSpdj(mxItem.getSpdj());
                jyspmx.setSpdw(mxItem.getSpdw());
                jyspmx.setSpggxh(mxItem.getSpggxh());
                jyspmx.setFphxz(mxItem.getFphxz());
                jyspmx.setSpje(mxItem.getSpje());
                jyspmx.setSpse(mxItem.getSpse());
                jyspmx.setSpsl(mxItem.getSpsl());
                jyspmx.setJshj(Double.valueOf(df.format(jyspmx.getSpje() + jyspmx.getSpse())));
                jyspmx.setYkphj(0d);
                jyspmx.setGsdm(mxItem.getGsdm());
                jyspmx.setLrsj(new Date());
                jyspmx.setLrry(yhid);
                jyspmx.setXgsj(new Date());
                jyspmx.setXgry(yhid);
                jymxService.save(jyspmx);
        	}
        }
        //更新申请表的状态
	}
}
