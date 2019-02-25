package org.think40.twitter.api;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude()
public class Paging {

    private Long totalCount;
    private Integer limit;
    private Integer offset;

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }
}
