package com.icool.reader.base;

import android.app.Application;
import android.content.Context;

import com.icool.reader.gen.DaoMaster;
import com.icool.reader.gen.DaoSession;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import org.greenrobot.greendao.database.Database;

/**
 * APP
 * Created by ZhaoZongyao on 2018/3/28.
 */

public class IcoolApplication extends Application {
    private static IcoolApplication application;
    /**
     * A flag to show how easily you can switch from standard SQLite to the encrypted SQLCipher.
     */
    public static final boolean ENCRYPTED = true;
    private DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        setupLeakCanary();
        initGreenDao();
    }

    public static IcoolApplication getInstance() {
        return application;
    }

    protected void setupLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        refWatcher = LeakCanary.install(this);
    }


    private RefWatcher refWatcher;

    public static RefWatcher getRefWatcher(Context context) {
        IcoolApplication leakApplication = (IcoolApplication) context.getApplicationContext();
        return leakApplication.refWatcher;
    }

    private void initGreenDao() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, ENCRYPTED ? "notes-db-encrypted" : "notes-db");
        Database db = ENCRYPTED ? helper.getEncryptedWritableDb("super-secret") : helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }
}
