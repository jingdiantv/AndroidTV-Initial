package com.zeewain.search.data.model;

import java.util.List;

public class RecommendInfo {
    private List<CourseInfo> courseInfoList;

    public List<CourseInfo> getCourseInfoList() {
        return courseInfoList;
    }

    public void setCourseInfoList(List<CourseInfo> courseInfoList) {
        this.courseInfoList = courseInfoList;
    }

    @Override
    public String toString() {
        return "RecommendInfo{" +
                "courseInfoList=" + courseInfoList +
                '}';
    }


}
