package com.palgona.palgona.controller;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "알림 삭제 api", description = "알림의 id를 입력받아 삭제를 진행한다.")
    public ResponseEntity<Void> deleteNotification(@AuthenticationPrincipal CustomMemberDetails memberDetails, Long notificaitonId){
        notificationService.deleteNotification(memberDetails, notificaitonId);
        return ResponseEntity.ok().build();
    }
}
