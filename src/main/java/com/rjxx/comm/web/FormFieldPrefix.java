package com.rjxx.comm.web;

import java.lang.annotation.*;

/**
 * Created by admin on 2016/4/7.
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FormFieldPrefix {
    String value();
}
