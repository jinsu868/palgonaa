package com.palgona.palgona.controller;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.common.dto.response.SliceResponse;
import com.palgona.palgona.dto.MemberDetailResponse;
import com.palgona.palgona.dto.MemberResponse;
import com.palgona.palgona.dto.MemberUpdateRequest;
import com.palgona.palgona.dto.MemberUpdateRequestWithoutImage;
import com.palgona.palgona.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/my")
    @Operation(summary = "본인 정보 조회 api", description = "본인의 프로필 정보를 조회한다.")
    public ResponseEntity<MemberDetailResponse> findMyProfile(
            @AuthenticationPrincipal CustomMemberDetails member
    ) {
        MemberDetailResponse response = memberService.findMyProfile(member);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{memberId}")
    @Operation(summary = "맴버 조회 api", description = "맴버 id로 프로필 정보를 조회한다.")
    public ResponseEntity<MemberResponse> findById(
            @PathVariable Long memberId
    ) {

        MemberResponse response = memberService.findById(memberId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "맴버 리스트 조회 api",
            description = "첫번째 요청에는 cursor에 값 세팅 X, 두번째 요청부터 이전 응답의 cursor를 파싱해서 넣으면 된다.")
    public ResponseEntity<SliceResponse<MemberResponse>> findAll(
            @RequestParam(required = false) String cursor) {

        SliceResponse<MemberResponse> response = memberService.findAllMember(cursor);
        return ResponseEntity.ok(response);
    }

    @PutMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "맴버 정보 수정 api", description = "닉네임과 프로필 이미지를 받아서 맴버 정보를 수정한다.")
    public ResponseEntity<Void> update(
            @AuthenticationPrincipal CustomMemberDetails member,
            @ModelAttribute MemberUpdateRequest request
    ) {

        memberService.update(member, request);

        return ResponseEntity.ok()
                .header("Location", "/api/v1/members/"
                + member.getMember().getId())
                .build();
    }
}
