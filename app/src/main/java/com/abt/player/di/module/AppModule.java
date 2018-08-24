package com.abt.player.di.module;

import com.abt.player.app.MusicPlayerApp;
import com.abt.player.core.DataManager;
import com.abt.player.core.db.DbHelper;
import com.abt.player.core.db.DbHelperImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * @描述： @AppModule
 * @作者： @黄卫旗
 * @创建时间： @25/08/2018
 */
@Module
public class AppModule {

    private final MusicPlayerApp application;

    public AppModule(MusicPlayerApp application) {
        this.application = application;
    }

    @Provides
    @Singleton
    MusicPlayerApp provideApplicationContext() {
        return application;
    }

/*
    @Provides
    @Singleton
    HttpHelper provideHttpHelper(HttpHelperImpl httpHelperImpl) {
        return httpHelperImpl;
    }
*/

    @Provides
    @Singleton
    DbHelper provideDBHelper(DbHelperImpl realmHelper) {
        return realmHelper;
    }

/*    @Provides
    @Singleton
    PreferenceHelper providePreferencesHelper(PreferenceHelperImpl implPreferencesHelper) {
        return implPreferencesHelper;
    }*/

    @Provides
    @Singleton
    DataManager provideDataManager(/*HttpHelper httpHelper, */DbHelper dbhelper/*, PreferenceHelper preferencesHelper*/) {
        return new DataManager(/*httpHelper, */dbhelper/*, preferencesHelper*/);
    }

}
