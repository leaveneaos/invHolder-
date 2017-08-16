package com.rjxx.taxeasy.service;

import java.util.Map;

/**
 * Created by wangyahui on 2017/8/9 0009
 */
public interface BarcodeService {
    String makeInvoice(String gsdm,String q,String gfmc,
                       String gfsh,String email,String gfyh,
                       String gfyhzh,String gfdz,String gfdh,String tqm,
                       String openid,String sjly);

    Map sm(String gsdm, String q);

    String getSpxx(String gsdm, String q);

    String checkStatus(String gsdm,String tqm);
}
