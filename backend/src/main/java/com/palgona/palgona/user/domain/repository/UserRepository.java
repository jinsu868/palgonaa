package com.palgona.palgona.user.domain.repository;

import com.palgona.palgona.user.domain.User;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final UserJpaRepository userJpaRepository;

    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email);
    }
}
