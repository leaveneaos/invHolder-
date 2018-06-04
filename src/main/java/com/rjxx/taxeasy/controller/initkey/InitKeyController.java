package com.rjxx.taxeasy.controller.initkey;

import com.rjxx.utils.AppKeySecretUtils;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wangyahui
 * @email wangyahui@datarj.com
 * @company 上海容津信息技术有限公司
 * @date 2018/6/4
 */

@RestController
@RequestMapping("/initKey")
public class InitKeyController {

    @ApiOperation("生成appid:key")
    @RequestMapping(value = "/init",method = RequestMethod.POST)
    public String init(){
        String[] arr=AppKeySecretUtils.generate();
        return arr[0].substring(0, 12) + ":" + arr[1];
    }
}
