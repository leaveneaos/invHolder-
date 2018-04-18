package com.rjxx.taxeasy.service;

import com.rjxx.taxeasy.dto.AdapterDataOrderBuyer;

import java.util.List;
import java.util.Map;

/**
 * Created by wangyahui on 2018/3/13 0013
 */
public interface AdapterService {
    Map getGrandMsg(String gsdm,String q);

    Map getGrandMsg(String gsdm,String on,String sn);

    List<String> checkStatus(String tqm, String gsdm);

    String getSpxx(String gsdm, String q);

    String getSpxx(String gsdm, String on,String sn,String tq);

    String makeInvoice(String gsdm, String q, String gfmc, String gfsh, String email,
                       String gfyh, String gfyhzh, String gfdz, String gfdh, String tqm, String openid, String sjly, String access_token, String weixinOrderNo);
    String makeInvoice(String gsdm,  String on,String sn,String tq,String gfmc, String gfsh, String email,
                       String gfyh, String gfyhzh, String gfdz, String gfdh, String tqm, String openid, String sjly, String access_token, String weixinOrderNo);

    boolean sendBuyer(String gsdm,String on,AdapterDataOrderBuyer buyer);

    Boolean isInvoiceDateRestriction(String gsdm, Integer xfid, Integer skpid, String orderTime);

    String getInvoiceList(String gsdm,String khh);

    String getConfirmMsg(String gsdm,String q);

    String makeInvoiceForFour(String gsdm,String jylsh,String gfmc, String gfsh, String gfdz,
                              String gfdh, String gfyhzh, String gfyh,String email,String openid,String sjly,String access_token,String weixinOrderNo);
}
