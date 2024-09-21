package com.palgona.palgona.auth.application;


import static com.palgona.palgona.common.error.code.AuthErrorCode.*;
import static com.palgona.palgona.common.error.code.MemberErrorCode.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.palgona.palgona.common.error.exception.BusinessException;
import com.palgona.palgona.common.redis.RedisUtils;
import com.palgona.palgona.image.application.S3Service;
import com.palgona.palgona.image.dto.ImageUploadRequest;
import com.palgona.palgona.image.util.FileUtils;
import com.palgona.palgona.member.domain.Member;
import com.palgona.palgona.member.domain.Role;
import com.palgona.palgona.member.domain.Status;
import com.palgona.palgona.auth.dto.response.KakaoUserInfoResponse;
import com.palgona.palgona.auth.dto.response.LoginResponse;
import com.palgona.palgona.member.domain.MemberRepository;
import com.palgona.palgona.member.dto.request.MemberCreateRequest;
import com.palgona.palgona.mileage.domain.Mileage;
import com.palgona.palgona.mileage.domain.MileageRepository;
import com.palgona.palgona.product.event.ImageUploadEvent;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class LoginService {

    @Value("${s3.default.image}")
    private String defaultImage;

    private static final String BEARER = "Bearer ";

    @Value("${spring.jwt.access.expireMs}")
    private Long accessExpirationTime;

    private final MemberRepository memberRepository;
    private final MileageRepository mileageRepository;
    private final S3Service s3Service;
    private final RestTemplate restTemplate;
    private final RedisUtils redisUtils;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public Long signUp(
            Member member,
            MemberCreateRequest memberCreateRequest
    ) {
        member = findMember(member.getId());
        String nickName = memberCreateRequest.nickName();

        validateRoleOfMember(member);
        validateNameDuplicated(nickName);

        MultipartFile image = memberCreateRequest.image();
        String imageUrl = uploadImage(image);

        Mileage mileage = Mileage.from(member);
        mileageRepository.save(mileage);

        member.updateNickName(nickName);
        member.updateProfileImage(imageUrl);
        member.signUp();

        return member.getId();
    }

    public LoginResponse login(HttpServletRequest request) {
        String accessToken = extractToken(request);
        KakaoUserInfoResponse kakaoUserInfo = getKakaoUserInfo(accessToken);

        Member findMember = memberRepository.findBySocialId(kakaoUserInfo.id())
                .orElseGet(() -> {
                    Member member = Member.of(
                            Status.ACTIVE,
                            kakaoUserInfo.id(),
                            Role.GUEST);
                    memberRepository.save(member);
                    return member;
                });

        return LoginResponse.from(findMember);
    }

    public void logout(String accessToken) {
        redisUtils.setBlacklist(accessToken, accessExpirationTime);
    }

    private KakaoUserInfoResponse getKakaoUserInfo(String accessToken) {
        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = getKakaoRequest(accessToken);
        ResponseEntity<String> kakaoResponse = null;
        try {
            kakaoResponse = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.GET,
                    kakaoUserInfoRequest,
                    String.class
            );
        } catch (Exception e) {
            throw new BusinessException(ILLEGAL_KAKAO_TOKEN);
        }

        KakaoUserInfoResponse kakaoUserInfoResponse = parseKakaoInfo(kakaoResponse);

        return kakaoUserInfoResponse;
    }

    private KakaoUserInfoResponse parseKakaoInfo(ResponseEntity<String> kakaoResponse) {
        ObjectMapper objectMapper = new ObjectMapper();
        KakaoUserInfoResponse kakaoUserInfoResponse;
        try {
            kakaoUserInfoResponse = objectMapper.readValue(kakaoResponse.getBody(), KakaoUserInfoResponse.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("json parsing error");
        }
        return kakaoUserInfoResponse;
    }

    private HttpEntity<MultiValueMap<String, String>> getKakaoRequest(String accessToken) {
        HttpHeaders header = new HttpHeaders();
        header.add(AUTHORIZATION, accessToken);
        header.add(
                "Content-type",
                "application/x-www-form-urlencoded;charset=utf-8"
        );

        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(header);
        return kakaoUserInfoRequest;
    }

    private String extractToken(HttpServletRequest request) {
        String token = request.getHeader(AUTHORIZATION);
        validateToken(token);
        return token;
    }

    private void validateToken(String token) {
        if (token == null || !token.toLowerCase().startsWith(BEARER.toLowerCase())) {
            throw new BusinessException(ILLEGAL_KAKAO_TOKEN);
        }
    }

    private void validateRoleOfMember(Member member) {
        if (!member.isGuest()) {
            throw new BusinessException(ALREADY_SIGNED_UP);
        }
    }

    private void validateNameDuplicated(String nickName) {
        if (memberRepository.existsByNickName(nickName)) {
            throw new BusinessException(DUPLICATE_NAME);
        }
    }

    private Member findMember(Long id) {
        return memberRepository.findById(id).orElseThrow(
                () -> new BusinessException(MEMBER_NOT_FOUND));
    }

    private String uploadImage(MultipartFile image) {
        String uploadFileName = FileUtils.createFileName(image.getOriginalFilename());
        List<ImageUploadRequest> uploadRequests = new ArrayList<>();
        uploadRequests.add(ImageUploadRequest.of(image, uploadFileName));
        if (image != null) {
            publisher.publishEvent(ImageUploadEvent.from(uploadRequests));
            return s3Service.generateS3FileUrl(uploadFileName);
        }
        return defaultImage;
    }
}
