package com.asearch.logvisualization.controller;

import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Slf4j
@Api(description = "알람", tags = {"alarm"})
@RequestMapping(value = "/api/v1/alarm", produces = "application/json")
public class AlarmController {


}
