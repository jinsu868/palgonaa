package com.palgona.palgona.fcm.presentation;


import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.fcm.dto.request.FCMTokenUpdateRequest;
import com.palgona.palgona.fcm.application.FCMService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/FCMTokens")
public class FCMController {
    private final FCMService fcmService;

    @PutMapping
    @Operation(summary = "FCM 디바이스 토큰 등록 api", description = "FCM 토큰을 입력받아 토큰 등록을 진행한다.")
    public ResponseEntity<Void> updateFCMToken(
            @AuthenticationPrincipal CustomMemberDetails member,
            @RequestBody FCMTokenUpdateRequest request
            ){

        fcmService.updateFCMToken(member, request);

        return ResponseEntity.ok().build();
    }
}
