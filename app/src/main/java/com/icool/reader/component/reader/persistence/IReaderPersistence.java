package com.icool.reader.component.reader.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.icool.reader.R;
import com.icool.reader.base.IcoolApplication;
import com.icool.reader.component.reader.config.IReaderConfig;
import com.icool.reader.component.reader.dao.BookMarkBean;
import com.icool.reader.component.reader.dao.BookRecordBean;
import com.icool.reader.component.reader.data.PageData;
import com.icool.reader.component.reader.utils.ReaderLogger;
import com.icool.reader.gen.BookMarkBeanDao;
import com.icool.reader.gen.BookRecordBeanDao;
import com.icool.reader.gen.DaoSession;
import com.icool.reader.utils.Logger;

import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;

import java.math.BigDecimal;
import java.util.List;


/**
 * 持久化保存
 * Created by ZhaoZongyao on 2018/1/30.
 */

public class IReaderPersistence {
    public static final String TAG = "IReaderPersistence";

    private static final String SP_NAME = "me.sugarkawhi.mreader.persistence.IReaderPersistence";
    //文字大小
    private static final String READER_FONT_SIZE = "READER_FONT_SIZE";
    //翻页模式
    private static final String READER_PAGE_MODE = "READER_PAGE_MODE";
    //背景
    protected static final String READER_BACKGROUND = "READER_BACKGROUND";
    //字体颜色
    protected static final String READER_FONT_COLOR = "READER_FONT_COLOR";

    //语音合成速度
    protected static final String READER_TTS_SPEED = "READER_TTS_SPEED";
    //语音合成发音人
    protected static final String READER_TTS_SPEAKER = "READER_TTS_SPEAKER";


    protected static SharedPreferences getSP(Context context) {
        return context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 获取当前文字大小
     */
    public static int getFontSize(Context context) {
        return getSP(context).getInt(READER_FONT_SIZE, IReaderConfig.FontSize.DEFAULT);
    }

    /**
     * 保存阅读器文字大小
     * <p>
     * 在View设置文字大小中已经做了保存，无需自行处理
     */
    public static void saveFontSize(Context context, int fontSize) {
        getSP(context).edit().putInt(READER_FONT_SIZE, fontSize).apply();
    }

    /**
     * 获取当前翻页模式
     */
    public static int getPageMode(Context context) {
        return getSP(context).getInt(READER_PAGE_MODE, IReaderConfig.PageMode.COVER);
    }

    /**
     * 保存翻页模式
     * <p>
     * 在View切换中已经做了保存，无需自行处理
     */
    public static void savePageMode(Context context, int mode) {
        getSP(context).edit().putInt(READER_PAGE_MODE, mode).apply();
    }

    /**
     * 保存语音合成速度
     *
     * @param speed 0-10
     */
    public static void saveTtsSpeed(Context context, int speed) {
        getSP(context).edit().putInt(READER_TTS_SPEED, speed).apply();
    }

    /**
     * 获取语音合成的速度
     *
     * @return
     */
    public static int getTtsSpeed(Context context) {
        return getSP(context).getInt(READER_TTS_SPEED, 5);
    }

    /**
     * 保存语音合成发音人
     */
    public static void saveTtsSpeaker(Context context, int speaker) {
        getSP(context).edit().putInt(READER_TTS_SPEAKER, speaker).apply();
    }

    /**
     * 获取语音合成发音人
     *
     * @return
     */
    public static int getTtsSpeaker(Context context) {
        return getSP(context).getInt(READER_TTS_SPEAKER, IReaderConfig.Speaker.FEMALE);
    }

    //背景
    public interface Background {
        //默认
        int DEFAULT = 1;
        //图片-蓝色
        int IMAGE_BLUE = 2;
        //图片-紫色
        int IMAGE_PURPLE = 3;
        //纯色-抹茶
        int COLOR_MATCHA = 4;
        //夜间模式
        int NIGHT = 5;
    }

    //字体颜色 对应于背景
    public interface FontColor {
        //默认
        int DEFAULT = R.color.reader_font_default;
        //对应于蓝色背景
        int BLUE = R.color.reader_font_blue;
        //对应于紫色背景
        int PURPLE = R.color.reader_font_purple;
        //对应于抹茶色背景
        int MATCHA = R.color.reader_font_matcha;
    }

    /**
     * 获取背景
     */
    public static int getBackground(Context context) {
        return getSP(context).getInt(READER_BACKGROUND, Background.DEFAULT);
    }

    /**
     * 保存背景
     */
    public static void saveBackground(Context context, int background) {
        getSP(context).edit().putInt(READER_BACKGROUND, background).apply();
    }

    /**
     * 获取字体颜色
     */
    public static int getFontColor(Context context) {
        return getSP(context).getInt(READER_FONT_COLOR, FontColor.DEFAULT);
    }

    /**
     * 保存字体颜色
     */
    public static void saveFontColor(Context context, int fontColor) {
        getSP(context).edit().putInt(READER_FONT_COLOR, fontColor).apply();
    }


    /**
     * 保存书籍浏览记录
     *
     * @param bookId 书籍id
     */
    public static void saveBookRecord(String bookId, String chapterId, float progress) {
        BookRecordBean record = queryBookRecord(bookId);
        DaoSession session = IcoolApplication.getInstance().getDaoSession();
        BookRecordBeanDao dao = session.getBookRecordBeanDao();
        if (record == null) {
            //插入
            record = new BookRecordBean(bookId, chapterId, progress);
            long rowID = dao.insert(record);
            Logger.i(TAG, "insert one book record rowID=" + rowID);
        } else {
            //更新
            record.setChapterId(chapterId);
            record.setProgress(progress);
            dao.update(record);
            Logger.e(TAG, "update one book record chapter=");
        }
    }


    /**
     * 获取书籍浏览[章节]及其[位置]
     *
     * @param bookId 书籍id
     */
    public static BookRecordBean queryBookRecord(String bookId) {
        if (TextUtils.isEmpty(bookId)) return null;
        DaoSession session = IcoolApplication.getInstance().getDaoSession();
        BookRecordBeanDao dao = session.getBookRecordBeanDao();
        List<BookRecordBean> list = dao.queryBuilder()
                .where(BookRecordBeanDao.Properties.BookId.eq(bookId))
                .list();
        if (list == null || list.size() == 0)
            return null;
        return list.get(0);
    }

    /**
     * 保存书签
     * 1.先查询数据库中是否有书签
     * 2.如果有不添加，如果没有添加
     */
    public static boolean addBookMark(String bookId, PageData pageData) {
        if (pageData == null) return false;
        String chapterId = pageData.getChapterId();
        float progress = pageData.getProgress();
        BookMarkBean bookMark = queryBookMark(bookId, chapterId, progress);
        DaoSession session = IcoolApplication.getInstance().getDaoSession();
        BookMarkBeanDao dao = session.getBookMarkBeanDao();
        if (bookMark == null) {
            //插入
            String chapterName = pageData.getChapterName();
            String content = pageData.getContent();
            long time = System.currentTimeMillis();
            bookMark = new BookMarkBean();
            bookMark.setBookId(bookId);
            bookMark.setTime(time);
            bookMark.setChapterName(chapterName);
            bookMark.setChapterId(chapterId);
            bookMark.setProgress(progress);
            bookMark.setContent(content);
            long rowID = dao.insert(bookMark);
            Logger.i(TAG, "insert one book mark rowID=" + rowID);
            return true;
        } else {
            Logger.w(TAG, "addBookMark has one");
            return false;
        }
    }

    /**
     * 删除书签
     */
    public static boolean deleteBookMark(String bookId, PageData page) {
        if (page == null) return false;
        String chapterId = page.getChapterId();
        float progress = page.getMarkProgress();
        BookMarkBean bookMark = queryBookMark(bookId, chapterId, progress);
        if (bookMark != null) {
            DaoSession session = IcoolApplication.getInstance().getDaoSession();
            BookMarkBeanDao dao = session.getBookMarkBeanDao();
            dao.deleteByKey(bookMark.get_id());
            return true;
        } else {
            return false;
        }
    }

    /**
     * 查询书签
     * 根据 书籍id 对应的章节id 还有 书签进度 来查询
     *
     * @param bookId       书籍id
     * @param chapterId    章节信息
     * @param markProgress 书签进度
     */
    private static BookMarkBean queryBookMark(String bookId, String chapterId, float markProgress) {
        DaoSession session = IcoolApplication.getInstance().getDaoSession();
        BookMarkBeanDao dao = session.getBookMarkBeanDao();
        QueryBuilder<BookMarkBean> builder = dao.queryBuilder();

        WhereCondition whereCondition = builder.and(
                BookMarkBeanDao.Properties.BookId.eq(bookId),
                BookMarkBeanDao.Properties.ChapterId.eq(chapterId)
        );

        List<BookMarkBean> targetList = builder.where(whereCondition).list();
        if (targetList == null || targetList.size() == 0) {
            return null;
        }
        for (BookMarkBean mark : targetList) {
            if (mark.getProgress() == markProgress) return mark;
        }
        return null;
    }

    /**
     * 根据书籍id查询本书籍的书签列表
     * <p>
     * 新加进来的在最前边-》时间降序排序
     *
     * @param bookId 书籍id
     */
    public static List<BookMarkBean> queryBookMarkList(String bookId) {
        if (TextUtils.isEmpty(bookId)) return null;
        DaoSession session = IcoolApplication.getInstance().getDaoSession();
        BookMarkBeanDao dao = session.getBookMarkBeanDao();
        List<BookMarkBean> list = dao.queryBuilder()
                .where(BookMarkBeanDao.Properties.BookId.eq(bookId))
                .orderDesc(BookMarkBeanDao.Properties.Time)
                .list();
        return list;
    }

    /**
     * 根据书籍id查询本书籍特定章节的书签列表
     * <p>
     * 新加进来的在最前边-》时间降序排序
     *
     * @param bookId 书籍id
     */
    public static List<BookMarkBean> queryBookMarkList(String bookId, String chapterId) {
        if (TextUtils.isEmpty(bookId)) return null;
        DaoSession session = IcoolApplication.getInstance().getDaoSession();
        BookMarkBeanDao dao = session.getBookMarkBeanDao();
        List<BookMarkBean> list = dao.queryBuilder()
                .where(BookMarkBeanDao.Properties.BookId.eq(bookId),
                        BookMarkBeanDao.Properties.ChapterId.eq(chapterId))
                .orderDesc(BookMarkBeanDao.Properties.Time)
                .list();
        return list;
    }

}
