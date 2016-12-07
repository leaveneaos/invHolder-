package com.rjxx.taxeasy.controller;

import com.rjxx.taxeasy.comm.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dzfp_sm")
public class Smtq2Controller extends BaseController {

    @RequestMapping
    public String index() {
        return "smtq/smtq2";
    }

}
