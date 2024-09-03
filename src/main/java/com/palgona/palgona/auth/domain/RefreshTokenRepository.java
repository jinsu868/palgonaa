package com.palgona.palgona.auth.domain;

import com.palgona.palgona.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    boolean existsBySocialId(String socialId);

    void deleteBySocialId(String socialId);
}
