package com.icool.reader.component.reader.anim;

import android.graphics.Canvas;

import com.icool.reader.component.reader.element.PageElement;
import com.icool.reader.component.reader.view.ReaderView;

/**
 * 仿真
 * Created by ZhaoZongyao on 2018/3/30.
 */

public class SimulateAnimController extends PageAnimController {

    public SimulateAnimController(ReaderView readerView, int readerWidth, int readerHeight, PageElement pageElement, IPageChangeListener pageChangeListener) {
        super(readerView, readerWidth, readerHeight, pageElement, pageChangeListener);
    }

    @Override
    void drawStatic(Canvas canvas) {

    }

    @Override
    void drawMove(Canvas canvas) {

    }
}
