package com.palgona.palgona.repository.notification;

import com.palgona.palgona.common.dto.response.SliceResponse;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.dto.NotificationResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.palgona.palgona.domain.notification.QNotification.notification;

@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public SliceResponse<NotificationResponse> findAllByMemberAndCursor(Member member, String cursor, int pageSize) {
        List<NotificationResponse> notifications = queryFactory.select(Projections.constructor(
                        NotificationResponse.class,
                notification.id,
                notification.body,
                notification.type,
                notification.targetId
                ))
                .from(notification)
                .where(notification.member.eq(member),
                        ltNotificationId(cursor))
                .orderBy(notification.id.desc())
                .limit(pageSize + 1)
                .fetch();

        return convertToSlice(notifications, pageSize);
    }


    private SliceResponse<NotificationResponse> convertToSlice(List<NotificationResponse> notifications, int pageSize){
        if(notifications.isEmpty()) {
            return SliceResponse.of(notifications, false, null);
        }

        boolean hasNext = existNextPage(notifications, pageSize);
        if(hasNext){
            deleteLastPage(notifications, pageSize);
        }

        String nextCursor = String.valueOf(notifications.get(notifications.size()-1).id());

        return SliceResponse.of(notifications, hasNext, nextCursor);
    }

    private boolean existNextPage(List<NotificationResponse> purchases, int pageSize) {
        if (purchases.size() > pageSize) {
            return true;
        }

        return false;
    }

    private void deleteLastPage(List<NotificationResponse> notifications, int pageSize) {
        notifications.remove(pageSize);
    }


    private BooleanExpression ltNotificationId(String cursor){
        if(cursor != null){
            return notification.id.lt(Long.valueOf(cursor));
        }
        return null;
    }
}
