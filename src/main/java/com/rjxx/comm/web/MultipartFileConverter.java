package com.rjxx.comm.web;

import org.springframework.core.convert.converter.Converter;
import org.springframework.web.multipart.MultipartFile;

/**
 * 解决上传文件为空的问题，上传文件为""，则返回null
 * Created by yahveh on 2016/7/31.
 */
public class MultipartFileConverter implements Converter<String, MultipartFile> {

    public MultipartFileConverter() {
        super();
    }

    @Override
    public MultipartFile convert(String s) {
        return null;
    }
}
