package com.palgona.palgona.auth.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDateTime;


public record KakaoUserInfoResponse(
        String id,
        String connected_at
) {
}
