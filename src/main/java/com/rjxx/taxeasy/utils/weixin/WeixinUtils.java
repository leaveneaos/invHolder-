package com.rjxx.taxeasy.utils.weixin;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rjxx.utils.WeixinUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 微信工具类
 * Created by zsq on 2017-08-03.
 */
public class WeixinUtils {
    private  static Logger logger = LoggerFactory.getLogger(WeixinUtils.class);

    /*
    * 获取微信token
    * */
    public Map hqtk() {
        Map<String, Object> result = new HashMap<String, Object>();
        // 获取token
        String turl = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="+WeiXinConstants.APP_ID+"&secret="+WeiXinConstants.APP_SECRET;
        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(turl);
        ObjectMapper jsonparer = new ObjectMapper();// 初始化解析json格式的对象
        try {
            HttpResponse res = client.execute(get);
            String responseContent = null; // 响应内容
            HttpEntity entity = res.getEntity();
            responseContent = EntityUtils.toString(entity, "UTF-8");
            Map map = jsonparer.readValue(responseContent, Map.class);
            // 将json字符串转换为json对象
            if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                if (map.get("errcode") != null) {// 错误时微信会返回错误码等信息，{"errcode":40013,"errmsg":"invalid
                    result.put("success", false);
                    result.put("msg", "获取微信token失败,错误代码为" + map.get("errcode"));
                    return result;
                } else {// 正常情况下{"access_token":"ACCESS_TOKEN","expires_in":7200}
                    map.put("success", true);

                    return map;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("msg", "获取微信token失败" + e.getMessage());
        } finally {
            // 关闭连接 ,释放资源
            client.getConnectionManager().shutdown();
        }
        return result;
    }

    /*
    * 获取微信spappid
    *
    * */
    public  String  getSpappid(){

        String invoice_url = "";
        String spappid = "";
        WeixinUtils weixinUtils = new WeixinUtils();
        String accessToken = (String) weixinUtils.hqtk().get("access_token");
        System.out.println("微信"+accessToken);
        String url = "https://api.weixin.qq.com/card/invoice/seturl?access_token="+accessToken;
        String jsonStr = WeixinUtil.httpRequest(url, "POST", JSON.toJSONString(""));
        System.out.println("返回信息"+jsonStr.toString());
        if(jsonStr !=null){
            ObjectMapper jsonparer = new ObjectMapper();// 初始化解析json格式的对象
            try {
                Map map = jsonparer.readValue(jsonStr, Map.class);
                invoice_url = (String) map.get("invoice_url");
                spappid = invoice_url.split("&")[1].split("=")[1];
                return  spappid;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
       return  null;
    }

    /*
    * 获取微信ticket
    * */
    public  String getTicket(){
        String ticket="";
        WeixinUtils weixinUtils = new WeixinUtils();
        String accessToken = (String)weixinUtils.hqtk().get("access_token");
        String ticketUrl="https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token="+accessToken+"&type=wx_card";
        String jsonStr = WeixinUtil.httpRequest(ticketUrl, "GET", null);
        System.out.println("返回信息"+jsonStr.toString());
        if(null!=jsonStr){
            ObjectMapper jsonparer = new ObjectMapper();// 初始化解析json格式的对象
            try {
                Map map = jsonparer.readValue(jsonStr, Map.class);
                ticket = (String) map.get("ticket");
                System.out.println("ticket获取成功"+ticket);
                return  ticket;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return  null;
    }
    /*
    * 初始化微信授权链接
    * */
   /* public  void  initWeiXinAuthorization(HttpServletRequest request, HttpServletResponse response, List lsit){

        WeixinUtils weixinUtils = new WeixinUtils();
        String accessToken = (String)weixinUtils.hqtk().get("access_token");
        String url ="https://api.weixin.qq.com/card/invoice/getauthurl?access_token="+accessToken;

        //response.sendRedirect(redirectUrl);
    }*/

    //微信授权跳转
    public static void main(String[] args) {

        //Map msp = new HashMap();
        WeixinUtils weixinUtils = new WeixinUtils();
        //weixinUtils.zdcxstatus();
        //weixinUtils.cksqzd();
        //weixinUtils.sqzd();
       // weixinUtils.getTicket();
        //*String url ="https://mp.weixin.qq.com/bizmall/authinvoice?action=list&s_pappid=d3g5YWJjNzI5ZTJiNDYzN2VlX0PARqxCKGk0d1fanZfCN3KxU5K6C-9JRLhQXmLzcptB";
        //String a = url.split("&")[1].split("=")[1];

        //System.out.println("截取"+a);
        // msp = weixinUtils.getSpappid();
        //msp = weixinUtils.hqtk();
        // String msp=weixinUtils.getTicket();
        // System.out.println("获取微信token-----------"+msp);

        String access_token = (String) weixinUtils.hqtk().get("access_token");//获取token
         String spappid =  weixinUtils.getSpappid();//获取开票平台
         String ticket = weixinUtils.getTicket();
         String orderid="12314532220011";
         int money = 12;
         int timestamp = 1574875876;
         String  source = "web";
         String redirect_url = "https://baidu.com";
         int type = 1;
        Map nvps = new HashMap();
        nvps.put("s_pappid",spappid);
        nvps.put("order_id",orderid);
        nvps.put("money",money);
        nvps.put("timestamp",timestamp);
        nvps.put("source",source);
        nvps.put("redirect_url",redirect_url);
        nvps.put("ticket",ticket);
        nvps.put("type",type);
        String sj = JSON.toJSONString(nvps);
        System.out.println("封装数据"+sj);
        String urls ="https://api.weixin.qq.com/card/invoice/getauthurl?access_token="+access_token;
        String jsonStr3 = WeixinUtil.httpRequest(urls, "POST", sj);
        System.out.println("返回信息"+jsonStr3.toString());
        if(null!=jsonStr3){
            ObjectMapper jsonparer = new ObjectMapper();// 初始化解析json格式的对象
            try {
                Map map = jsonparer.readValue(jsonStr3, Map.class);
               String auth_url = (String) map.get("auth_url");
                System.out.println("授权链接"+auth_url);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*
    * 主动查询授权完成状态
    * */
    public Map zdcxstatus(){

        Map result = new HashMap();
        String order_id="1231453222";
        WeixinUtils weixinUtils = new WeixinUtils();
        String s_pappid= weixinUtils.getSpappid();
        String access_token = (String) weixinUtils.hqtk().get("access_token");
        String URL = "https://api.weixin.qq.com/card/invoice/getauthdata?access_token="+access_token;

        Map nvps = new HashMap();
        nvps.put("s_pappid",s_pappid);
        nvps.put("order_id",order_id);
        String sj = JSON.toJSONString(nvps);
        System.out.println("封装数据"+sj);
        String jsonStr3 = WeixinUtil.httpRequest(URL, "POST", sj);
        System.out.println("返回信息"+jsonStr3.toString());
        if(null!=jsonStr3){
            ObjectMapper jsonparer = new ObjectMapper();// 初始化解析json格式的对象
            try {
                Map map = jsonparer.readValue(jsonStr3, Map.class);
                Integer errcode = (Integer) map.get("errcode");
                String invoice_status = (String) map.get("invoice_status");
                System.out.println("授权状态"+invoice_status);
                System.out.println("code"+errcode);
                //System.out.println("返回的用户发票抬头数据信息"+map.get("user_auth_info").toString());
                if(null!=errcode&&errcode.equals(0)){
                    System.out.println("返回数据成功！解析json数据");

                }else {
                    result.put("msg","主动查询授权完成状态失败,查询失败错误码为"+errcode);
                    System.out.println(""+result);
                    return result;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /*
    * 设置授权页字段
    * 一次性设置
    * */
    public void  sqzd(){
        WeixinUtils weixinUtils = new WeixinUtils();
        String access_token = (String) weixinUtils.hqtk().get("access_token");
        Map sjss = new HashMap();
        Map auth_field = new HashMap();
        Map user_field = new HashMap();
        Map biz_field = new HashMap();
        List custom_field1 = new ArrayList();
        List custom_field2 = new ArrayList();

        Map cus1 =new HashMap();
        Map cus2 =new HashMap();
        cus1.put("key","其他");
        cus2.put("key","其他");
        custom_field1.add(cus1);
        custom_field2.add(cus2);

        auth_field.put("user_field",user_field);
        auth_field.put("biz_field",biz_field);

        user_field.put("show_title",1);
        user_field.put("show_phone",0);
        user_field.put("show_email",0);
        user_field.put("custom_field",custom_field1);

        biz_field.put("show_title",1);
        biz_field.put("show_tax_no",1);
        biz_field.put("show_addr",0);
        biz_field.put("show_phone",0);
        biz_field.put("show_bank_type",0);
        biz_field.put("show_bank_no",0);
        biz_field.put("require_tax_no",0);
        biz_field.put("require_addr",1);
        biz_field.put("custom_field",custom_field2);

        sjss.put("auth_field",auth_field);
        String sj = JSON.toJSONString(sjss);
        System.out.println("封装数据"+sj);
        String urls ="https://api.weixin.qq.com/card/invoice/setbizattr?action=set_auth_field&access_token="+access_token;
       String jsonStr3 = WeixinUtil.httpRequest(urls, "POST", sj);
       System.out.println("返回信息"+jsonStr3.toString());
        if(null!=jsonStr3){
            ObjectMapper jsonparer = new ObjectMapper();// 初始化解析json格式的对象
            try {
                Map map = jsonparer.readValue(jsonStr3, Map.class);
                String errmsg = (String) map.get("errmsg");
                System.out.println("错误类型"+errmsg);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /*
    * 查看授权页面字段信息
    * */
    public void cksqzd(){
        WeixinUtils weixinUtils = new WeixinUtils();
        String  access_token= (String) weixinUtils.hqtk().get("access_token");
        String ckUrl ="https://api.weixin.qq.com/card/invoice/setbizattr?action=get_auth_field&access_token="+access_token;
        String jsonStr3 = WeixinUtil.httpRequest(ckUrl, "POST",  JSON.toJSONString(""));
        System.out.println("返回信息"+jsonStr3.toString());
        if(null!=jsonStr3){
            ObjectMapper jsonparer = new ObjectMapper();// 初始化解析json格式的对象
            try {
                Map map = jsonparer.readValue(jsonStr3, Map.class);
                String errmsg = (String) map.get("errmsg");
                System.out.println("错误类型"+errmsg);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
