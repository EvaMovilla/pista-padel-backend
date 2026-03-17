package edu.comillas.icai.gitt.pat.spring.pista_padel_backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/pistaPadel/health")
    public String health(){
        return "OK";
    }
}
