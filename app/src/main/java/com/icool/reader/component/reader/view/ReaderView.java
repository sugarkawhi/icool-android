package com.icool.reader.component.reader.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.icool.reader.R;
import com.icool.reader.component.reader.anim.CoverAnimController;
import com.icool.reader.component.reader.anim.NoneAnimController;
import com.icool.reader.component.reader.anim.PageAnimController;
import com.icool.reader.component.reader.anim.SlideAnimController;
import com.icool.reader.component.reader.bean.ChapterBean;
import com.icool.reader.component.reader.config.IReaderConfig;
import com.icool.reader.component.reader.data.LetterData;
import com.icool.reader.component.reader.data.PageData;
import com.icool.reader.component.reader.element.PageElement;
import com.icool.reader.component.reader.listener.IReaderChapterChangeListener;
import com.icool.reader.component.reader.listener.IReaderTouchListener;
import com.icool.reader.component.reader.manager.PageManager;
import com.icool.reader.component.reader.manager.PageRespository;
import com.icool.reader.component.reader.persistence.IReaderPersistence;
import com.icool.reader.component.reader.utils.BitmapUtils;
import com.icool.reader.component.reader.utils.ReaderLogger;
import com.icool.reader.component.reader.utils.ScreenUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


/**
 * Base
 * Created by ZhaoZongyao on 2018/1/11.
 */
public class ReaderView extends View {
    private static final String TAG = "MReaderView";
    //语音合成播放
    private boolean isSpeaking = false;
    //加载中
    private static final int STATE_LOADING = 1;
    //打开书籍成功
    private static final int STATE_OPEN = 2;
    //当前状态
    private int mCurrentState = STATE_LOADING;
    //页面生成器
    public PageElement mPageElement;
    //背景图
    private Bitmap mReaderBackgroundBitmap;
    //翻页模式
    private int mPageMode;
    //View 宽 强制全屏
    private int mWidth;
    //View 高 强制全屏
    private int mHeight;


    //章节名 Paint
    private Paint mChapterNamePaint;
    //内容 Paint
    private Paint mContentPaint;
    //头 底 Paint
    private Paint mHeaderPaint;
    //分页
    PageManager mPageManager;

    //承载封面内容的View
    private View mCoverView;

    private IReaderTouchListener mReaderTouchListener;
    private PageAnimController mAnimController;

    private PageRespository mRespository;


    public ReaderView(Context context) {
        this(context, null);
    }

    public ReaderView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ReaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ReaderView);
        float headerHeight = array.getDimensionPixelSize(R.styleable.ReaderView_reader_headerHeight, 50);
        float footerHeight = array.getDimensionPixelSize(R.styleable.ReaderView_reader_footerHeight, 50);
        float padding = array.getDimensionPixelSize(R.styleable.ReaderView_reader_padding, 8);
        array.recycle();
        int contentFontSize = IReaderPersistence.getFontSize();
        int lineSpacing = IReaderConfig.LineSpacing.DEFAULT;
        int letterSpacing = IReaderConfig.LetterSpacing.DEFAULT;
        int paragraphSpacing = IReaderConfig.ParagraphSpacing.DEFAULT;
        mWidth = ScreenUtils.getScreenWidth(context);
        mHeight = ScreenUtils.getScreenHeight(context);
        //内容
        mContentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mContentPaint.setTextSize(contentFontSize);
        //头部
        mHeaderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHeaderPaint.setTextSize(IReaderConfig.DEFAULT_HEADER_TEXTSIZE);

        mChapterNamePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mChapterNamePaint.setTextSize(contentFontSize * IReaderConfig.RATIO_CHAPTER_CONTENT);
        mChapterNamePaint.setColor(Color.parseColor("#A0522D"));

        Bitmap bookmarkBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.book_mark);
        mPageElement = new PageElement(mWidth, mHeight, bookmarkBitmap,
                headerHeight, footerHeight, padding,
                mHeaderPaint, mContentPaint, mChapterNamePaint);
        mPageManager = new PageManager(mWidth - padding - padding, mHeight - headerHeight - footerHeight,
                letterSpacing, lineSpacing, paragraphSpacing,
                20, mContentPaint, mChapterNamePaint);

        mRespository = new PageRespository(mPageElement);

        init();
    }

    /**
     * 初始化
     */
    private void init() {
        int pageMode = IReaderPersistence.getPageMode();
        setPageMode(pageMode);
        int background = IReaderPersistence.getBackground();
        setReaderBackground(background);
        int typeface = IReaderPersistence.getTypeface();
        setTypeface(typeface);
        int letterSpacing = IReaderPersistence.getLetterSpacing();
        setLetterSpacing(letterSpacing);
        int lineSpacing = IReaderPersistence.getLineSpacing();
        setLineSpacing(lineSpacing);
        int paragraphSpacing = IReaderPersistence.getParagraphSpacing();
        setParagraphSpacing(paragraphSpacing);

    }

    /**
     * 设置翻页模式
     * {@link IReaderConfig.PageMode#SLIDE)}  平移模式
     * {@link IReaderConfig.PageMode#NONE)}  无动画
     * {@link IReaderConfig.PageMode#SIMULATION)} 仿真
     *
     * @param mode 翻页模式
     */
    public void setPageMode(int mode) {
        switch (mode) {
            case IReaderConfig.PageMode.COVER:
                mAnimController = new CoverAnimController(this, mWidth, mHeight, mPageElement, mPageChangeListener);
                mPageMode = IReaderConfig.PageMode.COVER;
                break;
            case IReaderConfig.PageMode.SLIDE:
                mAnimController = new SlideAnimController(this, mWidth, mHeight, mPageElement, mPageChangeListener);
                mPageMode = IReaderConfig.PageMode.SLIDE;
                break;
            case IReaderConfig.PageMode.NONE:
            default:
                mAnimController = new NoneAnimController(this, mWidth, mHeight, mPageElement, mPageChangeListener);
                mPageMode = IReaderConfig.PageMode.NONE;
                break;
        }
        drawCurrentPage();//因为切换了AnimController对象 需要对其重新绘制
        mAnimController.setIReaderTouchListener(mReaderTouchListener);
    }

    /**
     * 获取当前的翻页模式
     */
    public int getPageMode() {
        return mPageMode;
    }

    private PageAnimController.IPageChangeListener mPageChangeListener = new PageAnimController.IPageChangeListener() {

        @Override
        public void onCancel(int direction) {
            mRespository.onCancel(direction);
        }

        @Override
        public void onSelectPage(int direction, boolean isCancel) {
            mRespository.onSelectPage(direction, isCancel);
            drawCurrentPage();
        }

        @Override
        public boolean hasPre() {
            boolean hasPre = mRespository.pre(mAnimController.getCurrentBitmap(), mAnimController.getNextBitmap());
            return hasPre;
        }

        @Override
        public boolean hasNext() {
            boolean hasNext = mRespository.next(mAnimController.getCurrentBitmap(), mAnimController.getNextBitmap());
            return hasNext;
        }
    };

    @Override
    protected void onDraw(Canvas canvas) {
        if (mCurrentState == STATE_LOADING) {
            canvas.drawBitmap(mReaderBackgroundBitmap, 0, 0, null);
        } else {
            mAnimController.dispatchDrawPage(canvas);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isSpeaking) {
            if (mReaderTouchListener != null)
                mReaderTouchListener.onTouchSpeaking();
            return false;
        }
        ReaderLogger.e(TAG, "onTouchEvent");
        return mAnimController.dispatchTouchEvent(event);
    }


    @Override
    public void computeScroll() {
        mAnimController.computeScroll();
        super.computeScroll();
    }


    /**
     * 阅读器触摸监听
     * set reader touch listener
     */
    public void setReaderTouchListener(IReaderTouchListener readerTouchListener) {
        this.mReaderTouchListener = readerTouchListener;
        mAnimController.setIReaderTouchListener(mReaderTouchListener);
    }

    /**
     * mRespository
     *
     * @param readerChapterChangeListener
     */
    public void setReaderChapterChangeListener(IReaderChapterChangeListener readerChapterChangeListener) {
        mRespository.setChapterChangeListener(readerChapterChangeListener);
    }

    /**
     * 设置当前章节
     * 阅读器维护一个页面的队列
     *
     * @param curChapter 当前章节
     * @param progress   进度
     */
    public void setCurrentChapter(ChapterBean curChapter, float progress) {
        mRespository.reset();
        mRespository.setCurChapter(curChapter);
        mRespository.setProgress(progress);
        rePlanCurChapter();
    }

    /**
     * 设置下一章节
     * 在保证有当前章节的情况下设置下一章节
     * 判断依据：下一章的索引是当前章节的索引+1 否则不予设置
     *
     * @param nextChapter 当前章节的下一章
     */
    public boolean setNextChapter(ChapterBean nextChapter) {
        ChapterBean curChapter = mRespository.getCurChapter();
        if (null == curChapter) return false;
        if (null == nextChapter) return false;
        if (nextChapter.getDorder() == curChapter.getDorder() + 1) {
            mRespository.setNextChapter(nextChapter);
            replanNextChapter();
            return true;
        } else {
            ReaderLogger.e(TAG, "dorder错误");
            return false;
        }
    }

    /**
     * 设置上一章节
     * 判断依据：上一章的索引是当前章节的索引-1 否则不予设置
     */
    public boolean setPreChapter(ChapterBean preChapter) {
        ChapterBean curChapter = mRespository.getCurChapter();
        if (null == curChapter) return false;
        if (null == preChapter) return false;
        if (preChapter.getDorder() == curChapter.getDorder() - 1) {
            mRespository.setPreChapter(preChapter);
            replanPreChapter();
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取阅读器背景图
     */
    public Bitmap getReaderBackgroundBitmap() {
        return mReaderBackgroundBitmap;
    }

    /**
     * 绘制当前页面
     */
    private void drawCurrentPage() {
        mPageElement.generatePage(mRespository.getCurPage(), mAnimController.getCurrentBitmap());
        invalidate();
    }

    /**
     * 为了更好的理解
     */
    public void invalidateSelf() {
        drawCurrentPage();
    }

    /**
     * 设置背景 {@link IReaderConfig.Background }
     *
     * @param background 背景
     */
    public void setReaderBackground(int background) {
        try {
            Bitmap bitmap = null;
            InputStream is = null;
            int fontColor = Color.BLACK;
            int bgColor;
            switch (background) {
                case IReaderConfig.Background.DEFAULT:
                    fontColor = ContextCompat.getColor(getContext(), R.color.reader_font_default);
                    is = getResources().getAssets().open("background/kraft_paper_new.jpg");
                    break;
                case IReaderConfig.Background.IMAGE_BLUE:
                    fontColor = ContextCompat.getColor(getContext(), R.color.reader_font_blue);
                    is = getResources().getAssets().open("background/dandelion.jpg");
                    break;
                case IReaderConfig.Background.IMAGE_PURPLE:
                    fontColor = ContextCompat.getColor(getContext(), R.color.reader_font_purple);
                    is = getResources().getAssets().open("background/butterfly.jpg");
                    break;
                case IReaderConfig.Background.NIGHT:
                    fontColor = ContextCompat.getColor(getContext(), R.color.reader_font_night);
                    is = getResources().getAssets().open("background/alone.jpg");
                    break;
                case IReaderConfig.Background.COLOR_MATCHA:
                    fontColor = ContextCompat.getColor(getContext(), R.color.reader_font_matcha);
                    bgColor = ContextCompat.getColor(getContext(), R.color.reader_bg_matcha);
                    bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.RGB_565);
                    Canvas canvas = new Canvas(bitmap);
                    canvas.drawColor(bgColor);
            }
            if (is != null) {
                bitmap = BitmapFactory.decodeStream(is);
            }
            mReaderBackgroundBitmap = BitmapUtils.scaleBitmap(bitmap, mWidth, mHeight);
            mPageElement.setBackgroundBitmap(mReaderBackgroundBitmap);
            mContentPaint.setColor(fontColor);
            mHeaderPaint.setColor(fontColor);
            //重新绘制封面扉页
            createCover();
            //需要重绘当前页面
            drawCurrentPage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 设置文字大小
     */
    public void setFontSize(int fontSize) {
        if (fontSize < IReaderConfig.FontSize.MIN) {
            ReaderLogger.w(TAG, "font size is too small");
            return;
        }
        if (fontSize > IReaderConfig.FontSize.MAX) {
            ReaderLogger.w(TAG, "font size is too large");
            return;
        }
        IReaderPersistence.saveFontSize(fontSize);
        mContentPaint.setTextSize(fontSize);
        mChapterNamePaint.setTextSize(fontSize * IReaderConfig.RATIO_CHAPTER_CONTENT);
        //需要将 当前、前、后 章节重新分页
        rePlanning();
    }

    /**
     * 如果是第一章 生成封面
     */
    private void createCover() {
        if (mCoverView == null) return;
        Bitmap coverBitmap = BitmapUtils.getCoverBitmap(mCoverView, getReaderBackgroundBitmap());
        mPageElement.setCoverBitmap(coverBitmap);
    }

    /**
     * 封面View
     *
     * @param coverView 封面view
     */
    public void setCoverView(View coverView) {
        mCoverView = coverView;
        createCover();
    }

    /**
     * 刷新封面图
     * 实际重新绘制了View为新的Bitmap
     */
    public void refreshCoverView() {
        createCover();
    }


    /**
     * 设置文字间距
     *
     * @param letterSpacing 文字间距
     */
    public void setLetterSpacing(int letterSpacing) {
        if (letterSpacing > IReaderConfig.LetterSpacing.MAX)
            letterSpacing = IReaderConfig.LetterSpacing.MAX;
        else if (letterSpacing < IReaderConfig.LetterSpacing.MIN)
            letterSpacing = IReaderConfig.LetterSpacing.MIN;
        mPageManager.setLetterSpacing(letterSpacing);
        rePlanning();
    }

    /**
     * setCoverView
     * 设置行间距
     *
     * @param lineSpacing 行间距
     */
    public void setLineSpacing(int lineSpacing) {
        if (lineSpacing > IReaderConfig.LineSpacing.MAX)
            lineSpacing = IReaderConfig.LineSpacing.MAX;
        else if (lineSpacing < IReaderConfig.LineSpacing.MIN)
            lineSpacing = IReaderConfig.LineSpacing.MIN;
        mPageManager.setLineSpacing(lineSpacing);
        rePlanning();
    }

    /**
     * 设置文字间距
     *
     * @param paragraphSpacing 段间距
     */
    public void setParagraphSpacing(int paragraphSpacing) {
        if (paragraphSpacing > IReaderConfig.ParagraphSpacing.MAX)
            paragraphSpacing = IReaderConfig.ParagraphSpacing.MAX;
        else if (paragraphSpacing < IReaderConfig.ParagraphSpacing.MIN)
            paragraphSpacing = IReaderConfig.ParagraphSpacing.MIN;
        mPageManager.setParagraphSpacing(paragraphSpacing);
        rePlanning();
    }


    //处理当前章节
    private Disposable mCurDisposable;
    //处理上一章节
    private Disposable mPreDisposable;
    //处理下一章节
    private Disposable mNextDisposable;

    /**
     * 需要规划
     * 1、设置了文字大小
     * 2、设置了行间距
     * 3、设置了段间距
     */
    private void rePlanning() {
        rePlanCurChapter();
        replanPreChapter();
        replanNextChapter();
    }

    /**
     * 需要规划 当前章节
     * 0、初次设置当前章节
     * 1、设置了文字大小
     * 2、设置了行间距
     * 3、设置了段间距
     */
    private void rePlanCurChapter() {
        if (mCurDisposable != null && !mCurDisposable.isDisposed()) mCurDisposable.dispose();
        Observable.create(new ObservableOnSubscribe<List<PageData>>() {
            @Override
            public void subscribe(ObservableEmitter<List<PageData>> e) throws Exception {
                List<PageData> list = mPageManager.generatePages(mRespository.getCurChapter());
                e.onNext(list);
                e.onComplete();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<PageData>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mCurDisposable = d;
                    }

                    @Override
                    public void onNext(List<PageData> pageList) {
                        if (pageList.isEmpty()) return;
                        float curProgress = mRespository.getProgress();
                        mRespository.setCurPageList(pageList);
                        mRespository.directPageByProgress(curProgress);
                        mCurrentState = STATE_OPEN;
                        drawCurrentPage();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
        replanPreChapter();
        replanNextChapter();
    }

    /**
     * 需要重新规划上一章节
     * 1、设置了文字大小
     * 2、设置了行间距
     * 3、设置了段间距
     */
    private void replanPreChapter() {
        if (mPreDisposable != null && !mPreDisposable.isDisposed()) mPreDisposable.dispose();
        Observable.create(new ObservableOnSubscribe<List<PageData>>() {
            @Override
            public void subscribe(ObservableEmitter<List<PageData>> e) throws Exception {
                List<PageData> list = mPageManager.generatePages(mRespository.getPreChapter());
                e.onNext(list);
                e.onComplete();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<PageData>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mPreDisposable = d;
                    }

                    @Override
                    public void onNext(List<PageData> pageList) {
                        mRespository.setPrePageList(pageList);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /**
     * 需要重新规划上一章节
     * 1、设置了文字大小
     * 2、设置了行间距
     * 3、设置了段间距
     */
    private void replanNextChapter() {
        if (mNextDisposable != null && !mNextDisposable.isDisposed()) mNextDisposable.dispose();
        Observable.create(new ObservableOnSubscribe<List<PageData>>() {
            @Override
            public void subscribe(ObservableEmitter<List<PageData>> e) throws Exception {
                List<PageData> list = mPageManager.generatePages(mRespository.getNextChapter());
                e.onNext(list);
                e.onComplete();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<PageData>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mNextDisposable = d;
                    }

                    @Override
                    public void onNext(List<PageData> pageList) {
                        mRespository.setNextPageList(pageList);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /**
     * 设置当前章节的进度
     * 1.首先要知道当前章节的总页数
     * 2.用总页数乘以这百分比 得出当前页数
     *
     * @param progress 进度 >=0 并且<=1
     */
    public void setChapterProgress(float progress) {
        mRespository.directPageByProgress(progress);
        drawCurrentPage();
    }

    /**
     * 直接跳转到下一章
     */
    public void directNextChapter() {
        mRespository.directNextChapter();
        drawCurrentPage();
    }

    /**
     * 直接跳转到上一章
     */
    public void directPreChapter() {
        mRespository.directPreChapter();
        drawCurrentPage();
    }


    /**
     * 设置书名 用于绘制章节首页头部
     *
     * @param bookName 书名
     */
    public void setBookName(String bookName) {
        mPageManager.setBookName(bookName);
    }

    /**
     * 获取当前的chapter
     * 为了保存当前进度
     */
    public ChapterBean getCurrentChapter() {
        return mRespository.getCurChapter();
    }

    /**
     * 获取当前阅读进度
     *
     * @return 章节阅读进度
     */
    public float getReadingProgress() {
        return mRespository.getProgress();
    }

    /**
     * 获取当前页
     *
     * @return 当前页
     */
    public PageData getCurrentPage() {
        return mRespository.getCurPage();
    }

    /**
     * 设置语音合成进度
     * 重写 LetterData 的equals 和 hashCode 方法
     */
    public void setTtsLetters(List<LetterData> list) {
        if (null == list) return;
        if (mPageElement.getTtsLetters() != null && mPageElement.getTtsLetters().equals(list)) {
            Log.i(TAG, "setTtsLetters again!");
            return;
        }
        mPageElement.setTtsLetters(list);
        drawCurrentPage();
    }

    /**
     * 清空绘制的文字
     */
    public void clearTtsLetters() {
        mPageElement.clearTtsLetters();
        drawCurrentPage();
    }

    /**
     * 直接翻到上一一页
     */
    public PageData directPrePage() {
        boolean hasPre = mAnimController.directPrePage();
        if (hasPre) return mRespository.getCurPage();
        return null;
    }

    /**
     * 直接翻到下一页
     */
    public PageData directNextPage() {
        boolean hasNext = mAnimController.directNextPage();
        if (hasNext) return mRespository.getCurPage();
        return null;
    }

    /**
     * 语音朗读翻到下一页
     */
    public PageData ttsNextPage() {
        PageData nextPage = mRespository.directNextPage();
        if (nextPage != null) {
            drawCurrentPage();
        }
        return nextPage;
    }


    /**
     * 是否在语音朗读模式
     *
     * @return t/f
     */
    public boolean isSpeaking() {
        return isSpeaking;
    }

    /**
     * 开始语音合成
     * 设置标志位
     */
    public void startTts() {
        isSpeaking = true;
    }

    /**
     * 退出语音合成
     */
    public void stopTts() {
        isSpeaking = false;
        mPageElement.stopTts();
        drawCurrentPage();
    }

    /**
     * 电量变化
     * level为整数 PageElement需要使用百分数
     * TODO  实时更新到页面
     *
     * @param level 电量
     */
    public void batteryChange(int level) {
        mPageElement.setBatteryLevel(level / 100f);
    }

    /**
     * 当前时间变化
     * TODO 实时更新到页面
     */
    public void timeChange(String time) {
        mPageElement.setTime(time);
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mCurDisposable != null && !mCurDisposable.isDisposed()) mCurDisposable.dispose();
        if (mPreDisposable != null && !mPreDisposable.isDisposed()) mPreDisposable.dispose();
        if (mNextDisposable != null && !mNextDisposable.isDisposed()) mNextDisposable.dispose();
        super.onDetachedFromWindow();
    }

    /**
     * 是否是打开书籍状态
     *
     * @return t/f
     */
    public boolean isOpening() {
        return mCurrentState == STATE_OPEN;
    }

    /**
     * 从本章节中获取当前页的下一页
     * 设计到 章节 切换的下一页 返回null
     *
     * @return page
     */
    public PageData getNextPageFromCurChapter() {
        return mRespository.getNextPageFromCurChapter();
    }

    /**
     * 设置字体
     * {@link IReaderConfig.Typeface}
     */
    public void setTypeface(int tf) {
        android.graphics.Typeface typeface;
        switch (tf) {
            case IReaderConfig.Typeface.CARTOON:
                typeface = android.graphics.Typeface.createFromAsset(getResources().getAssets(), "fonts/fzkatong.ttf");
                break;
            case IReaderConfig.Typeface.FANTI:
                typeface = android.graphics.Typeface.createFromAsset(getResources().getAssets(), "fonts/fzfanti.ttf");
                break;
            case IReaderConfig.Typeface.SONGTI:
                typeface = android.graphics.Typeface.createFromAsset(getResources().getAssets(), "fonts/fzsongti.ttf");
                break;
            default:
                typeface = android.graphics.Typeface.DEFAULT;
        }
        mContentPaint.setTypeface(typeface);
        mChapterNamePaint.setTypeface(typeface);
        //mHeaderPaint.setTypeface(typeface);
        rePlanning();
    }

    /**
     * 清空所有的数据
     * 1.clear Respository data
     * 2.state change loading
     */
    public void clearData() {
        mRespository.reset();
        mCurrentState = STATE_LOADING;
        invalidate();
    }
}
