package com.asearch.logvisualization.config;

import com.google.common.collect.Lists;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * API document를 위한 Swagger conf 설정
 */

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("com.wooyoung.logvisualization")
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.wooyung.logvisualization.controller"))
                .paths(PathSelectors.ant("/**"))
                .build()
                .useDefaultResponseMessages(false)
                .globalResponseMessage(RequestMethod.POST,
                        Lists.newArrayList(internalServerError()))
                .globalResponseMessage(RequestMethod.GET,
                        Lists.newArrayList(internalServerError()))
                .globalResponseMessage(RequestMethod.PUT,
                        Lists.newArrayList(internalServerError()))
                .globalResponseMessage(RequestMethod.DELETE,
                        Lists.newArrayList(internalServerError()));
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("ASearch")
                .description("KT 신입사원 프로젝트 - 로그 시각화 팀")
                .contact(new Contact("ASearch", "https://github.com/ASearchCrew", "ueong777@gmail.com"))
                .license("Apache License Version 2.0")
                .licenseUrl("https://www.apache.org/licenses/LICENSE-2.0")
                .version("1.0")
                .build();
    }

    private ResponseMessage internalServerError() {
        return new ResponseMessageBuilder()
                .code(500)
                .message("Internal Server Error")
                .responseModel(new ModelRef("Error"))
                .build();
    }
}
