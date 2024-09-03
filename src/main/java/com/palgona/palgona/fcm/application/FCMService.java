package com.palgona.palgona.fcm.application;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.fcm.domain.FCMToken;
import com.palgona.palgona.member.domain.Member;
import com.palgona.palgona.fcm.dto.request.FCMTokenUpdateRequest;
import com.palgona.palgona.fcm.domain.FCMRepository;
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
