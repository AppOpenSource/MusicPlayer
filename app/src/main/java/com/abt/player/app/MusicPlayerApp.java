package com.abt.player.app;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.abt.player.core.dao.DaoMaster;
import com.abt.player.core.dao.DaoSession;
import com.abt.player.di.component.DaggerAppComponent;
import com.abt.player.di.module.AppModule;

/**
 * @描述： @MusicPlayerApp
 * @作者： @黄卫旗
 * @创建时间： @25/08/2018
 */
public class MusicPlayerApp extends Application {

    private DaoSession mDaoSession;
    private static MusicPlayerApp instance;

    public static synchronized MusicPlayerApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DaggerAppComponent.builder()
                .appModule(new AppModule(instance))
                .build().inject(this);
        instance = this;

        initGreenDao();
    }

    private void initGreenDao() {
        DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(this, Constants.DB_NAME);
        SQLiteDatabase database = devOpenHelper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(database);
        mDaoSession = daoMaster.newSession();
    }

    public DaoSession getDaoSession() {
        return mDaoSession;
    }
}
