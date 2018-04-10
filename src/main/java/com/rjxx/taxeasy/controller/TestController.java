package com.rjxx.taxeasy.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rjxx.taxeasy.bizcomm.utils.*;
import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.dao.WxTokenJpaDao;
import com.rjxx.taxeasy.dao.WxfpxxJpaDao;
import com.rjxx.taxeasy.domains.*;
import com.rjxx.taxeasy.service.*;
import com.rjxx.utils.weixin.HttpClientUtil;
import com.rjxx.utils.IMEIGenUtils;
import com.rjxx.utils.StringUtils;
import com.rjxx.utils.TimeUtil;
import com.rjxx.utils.WeixinUtil;
import com.rjxx.utils.weixin.WechatBatchCard;
import com.rjxx.utils.weixin.WeiXinConstants;
import com.rjxx.utils.weixin.WeiXinInfo;
import com.rjxx.utils.weixin.WeixinUtils;
import com.rjxx.utils.yjapi.QCCUtils;
import com.sun.tools.internal.ws.wsdl.document.soap.SOAPUse;
import javafx.beans.binding.LongExpression;
import org.apache.axiom.om.OMElement;
import org.apache.commons.codec.binary.*;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import sun.net.ResourceManager;

import javax.sound.midi.Soundbank;
import javax.xml.crypto.Data;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.SimpleFormatter;

/**
 * Created by Administrator on 2017-08-15.
 */
@RestController
@RequestMapping("/test")
public class TestController extends BaseController {

    @Autowired
    private GetDataService getDataService;
    @Autowired
    private TqmtqService tqmtqService;//提取码提取
    @Autowired
    private JylsService jylsService;//交易流水

    @Autowired
    private WxfpxxJpaDao wxfpxxJpaDao;

    @Autowired
    private WxTokenJpaDao wxTokenJpaDao;

    @Autowired
    private WeixinUtils weixinUtils;

    @Autowired
    private JyxxsqService jyxxsqService;

    @Autowired
    private CszbService cszbService;

    @Autowired
    private BarcodeService barcodeService;

    @Autowired
    private QCCUtils qccUtils;
    @Autowired
    private SkpService skpService;
    @Autowired
    private XfService xfService;
    @Autowired
    private WechatBatchCard wechatBatchCard;
    @Autowired
    private KplsService kplsService;
    @Autowired
    private KpspmxService kpspmxService;

    @Autowired
    private YhService yhservice;

    private Integer i = 0;

    @Value("${rjxx.pdf_file_url:}")
    private String pdf_file_url;


    @RequestMapping(value = "/test")
    @ResponseBody
    public void test() {
        String tqm = "";
        for (long i = 40010114297l; i < 40012003033l; i++) {

            tqm = "00117071" + Long.toString(i);
            System.out.println("提取码" + tqm);
            Map resultSjMap = new HashMap();
            Map result = getDataService.getldyxFirData(tqm, "ldyx");
            System.out.println(result.toString());
            resultSjMap = getDataService.getldyxSecData(tqm, "ldyx", (String) result.get("accessToken"));
            System.out.println(resultSjMap.toString());
            List<Jyxxsq> jyxxsqList = (List) resultSjMap.get("jyxxsqList");
            List<Jymxsq> jymxsqList = (List) resultSjMap.get("jymxsqList");
            List<Jyzfmx> jyzfmxList = (List) resultSjMap.get("jyzfmxList");

            //封装数据
            Jyxxsq jyxxsq = jyxxsqList.get(0);
            jyxxsq.setGfmc("个人");
            jyxxsq.setGfemail("");
            jyxxsq.setSffsyj("1");
            jyxxsq.setGfsh("91370600050948561M");
            jyxxsq.setGfdz("徐家汇");
            jyxxsq.setGfdh("200123455");
            jyxxsq.setGfyh("中国银行");
            jyxxsq.setGfyhzh("40023154555");
            Map map = new HashMap<>();
            map.put("tqm", jyxxsq.getTqm());
            map.put("je", jyxxsq.getJshj());
            map.put("gsdm", jyxxsq.getGsdm());
            Tqmtq tqmtq = tqmtqService.findOneByParams(map);
            Jyls jyls1 = jylsService.findOne(map);
            if (tqmtq != null && tqmtq.getId() != null) {
                result.put("msg", "该提取码已提交过申请!");
                return;
            }
            if (jyls1 != null) {
                result.put("msg", "该订单正在开票!");
                return;
            }
            try {
                String xml = GetXmlUtil.getFpkjXml(jyxxsq, jymxsqList, jyzfmxList);
                String resultxml = HttpUtils.HttpUrlPost(xml, "RJcb0cb4d18ce7", "73e235a15ee5cb022691625a50edae3b");
                logger.info("-------返回值---------" + resultxml);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //插入表
            Tqmtq tqmtq1 = new Tqmtq();
            tqmtq1.setDdh(jyxxsq.getTqm());
            tqmtq1.setLrsj(new Date());
            tqmtq1.setZje(Double.valueOf(jyxxsq.getJshj()));
            tqmtq1.setGfmc("个人");
            tqmtq1.setNsrsbh("91370600050948561M");
            tqmtq1.setDz("徐家汇");
            tqmtq1.setDh("200123455");
            tqmtq1.setKhh("中国银行");
            tqmtq1.setKhhzh("40023154555");
            tqmtq1.setFpzt("0");
            tqmtq1.setYxbz("1");
            tqmtq1.setGfemail("");
            tqmtq1.setGsdm(jyxxsq.getGsdm());
            String llqxx = request.getHeader("User-Agent");
            tqmtq1.setLlqxx(llqxx);

            tqmtqService.save(tqmtq1);
        }

    }

    @RequestMapping(value = "/test2")
    @ResponseBody
    public void test2() throws Exception {

        String str1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<Request>\n" +
                "\t<ClientNO>gzc_01</ClientNO>\n" +
                "\t<!--必须,开票点编号 ,每个开票点对应唯一编号，比如KP001，KP002，该编号在开票通平台开票点信息模块维护-->\n" +
                "\t<SerialNumber>2016062412444500025</SerialNumber>\n" +
                "\t<!--SerialNumber必须，交易流水号String20，每次请求唯一值，不可重复，用于回写接口中与来源系统进行数据匹配-->\n" +
                "\t<InvType>12</InvType>\n" +
                "\t<!--InvType必须，发票种类（01、专用发票(纸质)；02、普通发票（纸质）；12、普通发票（电子）），电子发票使用12-->\n" +
                "\t<Spbmbbh>13.0</Spbmbbh>\n" +
                "\t<!-- Spbmbbh，商品编码版本号，即税局商品和服务税收分类编码的版本号-->\n" +
                "\t<Drawer>开票人</Drawer>\n" +
                "\t<!--Drawer必须，开票人String8，最多4个汉字，该栏目打印在发票上-->\n" +
                "\t<Payee>收款人</Payee>\n" +
                "\t<!--Payee可选，收款人String8，最多4个汉字，该栏目打印在发票上-->\n" +
                "\t<Reviewer>复核人</Reviewer>\n" +
                "\t<!--Reviewer可选，复核人String8，最多4个汉字，该栏目打印在发票上-->\n" +
                "\t<Seller>\n" +
                "\t<!--Seller 销方信息 -->\n" +
                "\t\t<Identifier>500102010003643</Identifier>\n" +
                "\t\t<!--Identifier必须，发票开具方税号String20，15、18或20位，该栏目打印在发票上-->\n" +
                "\t\t<Name>上海百旺测试3643</Name>\n" +
                "\t\t<!--Name必须，发票开具方名称String100，该栏目打印在发票上-->\n" +
                "\t\t<Address>德意志测试</Address>\n" +
                "\t\t<!--Address必须，发票开具方地址String100，该栏目打印在发票上-->\n" +
                "\t\t<TelephoneNo>021-59895352</TelephoneNo>\n" +
                "\t\t<!--TelephoneNo必须，发票开具方电话String20，该栏目打印在发票上-->\n" +
                "\t\t<Bank>美利坚大银行</Bank>\n" +
                "\t\t<!--Bank必须，发票开具方银行String100，该栏目打印在发票上-->\n" +
                "\t\t<BankAcc>128906323710203</BankAcc>\n" +
                "\t\t<!--BankAcc必须，发票开具方银行账号String30，该栏目打印在发票上-->\n" +
                "\t</Seller>\n" +
                "\t<OrderSize count=\"1\">\n" +
                "\t\t<Order>\n" +
                "\t\t\t<OrderMain>\n" +
                "\t\t\t\t<OrderNo>ME24156071</OrderNo>\n" +
                "\t\t\t\t<!-- OrderNo必须, 每笔订单号必须唯一，来源系统订单号，String20-->\n" +
                "\t\t\t\t<InvoiceList>0</InvoiceList>\n" +
                "\t\t\t\t<!--纸质票必须,是否打印清单 1 打印清单 0 不打印清单-->\n" +
                "\t\t\t\t<InvoiceSplit>1</InvoiceSplit>\n" +
                "\t\t\t\t<!--InvoiceSplit必须，超过最大开票限额或单张发票可开具行，是否自动拆分？（1、拆分；0、不拆分）-->\n" +
                "\t\t\t\t<InvoiceSfdy>1</InvoiceSfdy >\n" +
                "\t\t\t\t<!-- InvoiceSfdy必须，是否立即打印，1写税控设备并打印，0写税控设备不打印-->\n" +
                "\t\t\t\t<OrderDate>2016-06-22 23:59:59</OrderDate>\n" +
                "\t\t\t\t<!--OrderDate可选，来源系统订单时间，\"YYYY-MM-DD HH24:MI:SS\"格式-->\n" +
                "\t\t\t\t<ChargeTaxWay>0</ChargeTaxWay>\n" +
                "\t\t\t\t<!--ChargeTaxWay可选，征税方式，0-普通征税，1-减按征税，2-差额征税，String1-->\n" +
                "\t\t\t\t<TotalAmount>5410.00</TotalAmount>\n" +
                "\t\t\t\t<!--TotalAmount必须，价税合计，小数点后2位小数，该栏目打印在发票上-->\n" +
                "\t\t\t\t<TaxMark>0</TaxMark>\n" +
                "\t\t\t\t<!--TaxMark必须，交易流水中的金额是否含税？（1、含税；0、不含税）-->\n" +
                "\t\t\t\t<Remark>该栏目打印在发票上的备注</Remark>\n" +
                "\t\t\t\t<!--Remark可选，该栏目打印在发票上的备注，String200，该栏目打印在发票上-->\n" +
                "\t\t\t\t<ExtractedCode>4A2B3C4D5Y</ExtractedCode>\n" +
                "\t\t\t\t<!--ExtractedCode，可选，提取码，第三方系统产生，必须唯一，客户可以在网站上输入后根据提取码匹配交易信息和下载电子发票String100-->\n" +
                "\t\t\t\t<Buyer>\n" +
                "\t\t\t\t\t<CustomerType>0</CustomerType>\n" +
                "\t\t\t\t\t<!--CustomerType必选，客户类型（0个人、1公司），专票和需要报销的普票，客户类型为1-->\n" +
                "\t\t\t\t\t<Identifier>310105987654321</Identifier>\n" +
                "\t\t\t\t\t<!--Identifier可选，购买方税号String20，15、18或20位，该栏目打印在发票上，客户类型为1时必须-->\n" +
                "\t\t\t\t\t<Name>购买方名称</Name>\n" +
                "\t\t\t\t\t<!--Name必须，购买方名称String100，该栏目打印在发票上-->\n" +
                "\t\t\t\t\t<Address>某某路20号203室</Address>\n" +
                "\t\t\t\t\t<!--Address可选，购买方地址String100，该栏目打印在发票上，专用发票必须-->\n" +
                "\t\t\t\t\t<TelephoneNo>13912345678</TelephoneNo>\n" +
                "\t\t\t\t\t<!--TelephoneNo可选，购买方电话String20，该栏目打印在发票上，专用发票必须-->\n" +
                "\t\t\t\t\t<Bank>中国建设银行打浦桥支行</Bank>\n" +
                "\t\t\t\t\t<!--Bank可选，购买方银行String100，该栏目打印在发票上，专用发票必须-->\n" +
                "\t\t\t\t\t<BankAcc>123456789-0</BankAcc>\n" +
                "\t\t\t\t\t<!--BankAcc可选，购买方银行账号String30，该栏目打印在发票上，专用发票必须-->\n" +
                "\t\t\t\t\t<Email>abc@163.com</Email>\n" +
                "\t\t\t\t\t<!--Email可选，客户接收电子发票的电子邮箱地址String50-->\n" +
                "\t\t\t\t\t<IsSend>1</IsSend>\n" +
                "\t\t\t\t\t<!--IsSend电子发票生成后是否发送电子邮件（1、是；0、否）-->\n" +
                "\t\t\t\t\t<Recipient>张三</Recipient>\n" +
                "\t\t\t\t\t<!--Recipient可选，纸质发票收件人姓名String50-->\n" +
                "\t\t\t\t\t<ReciAddress>收件人地址</ReciAddress>\n" +
                "\t\t\t\t\t<!--Address可选，纸质发票收件人地址String200-->\n" +
                "\t\t\t\t\t<Zip>200000</Zip>\n" +
                "\t\t\t\t\t<!--zip可选，纸质发票收件人邮编String10-->\n" +
                "\t\t\t\t</Buyer>\n" +
                "\t\t\t</OrderMain>\n" +
                "\t\t\t<OrderDetails count=\"2\">\n" +
                "\t\t\t\t<!--size收费明细商品或服务行数，必须与ProductItem数量一致-->\n" +
                "\t\t\t\t<ProductItem>\n" +
                "\t\t\t\t\t<VenderOwnCode>商品自行编码</VenderOwnCode>\n" +
                "\t\t\t\t\t<!--可空，商品或收费项目的自行编码-->\n" +
                "\t\t\t\t\t<ProductCode>1010101010000000000</ProductCode>\n" +
                "\t\t\t\t\t<!--ProductCode必选，对应Spbmbbh的商品和服务税收分类编码String19 -->\n" +
                "\t\t\t\t\t<ProductName>稻谷</ProductName>\n" +
                "\t\t\t\t\t<!--ProductName必须，商品名称String30，该栏目打印在发票上-->\n" +
                "\t\t\t\t\t<RowType>0</RowType>\n" +
                "\t\t\t\t\t<!--RowType必须，发票行性质（0、正常行；1、折扣行；2、被折扣行）。比如充电器单价100元，折扣10元，则明细为2行，充电器行性质为2，折扣行性质为1。如果充电器没有折扣，则值应为0-->\n" +
                "\t\t\t\t\t<Spec>规格型号1</Spec>\n" +
                "\t\t\t\t\t<!--Spec可选，商品规格型号String20，该栏目打印在发票上-->\n" +
                "\t\t\t\t\t<Unit>单位1</Unit>\n" +
                "\t\t\t\t\t<!--Unit可选，商品单位String20，该栏目打印在发票上-->\n" +
                "\t\t\t\t\t<Quantity>1</Quantity>\n" +
                "\t\t\t\t\t<!--Quantity可选，商品数量，该栏目打印在发票上-->\n" +
                "\t\t\t\t\t<UnitPrice>1000.00</UnitPrice>\n" +
                "\t\t\t\t\t<!--UnitPrice可选，商品单价，如果TaxMark='1'，此单价为含税单价，否则不含税单价，该栏目打印在发票上-->\n" +
                "\t\t\t\t\t<Amount>1000.00</Amount>\n" +
                "\t\t\t\t\t<!--Amount必须，商品金额，如果TaxMark='1'，此金额为含税金额，否则不含税金额，该栏目打印在发票上-->\n" +
                "\t\t\t\t\t<DeductAmount></DeductAmount>  \n" +
                "\t\t\t\t\t<!--可空，ChargeTaxWay=2差额征收时必须，小数点后保留2位-->\n" +
                "\t\t\t\t\t<TaxRate>0.11</TaxRate>\n" +
                "\t\t\t\t\t<!--TaxRate必须，商品税率，税率只能为0或0.03或0.04或0.06或0.11或0.13或0.17，该栏目打印在发票上-->\n" +
                "\t\t\t\t\t<TaxAmount>110.00</TaxAmount>\n" +
                "\t\t\t\t\t<!--TaxAmount，商品税额，如果TaxMark='0'，商品税额必须，TaxMark='1'不须接入平台后由平台进行价税分离计算税额，该栏目打印在发票上-->\n" +
                "\t\t\t\t\t<MxTotalAmount>1110.00</MxTotalAmount>\n" +
                "\t\t\t\t\t<!--MxTotalAmount，商品明细价税合计，必须-->\n" +
                "\t\t\t\t\t<PolicyMark>0</PolicyMark>\n" +
                "\t\t\t\t\t<!--必须，0不使用优惠政策，1使用优惠政策。如果是“1”，优惠政策名称（PolicyName）必须与《商品和税收服务分类编码表.xlsx》表格中的该编码的优惠政策名称一一对应。-->\n" +
                "\t\t\t\t\t<TaxRateMark>0</TaxRateMark>\n" +
                "\t\t\t\t\t<!--必须,空或0是正常税率(税控服务器传空),1是免税。如果是免税，则税率（TaxRate）和税额（TaxAmount）必须为“0”，优惠政策标识必填“1”，且优惠政策（PolicyName）必须填“免税”。2是不征税,3普通零税率-->\n" +
                "\t\t\t\t\t<PolicyName>优惠政策名称2</PolicyName>\n" +
                "\t\t\t\t\t<!--优惠政策标识PolicyMark=1时必须，对应《商品和税收服务分类编码表.xlsx》表格中的“优惠政策名称”列的内容。如果优惠政策标识是“1”，此字段必填。-->\n" +
                "\t\t\t\t</ProductItem>\n" +
                "\t\t\t\t<ProductItem>\n" +
                "\t\t\t\t\t<ProductCode>1000000000000000000</ProductCode>\n" +
                "\t\t\t\t\t<!--ProductCode可选，商品代码String20 -->\n" +
                "\t\t\t\t\t<ProductName>商品2</ProductName>\n" +
                "\t\t\t\t\t<!--ProductName必须，商品名称String30，该栏目打印在发票上-->\n" +
                "\t\t\t\t\t<RowType>0</RowType>\n" +
                "\t\t\t\t\t<!--RowType必须，发票行性质（0、正常行；1、折扣行；2、被折扣行）。比如充电器单价100元，折扣10元，则明细为2行，充电器行性质为2，折扣行性质为1。如果充电器没有折扣，则值应为0-->\n" +
                "\t\t\t\t\t<Spec>规格型号2</Spec>\n" +
                "\t\t\t\t\t<!--Spec可选，商品规格型号String20，该栏目打印在发票上-->\n" +
                "\t\t\t\t\t<Unit>单位2</Unit>\n" +
                "\t\t\t\t\t<!--Unit可选，商品单位String20，该栏目打印在发票上-->\n" +
                "\t\t\t\t\t<Quantity>2</Quantity>\n" +
                "\t\t\t\t\t<!--Quantity可选，商品数量，该栏目打印在发票上-->\n" +
                "\t\t\t\t\t<UnitPrice>2000.00</UnitPrice>\n" +
                "\t\t\t\t\t<!--UnitPrice可选，商品单价，如果TaxMark='1'，此单价为含税单价，否则不含税单价，该栏目打印在发票上-->\n" +
                "\t\t\t\t\t<Amount>4000.00</Amount>\n" +
                "\t\t\t\t\t<!--Amount必须，商品金额，如果TaxMark='1'，此金额为含税金额，否则不含税金额，该栏目打印在发票上-->\n" +
                "\t\t\t\t\t<DeductAmount></DeductAmount>\n" +
                "\t\t\t\t\t<!--可空，ChargeTaxWay=2差额征收时必须，小数点后保留2位-->\n" +
                "\t\t\t\t\t<TaxRate>0.06</TaxRate>\n" +
                "\t\t\t\t\t<!--TaxRate必须，商品税率，税率只能为0或0.03或0.04或0.06或0.11或0.13或0.17，该栏目打印在发票上-->\n" +
                "\t\t\t\t\t<TaxAmount>240.00</TaxAmount>\n" +
                "\t\t\t\t\t<!--TaxAmount，商品税额，如果TaxMark='0'，商品税额必须，否则不须，该栏目打印在发票上-->\n" +
                "\t\t\t\t\t<MxTotalAmount>4240.00</MxTotalAmount>\n" +
                "\t\t\t\t\t<!-- MxTotalAmount，商品明细价税合计，必须-->\n" +
                "\t\t\t\t\t<VenderOwnCode>商品自行编码</VenderOwnCode>\n" +
                "\t\t\t\t\t<!--可空，ERP系统中商品或收费项目的自行编码-->\n" +
                "\t\t\t\t\t<PolicyMark>0</PolicyMark>\n" +
                "\t\t\t\t\t<!--必须，0不使用优惠政策，1使用优惠政策。如果是“1”，优惠政策名称（PolicyName）必须与《商品和税收服务分类编码表.xlsx》表格中的该编码的优惠政策名称一一对应。-->\n" +
                "\t\t\t\t\t<TaxRateMark>0</TaxRateMark>\n" +
                "\t\t\t\t\t<!--必须空或0，是正常税率(税控服务器传空)，1是免税。如果是免税，则税率（TaxRate）和税额（TaxAmount）必须为“0”，优惠政策标识必填“1”，且优惠政策（PolicyName）必须填“免税”。2是不征税3普通零税率-->\n" +
                "\t\t\t\t\t<PolicyName>优惠政策名称2</PolicyName>\n" +
                "\t\t\t\t\t<!--优惠政策标识PolicyMark=1时必须，对应《商品和税收服务分类编码表.xlsx》表格中的“优惠政策名称”列的内容。如果优惠政策标识是“1”，此字段必填。-->\n" +
                "\t\t\t\t</ProductItem>\n" +
                "\t\t\t</OrderDetails>\n" +
                "\t\t\t\n" +
                "\t\t</Order>\n" +
                "\t</OrderSize>\n" +
                "</Request>";
        try {
            //Map map = this.dealOperation01("gzc", str1);
            String resultxml = HttpUtils.HttpUrlPost(str1, "RJ5e56174e43bf", "9b372a74754ef2ad491f34f151dee6c8");
            System.out.println(JSON.toJSONString(resultxml));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 处理全部交易信息
     *
     * @param gsdm
     * @param OrderData
     * @return
     */
    private Map dealOperation01(String gsdm, String OrderData) {
        Map params1 = new HashMap();
        params1.put("gsdm", gsdm);
        Yh yh = yhservice.findOneByParams(params1);
        int lrry = yh.getId();
        OMElement root = null;
        List<Jyxxsq> jyxxsqList = new ArrayList();
        List<Jymxsq> jymxsqList = new ArrayList();
        List<Jyzfmx> jyzfmxList = new ArrayList<Jyzfmx>();
        Map rsMap = new HashMap();
        Document xmlDoc = null;
        try {
            xmlDoc = DocumentHelper.parseText(OrderData);
            root = XmlMapUtils.xml2OMElement(OrderData);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Map rootMap = XmlMapUtils.xml2Map(root, "Order");
        // 开票点代码
        String clientNO = (String) rootMap.get("ClientNO");

        // 交易流水号
        String serialNumber = (String) rootMap.get("SerialNumber");

        // 发票种类代码
        String invType = (String) rootMap.get("InvType");
        if (invType.equals("01")) {
            invType = "0";
        } else if (invType.equals("02")) {
            invType = "1";
        }
        // 发票业务类型
        String serviceType = (String) rootMap.get("ServiceType");

        // 开票人
        String drawer = (String) rootMap.get("Drawer");
        if (null == drawer) {
            drawer = "";
        }
        // 收款人
        String payee = (String) rootMap.get("Payee");
        if (null == payee) {
            payee = "";
        }
        // 复核人
        String reviewer = (String) rootMap.get("Reviewer");
        if (null == reviewer) {
            reviewer = "";
        }

        String sjly = String.valueOf(rootMap.get("DataSource"));
        if (null == sjly || sjly.equals("") ||  sjly.equals("null")) {
            sjly = "";
        }

        String openid =  String.valueOf(rootMap.get("OpenId"));
        if (null == openid || openid.equals("") || openid.equals("null")) {
            openid = "";
        }

        // 销方信息
        Map sellerMap = (Map) rootMap.get("Seller");
        String identifier = (String) sellerMap.get("Identifier");
        String name = (String) sellerMap.get("Name");
        String address = (String) sellerMap.get("Address");
        String telephoneNo = (String) sellerMap.get("TelephoneNo");
        String bank = (String) sellerMap.get("Bank");
        String bankAcc = (String) sellerMap.get("BankAcc");
        // 明细信息

        List<Element> xnList = xmlDoc.selectNodes("Request/OrderSize/Order");
        if (null != xnList && xnList.size() > 0) {
            for (Element xn : xnList) {
                Jyxxsq jyxxsq = new Jyxxsq();
                Element orderMainMap = (Element) xn.selectSingleNode("OrderMain");
                // 订单号
                String orderNo = "";
                if (null != orderMainMap.selectSingleNode("OrderNo")
                        && !orderMainMap.selectSingleNode("OrderNo").equals("")) {
                    orderNo = orderMainMap.selectSingleNode("OrderNo").getText();
                }
                // 是否打印清单 1 打印清单 0 不打印清单
                String invoiceList = "0";
                if (null != orderMainMap.selectSingleNode("InvoiceList")
                        && !orderMainMap.selectSingleNode("InvoiceList").equals("")) {
                    invoiceList = orderMainMap.selectSingleNode("InvoiceList").getText();
                }

                // 是否自动拆分（1、拆分；0、不拆分）
                String invoiceSplit = "1";
                if (null != orderMainMap.selectSingleNode("InvoiceSplit")
                        && !orderMainMap.selectSingleNode("InvoiceSplit").equals("")) {
                    invoiceSplit = orderMainMap.selectSingleNode("InvoiceSplit").getText();
                }
                //是否打印1打印，0不打印
                String InvoiceSfdy = "1";
                if (null != orderMainMap.selectSingleNode("InvoiceSfdy")
                        && !orderMainMap.selectSingleNode("InvoiceSfdy").equals("")) {
                    InvoiceSfdy = orderMainMap.selectSingleNode("InvoiceSfdy").getText();
                }
                // 订单日期
                String orderDate = "";
                if (null != orderMainMap.selectSingleNode("OrderDate")
                        && !orderMainMap.selectSingleNode("OrderDate").equals("")) {
                    orderDate = orderMainMap.selectSingleNode("OrderDate").getText();
                }

                // 征税方式
                String chargeTaxWay = "";
                if (null != orderMainMap.selectSingleNode("ChargeTaxWay")
                        && !orderMainMap.selectSingleNode("ChargeTaxWay").equals("")) {
                    chargeTaxWay = orderMainMap.selectSingleNode("ChargeTaxWay").getText();
                }

                // 价税合计
                String totalAmount = "";
                if (null != orderMainMap.selectSingleNode("TotalAmount")
                        && !orderMainMap.selectSingleNode("TotalAmount").equals("")) {
                    totalAmount = orderMainMap.selectSingleNode("TotalAmount").getText();
                }

                // 全局折扣
                String totalDiscount = "0.00";
                if (null != orderMainMap.selectSingleNode("TotalDiscount")
                        && !orderMainMap.selectSingleNode("TotalDiscount").equals("")) {
                    totalDiscount = orderMainMap.selectSingleNode("TotalDiscount").getText();
                }

                // 含税标志
                String taxMark = "";
                if (null != orderMainMap.selectSingleNode("TaxMark")
                        && !orderMainMap.selectSingleNode("TaxMark").equals("")) {
                    taxMark = orderMainMap.selectSingleNode("TaxMark").getText();
                }

                // 备注
                String remark = "";
                if (null != orderMainMap.selectSingleNode("Remark")
                        && !orderMainMap.selectSingleNode("Remark").equals("")) {
                    remark = orderMainMap.selectSingleNode("Remark").getText();
                }

                String ExtractedCode = "";
                if (null != orderMainMap.selectSingleNode("ExtractedCode")
                        && !orderMainMap.selectSingleNode("ExtractedCode").equals("")) {
                    ExtractedCode = orderMainMap.selectSingleNode("ExtractedCode").getText();
                }

                Element buyerMap = (Element) orderMainMap.selectSingleNode("Buyer");

                String buyerIdentifier = "";
                if (null != buyerMap.selectSingleNode("Identifier")
                        && !buyerMap.selectSingleNode("Identifier").equals("")) {
                    buyerIdentifier = buyerMap.selectSingleNode("Identifier").getText();
                }

                //购方客户类型0不报销、1报销
                String CustomerType = "";
                if (null != buyerMap.selectSingleNode("CustomerType")
                        && !buyerMap.selectSingleNode("CustomerType").equals("")) {
                    CustomerType = buyerMap.selectSingleNode("CustomerType").getText();
                }

                String buyerName = "";
                if (null != buyerMap.selectSingleNode("Name") && !buyerMap.selectSingleNode("Name").equals("")) {
                    buyerName = buyerMap.selectSingleNode("Name").getText();
                }

                String buyerAddress = "";
                if (null != buyerMap.selectSingleNode("Address") && !buyerMap.selectSingleNode("Address").equals("")) {
                    buyerAddress = buyerMap.selectSingleNode("Address").getText();
                }

                String buyerTelephoneNo = "";
                if (null != buyerMap.selectSingleNode("TelephoneNo")
                        && !buyerMap.selectSingleNode("TelephoneNo").equals("")) {
                    buyerTelephoneNo = buyerMap.selectSingleNode("TelephoneNo").getText();
                }

                String buyerBank = "";
                if (null != buyerMap.selectSingleNode("Bank") && !buyerMap.selectSingleNode("Bank").equals("")) {
                    buyerBank = buyerMap.selectSingleNode("Bank").getText();
                }

                String buyerBankAcc = "";
                if (null != buyerMap.selectSingleNode("BankAcc") && !buyerMap.selectSingleNode("BankAcc").equals("")) {
                    buyerBankAcc = buyerMap.selectSingleNode("BankAcc").getText();
                }

                String buyerEmail = "";
                if (null != buyerMap.selectSingleNode("Email") && !buyerMap.selectSingleNode("Email").equals("")) {
                    buyerEmail = buyerMap.selectSingleNode("Email").getText();
                }

                String buyerIsSend = "";
                if (null != buyerMap.selectSingleNode("IsSend") && !buyerMap.selectSingleNode("IsSend").equals("")) {
                    buyerIsSend = buyerMap.selectSingleNode("IsSend").getText();
                }

                String buyerExtractedCode = "";
                if (null != buyerMap.selectSingleNode("ExtractedCode")
                        && !buyerMap.selectSingleNode("ExtractedCode").equals("")) {
                    buyerExtractedCode = buyerMap.selectSingleNode("ExtractedCode").getText();
                }

                String buyerRecipient = "";
                if (null != buyerMap.selectSingleNode("Recipient")
                        && !buyerMap.selectSingleNode("Recipient").equals("")) {
                    buyerRecipient = buyerMap.selectSingleNode("Recipient").getText();
                }

                String buyerReciAddress = "";
                if (null != buyerMap.selectSingleNode("ReciAddress")
                        && !buyerMap.selectSingleNode("ReciAddress").equals("")) {
                    buyerReciAddress = buyerMap.selectSingleNode("ReciAddress").getText();
                }

                String buyerZip = "";
                if (null != buyerMap.selectSingleNode("Zip") && !buyerMap.selectSingleNode("Zip").equals("")) {
                    buyerZip = buyerMap.selectSingleNode("Zip").getText();
                }

                String khh = "";
                if (null != buyerMap.selectSingleNode("Khh") && !buyerMap.selectSingleNode("Khh").equals("")) {
                    khh = buyerMap.selectSingleNode("Khh").getText();
                }
                String MobilephoneNo = "";
                if (null != buyerMap.selectSingleNode("MobilephoneNo") && !buyerMap.selectSingleNode("MobilephoneNo").equals("")) {
                    MobilephoneNo = buyerMap.selectSingleNode("MobilephoneNo").getText();
                }
                // 保存主表信息
                jyxxsq.setKpddm(clientNO);
                jyxxsq.setJylsh(serialNumber);
                jyxxsq.setFpzldm(invType);
                jyxxsq.setKpr(drawer);
                jyxxsq.setSkr(payee);
                jyxxsq.setFhr(reviewer);
                jyxxsq.setOpenid(openid);
                jyxxsq.setXfsh(identifier);
                jyxxsq.setXfmc(name);
                jyxxsq.setXfdz(address);
                jyxxsq.setXfdh(telephoneNo);
                jyxxsq.setXfyh(bank);
                jyxxsq.setXfyhzh(bankAcc);
                jyxxsq.setDdh(orderNo);
                jyxxsq.setSfdyqd(invoiceList);
                jyxxsq.setSfcp(invoiceSplit);
                jyxxsq.setSfdy(InvoiceSfdy);
                SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    jyxxsq.setDdrq(orderDate == null ? new Date() : sim.parse(orderDate));
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                jyxxsq.setZsfs(chargeTaxWay);
                jyxxsq.setJshj(Double.valueOf(totalAmount));
                String tmpQjzk =(null ==totalDiscount || totalDiscount.equals(""))?"0.00":totalDiscount;
                jyxxsq.setQjzk(Double.valueOf(tmpQjzk));
                jyxxsq.setHsbz(taxMark);
                jyxxsq.setBz(remark);
                jyxxsq.setGflx(CustomerType);
                jyxxsq.setGfsh(buyerIdentifier.replaceAll(" ",""));
                jyxxsq.setGfmc(buyerName.replaceAll(" ",""));
                jyxxsq.setGfdz(buyerAddress);
                jyxxsq.setGfdh(buyerTelephoneNo);
                jyxxsq.setGfyh(buyerBank);
                jyxxsq.setGfyhzh(buyerBankAcc);
                jyxxsq.setGfemail(buyerEmail);
                jyxxsq.setGfsjh(MobilephoneNo);
                jyxxsq.setKhh(khh);
                jyxxsq.setSffsyj(buyerIsSend);
                //为了照顾亚朵，途家两家老版本的发票开具xml样例
                if(null != ExtractedCode && !ExtractedCode.equals("")){
                    jyxxsq.setTqm(ExtractedCode);
                }else if(null != buyerExtractedCode && !buyerExtractedCode.equals("")){
                    jyxxsq.setTqm(buyerExtractedCode);
                }
                jyxxsq.setGfsjr(buyerRecipient);
                jyxxsq.setGfsjrdz(buyerReciAddress);
                jyxxsq.setGfyb(buyerZip);
                jyxxsq.setYkpjshj(Double.valueOf("0.00"));
                jyxxsq.setYxbz("1");
                jyxxsq.setLrsj(new Date());
                jyxxsq.setLrry(lrry);
                jyxxsq.setXgry(lrry);
                jyxxsq.setFpczlxdm("11");
                jyxxsq.setXgsj(new Date());
                jyxxsq.setGsdm(gsdm);
                if(sjly.equals("") || null == sjly){
                    jyxxsq.setSjly("1");
                }else{
                    jyxxsq.setSjly(sjly);
                }

                jyxxsq.setClztdm("00");
                jyxxsqList.add(jyxxsq);
                // List orderDetailsList = (List)
                // orderMainMap.get("OrderDetails");
                Element OrderDetails = (Element) xn.selectSingleNode("OrderDetails");
                List<Element> orderDetailsList = (List<Element>) OrderDetails.elements("ProductItem");
                if (null != orderDetailsList && orderDetailsList.size() > 0) {
                    int spmxxh = 1;
                    for (Element orderDetails : orderDetailsList) {
                        Jymxsq jymxsq = new Jymxsq();
                        // Map ProductItem = (Map) orderDetailsList.get(j);
                        //spmxxh++;
                        // 商品代码
                        String ProductCode = "";
                        if (null != orderDetails.selectSingleNode("ProductCode")
                                && !orderDetails.selectSingleNode("ProductCode").equals("")) {
                            ProductCode = orderDetails.selectSingleNode("ProductCode").getText();
                        }

                        jymxsq.setSpdm(ProductCode);
                        // 商品名称
                        String ProductName = "";
                        if (null != orderDetails.selectSingleNode("ProductName")
                                && !orderDetails.selectSingleNode("ProductName").equals("")) {
                            ProductName = orderDetails.selectSingleNode("ProductName").getText();
                        }

                        jymxsq.setSpmc(ProductName);
                        jymxsq.setDdh(jyxxsq.getDdh());
                        jymxsq.setHsbz(jyxxsq.getHsbz());
                        // 发票行性质
                        String RowType = "";
                        if (null != orderDetails.selectSingleNode("RowType")
                                && !orderDetails.selectSingleNode("RowType").equals("")) {
                            RowType = orderDetails.selectSingleNode("RowType").getText();
                        }

                        jymxsq.setFphxz(RowType);
                        // 商品规格型号
                        String Spec = "";
                        if (null != orderDetails.selectSingleNode("Spec")
                                && !orderDetails.selectSingleNode("Spec").equals("")) {
                            Spec = orderDetails.selectSingleNode("Spec").getText();
                        }

                        jymxsq.setSpggxh(Spec);
                        // 商品单位
                        String Unit = "";
                        if (null != orderDetails.selectSingleNode("Unit")
                                && !orderDetails.selectSingleNode("Unit").equals("")) {
                            Unit = orderDetails.selectSingleNode("Unit").getText();
                        }

                        jymxsq.setSpdw(Unit);
                        // 商品数量
                        String Quantity = "";
                        if (null != orderDetails.selectSingleNode("Quantity")
                                && !orderDetails.selectSingleNode("Quantity").equals("")) {
                            Quantity = orderDetails.selectSingleNode("Quantity").getText();
                            try{jymxsq.setSps(Double.valueOf(Quantity));}catch (Exception e){jymxsq.setSps(null);}

                        }

                        // 商品单价
                        String UnitPrice = "";
                        if (null != orderDetails.selectSingleNode("UnitPrice")
                                && !orderDetails.selectSingleNode("UnitPrice").equals("")) {
                            UnitPrice = orderDetails.selectSingleNode("UnitPrice").getText();
                            try{jymxsq.setSpdj(Double.valueOf(UnitPrice));}catch (Exception e){jymxsq.setSpdj(null);}
                        }

                        // 商品金额
                        String Amount = "";
                        if (null != orderDetails.selectSingleNode("Amount")
                                && !orderDetails.selectSingleNode("Amount").equals("")) {
                            Amount = orderDetails.selectSingleNode("Amount").getText();
                            jymxsq.setSpje(Double.valueOf(Amount));
                        }

                        // 扣除金额
                        String DeductAmount = "";
                        if (null != orderDetails.selectSingleNode("DeductAmount")
                                && !orderDetails.selectSingleNode("DeductAmount").equals("")) {
                            DeductAmount = orderDetails.selectSingleNode("DeductAmount").getText();
                            jymxsq.setKce((null == DeductAmount || DeductAmount.equals("")) ? Double.valueOf("0.00")
                                    : Double.valueOf(DeductAmount));
                        }

                        String TaxRate = "";
                        if (null != orderDetails.selectSingleNode("TaxRate")
                                && !orderDetails.selectSingleNode("TaxRate").equals("")) {
                            TaxRate = orderDetails.selectSingleNode("TaxRate").getText();
                            jymxsq.setSpsl(Double.valueOf(TaxRate));
                        }

                        String TaxAmount = "";
                        if (null != orderDetails.selectSingleNode("TaxAmount")
                                && !orderDetails.selectSingleNode("TaxAmount").equals("")) {
                            TaxAmount = orderDetails.selectSingleNode("TaxAmount").getText();
                            try{jymxsq.setSpse(Double.valueOf(TaxAmount));}catch (Exception e){jymxsq.setSpse(null);}

                        }

                        String MxTotalAmount = "";
                        if (null != orderDetails.selectSingleNode("MxTotalAmount")
                                && !orderDetails.selectSingleNode("MxTotalAmount").equals("")) {
                            MxTotalAmount = orderDetails.selectSingleNode("MxTotalAmount").getText();
                            jymxsq.setJshj(Double.valueOf(MxTotalAmount));
                        }

                        jymxsq.setSpmxxh(spmxxh);
                        if(RowType.equals("2")){//如果为被折扣行，则明细序号不变，反之明细序号加1

                        }else{
                            spmxxh++;
                        }
                        jymxsq.setKkjje(Double.valueOf(MxTotalAmount));
                        jymxsq.setYkjje(0d);
                        String VenderOwnCode = "";
                        if (null != orderDetails.selectSingleNode("VenderOwnCode")
                                && !orderDetails.selectSingleNode("VenderOwnCode").equals("")) {
                            VenderOwnCode = orderDetails.selectSingleNode("VenderOwnCode").getText();
                        }
                        jymxsq.setSpzxbm(VenderOwnCode);

                        String PolicyMark = "";
                        if (null != orderDetails.selectSingleNode("PolicyMark")
                                && !orderDetails.selectSingleNode("PolicyMark").equals("")) {
                            PolicyMark = orderDetails.selectSingleNode("PolicyMark").getText();
                        }
                        jymxsq.setYhzcbs(PolicyMark);

                        String TaxRateMark = "";
                        if (null != orderDetails.selectSingleNode("TaxRateMark")
                                && !orderDetails.selectSingleNode("TaxRateMark").equals("")) {
                            TaxRateMark = orderDetails.selectSingleNode("TaxRateMark").getText();
                        }
                        jymxsq.setLslbz(TaxRateMark);

                        String PolicyName = "";
                        if (null != orderDetails.selectSingleNode("PolicyName")
                                && !orderDetails.selectSingleNode("PolicyName").equals("")) {
                            PolicyName = orderDetails.selectSingleNode("PolicyName").getText();
                        }
                        jymxsq.setYhzcmc(PolicyName);

                        jymxsq.setGsdm(gsdm);
                        jymxsq.setLrry(lrry);
                        jymxsq.setLrsj(new Date());
                        jymxsq.setXgry(lrry);
                        jymxsq.setXgsj(new Date());
                        jymxsq.setYxbz("1");
                        jymxsqList.add(jymxsq);

                    }

                }
                // 获取参数中对应的支付信息
                Element payments = (Element) xn.selectSingleNode("Payments");
                if (null != payments && !payments.equals("")) {
                    List<Element> paymentItemList = (List<Element>) payments.elements("PaymentItem");

                    if (null != paymentItemList && paymentItemList.size() > 0) {
                        for (Element PaymentItem : paymentItemList) {
                            Jyzfmx jyzfmx = new Jyzfmx();
                            String zffsDm = "";
                            if (null != PaymentItem.selectSingleNode("PayCode")
                                    && !PaymentItem.selectSingleNode("PayCode").equals("")) {
                                zffsDm = PaymentItem.selectSingleNode("PayCode").getText();
                                jyzfmx.setZffsDm(zffsDm);
                            }
                            String zfje = "";
                            if (null != PaymentItem.selectSingleNode("PayPrice")
                                    && !PaymentItem.selectSingleNode("PayPrice").equals("")) {
                                zfje = PaymentItem.selectSingleNode("PayPrice").getText();
                                jyzfmx.setZfje(Double.valueOf(zfje));
                            }
                            jyzfmx.setGsdm(gsdm);
                            jyzfmx.setDdh(jyxxsq.getDdh());
                            jyzfmx.setLrry(lrry);
                            jyzfmx.setLrsj(new Date());
                            jyzfmx.setXgry(lrry);
                            jyzfmx.setXgsj(new Date());
                            jyzfmxList.add(jyzfmx);
                        }

                    }
                }

            }
        }

        rsMap.put("jyxxsqList", jyxxsqList);
        rsMap.put("jymxsqList", jymxsqList);
        rsMap.put("jyzfmxList", jyzfmxList);
        return rsMap;
    }

    public static String WS_URL = "http://192.168.1.23:8080/service/afterPayDoKp";


    @RequestMapping(value = "/testfpIncard2")
    @ResponseBody
    public void fpIncard() {
        try {
            WeixinUtils weixinUtils = new WeixinUtils();
            WxFpxx wxFpxx = wxfpxxJpaDao.selectByWeiXinOrderNo("011120128903744060943");
            Map kplsMap = new HashMap();
            kplsMap.put("kplsh", wxFpxx.getKplsh());
            Kpls kpls = kplsService.findOneByParams(kplsMap);
            Map params2 = new HashMap();
            params2.put("kplsh", wxFpxx.getKplsh());
            List<Kpspmx> kpspmxList = kpspmxService.findMxNewList(params2);
            Map map = new HashMap();
            map.put("title",kpls.getGfmc());
            String dzfpInCard = weixinUtils.dzfpInCard("011120128903744060943",
                    "ps2OFs3VkOZ0MqeXKsQVSn1HLR9w",
                    "7857340736311788082", map, kpspmxList, kpls, "5_qk7lupjTJtoXwbWJDd62qFAcUYcUlXZWvbXN25mA49XM9eT70YVSIHc-ZhIshqeA59Y4f8zvTP71stvXJkQlN3joJn5a_jeP2-Wfqpaw7gUqfGrgY1DXGw3C7Hoo4m3OCgJGYiJCpqZYxNVgTOSdAHADYC");
            logger.info("结果"+dzfpInCard);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int compare_date(String DATE1, String DATE2) {
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            Date dt1 = df.parse(DATE1);
            Date dt2 = df.parse(DATE2);
            if (dt1.getTime() > dt2.getTime()) {
                System.out.println("dt1 在dt2前");
                return 1;
            } else if (dt1.getTime() < dt2.getTime()) {
                System.out.println("dt1在dt2后");
                return -1;
            } else {
                return 0;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return 2;
    }
    public static void main(String[] args) throws Exception {

        try {
          /*  SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar c = Calendar.getInstance();
            //过去一月
            c.setTime(new Date());
            c.add(Calendar.MONTH, -1);
            Date m = c.getTime();
            String mon = format.format(m);
            System.out.println("过去一个月："+mon);*/
            /*Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat y = new SimpleDateFormat("yyyy");
            String jssj = com.rjxx.time.TimeUtil.getBeforeDays(sdf.format(now),1);
            String year = y.format(now)+"-01-01";
            String kssj = com.rjxx.time.TimeUtil.getBeforeDays(jssj, 365);
            //System.out.println("今天之前的前一天,日期格式"+sdf.parse(jssj));
            //System.out.println("365天之前"+sdf.parse(kssj));
            System.out.println("当前年的前一年"+sdf.parse(year));
            Calendar calendar = Calendar.getInstance();
            Date date = new Date(System.currentTimeMillis());
            calendar.setTime(date);
            calendar.add(Calendar.YEAR, -1);
            date = calendar.getTime();
            System.out.println(date);*/
            String Email="olivier@sushi-o.com";
            //boolean b = str.matches("^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$");
            //String Email = (String) jyxxsq.getGfemail();
           /* if (Email != null && !Email.equals("") && !Email
                    .matches("^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-\\.\\_]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$")) {
                System.out.println(":购方邮箱(Email)格式有误;");
            }else {
                System.out.println(111);
            }*/

           String stri ="0301208888000,1181408418";
            String[] strs=stri.split(",");
            for(int i=0,len=strs.length;i<len;i++){
                System.out.println(strs[i].toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

