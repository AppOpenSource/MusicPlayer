package com.abt.player.core.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;

/**
 * @描述： @Music
 * @作者： @黄卫旗
 * @创建时间： @25/08/2018
 */
@Entity
public class Music {

    @Id(autoincrement = true)
    private long _id;
    private int music_id;
    private int clicks;
    @NotNull
    private String latest;
    @Generated(hash = 1017967170)
    public Music(long _id, int music_id, int clicks, @NotNull String latest) {
        this._id = _id;
        this.music_id = music_id;
        this.clicks = clicks;
        this.latest = latest;
    }
    @Generated(hash = 1263212761)
    public Music() {
    }
    public long get_id() {
        return this._id;
    }
    public void set_id(long _id) {
        this._id = _id;
    }
    public int getMusic_id() {
        return this.music_id;
    }
    public void setMusic_id(int music_id) {
        this.music_id = music_id;
    }
    public int getClicks() {
        return this.clicks;
    }
    public void setClicks(int clicks) {
        this.clicks = clicks;
    }
    public String getLatest() {
        return this.latest;
    }
    public void setLatest(String latest) {
        this.latest = latest;
    }

}
