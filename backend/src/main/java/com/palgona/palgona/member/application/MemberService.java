package com.palgona.palgona.member.application;

import static com.palgona.palgona.common.error.code.MemberErrorCode.*;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.common.dto.response.SliceResponse;
import com.palgona.palgona.common.error.exception.BusinessException;
import com.palgona.palgona.image.application.S3Service;
import com.palgona.palgona.image.dto.ImageUploadRequest;
import com.palgona.palgona.image.util.FileUtils;
import com.palgona.palgona.member.domain.Member;
import com.palgona.palgona.member.dto.response.MemberDetailResponse;
import com.palgona.palgona.member.dto.response.MemberResponse;
import com.palgona.palgona.member.dto.request.MemberUpdateRequest;
import com.palgona.palgona.member.domain.MemberRepository;
import com.palgona.palgona.mileage.domain.MileageRepository;
import com.palgona.palgona.product.event.ImageUploadEvent;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MileageRepository mileageRepository;
    private final S3Service s3Service;
    private final ApplicationEventPublisher publisher;

    public MemberDetailResponse findMyProfile(Member member) {
        Integer balance = mileageRepository.findBalanceByMember(member);

        return MemberDetailResponse.of(member, balance);
    }

    public MemberResponse findById(Long id) {
        Member member = findMember(id);

        return MemberResponse.from(member);
    }

    public SliceResponse<MemberResponse> findAllMember(Long cursor, int pageSize) {
        return memberRepository.findAllOrderByIdDesc(cursor, pageSize);
    }

    @Transactional
    public void update(
            Member member,
            MemberUpdateRequest memberUpdateRequest
    ) {
        List<ImageUploadRequest> uploadRequests = new ArrayList<>();

        String uploadFileName = FileUtils.createFileName(memberUpdateRequest.image().getOriginalFilename());
        String imageUrl = s3Service.generateS3FileUrl(uploadFileName);
        uploadRequests.add(ImageUploadRequest.of(memberUpdateRequest.image(), uploadFileName));

        member.updateNickName(memberUpdateRequest.nickName());
        member.updateProfileImage(imageUrl);

        publisher.publishEvent(ImageUploadEvent.from(uploadRequests));
    }

    private Member findMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MEMBER_NOT_EXIST));
    }
}
