package com.icool.reader.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;


import com.icool.reader.R;
import com.icool.reader.activity.ReaderActivity;
import com.icool.reader.adapter.BookMarkAdapter;
import com.icool.reader.base.BaseFragment;
import com.icool.reader.component.reader.dao.BookMarkBean;
import com.icool.reader.component.reader.persistence.IReaderPersistence;
import com.icool.reader.http.RxUtils;

import java.util.List;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;


/**
 * 目录
 * Created by ZhaoZongyao on 2018/1/10.
 */

public class BookMarkFragment extends BaseFragment {

    private String mStoryId;

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    private BookMarkAdapter mBookMarkAdapter;
    private BookMarkAdapter.IBookMarkItemClickListener mBookMarkItemClickListener;

    public static BookMarkFragment newInstance() {
        return new BookMarkFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BookMarkAdapter.IBookMarkItemClickListener) {
            mBookMarkItemClickListener = (BookMarkAdapter.IBookMarkItemClickListener) context;
        } else {
            try {
                throw new Exception("未实现IBookMarkItemClickListener");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_reader;
    }

    @Override
    protected void init(View view) {
        mBookMarkAdapter = new BookMarkAdapter(getContext());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mBookMarkAdapter);
        mBookMarkAdapter.setItemClickListener(new BookMarkAdapter.IBookMarkItemClickListener() {
            @Override
            public void onBookMarkItemClick(BookMarkBean bookMark) {
                if (mBookMarkItemClickListener != null)
                    mBookMarkItemClickListener.onBookMarkItemClick(bookMark);
            }
        });
        Bundle bundle = getArguments();
        if (bundle == null) return;
        mStoryId = bundle.getString(ReaderActivity.PARAM_STORY_ID);
    }


    @Override
    protected void loadData() {
        if (TextUtils.isEmpty(mStoryId)) return;
        Observable.create(new ObservableOnSubscribe<List<BookMarkBean>>() {
            @Override
            public void subscribe(ObservableEmitter<List<BookMarkBean>> e) throws Exception {
                e.onNext(IReaderPersistence.queryBookMarkList(mStoryId));
                e.onComplete();
            }
        })
                .compose(RxUtils.<List<BookMarkBean>>defaultSchedulers())
                .compose(this.<List<BookMarkBean>>bindToLifecycle())
                .subscribe(new io.reactivex.observers.DefaultObserver<List<BookMarkBean>>() {
                    @Override
                    public void onNext(List<BookMarkBean> bookMarkBeans) {
                        mBookMarkAdapter.setData(bookMarkBeans);
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
     * 刷新书签页面
     */
    public void refresh() {
        loadData();
    }
}
