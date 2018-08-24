package com.abt.player.core.db;

import com.abt.player.core.dao.Music;

import java.util.List;

/**
 * @描述： @DbHelper
 * @作者： @黄卫旗
 * @创建时间： @24/08/2018
 */
public interface DbHelper {

    /**
     * 增加音乐数据
     *
     * @param data  added string
     * @return  List<Music>
     */
    List<Music> addMusic(String data);

    /**
     * 清除所有的音乐数据
     */
    void clearMusic();

    /**
     * 加载所有音乐数据
     *
     * @return List<Music>
     */
    List<Music> loadAllMusic();
}
