package com.palgona.palgona.repository.notification;

import com.palgona.palgona.common.dto.response.SliceResponse;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.dto.NotificationResponse;

public interface NotificationRepositoryCustom {
    SliceResponse<NotificationResponse> findAllByMemberAndCursor(Member member, String cursor, int pageSize);

}
