package com.icool.reader.component.reader.anim;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import com.icool.reader.component.reader.config.IReaderConfig;
import com.icool.reader.component.reader.config.IReaderDirection;
import com.icool.reader.component.reader.element.PageElement;
import com.icool.reader.component.reader.listener.IReaderTouchListener;
import com.icool.reader.component.reader.utils.ReaderLogger;
import com.icool.reader.component.reader.view.ReaderView;

import static com.icool.reader.component.reader.config.IReaderConfig.DURATION_PAGE_SWITCH;


/**
 * page anim controller
 * Created by ZhaoZongyao on 2018/1/11.
 */

public abstract class PageAnimController {
    public String TAG = getClass().getSimpleName();

    protected ReaderView mReaderView;
    protected PageElement mPageElement;

    //阅读器宽高
    public int mReaderWidth, mReaderHeight;
    //中间区域
    private Rect mCenterRect;
    //右侧区域
    private Rect mRightRect;
    //左侧区域
    private Rect mLeftRect;
    //是否处于滑动状态
    protected boolean isMoveState;
    //翻下页 判断是否有下页、翻上页 判断是否有上页
    private boolean hasNextOrPre = true;
    //是否处于滚动状态
    protected boolean isScroll;
    //是否取消翻页
    protected boolean isCancel;
    //滑动方向
    protected int mDirection = IReaderDirection.NONE;
    //View滚动
    protected Scroller mScroller;
    //滑动的最小距离
    protected float mTouchSlop;

    /**
     * 一屏中至多显示两页
     * mCurrentBitmap 为 左边页
     * mNextBitmap 为右边页
     */
    public Bitmap mCurrentBitmap;
    public Bitmap mNextBitmap;


    //按下开始坐标
    protected int mStartX, mStartY;
    //滑动当前坐标
    protected float mTouchX, mTouchY;
    //判断当前是否取消坐标 - 类似于mTouchX,mTouchY。
    protected int mMoveX, mMoveY;


    private IPageChangeListener mPageChangeListener;
    private IReaderTouchListener mIReaderTouchListener;


    public PageAnimController(ReaderView readerView, int readerWidth, int readerHeight, PageElement pageElement, IPageChangeListener pageChangeListener) {
        this.mReaderView = readerView;
        this.mPageElement = pageElement;
        this.mReaderWidth = readerWidth;
        this.mReaderHeight = readerHeight;
        mPageChangeListener = pageChangeListener;

        mLeftRect = new Rect(0, 0, readerWidth / 4, readerHeight + 10);//+10处理边缘情况
        mRightRect = new Rect(readerWidth / 4 * 3, 0, readerWidth + 10, readerHeight + 10);//同上
        mCenterRect = new Rect(readerWidth / 4, 0, readerWidth / 4 * 3, readerHeight + 10);//同上
        mCurrentBitmap = Bitmap.createBitmap(readerWidth, readerHeight, Bitmap.Config.RGB_565);
        mNextBitmap = Bitmap.createBitmap(readerWidth, readerHeight, Bitmap.Config.RGB_565);
        mScroller = new Scroller(mReaderView.getContext(), new LinearInterpolator());
        mTouchSlop = ViewConfiguration.get(readerView.getContext()).getScaledTouchSlop();
    }

    public void setIReaderTouchListener(IReaderTouchListener listener) {
        mIReaderTouchListener = listener;
    }

    protected void drawStatic(Canvas canvas) {
        canvas.drawBitmap(mCurrentBitmap, 0, 0, null);
    }


    abstract void drawMove(Canvas canvas);

    protected void startScroll() {
        isScroll = true;
        float dx = 0;
        switch (mDirection) {
            case IReaderDirection.NEXT:
                if (isCancel) {
                    if (mStartX <= mTouchX) return;
                    dx = mStartX - mTouchX;
                } else {
                    dx = -(mReaderWidth - (mStartX - mTouchX));
                }
                break;
            case IReaderDirection.PRE:
                if (isCancel) {
                    if (mStartX >= mTouchX) return;
                    dx = mStartX - mTouchX;
                } else {
                    dx = mReaderWidth - (mTouchX - mStartX);
                }
                break;
        }
        int duration = 0;
        if (mReaderView.getPageMode() != IReaderConfig.PageMode.NONE) {
            duration = DURATION_PAGE_SWITCH;
        }
        duration = (int) (duration * Math.abs(dx) / mReaderWidth);
        mScroller.startScroll((int) mTouchX, 0, (int) dx, 0, duration);
        mReaderView.invalidate();
    }


    /**
     * drawPage 绘制页面
     */
    public void dispatchDrawPage(Canvas canvas) {
        if (isScroll) {
            drawMove(canvas);
        } else {
            drawStatic(canvas);
        }
    }

    /**
     * 分发事件
     */
    public boolean dispatchTouchEvent(MotionEvent event) {
        //非打开书籍状态
        if (!mReaderView.isOpening()) {
            return true;
        }
        if (mIReaderTouchListener != null && !mIReaderTouchListener.canTouch()) {
            if (event.getAction() == MotionEvent.ACTION_UP) mIReaderTouchListener.onTouchCenter();
            return true;
        }
        int x = (int) event.getX();
        int y = (int) event.getY();

        setTouchPoint(x, y);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                ReaderLogger.e(TAG, "dispatchTouchEvent ACTION_DOWN");
                //NOTICE:务必在最前面调用
                abortAnim();
                setStartPoint(x, y);
                mMoveX = 0;
                mMoveY = 0;
                //判断是否有上下页  默认有上/下页
                hasNextOrPre = true;
                //正在滚动 - false
                isScroll = false;
                //是否取消 - false
                isCancel = false;
                //是否是滑动状态
                isMoveState = false;
                return true;
            case MotionEvent.ACTION_MOVE:
                ReaderLogger.e(TAG, "dispatchTouchEvent ACTION_MOVE");
                //如果不在滑动状态 判断是否需要进入滑动状态
                if (!isMoveState) isMoveState = Math.abs(mTouchX - mStartX) > mTouchSlop;
                //如果不构成滑动 不处理
                if (!isMoveState) return true;
                //自己处理滑动事件 父View不要拦截
                if (mReaderView.getParent() != null)
                    mReaderView.getParent().requestDisallowInterceptTouchEvent(true);
                //构成滑动第一次 处理事件：1.判断方向2.根据方向判断存在上/下页进行回调
                if (mMoveX == 0 && mMoveY == 0) {
                    ReaderLogger.e(TAG, "MotionEvent1.构成滑动 判断方向 x=" + x + " mStartX=" + mStartX);
                    //上一页
                    if (x - mStartX > 0) {
                        ReaderLogger.e(TAG, "MotionEvent2.方向为上一页");
                        //处理事件1 判断方向
                        setDirection(IReaderDirection.PRE);
                        //处理事件2 判断存在上页
                        boolean hasPre = hasPre();
                        if (!hasPre) {//不存在上一页
                            ReaderLogger.e(TAG, "不存在上一页");
                            hasNextOrPre = false;
                            return true;
                        }
                    } else {
                        ReaderLogger.e(TAG, "MotionEvent2.方向为下一页");
                        //处理事件1 判断方向
                        setDirection(IReaderDirection.NEXT);
                        //处理事件2 判断存在下页
                        boolean hasPre = hasNext();
                        if (!hasPre) {//不存在下一页
                            ReaderLogger.e(TAG, "不存在下一页");
                            hasNextOrPre = false;
                            return true;
                        }
                    }
                }
                //开始滑动 这时候已经确定了方向
                // 处理事件：根据翻页方向判断是否取消翻页了
                else {
                    ReaderLogger.e(TAG, "MotionEvent3.开始滑动");
                    switch (mDirection) {
                        case IReaderDirection.NEXT:
                            isCancel = x > mMoveX;
                            ReaderLogger.e(TAG, "下一页：isCancel=" + isCancel);
                            break;
                        case IReaderDirection.PRE:
                            ReaderLogger.e(TAG, "上一页：isCancel=" + isCancel);
                            isCancel = x < mMoveX;
                            break;
                    }
                }
                mMoveX = x;
                mMoveY = y;
                isScroll = true;
                mReaderView.invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                ReaderLogger.e(TAG, "dispatchTouchEvent ACTION_UP");
                //i:非滑动状态 点击屏幕 1.中间2.左侧3.右侧
                if (!isMoveState) {
                    if (mCenterRect.contains((int) mTouchX, (int) mTouchY)) {
                        //点击中间区域
                        if (mIReaderTouchListener != null) mIReaderTouchListener.onTouchCenter();
                        return true;
                    } else if (mLeftRect.contains((int) mTouchX, (int) mTouchY)) {
                        //上一页
                        setDirection(IReaderDirection.PRE);
                        boolean hasPre = hasPre();
                        if (!hasPre) {
                            return true;
                        }
                    } else if (mRightRect.contains((int) mTouchX, (int) mTouchY)) {
                        //下一页
                        setDirection(IReaderDirection.NEXT);
                        boolean hasNext = hasNext();
                        if (!hasNext) {//不存在下一页
                            return true;
                        }
                    }
                }
                //ii：滑动状态下
                // 1.处理是否取消的情况2.有无上下页的情况
                if (isCancel) {
                    ReaderLogger.e(TAG, "ACTION_UP isCancel = " + isCancel);
                    mPageChangeListener.onCancel(mDirection);
                }
                ReaderLogger.e(TAG, "ACTION_UP hasNextOrPre = " + hasNextOrPre);
                if (hasNextOrPre) {
                    startScroll();
                    mReaderView.invalidate();
                }
                return true;

        }
        return true;
    }

    /**
     * 回调刷新
     */
    public void computeScroll() {
        boolean notFinished = mScroller.computeScrollOffset();
        //ReaderLogger.e(TAG, "computeScroll  computeScrollOffset -> " + notFinished);
        if (notFinished) {
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();
            setTouchPoint(x, y);
            if (isScroll && mScroller.getFinalX() == x && mScroller.getFinalY() == y) {
                isScroll = false;
                mPageChangeListener.onSelectPage(mDirection, isCancel);
            }
            mReaderView.invalidate();
        }
    }

    /**
     * 是否有上一页
     */
    private boolean hasPre() {
        return mPageChangeListener.hasPre();
    }


    /**
     * 是否有下一页
     */
    private boolean hasNext() {
        return mPageChangeListener.hasNext();
    }

    /**
     * 结束动画
     */
    private void abortAnim() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
            isScroll = false;
            int x = mScroller.getFinalX();
            int y = mScroller.getFinalY();
            setTouchPoint(x, y);
            mPageChangeListener.onSelectPage(mDirection, isCancel);
            mReaderView.invalidate();
            ReaderLogger.i(TAG, "abortAnim");
        }
    }


    /**
     * 获取下一页Bitmap
     */
    public Bitmap getNextBitmap() {
        return mNextBitmap;
    }

    /**
     * 获取当前页的Bitmap
     */
    public Bitmap getCurrentBitmap() {
        return mCurrentBitmap;
    }

    /**
     * 直接进入下一页
     * {@link #startScroll} 方法来控制动画
     */
    public boolean directNextPage() {
        if (isScroll) abortAnim();
        int startX, startY;
        float touchX, touchY;
        if (mReaderView.getPageMode() == IReaderConfig.PageMode.SIMULATION) {
            startX = mReaderWidth - 10;//设置在右边的一个范围即可
            startY = mReaderHeight;
            touchX = mReaderWidth - 10;
            touchY = mReaderHeight;
        } else {
            startX = 0;
            startY = 0;
            touchX = 0;
            touchY = 0;
        }
        setStartPoint(startX, startY);
        setTouchPoint(touchX, touchY);
        boolean hasNext = hasNext();
        if (hasNext) {
            setDirection(IReaderDirection.NEXT);
            startScroll();
        }
        return hasNext;
    }

    /**
     * 直接进入上一页
     * {@link #startScroll} 方法来控制动画
     */
    public boolean directPrePage() {
        if (isScroll) abortAnim();
        setStartPoint(0, 0);
        setTouchPoint(0, 0);
        boolean hasPre = hasPre();
        if (hasPre) {
            setDirection(IReaderDirection.PRE);
            startScroll();
        }
        return hasPre;
    }

    /**
     * 设置方向
     *
     * @param direction 方向
     */
    protected void setDirection(int direction) {
        mDirection = direction;
    }


    /**
     * 设置起始点
     *
     * @param x
     * @param y
     */
    public void setStartPoint(int x, int y) {
        mStartX = x;
        mStartY = y;
    }

    public void setTouchPoint(float x, float y) {
        mTouchX = x;
        mTouchY = y;
    }

    public interface IPageChangeListener {

        /**
         * 取消
         *
         * @param direction 方向
         */
        void onCancel(int direction);

        /**
         * 选中
         *
         * @param direction 方向
         * @param isCancel  是否取消
         */
        void onSelectPage(int direction, boolean isCancel);

        /**
         * 下一页
         *
         * @return t/f
         */
        boolean hasPre();

        /**
         * 上一页
         *
         * @return t/f
         */
        boolean hasNext();

    }
}
