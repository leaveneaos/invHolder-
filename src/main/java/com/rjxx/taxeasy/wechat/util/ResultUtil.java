package com.rjxx.taxeasy.wechat.util;


import com.rjxx.taxeasy.wechat.dto.Result;

/**
 * Created by rj-wyh on 2017/4/7.
 */
public class ResultUtil {
    public static Result success(Object data){
        Result result = new Result();
        result.setCode("1");
        result.setData(data);
        result.setMsg("成功");
        return result;
    }

    public static Result success(){
        return success(null);
    }

    public static Result error(String msg){
        Result result = new Result();
        result.setCode("0");
        result.setMsg(msg);
        return result;
    }
}
