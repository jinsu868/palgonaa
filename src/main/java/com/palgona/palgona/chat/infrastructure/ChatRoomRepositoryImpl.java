package com.palgona.palgona.chat.infrastructure;

import static com.palgona.palgona.chat.domain.QChatRoom.chatRoom;
import static com.palgona.palgona.member.domain.QMember.member;

import com.palgona.palgona.member.domain.Member;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatRoomRepositoryImpl implements ChatRoomRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public boolean existsBySenderAndReceiver(Member sender, Member receiver) {
        Integer result = jpaQueryFactory
                .selectOne()
                .from(chatRoom)
                .where(
                        (chatRoom.receiver.eq(receiver).and(chatRoom.sender.eq(sender)))
                                .or(chatRoom.receiver.eq(sender).and(chatRoom.sender.eq(receiver)))
                ).fetchOne();

        return result != null;
    }
}
