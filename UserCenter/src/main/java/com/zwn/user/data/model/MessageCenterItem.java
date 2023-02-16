package com.zwn.user.data.model;

public class MessageCenterItem {
    public String url;
    public String title;
    public String introduction;
    public String date;

    public MessageCenterItem(String url, String title, String introduction, String date) {
        this.url = url;
        this.title = title;
        this.introduction = introduction;
        this.date = date;
    }
}
