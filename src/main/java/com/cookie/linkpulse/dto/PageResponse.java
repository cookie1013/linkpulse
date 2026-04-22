package com.cookie.linkpulse.dto;

import java.util.List;

public class PageResponse<T> {

    private List<T> records;
    private long total;
    private int pageNum;
    private int pageSize;

    public PageResponse() {
    }

    public PageResponse(List<T> records, long total, int pageNum, int pageSize) {
        this.records = records;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public List<T> getRecords() {
        return records;
    }

    public long getTotal() {
        return total;
    }

    public int getPageNum() {
        return pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }
}