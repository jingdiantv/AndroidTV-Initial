package com.zwn.user.data.protocol.response;

import java.util.List;
import java.util.Map;

public class HistoryResp {
    public int current;
    public boolean hitCount;
    public int pages;
    public List<Map<String, String>> records;
    public boolean searchCount;
    public int size;
    public int total;

    @Override
    public String toString() {
        return "HistoryResp{" +
                "current=" + current +
                ", hitCount=" + hitCount +
                ", pages=" + pages +
                ", records=" + records +
                ", searchCount=" + searchCount +
                ", size=" + size +
                ", total=" + total +
                '}';
    }
}
