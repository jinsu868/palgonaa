package com.palgona.palgona.mileage.application;

import com.palgona.palgona.common.error.exception.BusinessException;
import com.palgona.palgona.mileage.domain.Mileage;
import com.palgona.palgona.mileage.domain.MileageHistory;
import com.palgona.palgona.mileage.domain.MileageRepository;
import com.palgona.palgona.mileage.domain.MileageState;
import com.palgona.palgona.member.domain.Member;
import com.palgona.palgona.mileage.dto.request.MileageChargeRequest;
import com.palgona.palgona.mileage.domain.MileageHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.palgona.palgona.common.error.code.MemberErrorCode.MEMBER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class MileageService {
    private final MileageHistoryRepository mileageHistoryRepository;
    private final MileageRepository mileageRepository;

    @Transactional
    public void chargeMileage(
            MileageChargeRequest request,
            Member member
    ){
        Mileage mileage = findMileageByMember(member);

        mileage.charge(request.amount());

        MileageHistory mileageHistory = MileageHistory.of(
                mileage.getBalance() - request.amount(),
                mileage.getBalance(),
                request.amount(),
                MileageState.CHARGE,
                member
        );

        mileageHistoryRepository.save(mileageHistory);
    }

    private Mileage findMileageByMember(Member member) {
        return mileageRepository.findByMember(member)
                .orElseThrow(() -> new BusinessException(MEMBER_NOT_FOUND));
    }
}
