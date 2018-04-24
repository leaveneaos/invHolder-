package com.rjxx.taxeasy.controller.buildqr;

import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.service.buildqr.BuildQRService;
import com.rjxx.taxeasy.wechat.dto.Result;
import com.rjxx.taxeasy.wechat.util.ResultUtil;
import com.rjxx.utils.HtmlUtils;
import com.rjxx.utils.PasswordUtils;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
public class BuildQRController extends BaseController{
    @Autowired
    private BuildQRService buildQRService;

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
            map.put("url", HtmlUtils.getBasePath(request) + "kptService/" + gsdm + "/" + q);
            return ResultUtil.success(map);
        }else{
            return ResultUtil.error("生成二维码失败");
        }
    }
}
