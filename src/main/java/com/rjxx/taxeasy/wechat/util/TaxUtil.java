package com.rjxx.taxeasy.wechat.util;

import com.rjxx.taxeasy.domains.Jymxsq;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/8/3 0003.
 */
public class TaxUtil {
    public static List<Jymxsq> separatePrice(List<Jymxsq> jyspmxs) throws Exception {
        List<Jymxsq> sepJyspmxs = new ArrayList<>();// 价税分离后的list
        for (int i = 0; i < jyspmxs.size(); i++) {
            Jymxsq mx = jyspmxs.get(i);
            BigDecimal jshj = new BigDecimal(mx.getJshj());
//            BigDecimal spje = mx.getSpje();
            BigDecimal spsl = new BigDecimal(mx.getSpsl());
            BigDecimal spdj = new BigDecimal(mx.getSpdj());
            BigDecimal jeWithoutTax = div(jshj, spsl.add(new BigDecimal(1)));
            BigDecimal jeTax = sub(jshj, jeWithoutTax);
            // 判断单价是否为空！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！todo
            //Double djWithoutTax = div(spdj, 1 + spsl, 6);
            BigDecimal djWithoutTax;
            if (spdj == null) {
                djWithoutTax = null;// 单价不含税
            } else {
                djWithoutTax = div(spdj, spsl.add(new BigDecimal(1)));
            }
//            mx.setSpje(jeWithoutTax.doubleValue());// 商品金额不含税
            mx.setSpse(jeTax.doubleValue());// 税额
//            mx.setJshj(spje);// 价税合计
//            mx.setSpdj(djWithoutTax.doubleValue());// 单价不含税
            sepJyspmxs.add(mx);
        }
        return sepJyspmxs;
    }

    public static BigDecimal div(BigDecimal dividend, BigDecimal divisor) {
        if (dividend == null) {
            return null;
        }
        if (divisor == null || divisor.doubleValue() == 0) {
            return null;
        }
        return div(dividend, divisor, 30);
    }

    public static BigDecimal div(BigDecimal dividend, BigDecimal divisor, Integer scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        if (dividend == null) {
            return null;
        }
        if (divisor == null || divisor.doubleValue() == 0) {
            return null;
        }
        return dividend.divide(divisor, scale, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal sub(BigDecimal value1, BigDecimal value2) {
        if (value1 == null) {
            return null;
        }
        if (value2 == null) {
            return null;
        }
        return value1.subtract(value2);
    }
}
