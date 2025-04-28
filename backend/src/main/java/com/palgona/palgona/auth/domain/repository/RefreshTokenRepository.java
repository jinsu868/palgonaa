package com.palgona.palgona.auth.domain.repository;

import org.springframework.data.repository.CrudRepository;

import com.palgona.palgona.auth.domain.RefreshToken;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
}
