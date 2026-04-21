package com.cookie.linkpulse.repository;

import com.cookie.linkpulse.entity.ShortLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShortLinkRepository extends JpaRepository<ShortLink, Long> {

    Optional<ShortLink> findByShortCode(String shortCode);

    Optional<ShortLink> findByShortCodeAndStatus(String shortCode, Integer status);

    boolean existsByShortCode(String shortCode);
}