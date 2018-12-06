package com.asearch.logvisualization.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@AllArgsConstructor
@Slf4j
@Api(description = "filebeat 설정", tags = {"set"})
@RequestMapping(value = "/api/v1/set", produces = "application/json")
public class SettingController {
	
	
}
