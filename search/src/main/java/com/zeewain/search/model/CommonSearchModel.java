package com.zeewain.search.model;

public class CommonSearchModel {
    private final int viewType;
    private String viewName;

    public CommonSearchModel(int viewType, String viewName) {
        this.viewType = viewType;
        this.viewName = viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public int getViewType() {
        return viewType;
    }

    public String getViewName() {
        return viewName;
    }
}
