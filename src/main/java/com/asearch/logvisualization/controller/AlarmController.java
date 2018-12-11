package com.asearch.logvisualization.controller;

import com.asearch.logvisualization.dto.AlarmKeywordDto;
import com.asearch.logvisualization.dto.KeywordListModel;
import com.asearch.logvisualization.service.AlarmService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.xml.ws.Response;
import java.io.IOException;
import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
@Api(description = "알람", tags = {"alarm"})
@RequestMapping(value = "/api/v1/alarm", produces = "application/json")
public class AlarmController {

    private AlarmService alarmService;

    @ApiOperation(value = "탐지 키워드 등록", notes = "탐지할 키워드를 등록한다.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 409, message = "Conflict")
    })
    @CrossOrigin
    @PostMapping("/keyword")
    public ResponseEntity registerAlarmKeyword(@RequestBody AlarmKeywordDto keyword) throws IOException {
        alarmService.registerAlarmKeyword(keyword);
        return new ResponseEntity(HttpStatus.CREATED);
    }

    @ApiOperation(value = "탐지 키워드 삭제", notes = "등록되어 있는 키워드를 삭제한다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 409, message = "COnflict")
    })
    @CrossOrigin
    @DeleteMapping("/keyword")
    public ResponseEntity removeAlarmKeyword(@RequestBody AlarmKeywordDto keyword) throws IOException {
        return alarmService.removeKeyword(keyword) ?
                new ResponseEntity(HttpStatus.OK) :
                new ResponseEntity(HttpStatus.CONFLICT);
    }

    @ApiOperation(value = "탐지 키워드 리스트 조회", notes = "탐지 키워드 리스트를 조회 한다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success")
    })
    @CrossOrigin
    @GetMapping("/keyword/list")
    public ResponseEntity<List<KeywordListModel>> getKeywords() throws IOException {
        return new ResponseEntity<>(alarmService.getKeywordList(), HttpStatus.OK);
    }
}
