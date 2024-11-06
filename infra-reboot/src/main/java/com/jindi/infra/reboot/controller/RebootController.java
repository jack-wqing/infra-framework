package com.jindi.infra.reboot.controller;

import com.jindi.infra.reboot.constant.RebootConstant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
public class RebootController {

    @GetMapping("readinessCheck")
    public String readinessCheck(HttpServletResponse response) {
        if (RebootConstant.readinessCheck) {
            response.setStatus(200);
            return "true";
        } else {
            response.setStatus(503);
            return "503";
        }
    }
}
