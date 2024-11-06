package com.jindi.infra.benchmark.server.controller;

import java.util.Collections;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class HealthController {

    @GetMapping("checkAlive")
    public Map<String, String> checkAlive () {
        log.info("你过来呀！");
        return Collections.singletonMap("status", "UP");
    }
}
