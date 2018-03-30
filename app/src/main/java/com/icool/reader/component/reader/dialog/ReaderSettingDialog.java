package com.icool.reader.component.reader.dialog;

import android.content.Context;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;


import com.icool.reader.R;
import com.icool.reader.component.reader.config.IReaderConfig;
import com.icool.reader.component.reader.persistence.IReaderPersistence;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

import static com.icool.reader.component.reader.config.IReaderConfig.PageMode.COVER;
import static com.icool.reader.component.reader.config.IReaderConfig.PageMode.NONE;
import static com.icool.reader.component.reader.config.IReaderConfig.PageMode.SIMULATION;
import static com.icool.reader.component.reader.config.IReaderConfig.PageMode.SLIDE;

/**
 * 设置字体
 * Created by ZhaoZongyao on 2018/1/30.
 */

public class ReaderSettingDialog extends BottomPopDialog {

    private IReaderSettingListener mSettingListener;
    @BindView(R.id.reader_fontSize)
    TextView readerFontSize;
    //background
    @BindView(R.id.reader_bg_blue)
    RadioButton readerBgBlue;
    @BindView(R.id.reader_bg_purple)
    RadioButton readerBgPurple;
    @BindView(R.id.reader_bg_default)
    RadioButton readerBgDefault;
    @BindView(R.id.reader_bg_matcha)
    RadioButton readerBgMatcha;
    @BindView(R.id.reader_bg_night)
    RadioButton readerBgNight;
    //page mode
    @BindView(R.id.reader_mode_cover)
    RadioButton readerCover;
    @BindView(R.id.reader_mode_slide)
    RadioButton readerSlide;
    @BindView(R.id.reader_mode_simulation)
    RadioButton readerSimulation;
    @BindView(R.id.reader_mode_none)
    RadioButton readerNone;
    //typeface
    @BindView(R.id.reader_tf_default)
    RadioButton readerTfDefault;
    @BindView(R.id.reader_tf_cartoon)
    RadioButton readerTfCartoon;
    @BindView(R.id.reader_tf_songti)
    RadioButton readerTfSongti;
    @BindView(R.id.reader_tf_fanti)
    RadioButton readerTfFanti;

    public ReaderSettingDialog(Context context) {
        super(context);
        ButterKnife.bind(this, getContentView());
        init();
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_reader_setting;
    }

    /**
     * 初始化操作
     */
    private void init() {
        int fontSize = IReaderPersistence.getFontSize();
        readerFontSize.setText(String.valueOf(fontSize));
        int mode = IReaderPersistence.getPageMode();
        switch (mode) {
            case COVER:
                readerCover.setChecked(true);
                break;
            case SLIDE:
                readerSlide.setChecked(true);
                break;
            case SIMULATION:
                readerSimulation.setChecked(true);
                break;
            case NONE:
                readerNone.setChecked(true);
                break;
        }
        int background = IReaderPersistence.getBackground();
        switch (background) {
            case IReaderConfig.Background.IMAGE_BLUE:
                readerBgBlue.setChecked(true);
                break;
            case IReaderConfig.Background.IMAGE_PURPLE:
                readerBgPurple.setChecked(true);
                break;
            case IReaderConfig.Background.DEFAULT:
                readerBgDefault.setChecked(true);
                break;
            case IReaderConfig.Background.COLOR_MATCHA:
                readerBgMatcha.setChecked(true);
                break;
            case IReaderConfig.Background.NIGHT:
                readerBgNight.setChecked(true);
                break;
        }
        int typeface = IReaderPersistence.getTypeface();
        switch (typeface) {
            case IReaderConfig.Typeface.DEFAULT:
                readerTfDefault.setChecked(true);
                break;
            case IReaderConfig.Typeface.CARTOON:
                readerTfCartoon.setChecked(true);
                break;
            case IReaderConfig.Typeface.FANTI:
                readerTfFanti.setChecked(true);
                break;
            case IReaderConfig.Typeface.SONGTI:
                readerTfSongti.setChecked(true);
                break;
        }
    }

    public ReaderSettingDialog setSettingListener(IReaderSettingListener settingListener) {
        mSettingListener = settingListener;
        return this;
    }

    /**
     * 文字变大
     */
    @OnClick(R.id.reader_font_big)
    public void fontBig() {
        int currentFont = IReaderPersistence.getFontSize();
        currentFont++;

        readerFontSize.setText(String.valueOf(currentFont));
        if (mSettingListener != null) mSettingListener.onFontSizeChange(currentFont);
    }

    /**
     * 文字变小
     */
    @OnClick(R.id.reader_font_small)
    public void fontSmall() {
        int currentFont = IReaderPersistence.getFontSize();
        currentFont--;
        readerFontSize.setText(String.valueOf(currentFont));
        if (mSettingListener != null) mSettingListener.onFontSizeChange(currentFont);
    }

    /**
     * 选择背景
     */
    @OnCheckedChanged({R.id.reader_bg_blue, R.id.reader_bg_purple, R.id.reader_bg_default, R.id.reader_bg_matcha, R.id.reader_bg_night})
    public void selectBg(CompoundButton view, boolean isChecked) {
        if (!isChecked) return;
        int background;
        switch (view.getId()) {
            case R.id.reader_bg_blue:
                background = (IReaderConfig.Background.IMAGE_BLUE);
                break;
            case R.id.reader_bg_purple:
                background = (IReaderConfig.Background.IMAGE_PURPLE);
                break;
            case R.id.reader_bg_matcha:
                background = (IReaderConfig.Background.COLOR_MATCHA);
                break;
            case R.id.reader_bg_night:
                background = (IReaderConfig.Background.NIGHT);
                break;
            case R.id.reader_bg_default:
            default:
                background = (IReaderConfig.Background.DEFAULT);
                break;
        }
        IReaderPersistence.saveBackground(background);
        if (mSettingListener != null) mSettingListener.onBackgroundChange(background);

    }

    /**
     * 选择翻页模式
     */
    @OnCheckedChanged({R.id.reader_mode_cover, R.id.reader_mode_slide, R.id.reader_mode_simulation, R.id.reader_mode_none})
    public void selectMode(CompoundButton view, boolean isChecked) {
        if (!isChecked) return;
        int pageMode = COVER;
        switch (view.getId()) {
            case R.id.reader_mode_cover:
                pageMode = COVER;
                break;
            case R.id.reader_mode_slide:
                pageMode = SLIDE;
                break;
            case R.id.reader_mode_simulation:
                pageMode = SIMULATION;
                break;
            case R.id.reader_mode_none:
                pageMode = NONE;
                break;
        }
        IReaderPersistence.savePageMode(pageMode);
        if (mSettingListener != null) mSettingListener.onPageModeChange(pageMode);

    }

    /**
     * 选择翻页模式
     */
    @OnCheckedChanged({R.id.reader_tf_default, R.id.reader_tf_fanti, R.id.reader_tf_cartoon, R.id.reader_tf_songti})
    public void selectTypeface(CompoundButton view, boolean isChecked) {
        if (!isChecked) return;
        int typeface;
        switch (view.getId()) {
            case R.id.reader_tf_fanti:
                typeface = IReaderConfig.Typeface.FANTI;
                break;
            case R.id.reader_tf_cartoon:
                typeface = IReaderConfig.Typeface.CARTOON;
                break;
            case R.id.reader_tf_songti:
                typeface = IReaderConfig.Typeface.SONGTI;
                break;
            case R.id.reader_tf_default:
            default:
                typeface = IReaderConfig.Typeface.DEFAULT;
                break;
        }
        IReaderPersistence.saveTypeface(typeface);
        if (mSettingListener != null) mSettingListener.onTypefaceChange(typeface);

    }


    public interface IReaderSettingListener {
        void onFontSizeChange(int fontSize);

        void onBackgroundChange(int background);

        void onPageModeChange(int pageMode);

        void onTypefaceChange(int typeface);
    }
}
