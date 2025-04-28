package com.palgona.palgona.chat.infrastructure;

import com.palgona.palgona.member.domain.Member;

public interface ChatRoomRepositoryCustom {

    boolean existsBySenderAndReceiver(Member sender, Member receiver);
}
