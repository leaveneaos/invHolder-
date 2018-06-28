package com.rjxx.taxeasy.controller;

import com.rjxx.taxeasy.comm.BaseController;
import com.rjxx.taxeasy.dao.ShortLinkJpaDao;
import com.rjxx.taxeasy.domains.ShortLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author: zsq
 * @date: 2018/6/27 10:48
 * @describe:短信控制器
 */
@Controller
@RequestMapping("/d")
public class SendMessageController extends BaseController {

    @Autowired
    private ShortLinkJpaDao shortLinkJpaDao;
    private static Logger logger = LoggerFactory.getLogger(SendMessageController.class);

    @RequestMapping()
    @ResponseBody
    public void index() throws Exception{
       logger.info("接收到的参数为={}",request.getQueryString());
        if(request.getQueryString()==null ||"".equals(request.getQueryString())){
            logger.info("error={}","参数错误shortLink"+request.getQueryString());
            errorRe("参数错误!");
            return;
        }else {
            ShortLink shortLink = null;
            try {
                shortLink = shortLinkJpaDao.findOneByShortLink(request.getQueryString());
            } catch (Exception e) {
                e.printStackTrace();
                logger.info("error={}","根据短链接"+shortLink+"查询错误");
                errorRe("未找到数据!");
                return;
            }
            if(shortLink==null){
                logger.info("error={}","根据短链接"+shortLink+"未找到数据");
                errorRe("未找到数据!");
                return;
            }
            response.sendRedirect(request.getContextPath() + "/tq?"+shortLink.getNormalLink()+"&&_t=" + System.currentTimeMillis());
            logger.info("success={}","跳转地址为："+request.getContextPath() + "/tq?"+shortLink.getNormalLink()+"&&_t=" + System.currentTimeMillis());
            return;
        }
    }

}
