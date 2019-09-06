package com.lin.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class ConsulController {

    @Value("${test}")
    private String test;

    @GetMapping("test")
    public String getTest() {
        return test;
    }

}
