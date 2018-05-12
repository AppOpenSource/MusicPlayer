package com.abt.player.activity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.abt.player.R;
import com.abt.player.global.GlobalConstant;
import com.abt.player.receiver.ScanSdReceiver;
import com.abt.player.service.MusicService;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getCanonicalName();
    private ListView mListView = null;
    private Intent mIntent = null;
    private static final int ITEM1 = Menu.FIRST;
    private static final int ITEM2 = Menu.FIRST + 1;
    public static IntentFilter mIntentFilter = null;
    public static ScanSdReceiver mScanSdReceiver = null;
    private AudioManager mAudioManager = null;
    private int mMaxVolume;     //最大音量
    private int mCurrentVolume; //当前音量

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        String[] str = {"All Music", "Recent Play", "Mostly Play"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, str);
        mListView = new ListView(this);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long id) {
                switch (position) {
                    case 0:
                        mIntent = new Intent();
                        mIntent.setClass(MainActivity.this, MusicBoxActivity.class);
                        startActivity(mIntent);
                        finish();
                        break;
                    case 1:
                        mIntent = new Intent();
                        mIntent.setClass(MainActivity.this, RecentlyActivity.class);
                        startActivity(mIntent);
                        finish();
                        break;
                    case 2:
                        mIntent = new Intent();
                        mIntent.setClass(MainActivity.this, ClicksActivity.class);
                        startActivity(mIntent);
                        finish();
                        break;
                }

            }
        });
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);//获得最大音量
        mCurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);//获得当前音量
        this.setContentView(mListView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, ITEM1, 0, "Refresh Lib").setIcon(R.drawable.navigationrefresh);
        menu.add(0, ITEM2, 0, "Exit").setIcon(R.drawable.contentundo);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case ITEM1:
                scanSdCard(0);
                break;
            case ITEM2:
                if (mScanSdReceiver != null) unregisterReceiver(mScanSdReceiver);
                this.finish();
                Intent intent = new Intent(this, MusicService.class);
                intent.setAction(GlobalConstant.MUSIC_SERVICE_ACTION);
                stopService(intent);
                break;
            default:
                break;
        }
        return true;
    }

    private void scanSdCard() {
        mIntentFilter = new IntentFilter(Intent.ACTION_MEDIA_SCANNER_STARTED);
        mIntentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        mIntentFilter.addDataScheme("file");
        mScanSdReceiver = new ScanSdReceiver();
        registerReceiver(mScanSdReceiver, mIntentFilter);
        System.out.println("scanSdCard: " + Environment.getExternalStorageDirectory());
        System.out.println("scanSdCard: " + Environment.getExternalStorageDirectory().getAbsolutePath());
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                Uri.parse("file://" + Environment.getExternalStorageDirectory().getAbsolutePath())));
    }

    private void scanSdCard(int i) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { // 判断SDK版本是不是4.4或者高于4.4
            String[] paths = new String[]{Environment.getExternalStorageDirectory().toString()};
            MediaScannerConnection.scanFile(this, paths, null, null);
        } else {
            final Intent intent;
            String path = Environment.getExternalStorageDirectory().getAbsolutePath();
            File file = new File(path);
            if (file.isDirectory()) {
                intent = new Intent(Intent.ACTION_MEDIA_MOUNTED);
                intent.setClassName("com.android.providers.media", "com.android.providers.media.MediaScannerReceiver");
                intent.setData(Uri.fromFile(Environment.getExternalStorageDirectory()));
                Log.d(TAG, "directory changed, send broadcast:" + intent.toString());
            } else {
                intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(new File(path)));
                Log.d(TAG, "file changed, send broadcast:" + intent.toString());
            }
            this.sendBroadcast(intent);
        }
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
