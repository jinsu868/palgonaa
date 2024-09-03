package com.palgona.palgona.mileage.application;

import com.palgona.palgona.common.dto.CustomMemberDetails;
import com.palgona.palgona.common.error.exception.BusinessException;
import com.palgona.palgona.mileage.domain.MileageHistory;
import com.palgona.palgona.mileage.domain.MileageState;
import com.palgona.palgona.member.domain.Member;
import com.palgona.palgona.mileage.dto.request.MileageChargeRequest;
import com.palgona.palgona.mileage.domain.MileageHistoryRepository;
import com.palgona.palgona.member.domain.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.palgona.palgona.common.error.code.MileageErrorCode.INVALID_CHARGE_AMOUNT;
import static com.palgona.palgona.common.error.code.MileageErrorCode.INVALID_MILEAGE_TRANSACTION;

@Service
@RequiredArgsConstructor
@Transactional
public class MileageHistoryService {
    private final MileageHistoryRepository mileageHistoryRepository;
    private final MemberRepository memberRepository;


    public void chargeMileage(MileageChargeRequest request, CustomMemberDetails memberDetails){
        Member member = memberDetails.getMember();

        //1.유효한 결제 내역인지 확인
        int before = member.getMileage();
        int amount = request.amount();
        int after = before + amount;

        if(amount < 0){
            throw new BusinessException(INVALID_CHARGE_AMOUNT);
        }

        //2. 멤버 마일리지 변경
        member.updateMileage(after);
        memberRepository.save(member); //스프링 시큐리티로 가져온 멤버는 영속 상태가 아니다...

        //3. 마일리지 변경이력 생성
        MileageHistory mileageHistory = MileageHistory.builder()
                .beforeMileage(before)
                .amount(amount)
                .afterMileage(after)
                .member(member)
                .state(MileageState.CHARGE)
                .build();

        mileageHistoryRepository.save(mileageHistory);
    }

    public int readMileage(CustomMemberDetails memberDetails){
        Member member = memberDetails.getMember();

        //1. 해당 멤버의 최신 마일리지 기록을 확인
        PageRequest limit = PageRequest.of(0, 1);
        List<MileageHistory> mileageHistory = mileageHistoryRepository.findTopByMember(member, limit);

        //2. 예외처리) 마일리지 거래 내역이 없는 경우
        if (mileageHistory.isEmpty()) {
            // 마일리지 값이 0이 아닌 경우
            if(member.getMileage() != 0) {
                member.updateMileage(0);
                memberRepository.save(member);
                throw new BusinessException(INVALID_MILEAGE_TRANSACTION);
            }
        }
        //3. 예외처리) 마일리지 최근 내역과 일치하지 않는 경우
        else if(!mileageHistory.get(0).getAfterMileage().equals(member.getMileage())){
            member.updateMileage(mileageHistory.get(0).getAfterMileage());
            memberRepository.save(member);
            throw new BusinessException(INVALID_MILEAGE_TRANSACTION);
        }

        return member.getMileage();
    }
}
