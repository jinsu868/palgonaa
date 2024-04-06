package com.palgona.palgona.controller;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.domain.notification.Notification;
import com.palgona.palgona.dto.NotificationResponse;
import com.palgona.palgona.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "알림 리스트 조회 api", description = "멤버의 알림 리스트를 조회한다.")
    public ResponseEntity<List<NotificationResponse>> getNotifications(@AuthenticationPrincipal CustomMemberDetails memberDetails,
                                                                       @RequestParam(required = false) Long cursorId,
                                                                       @RequestParam(defaultValue = "10") int size) {

        List<Notification> notifications = notificationService.readNotifications(memberDetails, cursorId == null ? Long.MAX_VALUE : cursorId, size);
        List<NotificationResponse> response = notifications.stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "알림 삭제 api", description = "알림의 id를 입력받아 삭제를 진행한다.")
    public ResponseEntity<Void> deleteNotification(@AuthenticationPrincipal CustomMemberDetails memberDetails, Long notificaitonId){
        notificationService.deleteNotification(memberDetails, notificaitonId);
        return ResponseEntity.ok().build();
    }
}
