package com.asearch.logvisualization.controller;

import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Api(description = "로그", tags = {"log"})
@Slf4j
@RequestMapping(value = "/api/v1/log", produces = "application/json")
public class LogController {


}
