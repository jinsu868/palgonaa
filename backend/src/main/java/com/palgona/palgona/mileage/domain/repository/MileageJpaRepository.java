package com.palgona.palgona.mileage.domain.repository;

import com.palgona.palgona.mileage.domain.Mileage;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface MileageJpaRepository extends JpaRepository<Mileage, Long> {
    Optional<Mileage> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Mileage m WHERE m.userId = :userId")
    Optional<Mileage> findByUserIdWithLock(Long userId);
}
