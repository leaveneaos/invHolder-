package com.rjxx.taxeasy.utils.weixin;

/**
 * Created by Administrator on 2017-08-03.
 */
public class WeiXinConstants {

    //微信appid
    public static final String APP_ID = "wx9abc729e2b4637ee";
    //沙箱appid
    //public static final String APP_ID = "wx8c2a4c2289e10ffb";
    //沙箱appSecret
    //public static  final  String APP_SECRET = "ad706ca065a0d384414ae3b568e030fb";
    //微信AppSecret
    public static  final  String APP_SECRET = "6415ee7a53601b6a0e8b4ac194b382eb";
    //微信回调地址
    public static final String AFTER_WEIXIN_REDIRECT_URL = "/getWeiXinURL";
    //微信授权成功后跳转url
    public static final String BEFORE_WEIXIN_REDIRECT_URL = "/getTiaoZhuanURL";
    //重定向url
    public static final String RJXX_REDIRECT_URL = "WWW.baidu.com";
    //创建发票卡卷模板
    public  static  final  String CREAT_MUBAN_URL = "https://api.weixin.qq.com/card/invoice/platform/createcard?access_token=";
    //将电子发票插入微信卡包
    public  static  final  String dzfpInCard_url = "https://api.weixin.qq.com/card/invoice/insert?access_token=";

    //全家发票模板card_id
    public static  final  String FAMILY_CARD_ID ="ps2OFsxAwbb7SJ2v4H2cvHAq6ScM";

    //上传PDF地址
    public  static  final String CREAT_PDF_URL = "https://api.weixin.qq.com/card/invoice/platform/setpdf?access_token=";

    //发票报销状态
    public  static  final String INVOICE_STATUS_INIT = "INVOICE_REIMBURSE_INIT";//发票初始状态，未锁定
    public  static  final String INVOICE_STATUS_LOCK = "INVOICE_REIMBURSE_LOCK";//发票已锁定
    public  static  final String INVOICE_STATUS_CLOUSE = "INVOICE_REIMBURSE_CLOSURE";//发票已核销

    //申请开票完成跳转url     http://fpj.datarj.com/einv/Family/witting.html
    public  static final String SUCCESS_REDIRECT_URL = "http://fpjtest.datarj.com/einv/Family/witting.html";//测试地址等待页面
}
