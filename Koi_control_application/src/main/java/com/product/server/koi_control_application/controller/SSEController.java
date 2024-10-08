package com.product.server.koi_control_application.controller;

import com.product.server.koi_control_application.service.SSEService;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;



@RestController
@RequestMapping("/api/sse")
@Hidden
public class SSEController<T> {

    private final SSEService<T> sseService;

    public SSEController(SSEService<T> sseService) {
        this.sseService = sseService;
    }

    @GetMapping(path = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<T> streamEvents() {
        return sseService.getEventStream();
    }


}