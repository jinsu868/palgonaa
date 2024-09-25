package com.palgona.palgona.chat.domain;

import com.palgona.palgona.chat.infrastructure.ChatRoomRepositoryCustom;
import com.palgona.palgona.member.domain.Member;
import com.palgona.palgona.chat.dto.response.ChatRoomCountResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long>, ChatRoomRepositoryCustom {
}
