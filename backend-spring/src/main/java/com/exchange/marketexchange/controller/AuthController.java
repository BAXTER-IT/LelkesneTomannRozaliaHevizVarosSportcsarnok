package com.exchange.marketexchange.controller;

import com.exchange.marketexchange.model.dto.UserLoginResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDTO> login(Principal principal) {
        // If Spring Security successfully authenticates (e.g., via Basic Auth),
        // the 'principal' object will be populated.
        // The BasicAuthenticationFilter in Spring Security handles the 401 response
        // if authentication fails before this controller method is even called.
        if (principal != null && principal.getName() != null) {
            UserLoginResponseDTO userDto = new UserLoginResponseDTO(principal.getName());
            return ResponseEntity.ok(userDto);
        }
        // This case should ideally not be reached if security is tight,
        // but as a fallback, or if a filter chain issue occurs.
        return ResponseEntity.status(401).build();
    }
}
