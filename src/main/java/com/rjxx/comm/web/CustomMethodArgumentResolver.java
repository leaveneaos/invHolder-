package com.rjxx.comm.web;

import org.apache.commons.beanutils.ConvertUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

/**
 * 自定义Action方法前缀识别
 * Created by admin on 2016/4/7.
 */
public class CustomMethodArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(FormFieldPrefix.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        String objName = parameter.getParameterName() + ".";
        Object o = BeanUtils.instantiate(parameter.getParameterType());
        StringBuffer tmp;
        String[] val;
        Field[] frr = parameter.getParameterType().getDeclaredFields();
        for (Iterator<String> itr = webRequest.getParameterNames(); itr.hasNext(); ) {
            tmp = new StringBuffer(itr.next());
            if (tmp.indexOf(objName) < 0) {
                continue;
            }
            for (int i = 0; i < frr.length; i++) {
                frr[i].setAccessible(true);
                if (tmp.toString().equals(objName + frr[i].getName())) {
                    val = webRequest.getParameterValues(tmp.toString());
                    //暂时不处理数组问题
                    setData(o, frr[i], val[0]);
                }
            }
        }
        return o;
    }

    /**
     * 设置属性，转换类型
     *
     * @param object
     * @param field
     * @param value
     */
    private void setData(Object object, Field field, String value) throws Exception {
        Class type = field.getType();
        if (type.isAssignableFrom(Date.class)) {
            //如果是日期类型
            SimpleDateFormat simpleDateFormat = null;
            if (value.length() == 10) {
                simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            } else if (value.length() == 16) {
                simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            } else if (value.length() == 19) {
                simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            }
            Date date = simpleDateFormat.parse(value);
            field.set(object, date);
            return;
        }
        Object val = ConvertUtils.convert(value, type);
        field.set(object, val);
    }

}
