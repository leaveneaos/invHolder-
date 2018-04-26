package com.rjxx.taxeasy.controller.dwz;

import com.alibaba.fastjson.JSON;
import com.rjxx.taxeasy.domains.Cszb;
import com.rjxx.taxeasy.service.CszbService;
import com.rjxx.taxeasy.wechat.dto.Result;
import com.rjxx.taxeasy.wechat.util.ResultUtil;
import com.rjxx.utils.dwz.ShortUrlUtil;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangyahui on 2017/12/18 0018.
 */
@RestController
@RequestMapping("/dwz")
public class ShortUrlController {
    @Autowired
    private CszbService cszbService;

    @RequestMapping(value = "/name",method = RequestMethod.GET)
    @ApiOperation(value="短网址转换")
    public Result getDwz(@RequestParam String gsdm,@RequestParam String longUrl){
        Cszb cszb = cszbService.getSpbmbbh(gsdm, null, null, "isStartDWZ");
        if("是".equals(cszb.getCsz())){
            String url= ShortUrlUtil.dwz(longUrl);
            if(StringUtils.isNotBlank(url)){
                Map map = new HashMap<>();
                map.put("url", url);
                return ResultUtil.success(JSON.toJSONString(map));
            }else{
                return ResultUtil.error("短网址生成失败");
            }
        }else{
            return ResultUtil.error("短网址参数未开启");
        }
    }
}
