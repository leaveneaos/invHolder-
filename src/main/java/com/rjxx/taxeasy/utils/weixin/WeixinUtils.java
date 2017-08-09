package com.rjxx.taxeasy.utils.weixin;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.domains.Kpspmx;
import com.rjxx.utils.StringUtils;
import com.rjxx.utils.WeixinUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.Response;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 微信工具类
 * Created by zsq on 2017-08-03.
 */

public class WeixinUtils {
    private  static Logger logger = LoggerFactory.getLogger(WeixinUtils.class);


    /**
     * 判断是不是微信浏览器
     *
     * @param
     * @return
     */
        public static boolean isWeiXinBrowser(HttpServletRequest request) {
        String ua = request.getHeader("user-agent").toLowerCase();
        boolean res = ua.contains("micromessenger");
        return res;
        }

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
    拿到数据,调用微信接口获取微信授权链接,
    如果成功跳转页面,失败返回null
    */
    public String getTiaoURL(String orderid,String money,long timestamp,String menDianId) throws Exception {

        String auth_url ="";
        WeixinUtils weixinUtils = new WeixinUtils();

        String spappid =  weixinUtils.getSpappid();//获取开票平台
        String ticket = weixinUtils.getTicket();

        String  source = "web";
        String redirect_url = "https://baidu.com";
        int type = 1;//填写抬头申请开票类型
        Map nvps = new HashMap();
        nvps.put("s_pappid",spappid);
        nvps.put("order_id",orderid);
        nvps.put("money",money);
        nvps.put("timestamp",timestamp);
        nvps.put("source",source);
        nvps.put("redirect_url",redirect_url);
        nvps.put("ticket",ticket);
        nvps.put("type",type);
        if(null==orderid&&StringUtils.isBlank(orderid)){
            logger.info("获取微信授权链接,订单编号为null");
            return null;
        }
        if(null==money&&StringUtils.isBlank(money)){
            logger.info("获取微信授权链接,金额为null");
            return  null;
        }

        String sj = JSON.toJSONString(nvps);
        System.out.println("封装数据"+sj);
        String access_token = (String) weixinUtils.hqtk().get("access_token");//获取token
        String urls ="https://api.weixin.qq.com/card/invoice/getauthurl?access_token="+access_token;
        String jsonStr3 = WeixinUtil.httpRequest(urls, "POST", sj);
        System.out.println("返回信息"+jsonStr3.toString());
        if(null!=jsonStr3){
            ObjectMapper jsonparer = new ObjectMapper();// 初始化解析json格式的对象
            try {
                Map map = jsonparer.readValue(jsonStr3, Map.class);
                 auth_url = (String) map.get("auth_url");
                System.out.println("授权链接"+auth_url);
                logger.info("跳转url"+auth_url);
                //response.sendRedirect(auth_url);
                //request.getSession().setAttribute(orderid+"auth_url",auth_url);//跳转url放进session
            } catch (Exception e) {
                //处理异常
                logger.error("Get Ali Access_token error", e);
                return auth_url;
                //request.getSession().setAttribute("msg", "获取微信授权出现异常!");
                //response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
            }
        }
        return auth_url;
    }
    //微信授权跳转
    public static void main(String[] args) {

        //Map msp = new HashMap();
        //WeixinUtils weixinUtils = new WeixinUtils();

        //System.out.println(""+in);
        /* //解析xml
            String data="<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<xml>"
                + "<ToUserName>1111</ToUserName>"
                + "<FromUserName></FromUserName>"
                + "<CreateTime>1475134700</CreateTime>"
                + "<MsgType>event</MsgType>"
                + "<Event>user_authorize_invoice</Event>"
                + "<SuccOrderId>1231453222001122</SuccOrderId>"
                + "<FailOrderId></FailOrderId>"
                + "<AppId>wx9abc729e2b4637ee</AppId>"
                + "<Source>web</Source>"
                + "</xml>";
        Document xmlDoc = null;
        try {
            xmlDoc = DocumentHelper.parseText(data);
            Element rootElt = xmlDoc.getRootElement();
            System.out.println("根节点：" + rootElt.getName());
            List<Element> childElements = rootElt.elements();
            String SuccOrderIdValue = "";
            String FailOrderIdValue = "";
            for (Element e:childElements){
                if(e.getName().equals("SuccOrderId")&&null!=e.getName()){
                    SuccOrderIdValue = e.getText();
                    System.out.println("成功的订单id");
                }
                if(e.getName().equals("FailOrderId")&&null!=e.getName()){
                    FailOrderIdValue=e.getText();
                    System.out.println("失败的订单id");
                }
            }
            System.out.println(""+SuccOrderIdValue);
            System.out.println(""+FailOrderIdValue);
            if(""!=SuccOrderIdValue&&null!=SuccOrderIdValue){
                System.out.println("拿到成功的订单id了");
            }
            if(""!=FailOrderIdValue&&null!=FailOrderIdValue){
                System.out.println("拿到失败的订单id了");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/



        //*String url ="https://mp.weixin.qq.com/bizmall/authinvoice?action=list&s_pappid=d3g5YWJjNzI5ZTJiNDYzN2VlX0PARqxCKGk0d1fanZfCN3KxU5K6C-9JRLhQXmLzcptB";
        //String a = url.split("&")[1].split("=")[1];

        //System.out.println("截取"+a);
        // msp = weixinUtils.getSpappid();
        //msp = weixinUtils.hqtk();
        // String msp=weixinUtils.getTicket();
        // System.out.println("获取微信token-----------"+msp);


        /*try {
            weixinUtils.getTiaoURL("1131453220170808",1,1474875876,"1");//获取微信授权
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        //weixinUtils.zdcxstatus("1131453220170808");//查询用户授权状态
        //weixinUtils.cksqzd();//查看授权字段
        //weixinUtils.sqzd();//授权字段--只设一次
        //weixinUtils.getTicket();//获取ticket
        //Map map =  weixinUtils.creatMb("全家超市");//创建模板
        //String card_id = (String) map.get("card_id");
        //System.out.println(""+card_id);


        //weixinUtils.dzfpInCard("1131453220170808",WeiXinConstants.FAMILY_CARD_ID,weixinUtils.zdcxstatus("1131453220170808"));
        //String in =  weixinUtils.jujuekp("1131453222001122","微信授权失败，请重新开票");//重新开票

        //上传PDF
       // weixinUtils.creatPDF("http://test.datarj.com/e-invoice-file/500102010003643/20170531/691fe064-80f4-4e81-9ae6-4d16ee0010a5.pdf","/usr/local/e-invoice-file");


    }

    /*
    * 主动查询授权完成状态
    * */
    public Map zdcxstatus(String order_id/*,String access_token*/){

        Map resultMap = new HashMap();
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
                System.out.println("code"+errcode);
                if(null!=errcode&&errcode.equals(0)){
                    System.out.println("返回数据成功！解析json数据");
                    System.out.println("返回数据"+map.toString());
                    String invoice_status = (String) map.get("invoice_status");
                    int auth_time = (int) map.get("auth_time");
                    Map user_auth_info = (Map) map.get("user_auth_info");
                    Date date = new Date(auth_time);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    String xdsj = sdf.format(date);//下单时间
                    System.out.println(""+xdsj.toString());
                    if(null!=user_auth_info.get("user_field")){
                        //个人抬头
                        Map user_field = (Map) user_auth_info.get("user_field");
                        System.out.println("个人抬头"+user_auth_info.toString());
                        String title = (String) user_field.get("title");
                        String phone = (String) user_field.get("phone");
                        String email = (String) user_field.get("email");
                        List custom_field = (List) user_field.get("custom_field");
                        String key ="";
                        String value="";
                        if(custom_field.size()>0){
                           for(int  i=0;i<custom_field.size();i++){
                               System.out.println("个人中的其他数据"+custom_field.get(i));
                              Map map1= (Map) custom_field.get(i);
                              key= (String) map1.get("key");
                              value = (String) map1.get("value");
                               //System.out.println("key"+key);
                               //System.out.println("value"+value);
                           }
                        }
                        resultMap.put("xdsj",xdsj);//下单时间
                        resultMap.put("title",title);//发票抬头名称
                        resultMap.put("phone",phone);//电话
                        resultMap.put("email",email);//邮箱
                        resultMap.put("key",key);
                        resultMap.put("value",value);
                        return  resultMap;
                    }if(null!=user_auth_info.get("biz_field")){
                        //单位抬头
                        Map biz_field = (Map) user_auth_info.get("biz_field");
                        System.out.println("个人抬头"+user_auth_info.toString());
                        String title = (String) biz_field.get("title");
                        String tax_no = (String) biz_field.get("tax_no");
                        String addr = (String) biz_field.get("addr");
                        String phone = (String) biz_field.get("phone");
                        String bank_type = (String) biz_field.get("bank_type");
                        String bank_no = (String) biz_field.get("bank_no");
                        List custom_field = (List) biz_field.get("custom_field");
                        String key ="";
                        String value="";
                        if(custom_field.size()>0){
                            for(int  i=0;i<custom_field.size();i++){
                                System.out.println("个人中的其他数据"+custom_field.get(i));
                                Map map1= (Map) custom_field.get(i);
                                 key= (String) map1.get("key");
                                 value = (String) map1.get("value");
                                System.out.println("key"+key);
                                System.out.println("value"+value);
                            }
                        }
                        resultMap.put("title",title);
                        resultMap.put("tax_no",tax_no);
                        resultMap.put("addr",addr);
                        resultMap.put("phone",phone);
                        resultMap.put("bank_type",bank_type);
                        resultMap.put("bank_no",bank_no);
                        resultMap.put("key",key);
                        resultMap.put("value",value);
                        return  resultMap;
                    }

                }else if(null!=errcode&&errcode.equals(72038)){
                    logger.info("主动查询授权完成状态失败,订单"+order_id+"没有授权,错误代码"+errcode);
                    System.out.println("主动查询授权完成状态失败,订单"+order_id+"没有授权,错误代码"+errcode);
                    return null;
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
                return ;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*拒绝开票*/

    public  String jujuekp(String order_id,String reason){
        WeixinUtils weixinUtils = new WeixinUtils();
        String access_token = (String) weixinUtils.hqtk().get("access_token");
        String jjkpURL ="https://api.weixin.qq.com/card/invoice/rejectinsert?access_token="+access_token;
        Map mapInfo = new HashMap();
        String s_pappid = weixinUtils.getSpappid();
        //String order_id ="1131453222001122";
        //String reason ="微信授权失败";
        String url =WeiXinConstants.RJXX_REDIRECT_URL;
        mapInfo.put("s_pappid",s_pappid);
        mapInfo.put("order_id",order_id);
        mapInfo.put("reason",reason);
        mapInfo.put("url",url);
        String info = JSON.toJSONString(mapInfo);
        String jsonStr3 = WeixinUtil.httpRequest(jjkpURL, "POST",  info);
        System.out.println("返回信息"+jsonStr3.toString());
        if(null!=jsonStr3){
            ObjectMapper jsonparer = new ObjectMapper();// 初始化解析json格式的对象
            try {
                Map map = jsonparer.readValue(jsonStr3, Map.class);
                int errcode = (int) map.get("errcode");
                String errmsg = (String) map.get("errmsg");
                System.out.println("错误码"+errcode);
                if(errcode==0){
                    return "拒绝开票成功";
                }else if(errcode ==72035){
                    logger.info("返回的错误信息为"+errmsg);
                    return "该发票已经被拒绝开票";
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    return null;
    }

    /*
    * 创建发票卡卷模板*/
    public Map creatMb(String gsmc ){
        Map resultMap = new HashMap();
        WeixinUtils weixinUtils = new WeixinUtils();
        String access_token = (String) weixinUtils.hqtk().get("access_token");
        String creatURL = WeiXinConstants.CREAT_MUBAN_URL+access_token;
        System.out.println("创建卡卷模板url地址"+creatURL);
        Map paraInfo = new HashMap();
        Map invoice_info = new HashMap();
        Map base_info = new HashMap();
        WeiXinMuBan weiXinMuBan = new WeiXinMuBan();
        weiXinMuBan.setPayee("测试-收款方");
        weiXinMuBan.setType("增值税普通发票");
        weiXinMuBan.setTitle(gsmc);
        weiXinMuBan.setLogo_url("http://mmbiz.qpic.cn/mmbiz_jpg/l249Gu1JJaIGMFSN5XWGdEFQlvG9VCemjLbSmw1enLNoluvfnV9JbM7zLkUgKGEVPcvqseHo9PZHTJM5mia2vSw/0");
        weiXinMuBan.setCustom_url_name("跳转名称");
        weiXinMuBan.setCustom_url_sub_title("右入口名称");
        weiXinMuBan.setPromotion_url_name("自定义入口");
        weiXinMuBan.setPromotion_url_sub_title("右自定义名");
        weiXinMuBan.setDescription("自己看流程");
        invoice_info.put("base_info",base_info);
        invoice_info.put("payee",weiXinMuBan.getPayee());
        invoice_info.put("type",weiXinMuBan.getType());
        invoice_info.put("detail","测试-detail");
        base_info.put("logo_url",weiXinMuBan.getLogo_url());
        base_info.put("title",weiXinMuBan.getTitle());
        base_info.put("description",weiXinMuBan.getDescription());
        base_info.put("custom_url_name",weiXinMuBan.getCustom_url_name());
        base_info.put("custom_url",weiXinMuBan.getCustom_url());
        base_info.put("custom_url_sub_title",weiXinMuBan.getCustom_url_sub_title());
        base_info.put("promotion_url_name",weiXinMuBan.getPromotion_url_name());
        base_info.put("promotion_url",weiXinMuBan.getPromotion_url());
        base_info.put("promotion_url_sub_title",weiXinMuBan.getPromotion_url_sub_title());
        paraInfo.put("invoice_info",invoice_info);

        System.out.println("参数"+JSON.toJSONString(paraInfo));
        String jsonStr = WeixinUtil.httpRequest(creatURL, "POST",JSON.toJSONString(paraInfo));
        if(null!=jsonStr){
            ObjectMapper jsonparer = new ObjectMapper();// 初始化解析json格式的对象
            try {
                Map map = jsonparer.readValue(jsonStr, Map.class);
                int errcode = (int) map.get("errcode");
                String errmsg = (String) map.get("errmsg");
                System.out.println("错误码"+errcode);
                if(errcode==0){
                    String card_id = (String) map.get("card_id");
                    resultMap.put("card_id",card_id);
                    resultMap.put("msg","发票卡券设置成功");
                    System.out.println("成功"+resultMap.toString());
                    return resultMap;
                }else{
                    resultMap.put("msg","发票卡券模板设置错误");
                    logger.info("返回的错误信息为"+errmsg);
                    System.out.println("卡券模板设置失败"+resultMap.toString());
                    return resultMap;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return  null;
    }

    /*
    * 将电子发票插入卡包
    * */

    public  String dzfpInCard(String order_id,String card_id,String pdf_file_url,Map weiXinData,List<Kpspmx> kpspmxList,Kpls kpls){
        //String order_id = "";
        //String card_id = "";
        String appid = WeiXinConstants.APP_ID;

        WeiXinInfo weiXinInfo = new WeiXinInfo();
        WeixinUtils weixinUtils = new WeixinUtils();


        Map sj = new HashMap();
        Map card_ext = new HashMap();
        Map user_card = new HashMap();
        Map info = new HashMap();
        Map invoice_user_data = new HashMap();
        sj.put("order_id",order_id);
        sj.put("card_id",card_id);
        sj.put("appid",appid);
        sj.put("card_ext",card_ext);

        String nonce_str = System.currentTimeMillis()+"";
        card_ext.put("nonce_str",nonce_str);
        card_ext.put("user_card",user_card);

        user_card.put("invoice_user_data",invoice_user_data);

        weiXinInfo.setTitle((String) weiXinData.get("title"));//发票抬头
        weiXinInfo.setFee(kpls.getJshj().intValue());//卡包开票金额,价税合计
       /* weiXinInfo.setFee(2);//发票金额
        weiXinInfo.setBilling_time(1480342498);//发票开票时间
        weiXinInfo.setBilling_no("150003522222");//发票代码
        weiXinInfo.setBilling_code("3643259");//发票号码
        weiXinInfo.setFee_without_tax(1);//不含税金额
        weiXinInfo.setTax(1);//税额
        weiXinInfo.setS_pdf_media_id("73181857960493557");
        weiXinInfo.setCheck_code("07729518401464999926");
        weiXinInfo.setName("衣服");
        weiXinInfo.setUnit("件");
        weiXinInfo.setNum(1);
        weiXinInfo.setPrice(12);*/
        if(kpspmxList.size()>0){
            for (Kpspmx kpspmx : kpspmxList){
            weiXinInfo.setName(kpspmx.getSpmc());
            weiXinInfo.setNum(kpspmx.getSps().intValue());
            weiXinInfo.setPrice(kpspmx.getSpdj().intValue());
            weiXinInfo.setUnit(kpspmx.getSpdw());
            }
        }


        String pdfUrl = kpls.getPdfurl();
        String  s_media_id_pdf = weixinUtils.creatPDF(pdfUrl,pdf_file_url);
        if(null!=s_media_id_pdf&& StringUtils.isNotBlank(s_media_id_pdf)){
            weiXinInfo.setS_pdf_media_id(s_media_id_pdf);
        }
        invoice_user_data.put("fee",weiXinInfo.getFee());
        invoice_user_data.put("title",weiXinInfo.getTitle());
        invoice_user_data.put("billing_time",weiXinInfo.getBilling_time());
        invoice_user_data.put("billing_no",weiXinInfo.getBilling_no());
        invoice_user_data.put("billing_code",weiXinInfo.getBilling_code());
        invoice_user_data.put("info",weiXinInfo.getInfo());
        invoice_user_data.put("fee_without_tax",weiXinInfo.getFee_without_tax());
        invoice_user_data.put("tax",weiXinInfo.getTax());
        invoice_user_data.put("s_pdf_media_id",weiXinInfo.getS_pdf_media_id());
        invoice_user_data.put("s_trip_pdf_media_id",weiXinInfo.getS_trip_pdf_media_id());
        invoice_user_data.put("check_code",weiXinInfo.getCheck_code());
        invoice_user_data.put("buyer_number",weiXinInfo.getBuyer_number());
        invoice_user_data.put("buyer_address_and_phone",weiXinInfo.getBuyer_address_and_phone());
        invoice_user_data.put("buyer_bank_account",weiXinInfo.getBuyer_bank_account());
        invoice_user_data.put("seller_number",weiXinInfo.getSeller_number());
        invoice_user_data.put("seller_address_and_phone",weiXinInfo.getSeller_address_and_phone());
        invoice_user_data.put("seller_bank_account",weiXinInfo.getSeller_bank_account());
        invoice_user_data.put("remarks",weiXinInfo.getRemarks());
        invoice_user_data.put("cashier",weiXinInfo.getCashier());
        invoice_user_data.put("maker",weiXinInfo.getMaker());

        info.put("name",weiXinInfo.getName());
        info.put("num",weiXinInfo.getNum());
        info.put("unit",weiXinInfo.getUnit());
        info.put("price",weiXinInfo.getPrice());

        System.out.println("封装的数据"+JSON.toJSONString(sj));
        if(null==sj.get("order_id")){
            logger.info("订单order_id为空");
            return  null;
        }
        if(null==sj.get("card_id")){
            logger.info("发票card_id为null");
            return  null;
        }
        if(null==sj.get("appid")){
            logger.info("商户appid为null");
            return  null;
        }
        if(null==card_ext.get("nonce_str")){
            logger.info("随机字符串nonce_str为null");
            return  null;
        }
        if(null==invoice_user_data.get("fee")){
            logger.info("发票金额fee为null");
            return  null;
        }
        if(null==invoice_user_data.get("title")){
            logger.info("发票抬头title为null");
            return  null;
        }
        if(null==invoice_user_data.get("billing_time")){
            logger.info("发票的开票时间billing_time为null");
            return  null;
        }
        if(null==invoice_user_data.get("billing_no")){
            logger.info("发票的发票代码billing_no为null");
            return  null;
        }
        if(null==invoice_user_data.get("billing_code")){
            logger.info("发票的发票号码billing_code为null");
            return  null;
        }
        if(null==invoice_user_data.get("fee_without_tax")){
            logger.info("不含税金额fee_without_tax为null");
            return  null;
        }
        if(null==invoice_user_data.get("s_pdf_media_id")){
            logger.info("上传PDF的s_pdf_media_id为null");
            return  null;
        }
        if(null==invoice_user_data.get("check_code")){
            logger.info("校验码check_code为null");
            return  null;
        }
        if(null==info.get("name")){
            logger.info("商品name为null");
            return  null;
        }
        if(null==info.get("name")){
            logger.info("商品name为null");
            return  null;
        }
        if(null==info.get("unit")){
            logger.info("商品unit为null");
            return  null;
        }
        if(null==info.get("price")){
            logger.info("商品price为null");
            return  null;
        }
        String access_token = (String) weixinUtils.hqtk().get("access_token");
        String URL =WeiXinConstants.dzfpInCard_url+access_token;
        System.out.println("电子发票插入卡包url++++"+URL);
        String jsonStr = WeixinUtil.httpRequest(URL, "POST",JSON.toJSONString(sj));
       if(null!=jsonStr){
            ObjectMapper jsonparer = new ObjectMapper();// 初始化解析json格式的对象
            try {
                Map map = jsonparer.readValue(jsonStr, Map.class);
                int errcode = (int) map.get("errcode");
                String errmsg = (String) map.get("errmsg");
                System.out.println("错误码"+errcode);
                if(errcode==0){
                    String openid = (String) map.get("openid");
                    logger.info("插入卡包成功,成功返回的openid为"+openid);
                    System.out.println("插入卡包成功,成功返回openid为"+openid);
                    return openid;
                }else{
                    logger.info("返回的错误信息为"+errmsg);
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    /*
    * 上传PDF
    * */
    public  String creatPDF(String pdfurl,String pdf_file_url){
        String pdfUrlPath="";

        if(null==pdf_file_url){
            logger.info("上传PDF路径pdf_file_url为null");
            return  null;
        }
        if(null==pdfurl){
            logger.info("上传PDF的url路径pdfurl为null");
            return  null;
        }
        if(null!=pdfurl&&StringUtils.isNotBlank(pdfurl)){
            String p = pdfurl.split("//")[1];
            if(null!=p&&StringUtils.isNoneEmpty(p)){
                String p1=pdfurl.split("//")[1].split("/")[1];
                String p2=pdfurl.split("//")[1].split("/")[2];
                String p3=pdfurl.split("//")[1].split("/")[3];
                String p4=pdfurl.split("//")[1].split("/")[4];
                pdfUrlPath= pdf_file_url+"/"+p1+"/"+p2+"/"+p3+"/"+p4;
            }
        }

        System.out.println("pdf路径问题"+pdfUrlPath);
        String s_media_id ="";
        WeixinUtils weixinUtils = new WeixinUtils();
        String access_token = (String) weixinUtils.hqtk().get("access_token");
        String pdfURL=WeiXinConstants.CREAT_PDF_URL+access_token;


        HttpPost httpPost = new HttpPost(pdfURL);
        HttpClient httpClient = new DefaultHttpClient();

        ObjectMapper jsonparer = new ObjectMapper();// 初始化解析json格式的对象
       // Map nvps = new HashMap();
       // nvps.put("pdf", file);
       // StringEntity requestEntity = new StringEntity(nvps.toString(), ContentType.MULTIPART_FORM_DATA);


        MultipartEntityBuilder builder  = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        FileBody fileBody = new FileBody(new File(pdfUrlPath));
        builder.addPart("pdf", fileBody);
        HttpEntity entit = builder.build();
        httpPost.setEntity(entit);
        try {
            HttpResponse res = httpClient.execute(httpPost);
            String responseContent = null; // 响应内容
            HttpEntity entityRes = res.getEntity();
            responseContent = EntityUtils.toString(entityRes, "UTF-8");
            Map map = jsonparer.readValue(responseContent, Map.class);
            int errcode = (int) map.get("errcode");
            // 将json字符串转换为json对象
            System.out.println("返回的数据"+map.toString());
            if(errcode==0){
                 System.out.println("上传PDF成功");
                 s_media_id = (String) map.get("s_media_id");
                 return  s_media_id;
            }else {
                String errmsg = (String) map.get("errmsg");
                System.out.println("上传PDF失败,失败原因"+errmsg);
                return  "上传PDF失败";
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
    }




}
