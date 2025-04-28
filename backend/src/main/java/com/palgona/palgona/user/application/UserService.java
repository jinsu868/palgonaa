package com.palgona.palgona.user.application;

import static com.palgona.palgona.common.error.ErrorCode.USER_NOT_FOUND;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.palgona.palgona.common.error.BadRequestException;
import com.palgona.palgona.user.domain.User;
import com.palgona.palgona.user.domain.repository.UserRepository;
import com.palgona.palgona.user.dto.request.UserUpdateRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public void update(Long userId, UserUpdateRequest request, String imageUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException(USER_NOT_FOUND));

        user.update(
                request.email(),
                request.nickname(),
                imageUrl,
                request.password()
        );
    }
}
