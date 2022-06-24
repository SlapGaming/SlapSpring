package com.telluur.slapspring.modules.ltg;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class LTGLoggerService {

    @Bean
    public Logger ltgLogger() {
        return LoggerFactory.getLogger("LTG");
    }

}
