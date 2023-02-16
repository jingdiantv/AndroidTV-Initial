package com.zeewain.search.data.model;

import java.io.Serializable;

public class CourseInfo implements Serializable {
    private String skuId;
    private String name;
    private String imageUrl;

    public CourseInfo(String skuId, String name, String imageUrl) {
        this.skuId = skuId;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public String getSkuId() {
        return skuId;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public String toString() {
        return "CourseInfo{" +
                "skuId='" + skuId + '\'' +
                ", name='" + name + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
