package com.zwn.launcher.data.protocol.request;

public class MainCategoryReq {
    /*
    EMBEDDED_COURSEWARE(1, "内嵌课件"),

    ALONE_COURSEWARE(2, "独立课件"),

    REFILL_CARD(3, "充值卡"),

    AI_INTERACTIVE_SCREEN(4, "AI互动屏"),

    AI_INTERACTIVE_BOX(5, "AI互动盒子"),

    RESOURCE_PACKAGE(6, "资源包"),
     */
    String categoryType;
    String parentId;

    public MainCategoryReq(String categoryType, String parentId) {
        this.categoryType = categoryType;
        this.parentId = parentId;
    }
}
