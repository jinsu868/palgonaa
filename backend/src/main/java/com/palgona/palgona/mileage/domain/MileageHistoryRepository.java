package com.palgona.palgona.mileage.domain;

import com.palgona.palgona.mileage.domain.MileageHistory;
import com.palgona.palgona.member.domain.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MileageHistoryRepository extends JpaRepository<MileageHistory, Long> {
}
