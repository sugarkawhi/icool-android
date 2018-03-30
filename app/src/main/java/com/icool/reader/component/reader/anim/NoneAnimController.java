package com.icool.reader.component.reader.anim;


import android.graphics.Canvas;

import com.icool.reader.component.reader.element.PageElement;
import com.icool.reader.component.reader.view.ReaderView;


/**
 * None page anim controller
 * Created by ZhaoZongyao on 2018/1/11.
 */

public class NoneAnimController extends PageAnimController {


    public NoneAnimController(ReaderView readerView, int readerWidth, int readerHeight, PageElement pageElement, IPageChangeListener pageChangeListener) {
        super(readerView, readerWidth, readerHeight, pageElement, pageChangeListener);
    }


    @Override
    void drawMove(Canvas canvas) {
        canvas.drawBitmap(mCurrentBitmap, 0, 0, null);
    }

}
