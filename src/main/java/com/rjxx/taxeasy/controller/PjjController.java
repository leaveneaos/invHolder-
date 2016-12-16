package com.rjxx.taxeasy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rjxx.comm.mybatis.Pagination;
import com.rjxx.taxeasy.bizcomm.utils.DataOperte;
import com.rjxx.taxeasy.bizcomm.utils.WxUtil;
import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.domains.*;
import com.rjxx.taxeasy.service.*;
import com.rjxx.taxeasy.vo.Fpcxvo;
import com.rjxx.taxeasy.vo.FpjVo;
import com.rjxx.utils.MailUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.mail.SendFailedException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@SuppressWarnings("deprecation")
@Controller
@RequestMapping("/pjj")
public class PjjController extends BaseController {

    public static final String APP_ID = "wx9abc729e2b4637ee";

    public static final String GET_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";// 获取access

    public static final String SECRET = "6415ee7a53601b6a0e8b4ac194b382eb";

    @Autowired
    private FpjService fpjService;

    @Autowired
    private KplsService kplsService;

    @Autowired
    private JylsService jylsService;

    @Autowired
    private CkhkService ckhkService;

    @Autowired
    private TqjlService tqjlService;

    @Autowired
    private JyspmxService jyspmxService;

    @Autowired
    private CszbService cszbService;

    @Autowired
    private DzfplogService dzfplogService;

    @Autowired
    private ZzfpqpService zzfpService;

    @Autowired
    private SkpService skpService;

    @Autowired
    private SpfyxService spfyxService;

    @Autowired
    private DataOperte dataOperte;

    @Autowired
    private GsxxService gsxxService;

    @Autowired
    private WxUtil wxUtil;

    @Value("${emailHost}")
    private String emailHost;
    @Value("${emailUserName}")
    private String emailUserName;
    @Value("${emailPwd}")
    private String emailPwd;
    @Value("${emailForm}")
    private String emailForm;
    @Value("${emailTitle}")
    private static String emailTitle;

    @RequestMapping
    @ResponseBody
    public void index() throws IOException {
        response.sendRedirect(request.getContextPath() + "/pjj/index.html?_t=" + System.currentTimeMillis());
    }

    @RequestMapping(value = "/sendMessage")
    @ResponseBody
    public Map sendMessage() {
        Map<String, Object> result = new HashMap<>();
        String msg = wxUtil.sendMessage("os2OFs_D2zIcHKHqAJT0AKuYwaq4", "上海百旺测试盘", "王敏", "电子发票", 20000.00, "2016年11月11日", 16096);
        result.put("msg", msg);

        return result;
    }

    /**
     * 获取交易信息
     *
     * @return
     */
    @RequestMapping(value = "/getKhjy")
    @ResponseBody
    public Map getKhjy(String gsdm, Integer rows, Integer page, String yf) {
        Map<String, Object> result = new HashMap<>();
        Pagination pagination = new Pagination<>();
        if (rows == null) {
            rows = 10;
        }
        if (page == null) {
            page = 1;
        }
        pagination.setPageNo(page);
        pagination.setPageSize(rows);
        String[] gsdms = null;
        List<String> gsdmList = null;
        if (gsdm != null && !"".equals(gsdm)) {
            if (gsdm.contains(",")) {
                gsdms = gsdm.split(",");
            } else {
                gsdms = new String[]{gsdm};
            }
            gsdmList = new ArrayList<>();
            for (String str : gsdms) {
                gsdmList.add(str);
            }
        }
        String openid = (String) session.getAttribute("openid");
//		if (openid == null) {
//			openid = "os2OFs_D2zIcHKHqAJT0AKuYwaq4";
//			session.setAttribute("openid", openid);
//		}
        pagination.addParam("unionid", openid);
        pagination.addParam("gsdm", gsdmList);
        pagination.addParam("yf", yf);
        List<FpjVo> list = fpjService.findByPage(pagination);
        List<Kpls> kps = null;
        Kpls kpls = new Kpls();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        for (FpjVo fpjVo : list) {
        	DecimalFormat d1 =new DecimalFormat("#,##0.00");    	
        	fpjVo.setJshj1("￥"+d1.format(fpjVo.getJshj()));
            kpls.setDjh(fpjVo.getDjh());
            kps = kplsService.findByDjh(kpls);
            if (!kps.isEmpty() && kps.size() > 0) {
                Kpls kp = kps.get(0);
                if (kp.getKprq() != null) {
                    fpjVo.setKprq(sdf.format(kp.getKprq()));
                }

            }
        }
        result.put("fps", list);
        return result;
    }

    /**
     * 获取发票信息
     *
     * @param djh
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/saveFp")
    @ResponseBody
    public void getFp(Integer djh) throws IOException {
        Map<String, Object> params = new HashMap<>();
        if (djh == null) {
            djh = -1;
        }
        params.put("djh", djh);
        List<Kpls> list = kplsService.findAll(params);
        for (Kpls kpls : list) {
            if (kpls.getPdfurl() != null && !"".equals(kpls.getPdfurl())) {
                kpls.setPdfurl(kpls.getPdfurl().replace(".pdf", ".jpg"));
            }
        }
        session.setAttribute("djh", djh);
        session.setAttribute("fps", list);
        response.sendRedirect(request.getContextPath() + "/pjj/imageviewer.html?_t=" + System.currentTimeMillis());
    }

    /**
     * 根据单据号获取重开换开信息
     *
     * @return
     */
    @RequestMapping(value = "/getCkhk")
    @ResponseBody
    public Map getCkhk(Integer djh) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> params = new HashMap<>();
        params.put("djh", djh);
        Ckhk ckhk = ckhkService.findOneByParams(params);
        result.put("ckhk", ckhk);
        result.put("success", true);
        return result;
    }

    /**
     * 根据单据号获取订单信息
     *
     * @return
     */
    @RequestMapping(value = "/getJyls")
    @ResponseBody
    public Map getJyls() {
        Map<String, Object> result = new HashMap<>();
        Integer djh = (Integer) session.getAttribute("djh");
        if (djh == null) {
            result.put("none", true);
            return result;
        }
        Jyls jyls = jylsService.findOne(djh);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        result.put("orderNo", jyls.getDdh());
        result.put("price", jyls.getJshj());
        result.put("orderTime", sdf.format(jyls.getJylssj()));
        result.put("success", true);
        return result;
    }

    /**
     * 根据单据号获取订单详情
     *
     * @return
     */
    @RequestMapping(value = "/getJy")
    @ResponseBody
    public Map getJy() {
        Map<String, Object> result = new HashMap<>();
        Integer djh = (Integer) session.getAttribute("djh");
        if (djh == null) {
            result.put("none", true);
            return result;
        }
        Jyls jyls = jylsService.findOne(djh);
        Map<String, Object> params = new HashMap<>();
        params.put("djh", djh);
        List<Jyspmx> mxList = jyspmxService.findAll(params);
        result.put("jyls", jyls);
        result.put("mx", mxList);
        result.put("success", true);
        return result;
    }

    /**
     * 重开保存发票抬头
     *
     * @return
     */
    @RequestMapping(value = "/save")
    @ResponseBody
    public Map saveTt(String fptt, String nsrsbh, String dz, String dh, String khh, String khhzh, String yx,
                      String sj) {
        Map<String, Object> result = new HashMap<>();
        Integer djh = (Integer) session.getAttribute("djh");
        String openid = (String) session.getAttribute("openid");
        if (djh == null || openid == null) {
            result.put("none", true);
            return result;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("djh", djh);
        Jyls jyls = jylsService.findOne(djh);
        Cszb cs = getCszb(jyls.getGsdm(), jyls.getXfid(), jyls.getSkpid(), "sfzcck");
        if (cs != null && "否".equals(cs.getCsz())) {
            result.put("success", false);
            result.put("msg", "不支持重开");
            return result;
        }
        Ckhk ckhk = ckhkService.findOneByParams(params);
        if (ckhk != null) {
            result.put("repeat", true);
            return result;
        }
        ckhk = new Ckhk();
        ckhk.setDjh(djh);
        ckhk.setUnionid(openid);
        ckhk.setGfsh(nsrsbh);
        ckhk.setGfmc(fptt);
        ckhk.setGfdz(dz);
        ckhk.setGfdh(dh);
        ckhk.setGfyh(khh);
        ckhk.setGfyhzh(khhzh);
        ckhk.setYxbz("1");
        ckhk.setGfyx(yx);
        ckhk.setGfsj(sj);
        ckhk.setZtbz("0");
        ckhk.setSqsj(new Date());
        ckhkService.save(ckhk);
        result.put("success", true);
        return result;
    }

    /**
     * 保存重开发票原因
     *
     * @return
     */
    @RequestMapping(value = "/update")
    @ResponseBody
    public Map update(String chk1, String chk2, String texterea) {
        Map<String, Object> result = new HashMap<>();

        Integer djh = (Integer) session.getAttribute("djh");
        String openid = (String) session.getAttribute("openid");
        if (djh == null || openid == null) {
            result.put("none", true);
            return result;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("djh", djh);
        Ckhk ckhk = ckhkService.findOneByParams(params);
        if (chk2.equals("true")) {
            ckhk.setCkhkyy("发票报销不了");
        }
        if (chk1.equals("true")) {
            ckhk.setCkhkyy("发票抬头写错了");
        }
        if (texterea != null) {
            ckhk.setCkhkyy(texterea);
        }
        Jyls jyls = jylsService.findOne(djh);
        Cszb cs = getCszb(jyls.getGsdm(), jyls.getXfid(), jyls.getSkpid(), "sfcksh");
        if (cs != null && "否".equals(cs.getCsz())) {
            Kpls kpls = new Kpls();
            kpls.setDjh(djh);
            List<Kpls> list = kplsService.findByDjh(kpls);
            params = new HashMap<>();
            params.put("kplsh", list.get(0).getKplsh());
            Fpcxvo cxvo = kplsService.selectMonth(params);
            if (cxvo != null) {
                if (cxvo.getXcyf() != null && cxvo.getXcyf() > 6) {
                    result.put("success", false);
                    result.put("msg", "超过开票日期6个月，不能重开！");
                    return result;
                } else {
                    List<Integer> kpList = new ArrayList<>();
                    for (Kpls kpls2 : list) {
                        kpList.add(kpls2.getKplsh());
                    }
                    try {
                        dataOperte.updateKpls(kpList, djh, 0);
                        dataOperte.saveLog(djh, "01", "0", "电子发票服务平台重开操作", "已向服务端发送重开请求", 0, jyls.getXfsh(), "");
                        result.put("success", true);
                        result.put("msg", "重开请求提交成功，请注意查看操作结果！");
                    } catch (Exception e) {
                        e.printStackTrace();
                        result.put("success", false);
                        result.put("msg", "后台出现错误: " + e.getMessage());
                        dataOperte.saveLog(djh, "92", "1", "", "电子发票服务平台重开请求失败!", 2, jyls.getXfsh(), "");
                    }
                }
            }
        }
        ckhkService.save(ckhk);
        result.put("success", true);
        return result;
    }

    /**
     * 修改重开邮箱
     *
     * @return
     */
    @RequestMapping(value = "/updateYx")
    @ResponseBody
    public Map updateYx(String yx) {
        Map<String, Object> result = new HashMap<>();
        Integer djh = (Integer) session.getAttribute("djh");
        String openid = (String) session.getAttribute("openid");
        if (djh == null || openid == null) {
            result.put("none", true);
            return result;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("djh", djh);
        Ckhk ckhk = ckhkService.findOneByParams(params);
        ckhk.setGfyx(yx);
        ckhkService.save(ckhk);
        result.put("success", true);
        return result;
    }

    /**
     * 换开
     *
     * @param chk1
     * @param chk2
     * @param texterea
     * @return
     */
    @RequestMapping(value = "/hk")
    @ResponseBody
    @Transactional
    public Map hk(String chk1, String chk2, String texterea) {
        Map<String, Object> result = new HashMap<>();

        Integer djh = (Integer) session.getAttribute("djh");
        String openid = (String) session.getAttribute("openid");
        if (djh == null || openid == null) {
            result.put("none", true);
            return result;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("djh", djh);
        Jyls jyls = jylsService.findOne(djh);
        Cszb cs = getCszb(jyls.getGsdm(), jyls.getXfid(), jyls.getSkpid(), "sfzchk");
        if (cs != null && "是".equals(cs.getCsz())) {
            Kpls kpls = new Kpls();
            kpls.setDjh(djh);
            List<Kpls> list = kplsService.findByDjh(kpls);
            params = new HashMap<>();
            params.put("kplsh", list.get(0).getKplsh());
            Fpcxvo cxvo = kplsService.selectMonth(params);
            if (cxvo != null) {
                if (cxvo.getXcyf() != null && cxvo.getXcyf() > 6) {
                    result.put("success", false);
                    result.put("msg", "超过开票日期6个月，不能换开！");
                    return result;
                } else {
                    params.put("djh", djh);
                    Ckhk hk = ckhkService.findOneByParams(params);
                    if (hk != null && !"3".equals(hk.getZtbz())) {
                        result.put("success", false);
                        result.put("msg", "发票已处理或正在处理...");
                        return result;
                    } else if (hk == null) {
                        hk = new Ckhk();
                        hk.setZtbz("3");
                        hk.setYxbz("1");
                        hk.setDjh(djh);
                        hk.setUnionid(openid);
                    }

                    if (chk2.equals("true")) {
                        hk.setCkhkyy("不了解电子发票");
                    }
                    if (chk1.equals("true")) {
                        hk.setCkhkyy("公司不支持电子发票报销");
                    }
                    if (texterea != null) {
                        hk.setCkhkyy(texterea);
                    }
                    hk.setSqsj(new Date());
                    ckhkService.save(hk);
                    cs = getCszb(jyls.getGsdm(), jyls.getXfid(), jyls.getSkpid(), "sfhksh");
                    if (cs != null && "否".equals(cs.getCsz())) {
                        try {
                            List<Integer> kpList = new ArrayList<>();
                            for (Kpls kpls2 : list) {
                                kpList.add(kpls2.getKplsh());
                            }
                            dataOperte.addJyls(kpList, djh, "13", 0);
                            Map prms = new HashMap<>();
                            params.put("id", hk.getId());
                            params.put("ztbz", "4");
                            ckhkService.updateZtbz(prms);
                            dataOperte.saveLog(djh, "01", "0", "电子发票服务平台换开操作", "已向服务端发送换开开请求", 0, jyls.getXfsh(), "");
                            result.put("success", true);
                            result.put("msg", "换开请求提交成功，请注意查看操作结果！");
                            return result;
                        } catch (Exception e) {
                            e.printStackTrace();
                            result.put("success", false);
                            result.put("msg", "后台出现错误: " + e.getMessage());
                            dataOperte.saveLog(djh, "92", "1", "", "电子发票服务平台换开请求失败!", 2, jyls.getXfsh(), "");
                            return result;
                        }
                    }
                    result.put("success", true);
                    return result;
                }
            }
        } else {
            result.put("success", false);
            result.put("msg", "不支持换开！");
        }

        return result;
    }

    /**
     * 保存邮寄信息
     *
     * @param chk1
     * @param chk2
     * @param texterea
     * @return
     */
    @RequestMapping(value = "/saveYjxx")
    @ResponseBody
    public Map saveYjxx(String sjr, String dwmc, String yjdz, String yb, String lxdh) {
        Map<String, Object> result = new HashMap<>();
        Integer djh = (Integer) session.getAttribute("djh");
        if (djh == null) {
            result.put("none", true);
            return result;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("djh", djh);
        Jyls jyls = jylsService.findOne(djh);
        Cszb cs = getCszb(jyls.getGsdm(), jyls.getXfid(), jyls.getSkpid(), "sfzcyj");
        if ((cs != null && "否".equals(cs.getCsz())) || cs == null) {
            result.put("success", false);
            result.put("msg", "不支持邮寄！");
            return result;
        }
        Zzfpqp zzfp = zzfpService.findOneByParams(params);
        if (zzfp != null) {
            result.put("repeat", true);
            return result;
        }
        zzfp = new Zzfpqp();
        zzfp.setDjh(djh);
        zzfp.setSjr(sjr);
        zzfp.setDwmc(dwmc);
        zzfp.setSjdz(yjdz);
        zzfp.setYb(yb);
        zzfp.setLxdh(lxdh);
        zzfp.setYxbz("1");
        zzfp.setQpfs("0");
        zzfpService.save(zzfp);
        result.put("success", true);
        return result;
    }

    /**
     * 保存自提信息
     *
     * @param chk1
     * @param chk2
     * @param texterea
     * @return
     */
    @RequestMapping(value = "/saveZtxx")
    @ResponseBody
    public Map saveZtxx(String divdm, String divqssj, String divjssj) throws ParseException {
        Map<String, Object> result = new HashMap<>();
        Integer djh = (Integer) session.getAttribute("djh");
        if (djh == null) {
            result.put("none", true);
            return result;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("djh", djh);
        Jyls jyls = jylsService.findOne(djh);
        Cszb cs = getCszb(jyls.getGsdm(), jyls.getXfid(), jyls.getSkpid(), "sfzczt");
        if ((cs != null && "否".equals(cs.getCsz())) || cs == null) {
            result.put("success", false);
            result.put("msg", "不支持自提！");
            return result;
        }
        Zzfpqp zzfp = zzfpService.findOneByParams(params);
        if (zzfp != null) {
            result.put("repeat", true);
            return result;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Integer month1 = Integer.valueOf(divqssj.substring(0, divqssj.indexOf("月")));
        Integer month2 = Integer.valueOf(divjssj.substring(0, divjssj.indexOf("月")));
        Integer day1 = Integer.valueOf(divqssj.substring(divqssj.indexOf("月") + 1, divqssj.indexOf("日")));
        Integer day2 = Integer.valueOf(divjssj.substring(divjssj.indexOf("月") + 1, divjssj.indexOf("日")));
        if (month1 > month2 && ((month1 - month2) > 6)) {
            divqssj = new Date().getYear() + 1900 + divqssj;
            divjssj = new Date().getYear() + 1901 + divjssj;
        } else {
            divqssj = new Date().getYear() + 1900 + divqssj;
            divjssj = new Date().getYear() + 1900 + divjssj;
        }
        if (month1 < 10) {
            divqssj = divqssj.replace(month1 + "月", "0" + month1 + "月");
        }
        if (month2 < 10) {
            divjssj = divjssj.replace(month2 + "月", "0" + month2 + "月");
        }
        divqssj = divqssj.replace("月", "");
        divqssj = divqssj.replace("日", "");
        divjssj = divjssj.replace("月", "");
        divjssj = divjssj.replace("日", "");
        Date date1 = sdf.parse(divqssj);
        Date date2 = sdf.parse(divjssj);
        if (date1.getTime() > date2.getTime()) {
            result.put("success", false);
            result.put("msg", "起始日期大于结束日期");
            return result;
        }
        zzfp = new Zzfpqp();
        zzfp.setDjh(djh);
        zzfp.setZtdm(divdm);
        zzfp.setQssj(divqssj);
        zzfp.setJssj(divjssj);
        zzfp.setQpfs("1");
        zzfpService.save(zzfp);
        result.put("success", true);
        return result;
    }

    /**
     * 获取可自提的开票点
     *
     * @param chk1
     * @param chk2
     * @param texterea
     * @return
     */
    @RequestMapping(value = "/getKpd")
    @ResponseBody
    public Map getKpd() {
        Map<String, Object> result = new HashMap<>();
        Integer djh = (Integer) session.getAttribute("djh");
        if (djh == null) {
            result.put("none", true);
            return result;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("djh", djh);
        Jyls jyls = jylsService.findOne(djh);
        params.put("xfid", jyls.getXfid());
        params.put("xfsh", jyls.getXfsh());
        List<Skp> list = skpService.getKpd(params);
        result.put("kpds", list);
        result.put("success", true);
        return result;
    }

    /**
     * 发票预览
     *
     * @return
     */
    @RequestMapping(value = "/getFp")
    @ResponseBody
    public Map getFp() {
        Map<String, Object> result = new HashMap<>();
        result.put("fps", session.getAttribute("fps"));
        return result;
    }

    /**
     * 跳转到邮箱页面
     *
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/youxiong")
    @ResponseBody
    public void youxiong() throws IOException {
        response.sendRedirect(request.getContextPath() + "/pjj/youxiang.html?_t=" + System.currentTimeMillis());
    }

    /**
     * 跳转到首页
     *
     * @throws IOException
     */
    @RequestMapping(value = "/first")
    @ResponseBody
    public void back() throws IOException {
        response.sendRedirect(request.getContextPath() + "/pjj/index.html?_t=" + System.currentTimeMillis());
    }

    /**
     * 跳转到错误页面
     *
     * @throws IOException
     */
    @RequestMapping(value = "/error")
    @ResponseBody
    public void error() throws IOException {
        response.sendRedirect(request.getContextPath() + "/smtq/demo.html?_t=" + System.currentTimeMillis());
    }

    /**
     * 添加到发票夹
     *
     * @param unionid
     * @return
     */
    @RequestMapping(value = "/saveFpj")
    @ResponseBody
    @Transactional
    public Map save(String openid) {
        Map<String, Object> result = new HashMap<>();
        Integer djh = (Integer) session.getAttribute("djh");
        if (djh == null) {
            result.put("error", true);
            return result;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("djh", djh);
        params.put("unionid", openid);
        Fpj fpj = fpjService.findOneByParams(params);
        if (fpj != null) {
            result.put("nopeat", true);
            result.put("msg", "发票已收藏到发票夹");
            return result;
        }
        fpj = new Fpj();
        fpj.setDjh(djh);
        fpj.setUnionid(openid);
        fpj.setYxbz("1");
        fpj.setLrsj(new Date());
        fpj.setXgsj(new Date());
        fpjService.save(fpj);
        Tqjl tqjl = new Tqjl();
        tqjl.setDjh(String.valueOf(djh));
        tqjl.setTqsj(new Date());
        String visiterIP;
        if (request.getHeader("x-forwarded-for") == null) {
            visiterIP = request.getRemoteAddr();// 访问者IP
        } else {
            visiterIP = request.getHeader("x-forwarded-for");// 访问者IP
        }

        tqjl.setIp(visiterIP);
        String llqxx = request.getHeader("User-Agent");
        tqjl.setLlqxx(llqxx);
        tqjl.setJlly("3");
        tqjlService.save(tqjl);
        result.put("success", true);
        return result;
    }

    /**
     * 判断unionID是否存在
     *
     * @return
     */
    @RequestMapping(value = "/getOpenid")
    @ResponseBody
    public Map getOpenid() {
        Map<String, Object> result = new HashMap<>();
        String openid = (String) session.getAttribute("openid");
//		if (openid == null) {
//			openid = "os2OFs_D2zIcHKHqAJT0AKuYwaq4";
//		}
        if (openid != null) {
            result.put("success", true);
        } else {
            result.put("success", false);
        }
        return result;
    }

    /**
     * 判断access_token是否存在
     *
     * @return
     */
    @RequestMapping(value = "/CheckToken")
    @ResponseBody
    public Map CheckToken() {
        Map<String, Object> result = new HashMap<>();
        String access_token = (String) session.getAttribute("access_token");
        if (access_token != null) {
            result.put("success", true);
        } else {
            result.put("success", false);
        }
        return result;
    }

    /**
     * 发送邮件
     *
     * @param yx
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/sendEmail")
    @ResponseBody
    @Transactional
    public Map sendMail(String yx) throws Exception {
        Map<String, Object> result = new HashMap<String, Object>();
        Integer djh = (Integer) session.getAttribute("djh");
        if (djh == null) {
            result.put("none", true);
            result.put("msg", "会话已过期");
            return result;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("djh", djh);
        Ckhk ckhk = ckhkService.findOneByParams(params);
        if (ckhk != null && ckhk.getZtbz().equals("2") && ckhk.getZtbz().equals("5")) {
            if (ckhk.getZtbz().equals("0")) {
                result.put("msg", "发票重开审核中，不能发送");
            } else if (ckhk.getZtbz().equals("1")) {
                result.put("msg", "发票已重开，不能发送");
            } else if (ckhk.getZtbz().equals("3")) {
                result.put("msg", "发票换开审核中，不能发送");
            } else if (ckhk.getZtbz().equals("4")) {
                result.put("msg", "发票已换开，不能发送");
            }

            result.put("success", false);
            return result;
        }
        boolean flag = false;
        try {
            Jyls jyls = jylsService.findOne(djh);
            List<Kpls> kplsList = kplsService.findAll(params);
            List<String> pdfUrlList = new ArrayList<>();
            for (Kpls kpls : kplsList) {
                pdfUrlList.add(kpls.getPdfurl());
            }
            if (kplsList.size() > 0) {
                sendMail(jyls.getDdh(), yx, pdfUrlList, jyls.getXfmc());
                flag = true;
            }
            Tqjl tqjl = new Tqjl();
            tqjl.setDjh(String.valueOf(jyls.getDjh()));
            tqjl.setTqsj(new Date());
            String visiterIP;
            if (request.getHeader("x-forwarded-for") == null) {
                visiterIP = request.getRemoteAddr();// 访问者IP
            } else {
                visiterIP = request.getHeader("x-forwarded-for");// 访问者IP
            }

            tqjl.setIp(visiterIP);
            String llqxx = request.getHeader("User-Agent");
            tqjl.setLlqxx(llqxx);
            tqjl.setJlly("2");
            tqjlService.save(tqjl);
            params = new HashMap<>();
            params.put("djh", djh);
            params.put("yx", yx);
            Spfyx spfyx = spfyxService.findOneByParams(params);
            if (null == spfyx) {
                spfyx = new Spfyx();
                spfyx.setEmail(yx);
                spfyx.setYxbz("1");
                spfyx.setLrsj(new Date());
                spfyx.setDjh(Integer.valueOf(djh));
                spfyxService.save(spfyx);
            }
            result.put("success", flag);
        } catch (SendFailedException e) {
            e.printStackTrace();
            result.put("error", true);
        }

        return result;
    }

    /**
     * 获取参数设置信息
     *
     * @param gsdm
     * @param xfid
     * @param kpdid
     * @param csm
     * @return
     */
    public Cszb getCszb(String gsdm, Integer xfid, Integer kpdid, String csm) {
        Map<String, Object> params = new HashMap<>();
        params.put("gsdm", gsdm);
        params.put("xfid", xfid);
        params.put("kpdid", kpdid);
        params.put("csm", csm);
        List<Cszb> list = new ArrayList<>();
        list = cszbService.findAllByParams(params);
        if (list.size() == 1) {
            return list.get(0);
        } else if (list.size() == 0) {
            return new Cszb();
        } else {
            return list.get(0);
        }
    }

    /**
     * 获取公司信息
     *
     * @return
     */
    @RequestMapping(value = "/getGsxx")
    @ResponseBody
    public Map getGsxx(String gsdm) {
        Map<String, Object> result = new HashMap<String, Object>();
        Integer djh = (Integer) session.getAttribute("djh");
        Map<String, Object> params = new HashMap<>();
        Gsxx gsxx;
        if (gsdm == null || "".equals(gsdm)) {
            if (djh == null) {
                gsxx = new Gsxx();
            } else {
                params.put("djh", djh);
                gsxx = gsxxService.findOneByDjh(params);
            }

        } else {
            params.put("gsdm", gsdm);
            gsxx = gsxxService.findOneByParams(params);
        }
        if (gsxx != null && (null == gsxx.getWxappid() || "".equals(gsxx.getWxappid()))) {
            gsxx.setWxappid(APP_ID);
            gsxx.setWxsecret(SECRET);
            gsxx.setGsdm("rjxx");
        }
        session.setAttribute("gsxx", gsxx);
        result.put("gsxx", gsxx);
        result.put("success", true);
        return result;
    }

    /**
     * 获取公司信息
     *
     * @return
     */
    @RequestMapping(value = "/getGs")
    @ResponseBody
    public Map getGs() {
        Map<String, Object> result = new HashMap<>();
        Gsxx gs = (Gsxx) session.getAttribute("gsxx");
        result.put("gsxx", gs);
        result.put("success", true);
        return result;
    }

    /**
     * 获取授权code
     *
     * @param apiUrl
     * @param appId
     * @param url
     * @return
     */
    public Map getCode(String apiUrl, String appId, String url) {
        Map<String, Object> result = new HashMap<>();
        String codeUrl = String.format(
                "%s?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_userinfo&state=STATE#wechat_redirect",
                apiUrl, appId, url);
        return result;
    }

    /**
     * 获取access_token
     *
     * @param apiurl
     * @param appid
     * @param code
     * @return
     */
    @RequestMapping(value = "/getToken")
    @ResponseBody
    public Map hqtk(String apiurl, String appid, String code) {
        Map<String, Object> result = new HashMap<String, Object>();
        Gsxx gsxx = (Gsxx) session.getAttribute("gsxx");
        // 获取token
        String turl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + gsxx.getWxappid() + "&secret=" + gsxx.getWxsecret()
                + "&code=" + code + "&grant_type=authorization_code";
        // https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
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
                    session.setAttribute("access_token", map.get("access_token"));
                    session.setAttribute("openid", map.get("openid"));
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

    /**
     * 刷新access_token
     *
     * @param apiurl
     * @param appid
     * @param code
     * @param refresh_token
     * @return
     */
    @RequestMapping(value = "/getRefresh")
    @ResponseBody
    public Map getRefresh(String apiurl, String appid, String code, String refresh_token) {
        Map<String, Object> result = new HashMap<String, Object>();
        // 获取token
        String turl = String.format("%s?grant_type=refresh_token&appid=%s&refresh_token=%s", apiurl, APP_ID,
                refresh_token);
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

    /**
     * 获取微信用户信息
     *
     * @param apiurl
     * @param openid
     * @param access_token
     * @return
     */
    @RequestMapping(value = "/getUserMsg")
    @ResponseBody
    public Map getUserMsg(String openid, String access_token) {
        Map<String, Object> result = new HashMap<String, Object>();
        // 获取token
        String turl = "https://api.weixin.qq.com/sns/userinfo?access_token=" + access_token + "&openid=" + openid
                + "&lang=zh_CN";
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
                    result.put("msg", "获取微信用户信息失败,错误代码为" + map.get("errcode"));
                    return result;
                } else {// 正常情况下{"access_token":"ACCESS_TOKEN","expires_in":7200}
                    map.put("success", true);
//					logger.info("unionid" + map.get("unionid"));
//					logger.info("openid" + map.get("openid"));
                    session.setAttribute("unionid", map.get("unionid"));
                    session.setAttribute("openid", map.get("openid"));
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

    /**
     * 生成邮件内容
     *
     * @param ddh
     * @param pdfUrlList
     * @param gsdm
     * @return
     * @throws Exception
     */
    private static String getAFMailContent(String ddh, List<String> pdfUrlList, String gsdm) throws Exception {
        StringBuffer sb = new StringBuffer();
        // sb.append(null2Wz(iurb.get("BUYER_NAME")));
        sb.append(" 先生/小姐您好：<br/>");
        sb.append("<br/>");
        sb.append("您的订单号码： ");
        sb.append(ddh).append("的电子发票已开具成功，电子发票下载地址：<br>");
        for (String pdfUrl : pdfUrlList) {
            sb.append("<a href='" + pdfUrl + "'>" + null2Wz(pdfUrl) + "</a><br>");
        }
        sb.append("请及时下载您的发票。");
        sb.append("<br/><br/>");
        sb.append(gsdm);
        sb.append("<br/>");
        sb.append("<br/>");
        Date d = new Date();
        sb.append(1900 + d.getYear()).append("年").append(d.getMonth() + 1).append("月").append(d.getDate()).append("日");
        return sb.toString();
    }

    // 判空
    private static Object null2Wz(Object s) {
        return s == null || "".equals(s) ? "未知" : s;
    }

    /**
     * 发送邮件
     *
     * @param ddh
     * @param email
     * @param pdfUrlList
     * @param gsdm
     * @throws Exception
     */
    public void sendMail(String ddh, String email, List<String> pdfUrlList, String gsdm) throws Exception {
        MailUtil sendmail = new MailUtil();
        sendmail.setHost(emailHost);
        sendmail.setUserName(emailUserName);
        sendmail.setPassWord(emailPwd);
        sendmail.setTo(email);

        sendmail.setFrom(emailForm);
        sendmail.setSubject(emailTitle);
        sendmail.setContent(getAFMailContent(ddh, pdfUrlList, gsdm));
        // TODO 这里需要根据邮件摸板内容进行调整。

        // XXX 先生/小姐您好：
        //
        // 订单号码： XXXXXXXX, 您的发票信息如下：
        //
        //
        // 发票将邮寄至地址（即订单收货地址）： XXXXXXX 收件人（即定单收货人）： xxxxxx
        // 上述资料是您在个人基本资料中所登陆的地址，并已输入发票系统，为避免退货情况产生，
        // 请您再次确认住址是否正确，若有需要修改邮寄资料请联络客服中心进行修改。
        //
        // 在此提醒若您是在收到此邮件后才修改个人基本资料，则新登陆的邮寄资料将会在下次发票开立时生效
        //
        //
        //
        // 爱芙趣商贸（上海）有限公司
        // 20xx年x月x日

        sendmail.sendMail();

        Thread.sleep(2000);
    }
}
