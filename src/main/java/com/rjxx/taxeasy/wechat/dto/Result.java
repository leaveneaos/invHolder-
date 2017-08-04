package com.rjxx.taxeasy.wechat.dto;

/**
 * Created by rj-wyh on 2017/4/7.
 */
public class Result<T> {
    /*错误信息*/
    private String msg;
    /*错误码*/
    private String code;
    /*数据*/
    private T data;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setData(T data) {
        this.data = data;
    }

    public T getData() {

        return data;
    }

    public Result(String msg, String code, T data) {
        this.msg = msg;
        this.code = code;
        this.data = data;
    }

    public Result() {
    }

    @Override
    public String toString() {
        return "Result{" +
                "msg='" + msg + '\'' +
                ", code='" + code + '\'' +
                ", data=" + data +
                '}';
    }
}
