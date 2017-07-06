package com.rjxx.taxeasy.controller;

import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.service.FpjService;
import com.rjxx.taxeasy.service.GsxxService;
import com.rjxx.taxeasy.service.JylsService;
import com.rjxx.taxeasy.service.TqjlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by xlm on 2017/7/3.
 * 绿地优鲜
 */
@Controller
@RequestMapping("/ldyx")
public class LdyxController extends BaseController{

    public static String SESSION_KEY_FPTQ_GSDM = "fptq_gsdm";

    @Autowired
    private JylsService jylsService;

    @Autowired
    private TqjlService tqjlService;

    @Autowired
    private FpjService fpjService;

    @Autowired
    private GsxxService gsxxService;

    public static final String APP_ID = "wx9abc729e2b4637ee";

    public static final String SECRET = "6415ee7a53601b6a0e8b4ac194b382eb";
    public void getData(String ExtractCode){

    }
}
