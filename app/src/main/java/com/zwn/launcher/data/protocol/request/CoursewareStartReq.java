package com.zwn.launcher.data.protocol.request;

import java.io.Serializable;

public class CoursewareStartReq implements Serializable {

    public String skuId;

    public String thirdpartyCode;

    public CoursewareStartReq(String skuId, String thirdpartyCode) {
        this.skuId = skuId;
        this.thirdpartyCode = thirdpartyCode;
    }

    @Override
    public String toString() {
        return "CoursewareStartReq{" +
                "skuId='" + skuId + '\'' +
                ", thirdpartyCode='" + thirdpartyCode + '\'' +
                '}';
    }
}
