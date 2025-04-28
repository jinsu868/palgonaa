package com.palgona.palgona.member.infrastructure;

import com.palgona.palgona.common.dto.response.SliceResponse;
import com.palgona.palgona.member.dto.response.MemberResponse;

public interface MemberRepositoryCustom {

    SliceResponse<MemberResponse> findAllOrderByIdDesc(Long cursor, int pageSize);
}
