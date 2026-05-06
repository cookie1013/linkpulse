package com.cookie.linkpulse.controller;
import com.cookie.linkpulse.dto.ShortLinkStatsOverviewResponse;
import com.cookie.linkpulse.common.ApiResponse;
import com.cookie.linkpulse.dto.ShortLinkStatsDetailResponse;
import com.cookie.linkpulse.dto.TopShortLinkItemResponse;
import com.cookie.linkpulse.service.ShortLinkService;
import org.springframework.web.bind.annotation.*;
import com.cookie.linkpulse.dto.PvTrendItemResponse;
import java.util.List;
import com.cookie.linkpulse.dto.AccessLogPageItemResponse;
import com.cookie.linkpulse.dto.PageResponse;
import com.cookie.linkpulse.dto.RefererStatsItemResponse;

@RestController
@RequestMapping("/api/admin/stats")
public class AdminStatsController {

    private final ShortLinkService shortLinkService;

    public AdminStatsController(ShortLinkService shortLinkService) {
        this.shortLinkService = shortLinkService;
    }

    @GetMapping("/links/{id}")
    public ApiResponse<ShortLinkStatsDetailResponse> getStatsDetail(@PathVariable Long id) {
        return ApiResponse.success(shortLinkService.getStatsDetail(id));
    }

    @GetMapping("/top-links")
    public ApiResponse<List<TopShortLinkItemResponse>> topLinks(
            @RequestParam(defaultValue = "5") Integer limit) {
        return ApiResponse.success(shortLinkService.listTopLinks(limit));
    }
    @GetMapping("/links/{id}/pv-trend")
    public ApiResponse<List<PvTrendItemResponse>> getPvTrend(
            @PathVariable Long id,
            @RequestParam(defaultValue = "7") Integer days) {
        return ApiResponse.success(shortLinkService.getPvTrend(id, days));
    }
    @GetMapping("/links/{id}/overview")
    public ApiResponse<ShortLinkStatsOverviewResponse> getStatsOverview(@PathVariable Long id) {
        return ApiResponse.success(shortLinkService.getStatsOverview(id));
    }
    @GetMapping("/links/{id}/logs")
    public ApiResponse<PageResponse<AccessLogPageItemResponse>> pageAccessLogs(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ApiResponse.success(shortLinkService.pageAccessLogs(id, pageNum, pageSize));
    }
    @GetMapping("/links/{id}/referers")
    public ApiResponse<List<RefererStatsItemResponse>> getRefererStats(@PathVariable Long id) {
        return ApiResponse.success(shortLinkService.getRefererStats(id));
    }
}