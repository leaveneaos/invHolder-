package com.rjxx.comm.utils;

/**
 * Created by admin on 2016/4/26.
 */
public class ResponseUtils {

    public static JsonStatus printSuccess() {
        JsonStatus jsonStatus = new JsonStatus();
        jsonStatus.setSuccess(true);
        return jsonStatus;
    }

    public static JsonStatus printSuccess(Object data) {
        JsonStatus jsonStatus = new JsonStatus();
        jsonStatus.setSuccess(true);
        jsonStatus.setData(data);
        return jsonStatus;
    }

    public static JsonStatus printSuccess(String message, Object data) {
        JsonStatus jsonStatus = new JsonStatus();
        jsonStatus.setSuccess(true);
        jsonStatus.setMessage(message);
        jsonStatus.setData(data);
        return jsonStatus;
    }

    public static JsonStatus printFailure() {
        JsonStatus jsonStatus = new JsonStatus();
        jsonStatus.setSuccess(false);
        return jsonStatus;
    }

    public static JsonStatus printFailure(String message) {
        JsonStatus jsonStatus = new JsonStatus();
        jsonStatus.setSuccess(false);
        jsonStatus.setMessage(message);
        return jsonStatus;
    }

    public static JsonStatus printFailure(String message, Object data) {
        JsonStatus jsonStatus = new JsonStatus();
        jsonStatus.setSuccess(false);
        jsonStatus.setMessage(message);
        jsonStatus.setData(data);
        return jsonStatus;
    }

}
