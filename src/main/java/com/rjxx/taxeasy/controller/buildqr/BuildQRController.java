package com.rjxx.taxeasy.controller.buildqr;

import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.domains.Cszb;
import com.rjxx.taxeasy.service.CszbService;
import com.rjxx.taxeasy.service.buildqr.BuildQRService;
import com.rjxx.taxeasy.wechat.dto.Result;
import com.rjxx.taxeasy.wechat.util.ResultUtil;
import com.rjxx.utils.HtmlUtils;
import com.rjxx.utils.PasswordUtils;
import com.rjxx.utils.dwz.ShortUrlUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wangyahui
 * @email wangyahui@datarj.com
 * @company 上海容津信息技术有限公司
 * @date 2018/4/24
 */
@RestController
@RequestMapping("/buildQR")
@CrossOrigin
public class BuildQRController extends BaseController{
    @Autowired
    private BuildQRService buildQRService;
    @Autowired
    private CszbService cszbService;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ApiOperation(value = "登录")
    public Result login(@RequestParam String user,
                        @RequestParam String pass) {
        String password;
        try {
            password = PasswordUtils.encrypt(pass);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.error("发生未知错误");
        }
        String status = buildQRService.login(user, password);
        session.setMaxInactiveInterval(45 * 60);
        session.setAttribute("username", user);
        session.setAttribute("password", password);
        if("-1".equals(status)){
            return ResultUtil.error("请使用非管理员账户登录");
        }else if("0".equals(status)){
            return ResultUtil.error("用户名不存在或密码错误");
        }else if("-2".equals(status)){
            return ResultUtil.error("该账户开票点权限有误");
        }else{
            return ResultUtil.success(status);
        }
    }

    @RequestMapping(value = "/create",method = RequestMethod.GET)
    @ApiOperation(value = "生成二维码")
    public Result create(@RequestParam String gsdm,@RequestParam String orderNo,@RequestParam String orderTime,
                         @RequestParam String storeNo, @RequestParam String price){

        String q = buildQRService.create(gsdm, orderNo, orderTime, storeNo, price);
        if(q!=null){
            Map map = new HashMap<>();
            Cszb cszb = cszbService.getSpbmbbh(gsdm, null, null, "isStartDWZ");
            if("是".equals(cszb.getCsz())){
                try {
                    String dwz = ShortUrlUtil.dwz(HtmlUtils.getBasePath(request) + "kptService/" + gsdm + "/" + q);
                    map.put("url", dwz);
                }catch (Exception e){
                    e.printStackTrace();
                    return ResultUtil.error("短网址服务错误");
                }
            }else{
                map.put("url", HtmlUtils.getBasePath(request) + "kptService/" + gsdm + "/" + q);
            }
            return ResultUtil.success(map);
        }else{
            return ResultUtil.error("生成二维码失败");
        }
    }
}
