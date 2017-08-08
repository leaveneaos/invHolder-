package com.rjxx.taxeasy.utils.weixin;

/**
 * Created by Administrator on 2017-08-03.
 */
public class WeiXinConstants {

    //微信appid
    public static final String APP_ID = "wx9abc729e2b4637ee";
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
}
