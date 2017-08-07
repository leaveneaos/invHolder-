//package com.rjxx.taxeasy.wechat.config;
//
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.annotation.AfterReturning;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Before;
//import org.aspectj.lang.annotation.Pointcut;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import javax.servlet.http.HttpServletRequest;
//
///**
// * Created by rj-wyh on 2017/4/10.
// */
//@Aspect
//@Component
//public class HttpAspect {
//    private static Logger logger = LoggerFactory.getLogger(HttpAspect.class);
//
//    //定义切点
//    @Pointcut("execution(public * com.rjxx.taxeasy.wechat.controller.InvoiceController.*(..))")
//    public void invoice(){}
//
//    //请求前
//    @Order(1) //设定优先级，1-10，@Before的话，越小优先级越高
//    @Before("invoice()")
//    public void before(JoinPoint joinPoint){
//        logger.info("在http请求之前触发");
//        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request=attributes.getRequest();
//        //url
//        logger.info("url={}",request.getRequestURL());
//        //method
//        logger.info("method={}",request.getMethod());
//        //ip
//        logger.info("ip={}",request.getRemoteAddr());
//        //class method
//        logger.info("class_method={}",joinPoint.getSignature().getDeclaringTypeName()+"."+joinPoint.getSignature().getName());
//        //参数
//        logger.info("args={}",joinPoint.getArgs());
//    }
//
////    //请求后
////    @After("department()")
////    @Order(10) //设定优先级，1-10，@After的话，越大优先级越高
////    public void after(){
////        logger.info("在http请求之后触发");
////    }
//
//    //拿到请求返回的信息
//    @AfterReturning(pointcut = "invoice()",returning = "obj")
//    public void returning(Object obj){
//        if(obj != null)
//        logger.info("response={}",obj.toString());
//    }
//}
