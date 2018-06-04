package com.rjxx.taxeasy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.async.DeferredResult;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Created by rj-wyh on 2017/4/10.
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket buildQRAPI() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("buildqr")
                .genericModelSubstitutes(DeferredResult.class)
                .useDefaultResponseMessages(false)
                .forCodeGeneration(false)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.rjxx.taxeasy.controller.buildqr"))
                .paths(PathSelectors.any())//过滤的接口
                .build()
                .apiInfo(buildQRInfo());
    }

    private ApiInfo buildQRInfo() {
        return new ApiInfoBuilder()
                .title("用户登录生成二维码API")
                .description("登录与生成")
                .termsOfServiceUrl("https://gitee.com/wyhtoString/projects")
                .contact("wangyahui")
                .version("1.0")
                .build();
    }


    @Bean
    public Docket dwzAPI() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("dwz")
                .genericModelSubstitutes(DeferredResult.class)
                .useDefaultResponseMessages(false)
                .forCodeGeneration(false)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.rjxx.taxeasy.controller.dwz"))
                .paths(PathSelectors.any())//过滤的接口
                .build()
                .apiInfo(dwzInfo());
    }

    private ApiInfo dwzInfo() {
        return new ApiInfoBuilder()
                .title("短网址转换")
                .description("长转短")
                .termsOfServiceUrl("https://gitee.com/wyhtoString/projects")
                .contact("wangyahui")
                .version("1.0")
                .build();
    }

    @Bean
    public Docket adapterAPI() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("adapter")
                .genericModelSubstitutes(DeferredResult.class)
                .useDefaultResponseMessages(false)
                .forCodeGeneration(false)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.rjxx.taxeasy.controller.adapter"))
                .paths(PathSelectors.any())//过滤的接口
                .build()
                .apiInfo(adapterInfo());
    }

    private ApiInfo adapterInfo() {
        return new ApiInfoBuilder()
                .title("开票通TOC接口")
                .description("通用用户提取页面")
                .termsOfServiceUrl("https://gitee.com/wyhtoString/projects")
                .contact("wangyahui")
                .version("1.0")
                .build();
    }

    @Bean
    public Docket randomCodeAPI() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("randomCode")
                .genericModelSubstitutes(DeferredResult.class)
                .useDefaultResponseMessages(false)
                .forCodeGeneration(false)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.rjxx.taxeasy.controller.randomCode"))
                .paths(PathSelectors.any())//过滤的接口
                .build()
                .apiInfo(randomCodeInfo());
    }

    private ApiInfo randomCodeInfo() {
        return new ApiInfoBuilder()
                .title("开票通验证码接口")
                .description("生成验证码")
                .termsOfServiceUrl("https://gitee.com/wyhtoString/projects")
                .contact("wangyahui")
                .version("1.0")
                .build();
    }

    @Bean
    public Docket initKeyApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("initKey")
                .genericModelSubstitutes(DeferredResult.class)
                .useDefaultResponseMessages(false)
                .forCodeGeneration(false)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.rjxx.taxeasy.controller.initkey"))
                .paths(PathSelectors.any())//过滤的接口
                .build()
                .apiInfo(initKeyInfo());
    }

    private ApiInfo initKeyInfo() {
        return new ApiInfoBuilder()
                .title("初始化信息")
                .description("生成APPID与KEY,生成的APPID入库之前在头部加上RJ")
                .termsOfServiceUrl("https://gitee.com/wyhtoString/projects")
                .contact("wangyahui")
                .version("1.0")
                .build();
    }
}
