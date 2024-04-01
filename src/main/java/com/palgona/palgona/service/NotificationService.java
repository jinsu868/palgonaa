package com.palgona.palgona.service;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.common.error.exception.BusinessException;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.domain.notification.Notification;
import com.palgona.palgona.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.palgona.palgona.common.error.code.NotificationErrorCode.*;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public void deleteNotification(CustomMemberDetails memberDetails, Long id){
        Member member = memberDetails.getMember();

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(INVALID_ID));

        //관리자 or 자기 자신만 알림 삭제 가능
        checkPermission(member, notification);

        notificationRepository.delete(notification);
    }

    private void checkPermission(Member member, Notification notification) {
        if (!(notification.isOwner(member) || member.isAdmin())) {
            throw new BusinessException(INSUFFICIENT_PERMISSION);
        }
    }

}
