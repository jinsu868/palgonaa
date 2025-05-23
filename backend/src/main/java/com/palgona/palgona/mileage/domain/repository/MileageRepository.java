package com.palgona.palgona.mileage.domain.repository;

import com.palgona.palgona.mileage.domain.Mileage;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MileageRepository {

    private final MileageJpaRepository mileageJpaRepository;

    public Optional<Mileage> findByUserId(Long userId) {
        return mileageJpaRepository.findByUserId(userId);
    }

    public Optional<Mileage> findByUserIdWithLock(Long userId) {
        return mileageJpaRepository.findByUserIdWithLock(userId);
    }
}
