package com.example.bookmanagesystem.controller;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/me")
    public CurrentUser me(Authentication authentication){
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length())) // Remove "ROLE_" prefix
                .toList();
        return new CurrentUser(authentication.getName(), roles);
    }

    @GetMapping("/csrf")
    public CsrfResponse csrf(CsrfToken token){
        return new CsrfResponse(token.getHeaderName(), token.getToken());
    }

    public record CurrentUser(String username, List<String> roles) {
    }
    public record CsrfResponse(String headerName, String token) {}
}
