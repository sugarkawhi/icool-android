package com.icool.reader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.icool.reader.adapter.BookListAdapter;
import com.icool.reader.bean.BookBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadData();
    }


    protected void loadData() {
        List<BookBean> bookList = new ArrayList<>();
        BookBean book1 = new BookBean("https://img.xhhread.cn/images/covers/20180126104842802671.jpg",
                "名门盛宠：新妻太美味",
                "8aada63960e363070161086c00df5be4");
        BookBean book6 = new BookBean("https://img.xhhread.cn/images/covers/20170303180356294.png",
                "战略级天使",
                "8aada6395a597779015a898b0e530308");
        BookBean book4 = new BookBean("https://img.xhhread.cn/images/covers/20170401234510133.png",
                "主播的致命诱惑",
                "8aada6395ab2985c015afaeb0a9c0619");
        BookBean book5 = new BookBean("http://192.168.1.2:18011/images/covers/20170802155615197.jpg",
                "伊甸一姐",
                "8aada6395ab2985c015aeec4c34f03f9");
        bookList.add(book1);
        bookList.add(book6);
        bookList.add(book4);
        bookList.add(book5);
        BookListAdapter adapter = new BookListAdapter(this, bookList);
        mRecyclerView.setAdapter(adapter);
    }


}
