package com.cookie.linkpulse.repository;

import com.cookie.linkpulse.entity.ShortLinkAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShortLinkAccessLogRepository extends JpaRepository<ShortLinkAccessLog, Long> {

    List<ShortLinkAccessLog> findTop10ByShortLinkIdOrderByAccessTimeDesc(Long shortLinkId);

    @Query(value = """
            SELECT DATE(access_time) AS accessDate, COUNT(*) AS pv
            FROM short_link_access_log
            WHERE short_link_id = :shortLinkId
              AND access_time >= :startTime
            GROUP BY DATE(access_time)
            ORDER BY accessDate ASC
            """, nativeQuery = true)
    List<Object[]> countPvTrend(@Param("shortLinkId") Long shortLinkId,
                                @Param("startTime") String startTime);
}