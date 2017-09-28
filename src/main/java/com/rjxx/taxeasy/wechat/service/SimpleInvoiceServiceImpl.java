package com.rjxx.taxeasy.wechat.service;

import com.rjxx.taxeasy.domains.Cszb;
import com.rjxx.taxeasy.domains.Gsxx;
import com.rjxx.taxeasy.domains.Skp;
import com.rjxx.taxeasy.domains.Xf;
import com.rjxx.taxeasy.service.*;
import com.rjxx.taxeasy.vo.Spvo;
import com.rjxx.taxeasy.wechat.dto.DefaultResult;
import com.rjxx.taxeasy.wechat.util.HttpUtil;
import com.rjxx.taxeasy.wechat.vo.*;
import com.rjxx.utils.RJCheckUtil;
import com.rjxx.utils.XmlJaxbUtils;
import com.rjxx.utils.XmlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2017/9/26 0026.
 */
@Service
public class SimpleInvoiceServiceImpl implements SimpleInvoiceService {

    @Autowired
    private GsxxService gsxxService;
    @Autowired
    private SkpService skpService;
    @Autowired
    private XfService xfService;
    @Autowired
    private CszbService cszbService;
    @Autowired
    private SpvoService spvoService;
    @Autowired
    private JyxxsqService jyxxsqService;
    @Autowired
    private JymxsqService jymxsqService;

    @Override
    public String getMsg(String appid, String sign, String orderData) {
        DefaultResult defaultResult = new DefaultResult();
        try {
            Map map = new HashMap<>();
            map.put("appkey", appid);
            Gsxx gsxx = gsxxService.findOneByParams(map);

            String key = gsxx.getSecretKey();
            String s = RJCheckUtil.decodeXml(key, orderData, sign);
            //如果没有返回错误信息
            if("1".equals(s)){
                Map msg = XmlUtil.xml2Map(orderData);
                String orderNo = (String)msg.get("OrderNo");
                String totalAmount = (String)msg.get("TotalAmount");
                String extractedCode = (String)msg.get("ExtractedCode");
                ;
                String gsdm = gsxx.getGsdm();

                Map skpMap = new HashMap();
                skpMap.put("gsdm", gsdm);
                Skp skp = skpService.findOneByParams(skpMap);

                String kpr = skp.getKpr();
                String fhr = skp.getFhr();
                String skr = skp.getSkr();
                String storeNo = skp.getKpddm();
                String lxdh = skp.getLxdh();
                String lxdz = skp.getLxdz();
                String yhzh = skp.getYhzh();
                String khyh = skp.getKhyh();

                Integer xfid = skp.getXfid();
                Xf xf = xfService.findOne(xfid);

                String xfmc = xf.getXfmc();
                String xfsh = xf.getXfsh();

                InvoiceRequest request = new InvoiceRequest();
                request.setClientNO(storeNo);
                request.setDrawer(kpr);
                request.setInvType("12");
                request.setPayee(skr);
                request.setReviewer(fhr);
                request.setSerialNumber(orderNo);

                Seller seller = new Seller();
                seller.setIdentifier(xfsh);
                seller.setName(xfmc);
                seller.setTelephoneNo(lxdh);
                seller.setAddress(lxdz);
                seller.setBank(khyh);
                seller.setBankAcc(yhzh);
                request.setSeller(seller);

                OrderMain orderMain = new OrderMain();
                orderMain.setOrderNo(orderNo);
                orderMain.setOrderDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                orderMain.setTotalAmount(totalAmount);
                orderMain.setTaxMark("1");

                Order order = new Order();
                order.setOrderMain(orderMain);

                Cszb cszb = cszbService.getSpbmbbh(gsdm, xfid, skp.getId(), "dyspbmb");
                Map spvoMap = new HashMap();
                spvoMap.put("gsdm", gsdm);
                spvoMap.put("spdm", cszb.getCsz());
                Spvo spvo = spvoService.findOneSpvo(spvoMap);

                List<ProductItem> productItemList = new ArrayList<>();
                ProductItem productItem = new ProductItem();
                productItem.setAmount(totalAmount);
                productItem.setProductName(spvo.getSpmc());
                productItem.setProductCode(spvo.getSpbm());
                productItem.setPolicyMark(spvo.getYhzcbs());
                productItem.setTaxRateMark(spvo.getLslbz());
                productItem.setPolicyName(spvo.getYhzcmc());
                productItem.setTaxRate(String.valueOf(spvo.getSl()));
                productItem.setMxTotalAmount(totalAmount);
                productItem.setSpec("");
                productItem.setTaxAmount("0");
                productItemList.add(productItem);

                order.setProductItem(productItemList);
                List<Order> orderList = new ArrayList<>();
                orderList.add(order);
                request.setOrder(orderList);

                String xml = XmlJaxbUtils.toXml(request);
                String resultxml = HttpUtil.HttpUrlPost(xml, appid, key, "02");
                Map<String, Object> resultMap = null;
                try {
                    resultMap = XmlUtil.xml2Map(resultxml);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String returnMsg=resultMap.get("ReturnMessage").toString();
                String returnCode=resultMap.get("ReturnCode").toString();
                defaultResult.setReturnCode(returnCode);
                defaultResult.setReturnMessage(returnMsg);
            }else{
                defaultResult.setReturnCode("9999");
                defaultResult.setReturnMessage("验签失败");
            }
        }catch (Exception e){
            e.printStackTrace();
            defaultResult.setReturnCode("9999");
            defaultResult.setReturnMessage("发生未知错误");
        }
        return XmlJaxbUtils.toXml(defaultResult);
    }

    @Override
    public String extractData(String extractCode) {
//        jyxxsqService.findOneByParams();
        return null;
    }
}
