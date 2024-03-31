package com.palgona.palgona.repository;

import com.palgona.palgona.domain.FCMToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FCMRepository extends JpaRepository<FCMToken, String> {
    boolean existsBySocialId(String socialId);

    void deleteBySocialId(String socialId);
}
