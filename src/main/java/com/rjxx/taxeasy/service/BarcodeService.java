package com.rjxx.taxeasy.service;

import com.rjxx.taxeasy.domains.Jymxsq;
import com.rjxx.taxeasy.domains.Jyxxsq;
import com.rjxx.taxeasy.domains.Jyzfmx;

import java.util.List;
import java.util.Map;

/**
 * Created by wangyahui on 2017/8/9 0009
 */
public interface BarcodeService {
    String makeInvoice(String gsdm,String q,String gfmc,
                       String gfsh,String email,String gfyh,
                       String gfyhzh,String gfdz,String gfdh,String tqm,
                       String openid,String sjly);

    /**
     * 拉取数据
     * 封装开具
     * @return
     */
    String pullInvioce(Map resultSjMap,String gsdm,String gfmc,
                       String gfsh,String email,String gfyh,
                       String gfyhzh,String gfdz,String gfdh,String tqm,
                       String openid,String sjly ,String access_token);

    Map sm(String gsdm, String q);

    String getSpxx(String gsdm, String q);

    String checkStatus(String gsdm,String tqm);
}
