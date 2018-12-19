package com.asearch.logvisualization.controller;

import com.asearch.logvisualization.dto.LogInfoDto;
import com.asearch.logvisualization.dto.LogModel;
import com.asearch.logvisualization.dto.PushTokenDto;
import com.asearch.logvisualization.service.LogService;
import io.micrometer.core.lang.Nullable;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@AllArgsConstructor
@Api(description = "로그", tags = {"log"})
@Slf4j
@RequestMapping(value = "/api/v1/logs", produces = "application/json")
public class LogController {

    private LogService logService;

    /**
     * Pre
     * 1. size = 100
     *
     *
     * Request
     * 1. direction
     * 2. time
     * 3. search(@nullable)
     * 4. isStream
     *
     * 5. upScrollOffset
     * 6.
     *
     * Response
     * 1. sumCount
     *
     */

    @ApiOperation(value = "로그 조회 & 검색", notes = "로그를 조회 & 검색 한다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 404, message = "Not Found")
    })
    @CrossOrigin
    @GetMapping
    public ResponseEntity<LogInfoDto> getDocuments(@RequestParam("direction") String direction,
                                                   @RequestParam("hostName") String hostName,
                                                   @RequestParam("time") String time,
                                                   @Nullable @RequestParam("search") String search,
                                                   @RequestParam("isStream") boolean isStream,
                                                   @Nullable @RequestParam("initialCount") long initialCount,
                                                   @RequestParam("upScrollOffset") long upScrollOffset,
                                                   @Nullable @RequestParam("id") String id,
                                                   @Nullable @RequestParam("calendarStartTime") String calendarStartTime,
                                                   @Nullable @RequestParam("calendarEndTime") String calendarEndTime) throws Exception {
        log.info(calendarEndTime + " End Time");
        log.info("=======================================IN===================================================");
        return new ResponseEntity<>(logService.getRawLogs(direction, hostName, time,
                search, isStream, initialCount, upScrollOffset, id, calendarStartTime, calendarEndTime), HttpStatus.OK);
    }

    @ApiOperation(value = "로그 상세화면 조회", notes = "로그 상세화면을 조회 한다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success"),
    })
    @CrossOrigin
    @GetMapping("/{documentId}")
    public ResponseEntity<String> searchLog(@PathVariable(value = "documentId") String id) throws IOException {
        return new ResponseEntity<>(logService.getDocument(id), HttpStatus.OK);
    }

    @ApiOperation(value = "User Push Token 등록", notes = "User Push Token 을 등록한다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success"),
    })
    @CrossOrigin
    @PostMapping("/admin/token")
    public ResponseEntity registerUserToken(@RequestBody PushTokenDto dto) throws IOException {
        logService.registerPushToken(dto);
        return new ResponseEntity(HttpStatus.OK);
    }

}
