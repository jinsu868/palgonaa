package com.palgona.palgona.chat.domain;

import com.palgona.palgona.chat.domain.ChatReadStatus;
import com.palgona.palgona.chat.domain.ChatRoom;
import com.palgona.palgona.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatReadStatusRepository extends JpaRepository<ChatReadStatus, Long> {
    ChatReadStatus findByMemberAndRoom(Member member, ChatRoom room);
}
