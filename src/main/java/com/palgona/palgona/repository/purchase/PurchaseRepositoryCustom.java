package com.palgona.palgona.repository.purchase;

import com.palgona.palgona.common.dto.response.SliceResponse;
import com.palgona.palgona.domain.member.Member;
import com.palgona.palgona.dto.purchase.PurchaseResponse;

public interface PurchaseRepositoryCustom {
    SliceResponse<PurchaseResponse> findAllByMember(Member member, int pageSize, String cursor);
}
