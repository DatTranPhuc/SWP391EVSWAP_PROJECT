package evswap.swp391to4.controller;

import java.io.IOException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class SpafallbackController {

    // Serve SPA index.html for known client routes (avoid regex issues)
    @GetMapping(value = {"/", "/dashboard", "/login", "/register", "/verify-otp"})
    public ResponseEntity<byte[]> spaIndex(@PathVariable(required = false) String path) throws IOException {
        ClassPathResource index = new ClassPathResource("static/index.html");
        byte[] content = index.getContentAsByteArray();
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(content);
    }
}


