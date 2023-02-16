package com.zeewain.search.data.protocol.response;

import java.io.Serializable;
import java.util.List;

public class SearchResp {

    private int recordStartNo;
    private List<RecordInfo> records;
    private int returnNum;
    private int total;

    @Override
    public String toString() {
        return "SearchResp{" +
                "recordStartNo=" + recordStartNo +
                ", records=" + records +
                ", returnNum=" + returnNum +
                ", total=" + total +
                '}';
    }

    public int getRecordStartNo() {
        return recordStartNo;
    }

    public void setRecordStartNo(int recordStartNo) {
        this.recordStartNo = recordStartNo;
    }

    public List<RecordInfo> getRecords() {
        return records;
    }

    public void setRecords(List<RecordInfo> records) {
        this.records = records;
    }

    public int getReturnNum() {
        return returnNum;
    }

    public void setReturnNum(int returnNum) {
        this.returnNum = returnNum;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public static class RecordInfo implements Serializable {
        private String categoryId;
        private String productDesc;
        private String productId;
        private String productImg;
        private int productStatus;
        private List<String> productTags;
        private String productTitle;
        private String productTitleHighlight;
        private String simplerIntroduce;
        private String skuId;

        public String getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(String categoryId) {
            this.categoryId = categoryId;
        }

        public String getProductDesc() {
            return productDesc;
        }

        public void setProductDesc(String productDesc) {
            this.productDesc = productDesc;
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public String getProductImg() {
            return productImg;
        }

        public void setProductImg(String productImg) {
            this.productImg = productImg;
        }

        public int getProductStatus() {
            return productStatus;
        }

        public void setProductStatus(int productStatus) {
            this.productStatus = productStatus;
        }

        public List<String> getProductTags() {
            return productTags;
        }

        public void setProductTags(List<String> productTags) {
            this.productTags = productTags;
        }

        public String getProductTitle() {
            return productTitle;
        }

        public void setProductTitle(String productTitle) {
            this.productTitle = productTitle;
        }

        public String getProductTitleHighlight() {
            return productTitleHighlight;
        }

        public void setProductTitleHighlight(String productTitleHighlight) {
            this.productTitleHighlight = productTitleHighlight;
        }

        public String getSimplerIntroduce() {
            return simplerIntroduce;
        }

        public void setSimplerIntroduce(String simplerIntroduce) {
            this.simplerIntroduce = simplerIntroduce;
        }

        public String getSkuId() {
            return skuId;
        }

        public void setSkuId(String skuId) {
            this.skuId = skuId;
        }

        @Override
        public String toString() {
            return "RecordInfo{" +
                    "categoryId='" + categoryId + '\'' +
                    ", productDesc='" + productDesc + '\'' +
                    ", productId='" + productId + '\'' +
                    ", productImg='" + productImg + '\'' +
                    ", productStatus=" + productStatus +
                    ", productTags=" + productTags +
                    ", productTitle='" + productTitle + '\'' +
                    ", productTitleHighlight='" + productTitleHighlight + '\'' +
                    ", simplerIntroduce='" + simplerIntroduce + '\'' +
                    ", skuId='" + skuId + '\'' +
                    '}';
        }
    }
}
