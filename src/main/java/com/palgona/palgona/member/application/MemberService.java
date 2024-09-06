package com.palgona.palgona.member.application;

import static com.palgona.palgona.common.error.code.MemberErrorCode.*;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.common.dto.response.SliceResponse;
import com.palgona.palgona.common.error.exception.BusinessException;
import com.palgona.palgona.member.domain.Member;
import com.palgona.palgona.member.dto.response.MemberDetailResponse;
import com.palgona.palgona.member.dto.response.MemberResponse;
import com.palgona.palgona.member.dto.request.MemberUpdateRequest;
import com.palgona.palgona.member.domain.MemberRepository;
import com.palgona.palgona.image.application.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final S3Service s3Service;

    public MemberDetailResponse findMyProfile(CustomMemberDetails loginMember) {
        Member member = loginMember.getMember();

        return MemberDetailResponse.from(member);
    }

    public MemberResponse findById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MEMBER_NOT_EXIST));

        return MemberResponse.from(member);
    }

    public SliceResponse<MemberResponse> findAllMember(String cursor) {
        return memberRepository.findAllOrderByIdDesc(cursor);
    }

    @Transactional
    public void update(
            CustomMemberDetails loginMember,
            MemberUpdateRequest memberUpdateRequest
    ) {

        String socialId = loginMember.getUsername();
        Member member = memberRepository.findBySocialId(socialId)
                .orElseThrow(() -> new BusinessException(MEMBER_NOT_FOUND));

        s3Service.deleteFile(member.getProfileImage());
        String imageUrl = s3Service.upload(memberUpdateRequest.image());

        member.updateNickName(memberUpdateRequest.nickName());
        member.updateProfileImage(imageUrl);
    }
}
