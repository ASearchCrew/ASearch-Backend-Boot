package com.asearch.logvisualization.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
@Slf4j
public class CronTable {

    

    @Scheduled(cron = "*/30 * * * * *")
    public String aJob() throws Exception {

        log.info("My Cron Test");
//        log.info(profileService.getBasicInfo());
        return "ABC";
    }
}
