package com.abt.player.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;

import com.abt.player.DBHelper;
import com.abt.player.R;
import com.abt.player.adapter.MusicListAdapter;

public class ClicksActivity extends AppCompatActivity {

    private DBHelper mDbHelper = null;
    private ListView mListView;
    private String[] _titles;
    private Cursor mCursor = null;
    private AudioManager mAudioManager = null;
    private int[] _ids;
    private int[] mMusicId;
    private int mMaxVolume;
    private int mCurrentVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        mDbHelper = new DBHelper(this, "music.db", null, 2);
        mCursor = mDbHelper.queryByClicks();
        mCursor.moveToFirst();
        int num;
        if (mCursor != null) {
            num = mCursor.getCount();
        } else {
            return;
        }
        String idString = "";
        if (num >= 10) {
            for (int i = 0; i < 10; i++) {
                mMusicId = new int[10];
                mMusicId[i] = mCursor.getInt(mCursor.getColumnIndex("mMusicId"));
                if (i < 9) {
                    idString = idString + mMusicId[i] + ",";
                } else {
                    idString = idString + mMusicId[i];
                }
                mCursor.moveToNext();
            }
        } else if (num > 0) {
            for (int i = 0; i < num; i++) {
                mMusicId = new int[num];
                mMusicId[i] = mCursor.getInt(mCursor.getColumnIndex("mMusicId"));
                if (i < num - 1) {
                    idString = idString + mMusicId[i] + ",";
                } else {
                    idString = idString + mMusicId[i];
                }
                mCursor.moveToNext();
            }
        }
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        if (mDbHelper != null) {
            mDbHelper.close();
            mDbHelper = null;
        }
        mListView = new ListView(this);
        Cursor c = this.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.DISPLAY_NAME}, MediaStore.Audio.Media._ID + " in (" + idString + ")", null, null);

        c.moveToFirst();
        _ids = new int[c.getCount()];
        _titles = new String[c.getCount()];
        for (int i = 0; i < c.getCount(); i++) {
            _ids[i] = c.getInt(3);
            _titles[i] = c.getString(0);
            c.moveToNext();
        }
        mListView.setAdapter(new MusicListAdapter(this, c));
        mListView.setOnItemClickListener(new ListItemClickListener());

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);//����������
        mCurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);//��õ�ǰ����
        LinearLayout list = new LinearLayout(this);
        list.setBackgroundResource(R.drawable.listbg);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        list.addView(mListView, params);
        setContentView(list);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mDbHelper != null) {
            mDbHelper.close();
            mDbHelper = null;
        }
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
    }

    class ListItemClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
            Intent intent = new Intent(ClicksActivity.this, MusicActivity.class);
            intent.putExtra("_ids", _ids);
            intent.putExtra("_titles", _titles);
            intent.putExtra("position", position);
            startActivity(intent);
            finish();
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == event.KEYCODE_BACK) {
            Intent intent = new Intent();
            intent.setClass(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        return true;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_UP) {
                    if (mCurrentVolume < mMaxVolume) {
                        mCurrentVolume = mCurrentVolume + 1;
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mCurrentVolume, 0);
                    } else {
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mCurrentVolume, 0);
                    }
                }
                return false;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_UP) {
                    if (mCurrentVolume > 0) {
                        mCurrentVolume = mCurrentVolume - 1;
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mCurrentVolume, 0);
                    } else {
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mCurrentVolume, 0);
                    }
                }
                return false;
            default:
                return super.dispatchKeyEvent(event);
        }
    }
}


