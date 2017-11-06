package com.rjxx.taxeasy.scheduled;

import com.alibaba.fastjson.JSON;
import com.rjxx.taxeasy.dao.WxTokenJpaDao;
import com.rjxx.taxeasy.domains.WxToken;
import com.rjxx.utils.weixin.WeixinUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * Created by Administrator on 2017-09-18.
 */
@Component
public class RefreshTokeScheduled {

    @Autowired
    private WxTokenJpaDao wxTokenJpaDao;

    @Autowired
    private WeixinUtils weixinUtils;

    private static Logger logger = LoggerFactory.getLogger(RefreshTokeScheduled.class);

    @Scheduled(fixedRate = 1000*60*30)
    //@Scheduled(fixedRate = 1000*5)
    public void refresh(){
        String flag="01";
        WxToken wxToken = wxTokenJpaDao.findByFlag(flag);
        Map map = weixinUtils.hqtk();
        String accessToken = (String) map.get("access_token");
        if(accessToken==null||"".equals(accessToken)){
            accessToken = (String) weixinUtils.hqtk().get("access_token");
        }
        String ticket = weixinUtils.getTicket(accessToken);
        if(wxToken==null){
            WxToken wxToken1 = new WxToken();
            wxToken1.setAccessToken(accessToken);
            wxToken1.setCreatTime(new Date());
            wxToken1.setExpiresin("7200");
            wxToken1.setFlag("01");
            wxToken1.setTicket(ticket);
            if(ticket==null || accessToken==null){
                return;
            }
            wxTokenJpaDao.save(wxToken1);
            logger.info("第一次定时任务获取微信token-----"+ JSON.toJSONString(wxToken1));
        }else {
            WxToken wxTokens = new WxToken();
            wxTokens.setId(wxToken.getId());
            wxTokens.setAccessToken(accessToken);
            wxTokens.setCreatTime(new Date());
            wxTokens.setExpiresin("7200");
            wxTokens.setFlag("01");
            wxTokens.setTicket(ticket);
            if(ticket==null || accessToken==null){
                return;
            }
            wxTokenJpaDao.save(wxTokens);
            logger.info("微信更新token-----------------"+JSON.toJSONString(wxTokens));
        }
    }
}
