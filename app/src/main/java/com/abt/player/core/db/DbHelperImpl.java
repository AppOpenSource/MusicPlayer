package com.abt.player.core.db;

import com.abt.player.app.MusicPlayerApp;
import com.abt.player.core.dao.DaoSession;
import com.abt.player.core.dao.Music;
import com.abt.player.core.dao.MusicDao;

import java.util.Iterator;
import java.util.List;

/**
 * @描述： @DbHelperImpl
 * @作者： @黄卫旗
 * @创建时间： @24/08/2018
 */
public class DbHelperImpl implements DbHelper {

    private static final int HISTORY_LIST_SIZE = 10;

    private DaoSession daoSession;
    private List<Music> musicList;
    private String data;
    private Music music;

    @Inject
    DbHelperImpl() {
        daoSession = MusicPlayerApp.getInstance().getDaoSession();
    }

    @Override
    public List<Music> addMusic(String data) {
        this.data = data;
        getHistoryDataList();
        createHistoryData();
        if (historyDataForward()) {
            return musicList;
        }

        if (musicList.size() < HISTORY_LIST_SIZE) {
            getHistoryDataDao().insert(music);
        } else {
            musicList.remove(0);
            musicList.add(music);
            getHistoryDataDao().deleteAll();
            getHistoryDataDao().insertInTx(musicList);
        }
        return musicList;
    }

    @Override
    public void clearMusic() {
        daoSession.getMusicDao().deleteAll();
    }

    @Override
    public List<Music> loadAllMusic() {
        return daoSession.getMusicDao().loadAll();
    }

    /**
     * 历史数据前移
     *
     * @return 返回true表示查询的数据已存在，只需将其前移到第一项历史记录，否则需要增加新的历史记录
     */
    private boolean historyDataForward() {
        //重复搜索时进行历史记录前移
        Iterator<Music> iterator = musicList.iterator();
        //不要在foreach循环中进行元素的remove、add操作，使用Iterator模式
        while (iterator.hasNext()) {
            Music historyData1 = iterator.next();
            /*if (historyData1.getData().equals(data)) {
                musicList.remove(historyData1);
                musicList.add(music);
                getHistoryDataDao().deleteAll();
                getHistoryDataDao().insertInTx(musicList);
                return true;
            }*/ // TODO
        }
        return false;
    }

    private void getHistoryDataList() {
        musicList = getHistoryDataDao().loadAll();
    }

    private void createHistoryData() {
        music = new Music();
        //music.setDate(System.currentTimeMillis());
        //music.setData(data); // TODO
    }

    private MusicDao getHistoryDataDao() {
        return daoSession.getMusicDao();
    }

}
