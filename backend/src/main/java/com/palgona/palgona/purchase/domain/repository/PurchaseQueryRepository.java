package com.palgona.palgona.purchase.domain.repository;

import java.util.List;

import com.palgona.palgona.purchase.domain.PurchaseState;
import com.palgona.palgona.purchase.dto.response.PurchaseResponse;

public interface PurchaseQueryRepository {
    List<PurchaseResponse> findAllByUserId(Long userId, String pageToken, PurchaseState state, int pageSize);
}
