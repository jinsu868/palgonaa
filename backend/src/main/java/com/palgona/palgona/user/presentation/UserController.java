package com.palgona.palgona.user.presentation;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.palgona.palgona.auth.annotation.AuthUser;
import com.palgona.palgona.user.application.UserService;
import com.palgona.palgona.user.domain.User;
import com.palgona.palgona.user.dto.response.UserDetailResponse;
import com.palgona.palgona.image.application.S3Service;
import com.palgona.palgona.user.dto.request.UserUpdateRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/users")
public class UserController {

    private final UserService userService;
    private final S3Service s3Service;

    @GetMapping("/my")
    public ResponseEntity<UserDetailResponse> getMe(@AuthUser User user) {
        return ResponseEntity.ok(UserDetailResponse.from(user));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> update(
            @AuthUser User user,
            @RequestPart(required = false) MultipartFile file,
            @RequestPart UserUpdateRequest request
    ) {
        String imageUrl = s3Service.uploadFile(file);
        userService.update(user.getId(), request, imageUrl);
        return ResponseEntity.noContent().build();
    }
}
