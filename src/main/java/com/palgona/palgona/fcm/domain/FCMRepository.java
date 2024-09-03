package com.palgona.palgona.fcm.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FCMRepository extends JpaRepository<FCMToken, String> {
    boolean existsBySocialId(String socialId);

    void deleteBySocialId(String socialId);
}
