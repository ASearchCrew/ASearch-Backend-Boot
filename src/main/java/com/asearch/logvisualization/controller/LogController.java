package com.asearch.logvisualization.controller;

import com.asearch.logvisualization.service.LogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@AllArgsConstructor
@Api(description = "로그", tags = {"log"})
@Slf4j
@RequestMapping(value = "/api/v1/log", produces = "application/json")
public class LogController {

    private LogService logService;

    @ApiOperation(value = "로그 조회", notes = "로그를 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    @CrossOrigin
    @GetMapping
    public ResponseEntity<List<String>> getDocuments(@RequestParam("offset") Integer count) throws Exception {
        return new ResponseEntity<>(logService.getRawLogs(count), HttpStatus.OK);
    }

    @ApiOperation(value = "로그 검색", notes = "로그를 검색한다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    @CrossOrigin
    @GetMapping("/search")
    public ResponseEntity<List<String>> searchLog(@RequestParam("word") String word) throws IOException {
        log.info(word);
        return new ResponseEntity<>(logService.searchLog(word), HttpStatus.OK);
    }
}
