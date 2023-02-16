package com.zwn.user.data.protocol.request;

import java.util.List;

public class DelFavoritesReq {

    public List<String> tidList;

    public DelFavoritesReq(List<String> tidList) {
        this.tidList = tidList;
    }
}
