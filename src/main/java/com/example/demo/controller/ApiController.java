package com.example.demo.controller;

import com.example.demo.service.Getallstandards;
import com.example.demo.service.Getoutcomes_id;
import com.example.demo.service.Getstate_standards;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/")
public class ApiController {

    private final Getstate_standards apiService;
    private final Getoutcomes_id apiService1;
    private final Getallstandards apiService2;

    public ApiController(Getstate_standards apiService, Getoutcomes_id apiService1,
                         Getallstandards apiService2) {
        this.apiService = apiService;
        this.apiService1 = apiService1;
        this.apiService2 = apiService2;
    }

    @GetMapping("/state-standards")
    public Mono<String> getAccounts() {
        return apiService.getAccounts();
    }

    @GetMapping("/outcome_urls")
    public Mono<String> geturls() {
        return apiService1.geturls();
    }

    @GetMapping("/all_standards")
    public Mono<String> processStateStandardUrls() {
        return apiService2.processStateStandardUrls();
    }
}
