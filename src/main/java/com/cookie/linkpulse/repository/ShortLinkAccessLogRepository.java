package com.cookie.linkpulse.repository;

import com.cookie.linkpulse.entity.ShortLinkAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShortLinkAccessLogRepository extends JpaRepository<ShortLinkAccessLog, Long> {

    List<ShortLinkAccessLog> findTop10ByShortLinkIdOrderByAccessTimeDesc(Long shortLinkId);
}