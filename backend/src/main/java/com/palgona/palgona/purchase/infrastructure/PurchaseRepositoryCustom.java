package com.palgona.palgona.purchase.infrastructure;

import com.palgona.palgona.common.dto.response.SliceResponse;
import com.palgona.palgona.member.domain.Member;
import com.palgona.palgona.purchase.dto.response.PurchaseResponse;

public interface PurchaseRepositoryCustom {
    SliceResponse<PurchaseResponse> findAllByMember(Member member, int pageSize, Long cursor);
}
