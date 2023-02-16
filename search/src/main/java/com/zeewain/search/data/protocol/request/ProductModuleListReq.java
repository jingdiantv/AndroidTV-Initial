package com.zeewain.search.data.protocol.request;

import com.zeewain.search.conf.Constant;

public class ProductModuleListReq {
    private String categoryId = "";
    private String moduleType = "5";
    private boolean count = true;
    private boolean hasSubclass = true;
    private int pageNo;
    private int pageSize;
    private int recordStartNo = 0;
    private String sort = "";
    private String sortOrder = "";

    public ProductModuleListReq(int pageNo) {
        this.pageNo = pageNo;
        pageSize = Constant.HOT_SEARCH_PAGE_SIZE;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getModuleType() {
        return moduleType;
    }

    public void setModuleType(String moduleType) {
        this.moduleType = moduleType;
    }

    public boolean isCount() {
        return count;
    }

    public void setCount(boolean count) {
        this.count = count;
    }

    public boolean isHasSubclass() {
        return hasSubclass;
    }

    public void setHasSubclass(boolean hasSubclass) {
        this.hasSubclass = hasSubclass;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getRecordStartNo() {
        return recordStartNo;
    }

    public void setRecordStartNo(int recordStartNo) {
        this.recordStartNo = recordStartNo;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }
}
