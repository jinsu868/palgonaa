package com.palgona.palgona.controller;

import com.palgona.palgona.common.jwt.util.JwtService;
import com.palgona.palgona.dto.AuthToken;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/tests")
public class TestTokenController {

    private static final String SOCIAL_ID = "12317237";
    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity<AuthToken> create() {
        return ResponseEntity.ok(jwtService.issueToken(SOCIAL_ID));
    }
}
