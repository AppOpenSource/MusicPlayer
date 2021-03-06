package com.abt.player.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;

import com.abt.player.app.Constants;
import com.abt.player.core.DBHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MusicService extends Service implements
        MediaPlayer.OnCompletionListener {

    private static final int MUSIC_PLAY = 1;
    private static final int MUSIC_PAUSE = 2;
    private static final int MUSIC_STOP = 3;
    private static final int PROGRESS_CHANGE = 4;
    private static final int MUSIC_REWIND = 5;
    private static final int MUSIC_FORWARD = 6;
    private MediaPlayer mMediaPlayer = null;
    private int progress;
    private Uri uri = null;
    private int id = 10000;
    private Handler mHandler = null;
    private Handler rHandler = null;
    private Handler fHandler = null;
    private int currentTime;
    private int duration;
    private DBHelper mDbHelper = null;
    private int flag;
    private int position;
    private int _ids[];
    private int _id;

    @Override
    public void onCreate() {
        super.onCreate();
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.ANSWER");
        registerReceiver(mInComingSMSReceiver, filter);

        rHandler = new Handler();
        fHandler = new Handler();
        rHandler.removeCallbacks(rewind);
        fHandler.removeCallbacks(forward);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        if (intent != null) {
            if ((flag == 0) && (intent.getExtras().getInt("list") == 1)) {
                System.out.println("Service flag=0");
                return;
            }
        }
        if (intent.getIntArrayExtra("_ids") != null) {
            _ids = intent.getIntArrayExtra("_ids");
        }
        int position1 = intent.getIntExtra("position", -1);
        System.out.println("position1:" + position1);
        System.out.println("position:" + position);
        if (position1 != -1) {
            position = position1;
            _id = _ids[position];
        }
        System.out.println("_id:" + _id);
        System.out.println("id:" + id);
        int length = intent.getIntExtra("length", -1);
        if (_id != -1) {
            if (id != _id) {
                id = _id;
                uri = Uri.withAppendedPath(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "" + _id);
                // dbOperate(_id);
                try {
                    mMediaPlayer.reset();
                    mMediaPlayer.setDataSource(this, uri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (length == 1) {
                try {
                    mMediaPlayer.reset();
                    mMediaPlayer.setDataSource(this, uri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        setup();
        init();
        if (position != -1) {
            Intent intent1 = new Intent();
            intent1.setAction(Constants.MUSIC_LIST);
            intent1.putExtra("position", position);
            sendBroadcast(intent1);
        }

        int op = intent.getIntExtra("op", -1);
        if (op != -1) {
            switch (op) {
                case MUSIC_PLAY:
                    if (!mMediaPlayer.isPlaying()) {
                        play();
                    }
                    break;
                case MUSIC_PAUSE:
                    if (mMediaPlayer.isPlaying()) {
                        pause();
                    }
                    break;
                case MUSIC_STOP:
                    stop();
                    break;
                case PROGRESS_CHANGE:
                    currentTime = intent.getExtras().getInt("progress");
                    mMediaPlayer.seekTo(currentTime);

                    break;
                case MUSIC_REWIND:
                    rewind();
                    break;
                case MUSIC_FORWARD:
                    forward();
                    break;
            }
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void play() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
        }
        flag = 1;
        rHandler.removeCallbacks(rewind);
        fHandler.removeCallbacks(forward);
    }

    private void pause() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
        }
        flag = 1;
    }

    private void stop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            try {
                mMediaPlayer.prepare();
                mMediaPlayer.seekTo(0);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mHandler.removeMessages(1);
            rHandler.removeCallbacks(rewind);
            fHandler.removeCallbacks(forward);
        }
    }

    private void init() {
        final Intent intent = new Intent();
        intent.setAction(Constants.MUSIC_CURRENT);
        if (mHandler == null) {
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if (msg.what == 1) {
                        if (flag == 1) {
                            currentTime = mMediaPlayer.getCurrentPosition();
                            intent.putExtra("currentTime", currentTime);
                            sendBroadcast(intent);
                        }
                        mHandler.sendEmptyMessageDelayed(1, 600);
                    }
                }
            };
        }
    }

    private void setup() {
        final Intent intent = new Intent();
        intent.setAction(Constants.MUSIC_DURATION);
        try {
            if (!mMediaPlayer.isPlaying()) {
                mMediaPlayer.prepare();
            }
            mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mMediaPlayer) {
                    mHandler.sendEmptyMessage(1);
                }
            });
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        duration = mMediaPlayer.getDuration();
        intent.putExtra("duration", duration);
        sendBroadcast(intent);
    }

    private void rewind() {// ���
        rHandler.post(rewind);
    }

    private void forward() {
        fHandler.post(forward);
    }

    private Runnable rewind = new Runnable() {

        @Override
        public void run() {
            if (currentTime >= 0) {
                currentTime = currentTime - 5000;
                mMediaPlayer.seekTo(currentTime);
                rHandler.postDelayed(rewind, 500);
            }

        }
    };

    private Runnable forward = new Runnable() {

        @Override
        public void run() {
            if (currentTime <= duration) {
                currentTime = currentTime + 5000;
                mMediaPlayer.seekTo(currentTime);
                fHandler.postDelayed(forward, 500);
            }
        }
    };

    @Override
    public void onCompletion(MediaPlayer mMediaPlayer) {
        /*
         * Intent intent = new Intent(); intent.setAction(MUSIC_NEXT);
		 * sendBroadcast(intent);
		 */
        if (_ids.length == 1) {
            position = position;

        } else if (position == _ids.length - 1) {
            position = 0;
        } else if (position < _ids.length - 1) {
            position++;
        }
        uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                "" + _ids[position]);
        // hwq
        // dbOperate(_ids[position]);
        id = _ids[position];
        _id = id;
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(this, uri);

        } catch (Exception e) {
            e.printStackTrace();
        }
        mHandler.removeMessages(1);
        rHandler.removeCallbacks(rewind);
        fHandler.removeCallbacks(forward);
        setup();
        init();
        play();

        Intent intent = new Intent();
        intent.setAction(Constants.MUSIC_LIST);
        intent.putExtra("position", position);
        sendBroadcast(intent);

        Intent intent1 = new Intent();
        intent1.setAction(Constants.MUSIC_UPDATE);
        intent1.putExtra("position", position);
        sendBroadcast(intent1);
    }

    private void dbOperate(int pos) {
        mDbHelper = new DBHelper(this, "music.db", null, 2);
        Cursor c = mDbHelper.query(pos);
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);
        if (c == null || c.getCount() == 0) {// �����ѯ���Ϊ��
            ContentValues values = new ContentValues();
            values.put("music_id", pos);
            values.put("clicks", 1);
            values.put("latest", dateString);
            mDbHelper.insert(values);
        } else {
            c.moveToNext();
            int clicks = c.getInt(2);
            clicks++;
            ContentValues values = new ContentValues();
            values.put("clicks", clicks);
            values.put("latest", dateString);
            mDbHelper.update(values, pos);
        }
        if (c != null) {
            c.close();
            c = null;
        }
        if (mDbHelper != null) {
            mDbHelper.close();
            mDbHelper = null;
        }
    }

    protected BroadcastReceiver mInComingSMSReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_ANSWER)) {
                TelephonyManager telephonymanager = (TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE);
                switch (telephonymanager.getCallState()) {
                    case TelephonyManager.CALL_STATE_RINGING:
                        pause();
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        play();
                        break;
                    default:
                        break;
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer = null;
        }
        if (mDbHelper != null) {
            mDbHelper.close();
            mDbHelper = null;
        }
        if (mHandler != null) {
            mHandler.removeMessages(1);
            mHandler = null;
        }
    }

}
