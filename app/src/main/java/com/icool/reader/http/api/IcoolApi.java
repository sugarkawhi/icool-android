package com.icool.reader.http.api;


import com.icool.reader.bean.BookBean;
import com.icool.reader.bean.ChapterBean;
import com.icool.reader.bean.ChapterListBean;
import com.icool.reader.http.BaseHttpResult;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @author zhzy
 * @date 2017/11/1
 */
public interface IcoolApi {

    //    String BASE_URL = "https://app.xhhread.com/";
    String BASE_URL = "http://192.168.1.2/";

    /* 章节目录 */
    @GET("/chapter/searchChapterListVO.i?pageSize=10000")
    Observable<BaseHttpResult<ChapterListBean>> searchChapterListVO(@Query("storyid") String storyid);

    /* 获取章节内容*/
    @GET("/chapter/getChapterReadById.i")
    Observable<BaseHttpResult<ChapterBean>> getChapterReadByIdV2(@Query("chapterid") String chapterid);


    /* 书籍特定章的上一章 */
    @GET("/chapter/getPreChapterReadById.i")
    Observable<BaseHttpResult<ChapterBean>> getPreChapterReadByIdV2(@Query("chapterid") String chapterid);


    /* 书籍特定章的下一章 */
    @GET("/chapter/getNextChapterReadById.i")
    Observable<BaseHttpResult<ChapterBean>> getNextChapterReadByIdV2(@Query("chapterid") String chapterid);


    //长篇详情
    @GET("/longstory/getLongStoryInfoByIdNew.i")
    Observable<BaseHttpResult<BookBean>> getLongStoryInfoByIdNew(@Query("storyid") String storyid);

}
