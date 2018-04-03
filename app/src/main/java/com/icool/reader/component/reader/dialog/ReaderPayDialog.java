package com.icool.reader.component.reader.dialog;

import android.content.Context;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.icool.reader.R;
import com.icool.reader.component.reader.config.IReaderConfig;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

/**
 * 阅读器收费
 * Created by ZhaoZongyao on 2018/2/28.
 */

public class ReaderPayDialog extends BottomPopDialog {

    @BindView(R.id.tv_chapterName)
    TextView mTvChapterName;
    @BindView(R.id.tv_content)
    TextView mTvContent;

    private IReaderPayListener mListener;

    public ReaderPayDialog(Context context) {
        super(context);
        ButterKnife.bind(this, getContentView());
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_reader_pay;
    }

    public ReaderPayDialog setListener(IReaderPayListener listener) {
        mListener = listener;
        return this;
    }

    public ReaderPayDialog setChapterName(String chapterName) {
        mTvChapterName.setText(chapterName);
        return this;
    }

    public ReaderPayDialog setContent(String content) {
        mTvContent.setText(content);
        return this;
    }


    @OnClick(R.id.btn_cancel)
    public void cancel() {
        dismiss();
    }

    @OnClick(R.id.btn_buy)
    public void buy() {
        //TODO 购买
        dismiss();
    }

    /**
     * 语音合成设置回调
     */
    public interface IReaderPayListener {

        void onTtsExit(); //退出语音朗读
    }
}
