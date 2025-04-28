package com.palgona.palgona.mileage.presentation;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.mileage.dto.request.MileageChargeRequest;
import com.palgona.palgona.mileage.application.MileageService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mileages")
public class MileageController {
    private final MileageService mileageService;

    @PostMapping
    @Operation(summary = "마일리지 충전 api", description = "마일리지 충전값을 받아서 충전을 진행한다.")
    public ResponseEntity<Void> chargeMileage(
            @RequestBody MileageChargeRequest request,
            @AuthenticationPrincipal CustomMemberDetails loginMember
    ){

        mileageService.chargeMileage(request, loginMember.getMember());

        return ResponseEntity.ok().build();
    }
}
