package com.icool.reader.component.reader.element;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * 页尾部分：绘制每一页的页尾,包括进度，时间和电量。
 * Created by ZhaoZongyao on 2018/1/11.
 */

public class BookMarkElement extends Element {

    private Bitmap mBookmarkBitmap;
    private int mBookmarkWidth;
    private int mReaderWidth;

    public BookMarkElement(Bitmap bitmap, int readerWidth) {
        mBookmarkBitmap = bitmap;
        mBookmarkWidth = mBookmarkBitmap.getWidth();
        mReaderWidth = readerWidth;
    }

    @Override
    public boolean onDraw(Canvas canvas) {
        canvas.drawBitmap(mBookmarkBitmap, mReaderWidth - mBookmarkWidth, 0, null);
        return false;
    }
}
