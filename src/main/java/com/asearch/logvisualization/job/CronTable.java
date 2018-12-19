package com.asearch.logvisualization.job;

import com.asearch.logvisualization.service.AlarmService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@AllArgsConstructor
@Component
public class CronTable {

    private AlarmService alarmService;

    /**
     * 1. 키워드 들을 Searching 한다. - 서버에서 가지고 있어야 한다.? -- ES는 latency 가 낫으므로,
     * 2. 서버가 키워드들을 얻었다.
     * 3. 그 키워드들이 관찰 서버가 가지고 있는지 searching 을 한다.
     *   -
     */
    @Scheduled(cron = "*/30 * * * * *")
    public void job() throws IOException {
        alarmService.detectKeyword();
    }
}
