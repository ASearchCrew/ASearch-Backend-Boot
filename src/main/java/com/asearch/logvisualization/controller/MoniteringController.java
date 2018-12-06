package com.asearch.logvisualization.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.asearch.logvisualization.service.MoniteringService;

import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@AllArgsConstructor
@Slf4j
@Api(description = "서버 모니터링", tags = {"/monitor"})
@RequestMapping(value = "/api/v1/monitor", produces = "application/json")
public class MoniteringController {
	
	@Resource(name="MoniteringService")
	private MoniteringService moniteringService;
	
	@RequestMapping(value = "/serverstatus", method = RequestMethod.GET)
	public void getServerStatus() {
		
	}
	
}
