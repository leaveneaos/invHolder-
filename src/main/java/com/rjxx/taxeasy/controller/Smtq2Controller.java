package com.rjxx.taxeasy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.rjxx.comm.web.BaseController;
@Controller
@RequestMapping("/dzfp_sm")
public class Smtq2Controller extends BaseController{

	@RequestMapping
	public String index() {
		return "smtq/smtq2";
	}

}
