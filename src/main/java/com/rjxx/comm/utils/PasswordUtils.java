package com.rjxx.comm.utils;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Created by Administrator on 2016/7/6.
 */
public class PasswordUtils {

    public static String encrypt(String source) {
        String res = DigestUtils.md5Hex(source);
        return DigestUtils.md5Hex(res.substring(10));
    }

//    public static void main(String[] args) {
//        System.out.println(encrypt("rjxx"));
//    }

}
