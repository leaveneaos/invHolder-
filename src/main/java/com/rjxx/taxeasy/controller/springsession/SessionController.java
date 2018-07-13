package com.rjxx.taxeasy.controller.springsession;

import com.rjxx.taxeasy.wechat.dto.Result;
import com.rjxx.taxeasy.wechat.util.ResultUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Created by Administrator on 2017/10/24 0024.
 */
@RequestMapping("/session")
@RestController
public class SessionController {

    private static Logger log = LoggerFactory.getLogger(SessionController.class);

    @RequestMapping(value = "get/{attribute}",method = RequestMethod.GET)
    public Result getSessionAttribute(@PathVariable String attribute, HttpSession session) {
        if (session.getAttribute(attribute) == null) {
            return ResultUtil.error("session已过期或者无数据");
        }
        return ResultUtil.success(session.getAttribute(attribute));
    }

    @RequestMapping(value = "set/{attribute}/{value}",method = RequestMethod.GET)
    public Result setSessionAttribute(@PathVariable String attribute, @PathVariable String value, HttpSession session) {
        session.setAttribute(attribute, value);
        return ResultUtil.success();
    }

    @RequestMapping("/getSession")
    @ResponseBody
    public String getSession(HttpServletRequest request){
        log.info("sessionId={}",request.getSession().getId());
        return request.getSession().getId();
    }
}
