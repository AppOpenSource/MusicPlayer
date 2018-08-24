package com.abt.player.di.component;

import com.abt.player.app.MusicPlayerApp;
import com.abt.player.core.DataManager;
import com.abt.player.di.module.AppModule;

import javax.inject.Singleton;

import dagger.Component;
import dagger.android.AndroidInjectionModule;
import dagger.android.support.AndroidSupportInjectionModule;

/**
 * @描述： @AppComponent
 * @作者： @黄卫旗
 * @创建时间： @25/08/2018
 */
@Singleton
@Component(modules = {AndroidInjectionModule.class,
        AndroidSupportInjectionModule.class,
        AppModule.class,
        })
public interface AppComponent {

    /**
     * 注入MusicPlayerApp实例
     *
     * @param musicPlayerApp MusicPlayerApp
     */
    void inject(MusicPlayerApp musicPlayerApp);

    /**
     * 提供App的Context
     *
     * @return MusicPlayerApp context
     */
    MusicPlayerApp getContext();

    /**
     * 数据中心
     *
     * @return DataManager
     */
    DataManager getDataManager();

}
