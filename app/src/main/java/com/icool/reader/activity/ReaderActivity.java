package com.icool.reader.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Pair;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.baidu.tts.auth.AuthInfo;
import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechSynthesizeBag;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.icool.reader.R;
import com.icool.reader.adapter.BookMarkAdapter;
import com.icool.reader.adapter.CatalogAdapter;
import com.icool.reader.base.BaseActivity;
import com.icool.reader.bean.BookBean;
import com.icool.reader.bean.ChapterListBean;
import com.icool.reader.bean.TtsBean;
import com.icool.reader.component.ptm.PtmLayout;
import com.icool.reader.component.reader.bean.ChapterBean;
import com.icool.reader.component.reader.config.IReaderConfig;
import com.icool.reader.component.reader.config.IReaderDirection;
import com.icool.reader.component.reader.dao.BookMarkBean;
import com.icool.reader.component.reader.dao.BookRecordBean;
import com.icool.reader.component.reader.data.LetterData;
import com.icool.reader.component.reader.data.PageData;
import com.icool.reader.component.reader.dialog.ReaderPayDialog;
import com.icool.reader.component.reader.dialog.ReaderSettingDialog;
import com.icool.reader.component.reader.dialog.ReaderSpacingDialog;
import com.icool.reader.component.reader.dialog.ReaderTtsDialog;
import com.icool.reader.component.reader.listener.IReaderChapterChangeListener;
import com.icool.reader.component.reader.listener.IReaderTouchListener;
import com.icool.reader.component.reader.persistence.IReaderPersistence;
import com.icool.reader.component.reader.tts.OfflineResource;
import com.icool.reader.component.reader.utils.BitmapUtils;
import com.icool.reader.component.reader.utils.ScreenUtils;
import com.icool.reader.component.reader.view.ReaderView;

import com.icool.reader.fragment.BookMarkFragment;
import com.icool.reader.fragment.CatalogueFragment;
import com.icool.reader.http.BaseHttpResult;
import com.icool.reader.http.HttpUtils;
import com.icool.reader.http.RxUtils;
import com.icool.reader.http.api.ApiConfig;
import com.icool.reader.http.api.IcoolApi;
import com.icool.reader.http.observer.DefaultObserver;
import com.icool.reader.utils.Logger;
import com.icool.reader.utils.TimeFormatUtils;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function3;


/**
 * 阅读页
 * Created by ZhaoZongyao on 2018/1/12.
 */

public class ReaderActivity extends BaseActivity implements BookMarkAdapter.IBookMarkItemClickListener,
        CatalogAdapter.IChapterItemClickListener {

    public static final String TAG = "ReaderActivity";
    public static final String PARAM_STORY_ID = "PARAM_STORY_ID";
    private static final char[] TAIL_CHAR = {'，', '。', ';', '”', '！', '？', '\n', '…', '：', ':'};
    private static final int ANIM_DURATION_BAR = 200;

    private String mStoryId;

    @BindView(R.id.app_bar)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer_layout;
    @BindView(R.id.readerView)
    ReaderView readerView;
    @BindView(R.id.reader_seekBar_chapter)
    SeekBar readerSeekBarChapter;
    @BindView(R.id.reader_bottom)
    View readerBottomView;
    @BindView(R.id.tv_progress)
    TextView readerProgress;

    @BindView(R.id.tv_retry)
    TextView mTvRetry; //点击重试
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;//加载

    @BindView(R.id.smartTabLayout)
    SmartTabLayout mSmartTabLayout;
    @BindView(R.id.viewPager)
    ViewPager mViewPager;

    @BindView(R.id.ptmLayout)
    PtmLayout mPtmLayout;


    private int mAppBarHeight;
    private int readerBottomHeight;
    private boolean isShow;
    //正在获取下一章节内容
    private boolean isGettingNext;
    //正在获取上一章节内容
    private boolean isGettingPre;
    //书籍详细信息
    private BookBean mBook;
    private BookMarkFragment mBookMarkFragment;
    private CatalogueFragment mCatalogFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        ButterKnife.bind(this);
        mStoryId = getIntent().getStringExtra(PARAM_STORY_ID);
        init();
    }

    /**
     * 初始化操作
     */
    private void init() {
        registerReaderReceiver();
        initDrawer();
        initPtmLayout();
        initReaderBar();
        initReaderView();
        getBookDetail();
    }

    /**
     * 初始化下拉添加书签控件
     */
    private void initPtmLayout() {
        mPtmLayout.setListener(new PtmLayout.OnPtmHandleListener() {

            private PageData getCurrentPage() {
                return readerView.getCurrentPage();
            }

            @Override
            public boolean canTouch() {
                return !isShow;
            }

            @Override
            public void onPtmStart() {
                //如果当前页已经是书签了 先把当前页临时变为非书签
                PageData page = getCurrentPage();
                if (page == null) return;
                if (page.isMark()) {
                    mPtmLayout.setMark(true);
                    //一下两行代码是为了阅读器上不显示书签标记 。如果是cancel操作要重新设置回来
                    page.setMark(false);
                    readerView.invalidateSelf();
                } else {
                    mPtmLayout.setMark(false);
                }
            }

            @Override
            public void onPtmAddSuccess() {
                PageData page = getCurrentPage();
                if (page == null) return;
                page.setMark(true);//设置当前页为标签页的时候，必须要设置他的标签进度
                page.setMarkProgress(page.getProgress());
                readerView.invalidateSelf();
                addMark(page);
            }

            @Override
            public void onPtmDeleteSuccess() {
                PageData page = getCurrentPage();
                if (page == null) return;
                page.setMark(false);
                readerView.invalidateSelf();
                deleteMark(page);
            }

            @Override
            public void onPtmCancel() {
                Logger.i(TAG, "onPtmCancel");
                PageData page = getCurrentPage();
                if (page == null) return;
                page.setMark(mPtmLayout.isMark());
                readerView.invalidateSelf();
            }
        });
    }

    /**
     * 初始化设置栏
     * TOP：1、添加书签 2、分享
     * BOTTOM： 1、目录 2、夜间/日间 3、翻页 4、设置
     */
    private void initReaderBar() {
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mAppBarLayout.measure(0, 0);
        int statusBarHeight = ScreenUtils.getStatusBarHeight(this);
        mAppBarHeight = mAppBarLayout.getMeasuredHeight() + statusBarHeight;
        mAppBarLayout.setTranslationY(-mAppBarHeight);
        readerBottomView.measure(0, 0);
        readerBottomHeight = readerBottomView.getMeasuredHeight();
        readerBottomView.setTranslationY(readerBottomHeight);
        readerSeekBarChapter.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                readerProgress.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                readerProgress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float progress = seekBar.getProgress() / 100f;
                readerView.setChapterProgress(progress);
                readerProgress.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 初始化阅读器
     */
    private void initReaderView() {
        //设置背景颜色(ps:也设置对应字体颜色)
        readerView.timeChange(TimeFormatUtils.hhmm(System.currentTimeMillis()));
        readerView.setReaderTouchListener(new IReaderTouchListener() {
            @Override
            public boolean canTouch() {
                return !isShow;
            }

            @Override
            public void onTouchCenter() {
                if (isShow) hideReaderBar();
                else showReaderBar();
            }

            @Override
            public void onTouchSpeaking() {
                if (mReaderTtsDialog != null && mReaderTtsDialog.isShowing()) {
                    mReaderTtsDialog.dismiss();
                } else {
                    showTtsDialog();
                }
            }

        });
        readerView.setReaderChapterChangeListener(new IReaderChapterChangeListener() {
            @Override
            public void onChapterChange(ChapterBean curChapter, int direction) {
                if (curChapter == null) {
                    stopTts();
                    return;
                }
                //todo 处理章节收费情况
                if (curChapter.getIsfree() == ApiConfig.YES_NO.N) {
                    showPayDialog(curChapter);
                }
                switch (direction) {
                    case IReaderDirection.NEXT:
                        getNextChapter(curChapter.getChapterid());
                        break;
                    case IReaderDirection.PRE:
                        getPreChapter(curChapter.getChapterid());
                        break;
                }
            }

            @Override
            public void onNoPrePage(ChapterBean curChapter) {
                //TODO 获取前一章
                showToast("暂无上一章节内容，稍后重试");
                if (readerView.getCurrentChapter() != null) {
                    getPreChapter(readerView.getCurrentChapter().getChapterid());
                }
            }

            @Override
            public void onNoNextPage(ChapterBean curChapter) {
                showToast("暂无下一章节内容，稍后重试");
                if (readerView.getCurrentChapter() != null) {
                    getNextChapter(readerView.getCurrentChapter().getChapterid());
                }
            }

            @Override
            public void onReachFirstChapter() {
                showToast("已经是第一章了");
            }

            @Override
            public void onReachLastChapter() {
                showToast("已经是最后一章了");
            }

            @Override
            public void onProgressChange(float progress) {
                Logger.d(TAG, "onProgressChange > " + progress);
                readerSeekBarChapter.setProgress((int) (progress * 100));
            }

            @Override
            public void onChapterChangeError(int direction) {
                if (direction == IReaderDirection.NEXT) {
                    Logger.e(TAG, "onChapterChangeError > NEXT");
                } else {
                    Logger.e(TAG, "onChapterChangeError > PRE");
                }
            }
        });
    }

    // 接收电池信息更新的广播
    // 接收时间变化的广播
    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                int level = intent.getIntExtra("level", 0);
                Logger.d(TAG, "ACTION_BATTERY_CHANGED  LEVEL=" + level);
                readerView.batteryChange(level);
            } else if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
                Logger.d(TAG, "ACTION_TIME_TICK");
                readerView.timeChange(TimeFormatUtils.hhmm(System.currentTimeMillis()));
            }
        }
    };

    /**
     * 注册阅读器广播接收器
     */
    private void registerReaderReceiver() {
        //注册广播接受器
        IntentFilter mfilter = new IntentFilter();
        mfilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mfilter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(myReceiver, mfilter);
    }


    /**
     * 初始化抽屉栏
     */
    private void initDrawer() {
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        Bundle bundle = new Bundle();
        bundle.putString(PARAM_STORY_ID, mStoryId);
        mCatalogFragment = CatalogueFragment.newInstance();
        mBookMarkFragment = BookMarkFragment.newInstance();
        mCatalogFragment.setArguments(bundle);
        mBookMarkFragment.setArguments(bundle);
        final List<Fragment> fragments = new ArrayList<>();
        fragments.add(mCatalogFragment);
        fragments.add(mBookMarkFragment);
        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return fragments.size();
            }

            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return position == 0 ? "目录" : "书签";
            }

        });
        mSmartTabLayout.setViewPager(mViewPager);

    }

    /**
     * 获取书籍详情
     */
    private void getBookDetail() {
        mProgressBar.setVisibility(View.VISIBLE);
        mTvRetry.setVisibility(View.GONE);
        HttpUtils.getApiInstance()
                .getLongStoryInfoByIdNew(mStoryId)
                .compose(RxUtils.<BaseHttpResult<BookBean>>defaultSchedulers())
                .compose(this.<BaseHttpResult<BookBean>>bindToLifecycle())
                .subscribe(new DefaultObserver<BookBean>() {
                    @Override
                    protected void onSuccess(BookBean bookBean) {
                        generateCover(bookBean);
                        mBook = bookBean;
                        getChapterList();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        mProgressBar.setVisibility(View.GONE);
                        mTvRetry.setVisibility(View.VISIBLE);
                    }

                    @Override
                    protected void onFail(BaseHttpResult<BookBean> result) {
                        super.onFail(result);
                        showToast(result.getMessage());
                        mProgressBar.setVisibility(View.GONE);
                        mTvRetry.setVisibility(View.VISIBLE);
                    }
                });
    }

    /**
     * 获取章节
     */
    private void getChapterList() {
        HttpUtils.getApiInstance()
                .searchChapterListVO(mStoryId)
                .compose(RxUtils.<BaseHttpResult<ChapterListBean>>defaultSchedulers())
                .compose(this.<BaseHttpResult<ChapterListBean>>bindToLifecycle())
                .subscribe(new DefaultObserver<ChapterListBean>() {
                    @Override
                    protected void onSuccess(ChapterListBean chapterListBean) {
                        BookRecordBean record = IReaderPersistence.queryBookRecord(mStoryId);
                        if (record == null) {
                            List<ChapterBean> chapterBeans = chapterListBean.getDatas();
                            if (chapterBeans != null && !chapterBeans.isEmpty()) {
                                getChapterById(chapterBeans.get(0).getChapterid(), 0);
                            } else {
                                showToast("目录为空");
                            }
                        } else if (record.getChapterId() != null) {
                            getChapterById(record.getChapterId(), record.getProgress());
                        }
                    }

                    @Override
                    protected void onException(ExceptionReason reason) {
                        super.onException(reason);
                        mProgressBar.setVisibility(View.GONE);
                        mTvRetry.setVisibility(View.VISIBLE);
                    }
                });

    }

    /**
     * 根据chapter id 来 获取章节内容
     * 同时获取前后两章  保证上下章切换时的准确
     *
     * @param chapterId 当前章节id
     */
    private void getChapterById(final String chapterId, final float progress) {
        mProgressBar.setVisibility(View.VISIBLE);
        mTvRetry.setVisibility(View.GONE);
        readerView.clearData();
        if (TextUtils.isEmpty(chapterId)) return;
        final IcoolApi api = HttpUtils.getApiInstance();
        final Observable<BaseHttpResult<ChapterBean>> cur = api.getChapterReadByIdV2(chapterId);
        Observable<BaseHttpResult<ChapterBean>> pre = api.getPreChapterReadByIdV2(chapterId);
        Observable<BaseHttpResult<ChapterBean>> next = api.getNextChapterReadByIdV2(chapterId);
        Observable.zip(cur, pre, next, new Function3<BaseHttpResult<ChapterBean>, BaseHttpResult<ChapterBean>, BaseHttpResult<ChapterBean>, ZipChapter>() {
            @Override
            public ZipChapter apply(BaseHttpResult<ChapterBean> result, BaseHttpResult<ChapterBean> result2, BaseHttpResult<ChapterBean> result3) throws Exception {
                ZipChapter zipChapter = new ZipChapter();
                zipChapter.cur = result.getData();
                zipChapter.pre = result2.getData();
                zipChapter.next = result3.getData();
                return zipChapter;
            }
        })
                .compose(RxUtils.<ZipChapter>defaultSchedulers())
                .compose(this.<ZipChapter>bindToLifecycle())
                .subscribe(new Observer<ZipChapter>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ZipChapter zipChapter) {
                        ChapterBean curChapter = zipChapter.cur;
                        readerView.setCurrentChapter(curChapter, progress);
                        if (curChapter != null && curChapter.getIsfree() == ApiConfig.YES_NO.N) {
                            showPayDialog(curChapter);
                        }
                        boolean next = readerView.setNextChapter(zipChapter.next);
                        boolean pre = readerView.setPreChapter(zipChapter.pre);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mProgressBar.setVisibility(View.GONE);
                        mTvRetry.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onComplete() {
                        mProgressBar.setVisibility(View.GONE);
                        mTvRetry.setVisibility(View.GONE);
                    }
                });
    }


    /**
     * 获取章节的前一章
     *
     * @param chapterId 当前章节id
     */
    private void getPreChapter(String chapterId) {
        if (isGettingPre) return;
        isGettingPre = true;
        HttpUtils.getApiInstance()
                .getPreChapterReadByIdV2(chapterId)
                .compose(RxUtils.<BaseHttpResult<ChapterBean>>defaultSchedulers())
                .compose(this.<BaseHttpResult<ChapterBean>>bindToLifecycle())
                .subscribe(new DefaultObserver<ChapterBean>() {
                    @Override
                    protected void onSuccess(ChapterBean chapterBean) {
                        readerView.setPreChapter(chapterBean);
                        isGettingPre = false;
                    }

                    @Override
                    protected void onFail(BaseHttpResult<ChapterBean> result) {
                        super.onFail(result);
                        isGettingPre = false;
                    }

                    @Override
                    protected void onException(ExceptionReason reason) {
                        super.onException(reason);
                        isGettingPre = false;
                    }
                });
    }

    /**
     * 获取章节下一章
     *
     * @param chapterId 章节id
     */
    private void getNextChapter(String chapterId) {
        if (isGettingNext) return;
        isGettingNext = true;
        HttpUtils.getApiInstance()
                .getNextChapterReadByIdV2(chapterId)
                .compose(RxUtils.<BaseHttpResult<ChapterBean>>defaultSchedulers())
                .subscribe(new DefaultObserver<ChapterBean>() {
                    @Override
                    protected void onSuccess(ChapterBean chapterBean) {
                        readerView.setNextChapter(chapterBean);
                        isGettingNext = false;
                    }

                    @Override
                    protected void onFail(BaseHttpResult<ChapterBean> result) {
                        super.onFail(result);
                        isGettingNext = false;
                    }

                    @Override
                    protected void onException(ExceptionReason reason) {
                        super.onException(reason);
                        isGettingNext = false;
                    }
                });
    }

    /**
     * 显示 APP_BAR 和 Bottom_Bar 和状态栏
     */
    private void showReaderBar() {
        if (isShow) return;
        isShow = true;
        mAppBarLayout.animate()
                .translationY(0)
                .setDuration(ANIM_DURATION_BAR)
                .start();
        readerBottomView.animate()
                .translationY(0)
                .setDuration(ANIM_DURATION_BAR)
                .start();
        showSystemUI();
    }

    /**
     * 隐藏 APP_BAR 和 Bottom_Bar 和 状态栏
     */
    private void hideReaderBar() {
        if (!isShow) return;
        isShow = false;
        mAppBarLayout.animate()
                .translationY(-mAppBarHeight)
                .setDuration(ANIM_DURATION_BAR)
                .start();
        readerBottomView.animate()
                .translationY(readerBottomHeight)
                .setDuration(ANIM_DURATION_BAR)
                .start();
        hideSystemUI();
    }


    /**
     * 隐藏系统状态栏
     */
    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hideReaderBar status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //隐藏状态栏
    }


    /**
     * 显示状态栏
     */
    private void showSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //显示状态栏
    }

    /**
     * 添加书签
     */
    private void addMark(final PageData page) {
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                boolean addMark = IReaderPersistence.addBookMark(mStoryId, page);
                e.onNext(addMark);
                e.onComplete();
            }
        })
                .compose(RxUtils.<Boolean>defaultSchedulers())
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(new io.reactivex.observers.DefaultObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) Logger.i(TAG, "添加书签成功");
                        else Logger.e(TAG, "添加书签失败");
                        mBookMarkFragment.refresh();
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
     * 删除书签
     */
    private void deleteMark(final PageData page) {
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                e.onNext(IReaderPersistence.deleteBookMark(mStoryId, page));
                e.onComplete();
            }
        })
                .compose(RxUtils.<Boolean>defaultSchedulers())
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) Logger.i(TAG, "删除书签成功");
                        else Logger.e(TAG, "删除书签失败");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        mBookMarkFragment.refresh();
                    }
                });
    }

    /**
     * 点击重试
     */
    @OnClick(R.id.tv_retry)
    public void retry() {
        if (mBook == null) {
            getBookDetail();
        } else {
            getChapterList();
        }
    }

    /**
     * 上一章
     */
    @OnClick(R.id.reader_pre)
    public void preChapter() {
        readerView.directPreChapter();
    }

    /**
     * 下一章
     */
    @OnClick(R.id.reader_next)
    public void nextChapter() {
        readerView.directNextChapter();
    }

    /**
     * 点击设置 弹框
     */
    @OnClick(R.id.tv_setting)
    public void setting() {
        hideReaderBar();
        ReaderSettingDialog dialog = new ReaderSettingDialog(this);
        dialog.setSettingListener(new ReaderSettingDialog.IReaderSettingListener() {
            @Override
            public void onFontSizeChange(int fontSize) {
                readerView.setFontSize(fontSize);
            }

            @Override
            public void onBackgroundChange(int background) {
                readerView.setReaderBackground(background);
            }

            @Override
            public void onPageModeChange(int pageMode) {
                readerView.setPageMode(pageMode);
            }

            @Override
            public void onTypefaceChange(int typeface) {
                readerView.setTypeface(typeface);
            }
        })
                .show();
    }


    /**
     * 设置间距
     */
    @OnClick(R.id.iv_spacing)
    public void setSpacing() {
        hideReaderBar();
        showSpacingDialog();
    }

    /**
     * 显示间距对话框
     */
    private void showSpacingDialog() {
        ReaderSpacingDialog dialog = new ReaderSpacingDialog(this);
        dialog.setSpacingChangeListener(new ReaderSpacingDialog.IReaderSpacingChangeListener() {

            @Override
            public void onLetterSpacingChange(int letterSpacing) {
                readerView.setLetterSpacing(letterSpacing);
            }

            @Override
            public void onLineSpacingChange(int lineSpacing) {
                readerView.setLineSpacing(lineSpacing);
            }

            @Override
            public void onParagraphSpacingChange(int paragraphSpacing) {
                readerView.setParagraphSpacing(paragraphSpacing);
            }
        })
                .show();
    }


    /**
     * 打开目录页
     */
    @OnClick(R.id.tv_catalog)
    public void openDrawer() {
        hideReaderBar();
        drawer_layout.openDrawer(Gravity.START);
    }


    @Override
    public void onBackPressed() {
        if (isShow) {
            hideReaderBar();
            return;
        }
        if (readerView.isSpeaking()) {
            stopTts();
            showToast("已退出语音朗读");
            return;
        }
        if (drawer_layout.isDrawerOpen(Gravity.START)) {
            drawer_layout.closeDrawer(Gravity.START);
            return;
        }
        saveReadingProgress();
        super.onBackPressed();
    }


    /**
     * 如果是第一章 生成封面
     */
    public void generateCover(BookBean book) {
        final View view = getLayoutInflater().inflate(R.layout.layout_reader_cover, null);
        final ImageView cover_img = view.findViewById(R.id.reader_cover_img);
        TextView cover_bookName = view.findViewById(R.id.reader_cover_bookName);
        TextView cover_authorName = view.findViewById(R.id.reader_cover_authorName);
        cover_bookName.setText(book.getName());
        cover_authorName.setText(book.getAuthorName());
        int width = ScreenUtils.getScreenWidth(this);
        int height = ScreenUtils.getScreenHeight(this);
        view.layout(0, 0, width, height);
        int measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int measuredHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.AT_MOST);
        /** 当然，measure完后，并不会实际改变View的尺寸，需要调用View.layout方法去进行布局。
         * 按示例调用layout函数后，View的大小将会变成你想要设置成的大小。
         */
        view.measure(measuredWidth, measuredHeight);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        readerView.setCoverView(view);
        Glide.with(this)
                .load(book.getCover())
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                        cover_img.setImageDrawable(resource);
                        readerView.refreshCoverView();
                    }
                });
    }

    /**
     * 书签Item点击
     *
     * @param bookMark 书签
     */
    @Override
    public void onBookMarkItemClick(BookMarkBean bookMark) {
        drawer_layout.closeDrawer(Gravity.START);
        getChapterById(bookMark.getChapterId(), bookMark.getProgress());
    }

    @Override
    public void onChapterItemClick(ChapterBean chapter) {
        drawer_layout.closeDrawer(Gravity.START);
        getChapterById(chapter.getChapterid(), 0);

    }

    private class ZipChapter {
        ChapterBean cur;
        ChapterBean pre;
        ChapterBean next;
    }

    /**
     * 保存阅读进度
     */
    private void saveReadingProgress() {
        ChapterBean chapter = readerView.getCurrentChapter();
        if (chapter == null) return;
        IReaderPersistence.saveBookRecord(mStoryId, chapter.getChapterid(), readerView.getReadingProgress());
    }

    /****************************语音合成*****************************
     * **************************语音合成*****************************
     * **************************语音合成*****************************
     * **************************语音合成*****************************/

    //语音朗读设置弹框
    private ReaderTtsDialog mReaderTtsDialog;
    //初始化等待弹框
    private ProgressDialog mInitialBaiduTtsDialog;
    //初始化成功
    private boolean isInitied;
    //当前批量阅读到的索引位置
    private int mTtsIndex;
    // 主控制类，所有合成控制方法从这个类开始
    protected SpeechSynthesizer mSpeechSynthesizer;
    //语音合成列表
    private List<TtsBean> mTtsList;
    //页面切换时 避免整句被分割,字符偏移量 默认为0
    private int mTtsLetterOffset = 0;

    //开启语音合成
    @OnClick(R.id.iv_tts)
    public void tts() {
        //TODO 付费章节不支持
        if (readerView.getCurrentChapter() != null
                && readerView.getCurrentChapter().getIsfree() == ApiConfig.YES_NO.N) {
            showToast("付费章节不支持语音朗读");
            return;
        }
        if (isInitied) {
            startTts();
        } else {
            initialTts();
        }
        hideReaderBar();
    }

    /**
     * 开始语音合成
     */
    private void startTts() {
        PageData pageData = readerView.getCurrentPage();
        if (pageData == null) {
            showToast("当前页内容为空");
            return;
        }
        speak(pageData);
    }

    /**
     * 语音合成计算
     *
     * @param letters 所给字符
     */
    public List<TtsBean> calculate(List<LetterData> letters) {
        List<TtsBean> ttsList = new ArrayList<>();
        if (letters == null || letters.size() == 0) {
            return ttsList;
        }
        if (mTtsLetterOffset != 0) letters = letters.subList(mTtsLetterOffset, letters.size());
        List<LetterData> letterList = new ArrayList<>();
        int utteranceId = 0;
        for (int i = 0; i < letters.size(); i++) {
            LetterData letter = letters.get(i);
            letterList.add(letter);
            char c = letter.getLetter();
            if (ArrayUtils.contains(TAIL_CHAR, c)) {
                TtsBean ttsBean = new TtsBean();
                ttsBean.setUtteranceId(utteranceId);
                ttsBean.setLetterList(new ArrayList<>(letterList));
                utteranceId++;
                letterList.clear();
                ttsList.add(ttsBean);
            }
        }
        if (letterList.size() > 0) {
            TtsBean ttsBean = new TtsBean();
            ttsBean.setUtteranceId(utteranceId);
            //判断最后一个字符是否是标点 如果是：那么本页正好处理完 如果否：需要加下一页补全这句话
            LetterData lastLetterData = letterList.get(letterList.size() - 1);
            if (ArrayUtils.contains(TAIL_CHAR, lastLetterData.getLetter())) {
                ttsBean.setLetterList(new ArrayList<>(letterList));
                mTtsLetterOffset = 0;//偏移量置0
            } else {
                comleteSentenceWithNextPage(readerView.getNextPageFromCurChapter(), letterList);
                ttsBean.setLetterList(new ArrayList<>(letterList));
            }
            letterList.clear();
            ttsList.add(ttsBean);
        } else {
            mTtsLetterOffset = 0;//偏移量置0
        }

        for (TtsBean tts : ttsList) {
            Logger.e(TAG, "utteranceId > " + tts.getUtteranceId() + " content > " + tts.getContent());
        }
        return ttsList;
    }

    /**
     * 用下一页的内容来补全句子
     *
     * @param nextPage           下一页内容
     * @param sentenceLetterList 句子中的字符集合
     */
    private void comleteSentenceWithNextPage(PageData nextPage, List<LetterData> sentenceLetterList) {
        if (nextPage == null) return;
        List<LetterData> letterList = nextPage.getLetters();
        if (letterList == null) return;
        mTtsLetterOffset = 0;//偏移量置0 重新计算下一页偏移量
        for (LetterData letter : letterList) {
            mTtsLetterOffset++;//偏移量
            sentenceLetterList.add(letter);
            if (ArrayUtils.contains(TAIL_CHAR, letter.getLetter())) {
                break;
            }
        }
    }


    // ================== 初始化参数设置开始 ==========================
    /**
     * 发布时请替换成自己申请的appId appKey 和 secretKey。注意如果需要离线合成功能,请在您申请的应用中填写包名。
     * 本demo的包名是com.baidu.tts.sample，定义在build.gradle中。
     */

    // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
    protected TtsMode ttsMode = TtsMode.MIX;
    //
//    // 离线发音选择，VOICE_FEMALE即为离线女声发音。
    // assets目录下bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat为离线男声模型；
    // assets目录下bd_etts_common_speech_f7_mand_eng_high_am-mix_v3.0.0_20170512.dat为离线女声模型

    // ===============初始化参数设置完毕，更多合成参数请至getParams()方法中设置 =================


    /**
     * 初始化引擎，需要的参数均在InitConfig类里
     * <p>
     * DEMO中提供了3个SpeechSynthesizerListener的实现
     * MessageListener 仅仅用log.i记录日志，在logcat中可以看见
     * UiMessageListener 在MessageListener的基础上，对handler发送消息，实现UI的文字更新
     * FileSaveListener 在UiMessageListener的基础上，使用 onSynthesizeDataArrived回调，获取音频流
     */
    protected void initialTts() {
        //TODO 需要弹框表示正在初始化 并且不能取消 否则可能导致报错
        mInitialBaiduTtsDialog = new ProgressDialog(this);
        mInitialBaiduTtsDialog.setCancelable(false);
        mInitialBaiduTtsDialog.setCanceledOnTouchOutside(false);
        mInitialBaiduTtsDialog.setMessage("正在载入语音朗读,请稍后");
        mInitialBaiduTtsDialog.show();
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                LoggerProxy.printable(true); // 日志打印在logcat中
                // 设置初始化参数
                // appId appKey secretKey 网站上您申请的应用获取。注意使用离线合成功能的话，需要应用中填写您app的包名。包名在build.gradle中获取。
                mSpeechSynthesizer = SpeechSynthesizer.getInstance();
                mSpeechSynthesizer.setContext(ReaderActivity.this);
                mSpeechSynthesizer.setSpeechSynthesizerListener(mSynthesizerListener);
                mSpeechSynthesizer.auth(ttsMode);
                mSpeechSynthesizer.setAppId(IReaderConfig.TTS_APP_ID);
                mSpeechSynthesizer.setApiKey(IReaderConfig.TTS_API_KEY, IReaderConfig.TTS_SECRET_KEY);


                // 授权检测接口(只是通过AuthInfo进行检验授权是否成功。选择纯在线可以不必调用auth方法。
                AuthInfo authInfo = mSpeechSynthesizer.auth(ttsMode);
                if (!authInfo.isSuccess()) {
                    // 离线授权需要网站上的应用填写包名。本demo的包名是com.baidu.tts.sample，定义在build.gradle中
                    String errorMsg = authInfo.getTtsError().getDetailMessage();
                }
                setParams();
                // 初始化tts TODO 这儿有报错的可能 在初始化Tts的时候销毁了activity 会空指针
                int result = mSpeechSynthesizer.initTts(ttsMode);
                if (result != 0) {
                    Logger.e(TAG, "【error】initTts 初始化失败 + errorCode：" + result);
//                    e.onError(new Throwable("【error】initTts 初始化失败 + errorCode：" + result));
                    e.onNext(false);
                } else {
                    // 此时可以调用 speak和synthesize方法
                    Logger.d(TAG, "\"合成引擎初始化成功,此时可以调用 speak和synthesize方法\"");
                    e.onNext(true);
                }
                e.onComplete();
            }
        }).compose(RxUtils.<Boolean>defaultSchedulers())
                .compose(this.<Boolean>bindToLifecycle())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        mInitialBaiduTtsDialog.dismiss();
                        if (!aBoolean) showToast("初始化失败");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(TAG, "onError 语音朗读载入失败");
                        e.printStackTrace();
                        mInitialBaiduTtsDialog.dismiss();
                        showToast("语音朗读载入失败");
                    }

                    @Override
                    public void onComplete() {
                        isInitied = true;
                        startTts();
                        mInitialBaiduTtsDialog.dismiss();
                    }
                });

    }

    private SpeechSynthesizerListener mSynthesizerListener = new SpeechSynthesizerListener() {
        //已经翻到下一页了
        //做标志位的原因：onSpeechProgressChanged中progress的回调与播放到哪个字无关
        //所以就无从知道具体读到哪里了 只能粗略统计和判断 无法精准判断
        private boolean hasSwitchNextPage = false;

        @Override
        public void onSynthesizeStart(String s) {

        }

        @Override
        public void onSynthesizeDataArrived(String s, byte[] bytes, int i) {

        }

        @Override
        public void onSynthesizeFinish(String s) {

        }

        @Override
        public void onSpeechStart(String utteranceId) {

        }

        /**
         * 播放进度回调接口，分多次回调
         * 注意：progress表示进度，与播放到哪个字无关【真的坑】
         *
         * @param utteranceId
         * @param progress 文本按字符划分的进度，比如:你好啊 进度是0-3
         */
        @Override
        public void onSpeechProgressChanged(String utteranceId, final int progress) {
            mTtsIndex = Integer.parseInt(utteranceId);
            if (mTtsList == null || mTtsIndex >= mTtsList.size())
                return;
            final List<LetterData> letterDataList = mTtsList.get(mTtsIndex).getLetterList();
            Logger.e(TAG, "onSpeechProgressChanged > utteranceId=" + utteranceId + " progress=" + progress);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //todo 最后一句要做处理
                    if (mTtsIndex == mTtsList.size() - 1) {
                        if (mTtsLetterOffset == 0) {//没有涉及到下一页
                            readerView.setTtsLetters(letterDataList);
                        } else {
                            //TODO 要翻页了
                            if (progress > letterDataList.size() - mTtsLetterOffset) {
                                if (hasSwitchNextPage) return;
                                hasSwitchNextPage = true;
                                //后半部分
                                List<LetterData> list = letterDataList.subList(letterDataList.size() - mTtsLetterOffset, letterDataList.size());
                                readerView.setTtsLetters(list);
                                readerView.ttsNextPage();
                            } else {
                                //前半部分
                                List<LetterData> list = letterDataList.subList(0, letterDataList.size() - mTtsLetterOffset);
                                readerView.setTtsLetters(list);
                            }
                        }
                    } else {
                        readerView.setTtsLetters(letterDataList);
                    }
                }
            });
        }

        @Override
        public void onSpeechFinish(String utteranceId) {
            final int index = Integer.parseInt(utteranceId);
            //最后一段读完了 切换到下一页
            if (index == mTtsList.size() - 1) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        readerView.clearTtsLetters();
                        if (mTtsLetterOffset == 0) {
                            PageData pageData = readerView.ttsNextPage();
                            //判断是否切换章节了 并且是付费章节 就不要读了
                            if (pageData != null
                                    && pageData.getIndexOfChapter() == 0
                                    && readerView.getCurrentChapter() != null
                                    && readerView.getCurrentChapter().getIsfree() == ApiConfig.YES_NO.N) {
                                stopTts();
                            } else {
                                speak(pageData);
                            }
                        } else {
                            hasSwitchNextPage = false;//重置标志位
                            PageData pageData = readerView.getCurrentPage();
                            speak(pageData);
                        }
                    }
                });
            }
        }

        @Override
        public void onError(String s, com.baidu.tts.client.SpeechError speechError) {

        }
    };

    public void setParams() {
        // 以下参数均为选填
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, String.valueOf(IReaderPersistence.getTtsSpeaker()));
        // 设置合成的音量，0-9 ，默认 5
        // mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "5");
        // 设置合成的语速，0-9 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, String.valueOf(IReaderPersistence.getTtsSpeed()));
        // 设置合成的语调，0-9 ，默认 5
        // mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "5");

        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI);
        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线

        // 离线资源文件
        OfflineResource offlineResource = createOfflineResource(IReaderPersistence.getTtsSpeaker());
        // 声学模型文件路径 (离线引擎使用), 请确认下面两个文件存在
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, offlineResource.getTextFilename());
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, offlineResource.getModelFilename());
    }


    protected OfflineResource createOfflineResource(int speaker) {
        OfflineResource offlineResource = null;
        try {
            offlineResource = new OfflineResource(this, speaker);
        } catch (IOException e) {
            // IO 错误自行处理
            e.printStackTrace();
            Logger.e(TAG, "【error】:copy files from assets failed." + e.getMessage());
        }
        return offlineResource;
    }


    /**
     * speak 实际上是调用 synthesize后，获取音频流，然后播放。
     * 获取音频流的方式见SaveFileActivity及FileSaveListener
     * 需要合成的文本text的长度不能超过1024个GBK字节。
     */
    private void speak(PageData pageData) {
        if (pageData == null) {
            readerView.stopTts();
            return;
        }
        readerView.startTts();
        mTtsList = calculate(pageData.getLetters());
        batchSpeak();
    }

    /**
     * 只用了batchSpeak方法 批量合成
     */
    private void batchSpeak() {
        if (mTtsList == null) return;
        // 合成前可以修改参数：
        // Map<String, String> params = getParams();
        // synthesizer.setParams(params);
        List<Pair<String, Integer>> texts = new ArrayList<>();

        int count = 0;
        for (TtsBean ttsBean : mTtsList) {
            texts.add(new Pair<>(ttsBean.getContent(), count++));
        }

        List<SpeechSynthesizeBag> bags = new ArrayList<>();
        for (Pair<String, Integer> pair : texts) {
            SpeechSynthesizeBag speechSynthesizeBag = new SpeechSynthesizeBag();
            speechSynthesizeBag.setText(pair.first);
            if (pair.second != null) {
                speechSynthesizeBag.setUtteranceId(pair.second + "");
            }
            bags.add(speechSynthesizeBag);

        }
        mSpeechSynthesizer.batchSpeak(bags);
    }


    /**
     * 切换离线发音。注意需要添加额外的判断：引擎在合成时该方法不能调用
     */
    private int loadModel(int speaker) {
        OfflineResource offlineResource = createOfflineResource(speaker);
        int result = mSpeechSynthesizer.loadModel(offlineResource.getModelFilename(), offlineResource.getTextFilename());
        checkResult(result, "loadModel");
        return result;
    }

    private void checkResult(int result, String method) {
        if (result != 0) {
            Logger.e(TAG, "error code :" + result + " method:" + method + ", 错误码文档:http://yuyin.baidu.com/docs/tts/122 ");
        }
    }


    /**
     * 暂停播放。仅调用speak后生效
     */
    private void pauseTts() {
        if (isInitied && mSpeechSynthesizer != null) {
            int result = mSpeechSynthesizer.pause();
            checkResult(result, "pause");
        }
    }

    /**
     * 继续播放。仅调用speak后生效，调用pause生效
     */
    private void resumeTts() {
        if (isInitied && mSpeechSynthesizer != null) {
            int result = mSpeechSynthesizer.resume();
            checkResult(result, "resume");
        }
    }

    /*
     * 停止合成引擎。即停止播放，合成，清空内部合成队列。
     */
    private void stopTts() {
        mTtsLetterOffset = 0;
        if (mSpeechSynthesizer != null) {
            int result = mSpeechSynthesizer.stop();
            checkResult(result, "stop");
        }
        if (mReaderTtsDialog != null && mReaderTtsDialog.isShowing())
            mReaderTtsDialog.dismiss();
        readerView.stopTts();
    }


    @Override
    protected void onPause() {
        super.onPause();
        pauseTts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeTts();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSpeechSynthesizer != null) {
            int result = mSpeechSynthesizer.release();
            checkResult(result, "release");
            mSpeechSynthesizer = null;
        }
        if (myReceiver != null) {
            unregisterReceiver(myReceiver);
        }
    }


    /**
     * 显示语音合成设置Dialog
     */
    private void showTtsDialog() {
        mReaderTtsDialog = new ReaderTtsDialog(this);
        mReaderTtsDialog.setTtsSpeed(IReaderPersistence.getTtsSpeed())
                .setTtsSpeaker(IReaderPersistence.getTtsSpeaker())
                .setListener(new ReaderTtsDialog.IReaderTtsChangeListener() {
                    @Override
                    public void onTtsSpeedChange(int speed) {
                        IReaderPersistence.saveTtsSpeed(speed);
                        //语速变了 TODO 需要重新合成
                        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, String.valueOf(speed));
                        reSetParams(IReaderPersistence.getTtsSpeaker());
                    }

                    @Override
                    public void onTtsSpeakerChange(int speaker) {
                        IReaderPersistence.saveTtsSpeaker(speaker);
                        //发音人变了 TODO  需要重新合成
                        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, String.valueOf(speaker));
                        reSetParams(speaker);
                    }


                    @Override
                    public void onTtsExit() {
                        stopTts();
                    }
                });
        mReaderTtsDialog.show();
        mReaderTtsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mSpeechSynthesizer.resume();//恢复朗读
            }
        });
        mSpeechSynthesizer.pause();
    }

    /**
     * 1.语速变了  2.发音人变了
     * 以上两种情况都要重新进行语音合成
     */
    private void reSetParams(final int speaker) {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                //即停止播放，合成，清空内部合成队列。
                mSpeechSynthesizer.stop();
                e.onNext(loadModel(speaker));
                e.onComplete();
            }
        })
                .compose(RxUtils.<Integer>defaultSchedulers())
                .compose(this.<Integer>bindToLifecycle())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        checkResult(integer, "loadModel");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        //当前索引截断
                        reSpeaking();
                    }
                });
    }

    /**
     * 重新使用新配置的参数进行合成
     */
    private void reSpeaking() {
        if (mTtsList == null || mTtsList.isEmpty()) {
            stopTts();
            Logger.e(TAG, "语音合成列表为空,停止语音合成");
            return;
        }
        if (mTtsIndex < 0 || mTtsIndex >= mTtsList.size()) {
            stopTts();
            Logger.e(TAG, "索引位置不准确,停止语音合成");
            return;
        }
        List<TtsBean> newTtsList = mTtsList.subList(mTtsIndex, mTtsList.size());
        mTtsList = newTtsList;
        batchSpeak();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!readerView.isSpeaking() && !isShow) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    readerView.directNextPage();
                    return true;
                case KeyEvent.KEYCODE_VOLUME_UP:
                    readerView.directPrePage();
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 提示购买章节
     */
    private void showPayDialog(ChapterBean chapter) {
        new ReaderPayDialog(this)
                .setChapterName(chapter.getName())
                .setContent(chapter.getContent())
                .show();
        hideReaderBar();
    }
}
