package com.palgona.palgona.chat.domain;

import com.palgona.palgona.member.domain.Member;
import com.palgona.palgona.chat.dto.response.ChatRoomCountResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findBySenderAndReceiver(Member sender, Member receiver);

    @Query("select c from ChatRoom c where c.sender = :member or c.receiver = :member")
    List<ChatRoom> findBySenderOrReceiver(Member member);

    @Query("SELECT new com.palgona.palgona.chat.dto.response.ChatRoomCountResponse(cr.id, cr.sender.id, cr.receiver.id, COUNT(cm)) " +
            "FROM ChatRoom cr " +
            "LEFT JOIN cr.chatMessages cm " +
            "WHERE (cr.sender = :member OR cr.receiver = :member) " +
            "AND cm.id > (SELECT crs.messageCursor FROM ChatReadStatus crs WHERE crs.room = cr AND crs.member = :member) " +
            "GROUP BY cr.id, cr.sender.id, cr.receiver.id")
    List<ChatRoomCountResponse> countUnreadMessagesInRooms(Member member);
}
