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
        String pdf = new String(request.getParameter("pdf").getBytes("iso8859-1"),"UTF-8");
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
            response.sendRedirect(pdf_file_now_path +URLEncoder.encode(pdfname[2],"utf-8")+"/"+URLEncoder.encode(pdfname[3],"utf-8")+"/"+URLEncoder.encode(pdfname[4],"utf-8"));
        }else {
            logger.info("PDF文件不存在");
            response.sendRedirect(pdf_file_history_path+URLEncoder.encode(pdfname[2],"utf-8")+"/"+URLEncoder.encode(pdfname[3],"utf-8")+"/"+URLEncoder.encode(pdfname[4],"utf-8"));
        }
    }
}
