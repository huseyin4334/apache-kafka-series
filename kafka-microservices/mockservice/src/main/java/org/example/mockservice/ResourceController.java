package org.example.mockservice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api")
@RestController
public class ResourceController {


    @GetMapping("/200")
    public ResponseEntity<String> get200() {
        return ResponseEntity.ok("200");
    }

    @GetMapping("/500")
    public ResponseEntity<String> get500() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("500");
    }

    @GetMapping("/400")
    public ResponseEntity<String> get400() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("400");
    }
}
