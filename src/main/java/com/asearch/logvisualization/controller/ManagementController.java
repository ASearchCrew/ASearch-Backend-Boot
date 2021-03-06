package com.asearch.logvisualization.controller;


import com.asearch.logvisualization.dto.DeleteServerModel;
import com.asearch.logvisualization.dto.RegisterServerModel;
import com.asearch.logvisualization.dto.ServerListDto;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asearch.logvisualization.dto.LogCountBySecondsModel;
import com.asearch.logvisualization.service.ManagementService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
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
    public ResponseEntity modifyFilebeatConfiguration(@RequestParam(value = "path", required = true) String path) throws Exception{
        System.out.println(path);
    	managementService.modifyFilebeatConf(path);
        return new ResponseEntity(HttpStatus.OK);
    }
    @ApiOperation(value = "모니터링 할 서버 등록", notes = "모니터링 할 서버를 등록한다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 409, message = "Already Exist")
    })
    @CrossOrigin
    @PostMapping("/server")
    public ResponseEntity registerServerToMonitor(@RequestBody RegisterServerModel serverInfo) throws IOException {
        log.info(serverInfo.toString());
        managementService.registerServerToMonitor(serverInfo);
        return new ResponseEntity(HttpStatus.OK);
    }
    @ApiOperation(value = "모니터링 할 서버 삭제", notes = "모니터링 할 서버를 삭제한다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 409, message = "Already Exist")
    })
    @CrossOrigin
    @DeleteMapping("/server")
    public ResponseEntity deleteServerToMonitor(@RequestBody DeleteServerModel serverInfo) throws IOException {
        log.info(serverInfo.toString());
        managementService.deleteServerToMonitor(serverInfo);
        return new ResponseEntity(HttpStatus.OK);
    }
    
    @ApiOperation(value = "서버별 시간확인", notes = "서버별 시간확인.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    @CrossOrigin
    @GetMapping("/servertime")
    public ResponseEntity<List<HashMap<String, Object>>> getLogCountList() throws Exception{
    	ResponseEntity<List<HashMap<String, Object>>> rs = new ResponseEntity<List<HashMap<String, Object>>>(managementService.getLogCountList(), HttpStatus.OK);
        return rs;
    }
    
    @ApiOperation(value = "날짜별 로그갯수 출력", notes = "날짜별 로그갯수 출력.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    @CrossOrigin
    @GetMapping("/datecount")
    public ResponseEntity<List<HashMap<String, Object>>> getDateCountList() throws Exception{
    	ResponseEntity<List<HashMap<String, Object>>> rs = new ResponseEntity<List<HashMap<String, Object>>>(managementService.getDateCountList(), HttpStatus.OK);
        return rs;
    }
    
    @ApiOperation(value = "모니터링 할 서버 조회", notes = "모니터링 할 서버를 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    @CrossOrigin
    @GetMapping("/serverlist") // monitoring/server
    public ResponseEntity<List<Object>> getServerList() throws IOException {
        ResponseEntity<List<Object>> rs = new ResponseEntity<List<Object>>(managementService.getServerList(), HttpStatus.OK);
        return rs;
    }
    
    @ApiOperation(value = "키워드별 로그갯수 출력", notes = "키워드별 로그갯수 출력.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    @CrossOrigin
    @GetMapping("/keywordcount")
    public ResponseEntity<List<HashMap<String, Object>>> getKeywordCountList(@RequestParam(value = "hostName", required = true) String hostName) throws Exception{
        return new ResponseEntity<List<HashMap<String, Object>>>(managementService.getKeywordCountList(hostName), HttpStatus.OK);
    }
    
    @ApiOperation(value = "초별 로그갯수 조회", notes = "초별 로그갯수 조회.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    @CrossOrigin
    @GetMapping("/logcount/hour/minute") //
    public ResponseEntity<LogCountBySecondsModel> getLogCountBySeconds() throws IOException {
    	ResponseEntity<LogCountBySecondsModel> rs = new ResponseEntity<LogCountBySecondsModel>(managementService.getLogCountBySeconds(), HttpStatus.OK);
        return rs;
    }
}
