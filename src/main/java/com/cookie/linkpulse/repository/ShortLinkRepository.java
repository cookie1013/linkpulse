package com.cookie.linkpulse.repository;

import com.cookie.linkpulse.entity.ShortLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface ShortLinkRepository extends JpaRepository<ShortLink, Long> {

    Optional<ShortLink> findByShortCode(String shortCode);

    Optional<ShortLink> findByShortCodeAndStatus(String shortCode, Integer status);

    boolean existsByShortCode(String shortCode);
    Page<ShortLink> findByShortCodeContainingAndOriginalUrlContaining(
            String shortCode, String originalUrl, Pageable pageable);
}