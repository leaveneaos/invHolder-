package com.rjxx.taxeasy.controller;

import com.rjxx.taxeasy.comm.BaseController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

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
    public void index(String pdf) throws Exception{
        logger.info("----"+pdf);
        String pdfname []=pdf.split("/");
        File file =new File(pdf_file_path + pdfname[2] + "/"+ pdfname[3] +"/"+ pdfname[4] +".pdf");
        if(file.exists()) {
            logger.info("PDF文件存在");
            response.sendRedirect(pdf_file_now_path +pdfname[2]+"/"+pdfname[3]+"/"+pdfname[4]+".pdf");
        }else {
            logger.info("PDF文件不存在");
            response.sendRedirect(pdf_file_history_path+pdfname[2]+"/"+pdfname[3]+"/"+pdfname[4]+".pdf");
        }
    }
}
