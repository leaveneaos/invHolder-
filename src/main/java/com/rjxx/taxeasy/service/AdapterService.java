package com.rjxx.taxeasy.service;

import java.util.List;
import java.util.Map;

/**
 * Created by wangyahui on 2018/3/13 0013
 */
public interface AdapterService {
    Map getGrandMsg(String gsdm,String q);

    List<String> checkStatus(String tqm, String gsdm);

    String getSpxx(String gsdm, String q);

    String makeInvoice(String gsdm, String q, String gfmc, String gfsh, String email,
                       String gfyh, String gfyhzh, String gfdz, String gfdh, String tqm, String openid, String sjly, String access_token, String weixinOrderNo);
}
