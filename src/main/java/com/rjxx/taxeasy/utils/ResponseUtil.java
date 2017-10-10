package com.rjxx.taxeasy.utils;

import com.rjxx.taxeasy.bizcomm.utils.InvoiceResponse;
import com.rjxx.taxeasy.domains.Jyls;
import com.rjxx.taxeasy.domains.Kpls;
import com.rjxx.taxeasy.service.JylsService;
import com.rjxx.taxeasy.service.KplsService;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;

@Service
public class ResponseUtil {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private KplsService kplsService;

    @Autowired
    private JylsService jylsService;

    /**
     * 封装调用组件接口开票的返回报文，Operation为01
     *
     * @param list
     * @return String
     */
    public String response(List list) {
        Document xmlDoc = null;
        XMLWriter xw;
        Document doc = DocumentHelper.createDocument();
        StringWriter sendXml = new StringWriter();
        //封装对象用于回写发票代码和号码
        InvoiceResponse response = new InvoiceResponse();
        // 增加根节点
        Element Responese = doc.addElement("Responese");
        Element returnCode = Responese.addElement("ReturnCode");
        returnCode.setText("0000");
        Element returnMessage = Responese.addElement("ReturnMessage");
        returnMessage.setText("成功");
        if (null != list && !list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                String tmp = String.valueOf(list.get(i));
                try {
                    xmlDoc = DocumentHelper.parseText(tmp);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Element xnt = (Element) xmlDoc.selectSingleNode("InvoiceData/body/output");
                // 为根节点增加元素body

                Element RCF = Responese.addElement("invoice");
                Element CLIENTNO = RCF.addElement("CLIENTNO");
                CLIENTNO.setText(xnt.selectSingleNode("CLIENTNO").getText() == null ? ""
                        : xnt.selectSingleNode("CLIENTNO").getText());// 添加值
                Element SwiftNumber = RCF.addElement("SwiftNumber");
                SwiftNumber.setText(xnt.selectSingleNode("SwiftNumber") == null ? ""
                        : xnt.selectSingleNode("SwiftNumber").getText());
                Element SysInvNo = RCF.addElement("SysInvNo");
                SysInvNo.setText(
                        xnt.selectSingleNode("SysInvNo") == null ? "" : xnt.selectSingleNode("SysInvNo").getText());

                response.setLsh(xnt.selectSingleNode("SysInvNo") == null ? "" : xnt.selectSingleNode("SysInvNo").getText());

                Element InvCode = RCF.addElement("InvCode");
                InvCode.setText(
                        xnt.selectSingleNode("InvCode") == null ? "" : xnt.selectSingleNode("InvCode").getText());

                response.setFpdm(xnt.selectSingleNode("InvCode") == null ? "" : xnt.selectSingleNode("InvCode").getText());

                Element InvNo = RCF.addElement("InvNo");
                InvNo.setText(xnt.selectSingleNode("InvNo") == null ? "" : xnt.selectSingleNode("InvNo").getText());

                response.setFphm(xnt.selectSingleNode("InvNo") == null ? "" : xnt.selectSingleNode("InvNo").getText());

                Element InvDate = RCF.addElement("InvDate");
                InvDate.setText(
                        xnt.selectSingleNode("InvDate") == null ? "" : xnt.selectSingleNode("InvDate").getText());

                response.setKprq(xnt.selectSingleNode("InvDate") == null ? "" : xnt.selectSingleNode("InvDate").getText());

                Element CancelDate = RCF.addElement("CancelDate");
                CancelDate.setText(
                        xnt.selectSingleNode("CancelDate") == null ? "" : xnt.selectSingleNode("CancelDate").getText());
                Element OperateFlag = RCF.addElement("OperateFlag");
                OperateFlag.setText(xnt.selectSingleNode("OperateFlag") == null ? ""
                        : xnt.selectSingleNode("OperateFlag").getText());

                response.setReturnCode(xnt.selectSingleNode("OperateFlag") == null ? ""
                        : xnt.selectSingleNode("OperateFlag").getText());

                Element PrintFlag = RCF.addElement("PrintFlag");
                PrintFlag.setText(
                        xnt.selectSingleNode("PrintFlag") == null ? "" : xnt.selectSingleNode("PrintFlag").getText());

                response.setPrintFlag(xnt.selectSingleNode("PrintFlag") == null ? Integer.valueOf("1") : Integer.valueOf(xnt.selectSingleNode("PrintFlag").getText()));

                Element returnmsg = RCF.addElement("returnmsg");
                returnmsg.setText(
                        xnt.selectSingleNode("returnmsg") == null ? "" : xnt.selectSingleNode("returnmsg").getText());
                response.setReturnMessage(xnt.selectSingleNode("returnmsg") == null ? "" : xnt.selectSingleNode("returnmsg").getText());

                //回写没条发票的代码及号码或者是写错误信息
                reWriteCoAndNo(response);
            }
            // 规范格式
            OutputFormat format = OutputFormat.createPrettyPrint();
            // 设置输出编码
            format.setEncoding("gbk");
            xw = new XMLWriter(sendXml, format);
            try {
                xw.write(doc);
                xw.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        return sendXml.toString();
    }


    /**
     * 封装调用录屏方式开票的返回报文，Operation为01
     *
     * @param list
     * @return String
     */
    public String lpResponse(List list) {
        Document xmlDoc = null;
        XMLWriter xw;
        Document doc = DocumentHelper.createDocument();
        StringWriter sendXml = new StringWriter();
        // 增加根节点
        Element Responese = doc.addElement("Responese");
        Element returnCode = Responese.addElement("ReturnCode");
        returnCode.setText("0000");
        Element returnMessage = Responese.addElement("ReturnMessage");
        returnMessage.setText("成功");
        if (null != list && !list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                InvoiceResponse invoiceresponse = (InvoiceResponse) list.get(i);
                Element RCF = Responese.addElement("invoice");
                Element CLIENTNO = RCF.addElement("CLIENTNO");
                CLIENTNO.setText(invoiceresponse.getKpddm() == null ? "" : invoiceresponse.getKpddm());// 添加值

                Element SwiftNumber = RCF.addElement("SwiftNumber");
                SwiftNumber.setText(invoiceresponse.getJylsh() == null ? "" : invoiceresponse.getJylsh());

                Element SysInvNo = RCF.addElement("SysInvNo");
                SysInvNo.setText(invoiceresponse.getLsh() == null ? "" : invoiceresponse.getLsh());

                Element InvCode = RCF.addElement("InvCode");
                InvCode.setText(invoiceresponse.getFpdm() == null ? "" : invoiceresponse.getFpdm());

                Element InvNo = RCF.addElement("InvNo");
                InvNo.setText(invoiceresponse.getFphm() == null ? "" : invoiceresponse.getFphm());

                Element InvDate = RCF.addElement("InvDate");
                InvDate.setText(invoiceresponse.getKprq() == null ? "" : invoiceresponse.getKprq());

                Element CancelDate = RCF.addElement("CancelDate");
                CancelDate.setText("");

                Element OperateFlag = RCF.addElement("OperateFlag");
                OperateFlag.setText(invoiceresponse.getReturnCode() == "0000" ? "0" : "1");

                Element PrintFlag = RCF.addElement("PrintFlag");
                PrintFlag.setText(invoiceresponse == null ? "" : String.valueOf(invoiceresponse.getPrintFlag()));
                Element returnmsg = RCF.addElement("returnmsg");
                returnmsg.setText(invoiceresponse.getReturnMessage() == null
                        ? "" : invoiceresponse.getReturnMessage());

            }
        }
        // 规范格式
        OutputFormat format = OutputFormat.createPrettyPrint();
        // 设置输出编码
        format.setEncoding("UTF-8");
        xw = new XMLWriter(sendXml, format);
        try {
            xw.write(doc);
            xw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return sendXml.toString();
    }


    /**
     * 封装查询接口的返回报文，Operation为08
     *
     * @param result
     * @return
     */
    public String response08(String result) {
        Document xmlDoc = null;
        XMLWriter xw;
        Document doc = DocumentHelper.createDocument();
        StringWriter sendXml = new StringWriter();
        // 增加根节点
        Element Responese = doc.addElement("Responese");
        try {
            xmlDoc = DocumentHelper.parseText(result);
            Element xnt = (Element) xmlDoc.selectSingleNode("InvoiceData/body/output");
            Element ClientNO = Responese.addElement("CLIENTNO");
            ClientNO.setText(xnt.selectSingleNode("CLIENTNO").getText() == null ? ""
                    : xnt.selectSingleNode("CLIENTNO").getText());// 添加值
            Element Fplxdm = Responese.addElement("Fplxdm");
            String fplxdm = xnt.selectSingleNode("fplxdm").getText();
            if (fplxdm.equals("0")) {
                fplxdm = "01";
            } else if (fplxdm.equals("1")) {
                fplxdm = "02";
            }
            Fplxdm.setText(fplxdm);// 添加值
            Element Dqfpdm = Responese.addElement("Dqfpdm");
            Dqfpdm.setText(xnt.selectSingleNode("dqfpdm") == null ? "" : xnt.selectSingleNode("dqfpdm").getText());
            Element Dqfphm = Responese.addElement("Dqfphm");
            Dqfphm.setText(xnt.selectSingleNode("dqfphm") == null ? "" : xnt.selectSingleNode("dqfphm").getText());
            Element OperateFlag = Responese.addElement("OperateFlag");
            OperateFlag.setText(
                    xnt.selectSingleNode("OperateFlag") == null ? "" : xnt.selectSingleNode("OperateFlag").getText());
            Element PrintFlag = Responese.addElement("PrintFlag");
            PrintFlag.setText(
                    xnt.selectSingleNode("PrintFlag") == null ? "" : xnt.selectSingleNode("PrintFlag").getText());
            Element Returnmsg = Responese.addElement("Returnmsg");
            Returnmsg.setText(
                    xnt.selectSingleNode("returnmsg") == null ? "" : xnt.selectSingleNode("returnmsg").getText());
        } catch (DocumentException e) {
            logger.error("", e);
        }
        // 规范格式
        OutputFormat format = OutputFormat.createPrettyPrint();
        // 设置输出编码
        format.setEncoding("gbk");
        xw = new XMLWriter(sendXml, format);
        try {
            xw.write(doc);
            xw.close();
        } catch (IOException e) {
            logger.error("", e);
        }
        return sendXml.toString();
    }

    /**
     * 调用组件接口时，回写发票代码和发票号码
     *
     * @param response
     */
    private void reWriteCoAndNo(InvoiceResponse response) {
        if ("0".equals(response.getReturnCode())) {
            Kpls kpls = kplsService.findOne(Integer.valueOf(response.getLsh()));
            kpls.setFpdm(response.getFpdm());
            kpls.setFphm(response.getFphm());
            kpls.setFpztdm("00");
            kpls.setErrorReason(null);
            kpls.setPrintflag("" + response.getPrintFlag());
            kpls.setKprq(new Date());

            kpls.setXgsj(new Date());
            kpls.setXgry(1);
            kplsService.save(kpls);
            Jyls jyls = jylsService.findOne(kpls.getDjh());
            jyls.setClztdm("91");
            jylsService.save(jyls);
        } else {
            Kpls kpls = kplsService.findOne(Integer.valueOf(response.getLsh()));
            kpls.setFpztdm("05");
            kpls.setErrorReason(response.getReturnMessage().substring(0, 300));
            kpls.setXgsj(new Date());
            kpls.setXgry(1);
            kplsService.save(kpls);
            Jyls jyls = jylsService.findOne(kpls.getDjh());
            jyls.setClztdm("92");
            jylsService.save(jyls);
        }
    }

}