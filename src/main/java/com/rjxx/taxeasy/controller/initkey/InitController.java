package com.rjxx.taxeasy.controller.initkey;

import com.rjxx.taxeasy.service.InitCompanyService;
import com.rjxx.taxeasy.wechat.dto.Result;
import com.rjxx.taxeasy.wechat.util.ResultUtil;
import com.rjxx.utils.AppKeySecretUtils;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wangyahui
 * @email wangyahui@datarj.com
 * @company 上海容津信息技术有限公司
 * @date 2018/6/4
 */

@RestController
@RequestMapping("/initCompany")
public class InitController {
    @Autowired
    private InitCompanyService initCompanyService;

    @ApiOperation(value = "生成appid:key")
    @RequestMapping(value = "/initKey",method = RequestMethod.POST)
    public String initKey(){
        String[] arr=AppKeySecretUtils.generate();
        return arr[0].substring(0, 12) + ":" + arr[1];
    }

    @ApiOperation(value = "测试环境初始化")
    @RequestMapping(value = "/intGsxx",method = RequestMethod.POST)
    public Result initGsxx(@RequestParam String gsdm, @RequestParam String gsmc){
        String result = initCompanyService.initGsxx(gsdm, gsmc);
        if(result==null){
            return ResultUtil.success();
        }else{
            return ResultUtil.error(result);
        }
    }
}
