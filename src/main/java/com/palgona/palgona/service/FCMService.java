package com.palgona.palgona.service;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.domain.FCMToken;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.dto.FCMTokenUpdateRequest;
import com.palgona.palgona.repository.FCMRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FCMService {
    private final FCMRepository fcmRepository;

    @Transactional
    public void updateFCMToken(CustomMemberDetails memberDetails, FCMTokenUpdateRequest request){
        Member member = memberDetails.getMember();
        String socialId = member.getSocialId();

        if (fcmRepository.existsBySocialId(socialId)) {
            fcmRepository.deleteBySocialId(socialId);
        }

        fcmRepository.flush();

        FCMToken fcmToken = FCMToken.builder()
                .token(request.token())
                .socialId(member.getSocialId())
                .build();

        fcmRepository.save(fcmToken);
    }

}
