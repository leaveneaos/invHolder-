package com.rjxx.taxeasy.controller;

import com.rjxx.taxeasy.comm.BaseController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Created by Administrator on 2018-01-16.
 */
@RestController
@RequestMapping(value = "/e-invoice-file")
public class WjtzController extends BaseController {

    @Value("${pdf_file_path}")
    private String pdf_file_path;

    @Value("${pdf_file_now_path}")
    private String pdf_file_now_path;

    @Value("${pdf_file_history_path}")
    private String pdf_file_history_path;


    @RequestMapping
    public void index() throws Exception{
        String pdf=request.getParameter("pdf");
        logger.info("----"+pdf);
        pdf= URLDecoder.decode(pdf,"utf-8");
        logger.info("----"+pdf);
        String pdfname []=pdf.split("/");
        logger.info("----"+pdfname[2]);
        logger.info("----"+pdfname[3]);
        logger.info("----"+pdfname[4]);
        File file =new File(pdf_file_path + pdfname[2] + "/"+ pdfname[3] +"/"+ pdfname[4]);
        if(file.exists()) {
            logger.info("PDF文件存在");
            response.sendRedirect(pdf_file_now_path +pdfname[2]+"/"+pdfname[3]+"/"+pdfname[4]);
        }else {
            logger.info("PDF文件不存在");
            response.sendRedirect(pdf_file_history_path+pdfname[2]+"/"+pdfname[3]+"/"+pdfname[4]);
        }
    }

    public static void main(String[] args) {
        String s="%E4%B8%AD%E8%88%AA%E7%9B%B4%E5%8D%87%E6%9C%BA%E8%82%A1%E4%BB%BD%E6%9C%89%E9%99%90%E5%85%AC%E5%8F%B8%E2%80%94%E2%80%94JY20180116182340769%E2%80%94%E2%80%942290.pdf";
        try {
            s= URLDecoder.decode(s,"utf-8");
            System.out.println(s);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }
}
