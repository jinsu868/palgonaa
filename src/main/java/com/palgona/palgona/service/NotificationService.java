package com.palgona.palgona.service;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.common.dto.response.SliceResponse;
import com.palgona.palgona.common.error.exception.BusinessException;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.domain.notification.Notification;
import com.palgona.palgona.dto.NotificationResponse;
import com.palgona.palgona.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.palgona.palgona.common.error.code.NotificationErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public SliceResponse<NotificationResponse> readNotifications(CustomMemberDetails memberDetails, int cursor, int size) {
         PageRequest limit = PageRequest.of(cursor, size);
         List<Notification> notifications = notificationRepository.findAllByMember(memberDetails.getMember(), limit);

         List<NotificationResponse> notificationResponses = notifications.stream()
                 .map(NotificationResponse::from)
                 .toList();

         return convertToSlice(notificationResponses, cursor);
    }

    @Transactional
    public void deleteNotification(CustomMemberDetails memberDetails, Long id){
        Member member = memberDetails.getMember();

        Notification notification = notificationRepository.findByIdWithMember(id)
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

    private SliceResponse<NotificationResponse> convertToSlice(List<NotificationResponse> notifications, int nowCursor){
        boolean hasNext = true;
        if(notifications.isEmpty()) {
            hasNext = false;
        }

        String nextCursor = String.valueOf(nowCursor + 1);

        return SliceResponse.of(notifications, hasNext, nextCursor);
    }

}
