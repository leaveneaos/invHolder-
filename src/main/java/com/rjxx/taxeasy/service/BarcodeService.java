package com.rjxx.taxeasy.service;

import com.rjxx.taxeasy.domains.Jyxx;
import com.rjxx.taxeasy.domains.Jyxxsq;

import java.util.List;
import java.util.Map;

/**
 * Created by wangyahui on 2017/8/9 0009
 */
public interface BarcodeService {
    String makeInvoice(String gsdm,String q,String gfmc,
                       String gfsh,String email,String gfyh,
                       String gfyhzh,String gfdz,String gfdh,String tqm,
                       String openid,String sjly,String access_token,String weixinOrderNo);

    /**
     * 拉取数据
     * 封装开具
     * @return
     */
    String pullInvioce(Map resultSjMap,String gsdm,String gfmc,
                       String gfsh,String email,String gfyh,
                       String gfyhzh,String gfdz,String gfdh,String tqm,
                       String openid,String sjly ,String access_token,String AppId,String key,String weixinOrderNo);

    Map sm(String gsdm, String q);

    String getSpxx(String gsdm, String q);

    List<String> checkStatus(String tqm,String gsdm);

    /**
     * 扫码保存数据库
     */
    String savaWxFpxx(String tqm,String gsdm,String q,String openid,String orderNo);
    /**
     *封装跳转方法
     */
    Map redirct(String tqm,String gsdm,String opendid,String visiterIP,String llqxx);

    /**
     * 食其家
     * 封装开具
     * @return
     */
    String sqjInvioce(String q, String gsdm, String gfmc,
                      String gfsh, String email, String gfyh,
                      String gfyhzh, String gfdz, String gfdh, String tqm,
                      String openid, String sjly , String access_token, String weixinOrderNo);

}
