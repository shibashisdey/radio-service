package com.shibashis.radio.controller;

import com.shibashis.radio.streaming.BroadcastService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
public class StreamController {

    private final BroadcastService service;

    public StreamController(BroadcastService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String home() {
        return """
                <html>
                <body>
                    <h2>Live Radio</h2>
                    <audio controls autoplay>
                        <source src="/live" type="audio/mpeg">
                    </audio>
                </body>
                </html>
                """;
    }

    @GetMapping(value = "/live", produces = "audio/mpeg")
    public StreamingResponseBody stream() {

        return outputStream -> {
            service.addListener(outputStream);

            try {
                while (true) {
                    Thread.sleep(10000);
                }
            } catch (InterruptedException e) {
                service.removeListener(outputStream);
            }
        };
    }
}
