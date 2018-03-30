package com.icool.reader.component.reader.dialog;

import android.content.Context;
import android.widget.SeekBar;


import com.icool.reader.R;
import com.icool.reader.component.reader.config.IReaderConfig;
import com.icool.reader.component.reader.persistence.IReaderPersistence;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 设置 间距
 * Created by ZhaoZongyao on 2018/1/30.
 */

public class ReaderSpacingDialog extends BottomPopDialog {
    @BindView(R.id.reader_seekBar_letterSpacing)
    SeekBar mLetterSeekBar;
    @BindView(R.id.reader_seekBar_linerSpacing)
    SeekBar mLineSeekBar;
    @BindView(R.id.reader_seekBar_paragraphSpacing)
    SeekBar mParagraphSeekBar;

    private IReaderSpacingChangeListener mSpacingChangeListener;

    private int letterOffset = IReaderConfig.LetterSpacing.MAX - IReaderConfig.LetterSpacing.MIN;
    private int lineOffset = IReaderConfig.LineSpacing.MAX - IReaderConfig.LineSpacing.MIN;
    private int paragraphOffset = IReaderConfig.ParagraphSpacing.MAX - IReaderConfig.ParagraphSpacing.MIN;

    public ReaderSpacingDialog(Context context) {
        super(context);
        ButterKnife.bind(this, getContentView());
        init();
        setListener();
    }

    private void init() {
        int letterSpacing = IReaderPersistence.getLetterSpacing();
        int letterProgress = (int) ((letterSpacing - IReaderConfig.LetterSpacing.MIN) * 1f / letterOffset * 100);
        mLetterSeekBar.setProgress(letterProgress);

        int lineSpacing = IReaderPersistence.getLineSpacing();
        int lineProgress = (int) ((lineSpacing - IReaderConfig.LineSpacing.MIN) * 1f / lineOffset * 100);
        mLineSeekBar.setProgress(lineProgress);

        int paragraphSpcing = IReaderPersistence.getParagraphSpacing();
        int paragraphProgress = (int) ((paragraphSpcing - IReaderConfig.ParagraphSpacing.MIN) * 1f / paragraphOffset * 100);
        mParagraphSeekBar.setProgress(paragraphProgress);
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_reader_spacing;
    }

    private void setListener() {
        mLetterSeekBar.setOnSeekBarChangeListener(new MySeekBarChangeListener(mLetterSeekBar));
        mLineSeekBar.setOnSeekBarChangeListener(new MySeekBarChangeListener(mLineSeekBar));
        mParagraphSeekBar.setOnSeekBarChangeListener(new MySeekBarChangeListener(mParagraphSeekBar));
    }

    public ReaderSpacingDialog setSpacingChangeListener(IReaderSpacingChangeListener spacingChangeListener) {
        mSpacingChangeListener = spacingChangeListener;
        return this;
    }

    private class MySeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        private SeekBar mSeekBar;

        MySeekBarChangeListener(SeekBar seekBar) {
            mSeekBar = seekBar;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (mSpacingChangeListener == null) return;
            int progress = seekBar.getProgress();
            switch (mSeekBar.getId()) {
                case R.id.reader_seekBar_letterSpacing:
                    int letterSpacing = (int) (progress / 100f * letterOffset) + IReaderConfig.LetterSpacing.MIN;
                    mSpacingChangeListener.onLetterSpacingChange(letterSpacing);
                    IReaderPersistence.saveLetterSpacing(letterSpacing);
                    break;
                case R.id.reader_seekBar_linerSpacing:
                    int lineSpacing = (int) (progress / 100f * lineOffset) + IReaderConfig.LineSpacing.MIN;
                    mSpacingChangeListener.onLineSpacingChange(lineSpacing);
                    IReaderPersistence.saveLineSpacing(lineSpacing);
                    break;
                case R.id.reader_seekBar_paragraphSpacing:
                    int paragraphSpacing = (int) (progress / 100f * paragraphOffset) + IReaderConfig.ParagraphSpacing.MIN;
                    mSpacingChangeListener.onParagraphSpacingChange(paragraphSpacing);
                    IReaderPersistence.saveParagraphSpacing(paragraphSpacing);
                    break;
            }
        }
    }

    public interface IReaderSpacingChangeListener {
        void onLetterSpacingChange(int letterSpacing);

        void onLineSpacingChange(int lineSpacing);

        void onParagraphSpacingChange(int paragraphSpacing);
    }

}
