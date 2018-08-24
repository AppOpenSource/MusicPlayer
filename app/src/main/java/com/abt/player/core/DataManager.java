package com.abt.player.core;

import com.abt.player.core.dao.Music;
import com.abt.player.core.db.DbHelper;

import java.util.List;

/**
 * @描述： @DataManager
 * @作者： @黄卫旗
 * @创建时间： @24/08/2018
 */
public class DataManager implements DbHelper {

    private DbHelper mDbHelper;

    public DataManager(DbHelper dbHelper) {
        mDbHelper = dbHelper;
    }

    @Override
    public List<Music> addMusic(String data) {
        return mDbHelper.addMusic(data);
    }

    @Override
    public void clearMusic() {
        mDbHelper.clearMusic();
    }

    @Override
    public List<Music> loadAllMusic() {
        return mDbHelper.loadAllMusic();
    }

}
