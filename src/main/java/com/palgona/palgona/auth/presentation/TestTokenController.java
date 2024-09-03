package com.palgona.palgona.auth.presentation;

import com.palgona.palgona.common.jwt.util.JwtService;
import com.palgona.palgona.member.domain.Member;
import com.palgona.palgona.notification.domain.Notification;
import com.palgona.palgona.notification.domain.Type;
import com.palgona.palgona.auth.dto.AuthToken;
import com.palgona.palgona.notification.dto.response.NotificationResponse;
import com.palgona.palgona.member.domain.MemberRepository;
import com.palgona.palgona.notification.domain.NotificationRepository;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/tests")
@Slf4j
public class TestTokenController {

    private static final String SOCIAL_ID = "12317237";
    private final JwtService jwtService;
    private final MemberRepository memberRepository;
    private final NotificationRepository notificationRepository;

    @GetMapping
    public ResponseEntity<AuthToken> create() {
        return ResponseEntity.ok(jwtService.issueToken(SOCIAL_ID));
    }

    @GetMapping("/{social_id}")
    @Operation(summary = "테스트용 토큰 발급 api2", description = "social_id를 입력받아 토큰을 생성한다.")
    public ResponseEntity<AuthToken> createV2(@PathVariable String social_id) {
        return ResponseEntity.ok(jwtService.issueToken(social_id));
    }

    @GetMapping("/v3/{member_id}")
    @Operation(summary = "테스트용 토큰 발급 api3", description = "member_id를 입력받아 토큰을 생성한다.")
    public ResponseEntity<AuthToken> createV3(@PathVariable Long member_id) {
        Member member = memberRepository.findById(member_id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid member ID"));

        return ResponseEntity.ok(jwtService.issueToken(member.getSocialId()));
    }

    @PostMapping("/notifications")
    @Operation(summary = "테스트용 알림 생성 api", description = "알림 API 테스트를 위한 알림 생성 API")
    public ResponseEntity<NotificationResponse> createNotification(@RequestBody NotificationRequest request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid member ID"));

        log.info("멤버: " + member.getSocialId());

        Notification notification = Notification.builder()
                .title(request.title())
                .body(request.body())
                .type(request.type())
                .targetId(request.targetId())
                .member(member)
                .build();

        notification = notificationRepository.save(notification);

        log.info("알림: " + notification.getTitle());

        return ResponseEntity.ok(NotificationResponse.from(notification));
    }


    public record NotificationRequest(
            String title,
            String body,
            Type type,
            Long targetId,
            Long memberId

    ) {   }
}
