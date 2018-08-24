package com.abt.player.ui.activity;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TabHost;

import com.abt.player.R;
import com.abt.player.ui.adapter.AlbumListAdapter;
import com.abt.player.ui.adapter.ArtistListAdapter;
import com.abt.player.ui.adapter.MusicListAdapter;
import com.abt.player.app.GlobalConstant;
import com.abt.player.ui.receiver.ScanSdReceiver;
import com.abt.player.service.MusicService;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class MusicBoxActivity extends TabActivity implements TabHost.TabContentFactory {

    private ListView listview;
    private int[] _ids;
    private String[] _titles;
    private String[] albums;
    private String[] artists;
    private String[] _path;
    private int pos;
    private int num;
    private MusicListAdapter adapter;
    private ScanSdReceiver scanSdReceiver = null;
    private AlertDialog ad = null;
    private AlertDialog.Builder builder = null;
    private Cursor mCursor;
    private String tag;
    private static final int PLAY_ITEM = Menu.FIRST;
    private static final int DELETE_ITEM = Menu.FIRST + 1;
    private AudioManager mAudioManager = null;
    private int maxVolume;
    private int currentVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        TabHost th = getTabHost();
        th.addTab(th.newTabSpec("list").setIndicator("Music List").setContent(this));
        th.addTab(th.newTabSpec("artists").setIndicator("Artist List").setContent(this));
        th.addTab(th.newTabSpec("albums").setIndicator("Album").setContent(this));

        Intent intent = new Intent(this, MusicService.class);
        intent.setAction(GlobalConstant.MUSIC_SERVICE_ACTION);
        intent.putExtra("list", 1);
        startService(intent);
    }

    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(GlobalConstant.MUSIC_LIST);
        registerReceiver(changeItem, filter);
        super.onStart();
    }

    @Override
    public View createTabContent(String tag) {
        this.tag = tag;
        if (tag.equals("list")) {
            listview = new ListView(this);
            setListData();
            listview.setCacheColorHint(0);
            listview.setOnItemClickListener(new ListItemClickListener());
            listview.setOnCreateContextMenuListener(new ContextMenuListener());
        } else if (tag.equals("artists")) {
            mCursor = this.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Audio.Media.TITLE,
                            MediaStore.Audio.Media.DURATION,
                            MediaStore.Audio.Media.ARTIST,
                            MediaStore.Audio.Media._ID,
                            MediaStore.Audio.Media.DISPLAY_NAME,}, null, null,
                    null);
            mCursor.moveToFirst();
            int num = mCursor.getCount();
            HashSet set = new HashSet();
            for (int i = 0; i < num; i++) {
                if (mCursor.getString(2).equals("<unknown>")) {
                    set.add("unknown");
                } else {
                    set.add(mCursor.getString(2));
                    mCursor.moveToNext();
                }
            }
            num = set.size();
            Iterator it = set.iterator();
            artists = new String[num];
            int i = 0;
            while (it.hasNext()) {
                artists[i] = it.next().toString();
                i++;
            }
            listview = new ListView(this);
            listview.setCacheColorHint(0);
            listview.setAdapter(new ArtistListAdapter(this, artists));
            listview.setOnItemClickListener(new ArtistsItemClickListener());
        } else if (tag.equals("albums")) {
            Cursor c = this.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Audio.Media.TITLE,
                            MediaStore.Audio.Media.DURATION,
                            MediaStore.Audio.Media.ARTIST,
                            MediaStore.Audio.Media.ALBUM,
                            MediaStore.Audio.Media._ID,
                            MediaStore.Audio.Media.DISPLAY_NAME}, null, null,
                    MediaStore.Audio.Media.ALBUM);
            c.moveToFirst();
            int num = c.getCount();
            HashSet set = new HashSet();
            for (int i = 0; i < num; i++) {
                set.add(c.getString(3));
                c.moveToNext();
            }
            num = set.size();
            Iterator it = set.iterator();
            albums = new String[num];
            int i = 0;
            while (it.hasNext()) {
                albums[i] = it.next().toString();
                i++;
            }
            String album = "";
            for (int j = 0; j < num; j++) {
                if (j < num - 1) {
                    album = album + "'" + albums[j] + "',";
                } else {
                    album = album + "'" + albums[j] + "'";
                }
            }

            Cursor c1 = this.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Audio.Media.TITLE,
                            MediaStore.Audio.Media.DURATION,
                            MediaStore.Audio.Media.ARTIST,
                            MediaStore.Audio.Media.ALBUM,
                            MediaStore.Audio.Media._ID,
                            MediaStore.Audio.Media.DISPLAY_NAME,}, null, null,
                    MediaStore.Audio.Media.ALBUM);
            c1.moveToFirst();
            HashMap<String, String> map = new HashMap<String, String>();
            int num1 = c1.getCount();
            for (int j = 0; j < num1; j++) {
                map.put(c1.getString(3), c1.getString(2));
                c1.moveToNext();
            }
            listview = new ListView(this);
            listview.setCacheColorHint(0);
            listview.setAdapter(new AlbumListAdapter(this, albums, map));
            listview.setOnItemClickListener(new AlbumsItemClickListener());
        }

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);// ����������
        currentVolume = mAudioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC);// ��õ�ǰ����
        LinearLayout list = new LinearLayout(this);
        list.setBackgroundResource(R.drawable.listbg);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        list.removeAllViews();
        list.addView(listview, params);
        return list;
    }

    private void playMusic(int position) {
        Intent intent = new Intent(MusicBoxActivity.this, MusicActivity.class);
        intent.putExtra("_ids", _ids);
        intent.putExtra("_titles", _titles);
        intent.putExtra("artists", artists);
        intent.putExtra("position", position);
        startActivity(intent);
        finish();
    }

    private void deleteMusic(int position) {
        this.getContentResolver().delete(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Audio.Media._ID + "=" + _ids[position], null);
    }

    private void deleteMusicFile(int position) {
        File file = new File(_path[position]);
        file.delete();
    }

    class ListItemClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> arg0, View view, int position,
                                long id) {
            playMusic(position);
        }
    }

    class ArtistsItemClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
            Intent intent = new Intent();
            intent.setClass(MusicBoxActivity.this, ArtistActivity.class);
            intent.putExtra("artist", artists[position]);
            startActivity(intent);
        }
    }

    class AlbumsItemClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
            Intent intent = new Intent();
            intent.setClass(MusicBoxActivity.this, AlbumActivity.class);
            intent.putExtra("albums", albums[position]);
            startActivity(intent);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == event.KEYCODE_BACK) {
            if (scanSdReceiver != null)
                unregisterReceiver(scanSdReceiver);
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
                    if (currentVolume < maxVolume) {
                        currentVolume = currentVolume + 1;
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                                currentVolume, 0);
                    } else {
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                                currentVolume, 0);
                    }
                }
                return false;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_UP) {
                    if (currentVolume > 0) {
                        currentVolume = currentVolume - 1;
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                                currentVolume, 0);
                    } else {
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                                currentVolume, 0);
                    }
                }
                return false;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    private BroadcastReceiver changeItem = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(GlobalConstant.MUSIC_LIST)) {
                pos = intent.getExtras().getInt("position");
                adapter.setItemIcon(pos);
                adapter.notifyDataSetChanged();
                System.out.println("List Update...");
            }

        }
    };

    private void setListData() {
        mCursor = this.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.DATA}, null, null, null);
        if (mCursor == null || mCursor.getCount() == 0) {
            builder = new AlertDialog.Builder(this);
            builder.setMessage("list is empty...").setPositiveButton("OK", null);
            ad = builder.create();
            ad.show();
        }
        mCursor.moveToFirst();
        _ids = new int[mCursor.getCount()];
        _titles = new String[mCursor.getCount()];
        artists = new String[mCursor.getCount()];
        _path = new String[mCursor.getCount()];
        for (int i = 0; i < mCursor.getCount(); i++) {
            _ids[i] = mCursor.getInt(3);
            _titles[i] = mCursor.getString(0);
            artists[i] = mCursor.getString(2);
            _path[i] = mCursor.getString(5).substring(4);
            mCursor.moveToNext();
        }
        adapter = new MusicListAdapter(this, mCursor);
        listview.setAdapter(adapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(changeItem);
    }

    class ContextMenuListener implements OnCreateContextMenuListener {
        @Override
        public void onCreateContextMenu(ContextMenu menu, View view,
                                        ContextMenuInfo info) {
            if (tag.equals("list")) {
                menu.setHeaderTitle("Action");
                menu.add(0, PLAY_ITEM, 0, "Play");
                menu.add(0, DELETE_ITEM, 0, "Delete");
                final AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) info;
                num = menuInfo.position;
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case PLAY_ITEM:
                playMusic(num);
                break;
            case DELETE_ITEM:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("want to delete ?").setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteMusic(num);
                                deleteMusicFile(num);
                                setListData();
                                adapter.notifyDataSetChanged();
                            }
                        }).setNegativeButton("No", null);
                AlertDialog ad = builder.create();
                ad.show();
                break;
        }
        return true;
    }
}
