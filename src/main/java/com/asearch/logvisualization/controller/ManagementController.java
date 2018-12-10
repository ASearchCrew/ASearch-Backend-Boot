package com.asearch.logvisualization.controller;

import com.asearch.logvisualization.service.ManagementService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(description = "관리", tags = {"management"})
@AllArgsConstructor
@RestController
@RequestMapping(value = "/api/v1/management", produces = "application/json")
public class ManagementController {

    private ManagementService managementService;

    @ApiOperation(value = "Filebeat Configuration 변경", notes = "Fillbeat Conf를 변경한다. // Todo 수정 필요.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    @CrossOrigin
    @PutMapping("/filebeat/conf")
    public String modifyFilebeatConfiguration() {

        managementService.modifyFilebeatConf();
        return "우영";
    }
    @ApiOperation(value = "모니터링 할 서버 등록", notes = "모니터링 할 서버를 등록한다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 409, message = "Already Exist")
    })
    @CrossOrigin
    @PutMapping("/server") // monitoring/server
    public ResponseEntity registerServerToMonitor() {

        managementService.registerServerToMonitor();
        return new ResponseEntity(HttpStatus.OK);
    }

}
