package com.cookie.linkpulse.repository;

import com.cookie.linkpulse.entity.ShortLinkAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query(value = """
            SELECT COUNT(*)
            FROM short_link_access_log
            WHERE short_link_id = :shortLinkId
              AND access_time >= :startTime
              AND access_time < :endTime
            """, nativeQuery = true)
    Long countPvInRange(@Param("shortLinkId") Long shortLinkId,
                        @Param("startTime") String startTime,
                        @Param("endTime") String endTime);

    @Query(value = """
            SELECT COUNT(DISTINCT client_ip)
            FROM short_link_access_log
            WHERE short_link_id = :shortLinkId
              AND client_ip IS NOT NULL
            """, nativeQuery = true)
    Long countUniqueIp(@Param("shortLinkId") Long shortLinkId);
    Page<ShortLinkAccessLog> findByShortLinkIdOrderByAccessTimeDesc(Long shortLinkId, Pageable pageable);
    @Query(value = """
        SELECT 
            CASE 
                WHEN referer IS NULL OR referer = '' THEN 'direct'
                ELSE referer
            END AS refererSource,
            COUNT(*) AS pv
        FROM short_link_access_log
        WHERE short_link_id = :shortLinkId
        GROUP BY 
            CASE 
                WHEN referer IS NULL OR referer = '' THEN 'direct'
                ELSE referer
            END
        ORDER BY pv DESC
        """, nativeQuery = true)
    List<Object[]> countRefererStats(@Param("shortLinkId") Long shortLinkId);
    @Query(value = """
        SELECT user_agent AS userAgent, COUNT(*) AS pv
        FROM short_link_access_log
        WHERE short_link_id = :shortLinkId
        GROUP BY user_agent
        ORDER BY pv DESC
        """, nativeQuery = true)
    List<Object[]> countUserAgentStats(@Param("shortLinkId") Long shortLinkId);
    boolean existsByShortLinkIdAndEventId(Long shortLinkId, String eventId);
}