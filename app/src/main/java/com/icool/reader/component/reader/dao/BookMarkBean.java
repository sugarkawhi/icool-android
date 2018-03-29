package com.icool.reader.component.reader.dao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;

import java.math.BigDecimal;

/**
 * 书签
 * Created by ZhaoZongyao on 2018/3/15.
 */
@Entity
public class BookMarkBean {

    @Id(autoincrement = true)
    private Long _id;
    @NotNull
    private String bookId;
    @NotNull
    private Long time;
    @NotNull
    private String chapterName;
    @NotNull
    private String chapterId;
    @NotNull
    private Float progress;//为精确小数 0-1f 必须要精确值
    @NotNull
    private String content;
    @Generated(hash = 852098298)
    public BookMarkBean(Long _id, @NotNull String bookId, @NotNull Long time,
            @NotNull String chapterName, @NotNull String chapterId,
            @NotNull Float progress, @NotNull String content) {
        this._id = _id;
        this.bookId = bookId;
        this.time = time;
        this.chapterName = chapterName;
        this.chapterId = chapterId;
        this.progress = progress;
        this.content = content;
    }
    @Generated(hash = 237936453)
    public BookMarkBean() {
    }
    public Long get_id() {
        return this._id;
    }
    public void set_id(Long _id) {
        this._id = _id;
    }
    public String getBookId() {
        return this.bookId;
    }
    public void setBookId(String bookId) {
        this.bookId = bookId;
    }
    public Long getTime() {
        return this.time;
    }
    public void setTime(Long time) {
        this.time = time;
    }
    public String getChapterName() {
        return this.chapterName;
    }
    public void setChapterName(String chapterName) {
        this.chapterName = chapterName;
    }
    public String getChapterId() {
        return this.chapterId;
    }
    public void setChapterId(String chapterId) {
        this.chapterId = chapterId;
    }
    public Float getProgress() {
        return this.progress;
    }
    public void setProgress(Float progress) {
        this.progress = progress;
    }
    public String getContent() {
        return this.content;
    }
    public void setContent(String content) {
        this.content = content;
    }

  


}
