package com.icool.reader.bean;

import java.io.Serializable;

/**
 * 章节
 * Created by ZhaoZongyao on 2018/1/11.
 */

public class BookBean implements Serializable {

    private String authorname;
    private String cover;
    private String name;
    private String storyid;


    public BookBean(String cover, String name, String storyid) {
        this.cover = cover;
        this.name = name;
        this.storyid = storyid;
    }

    public BookBean() {
    }

    public String getAuthorName() {
        return authorname;
    }

    public void setAuthorName(String authorName) {
        this.authorname = authorName;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStoryid() {
        return storyid;
    }

    public void setStoryid(String storyid) {
        this.storyid = storyid;
    }
}

