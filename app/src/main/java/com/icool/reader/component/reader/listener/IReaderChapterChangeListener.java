package com.icool.reader.component.reader.listener;


import com.icool.reader.component.reader.bean.ChapterBean;

/**
 * 章节切换
 * Created by ZhaoZongyao on 2018/1/17.
 */

public interface IReaderChapterChangeListener {

    /**
     * 翻页切换到下一章
     * 或者直接跳转到下一章
     * <p>
     * 此时需要请求前一章来填充
     *
     * @param curChapter 当前的章节
     * @param direction  方向{#IReaderConfig.Pre,IReaderConfig.Next}
     */
    void onChapterChange(ChapterBean curChapter, int direction);

    /**
     * 无上一页
     * 回调的情况是：比如因为网络问题没有加载出来 导致当前无上一章 但是当前不是第一章
     */
    void onNoPrePage(ChapterBean curChapter);

    /**
     * 无下一页
     * 回调的情况是：比如因为网络问题没有加载出来 导致当前无下一章 但是当前不是最后一章
     */
    void onNoNextPage(ChapterBean curChapter);

    /**
     * 到达第一章的第一页
     */
    void onReachFirstChapter();

    /**
     * 到达最后一张的最后一页
     */
    void onReachLastChapter();

    /**
     * 当前章节进度回调
     *
     * @param progress 进度
     */
    void onProgressChange(float progress);

    /**
     * 章节切换出错
     * 当下一章或上一章 没有数据会回调
     */
    void onChapterChangeError(int direction);
}
