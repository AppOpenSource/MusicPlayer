package com.abt.mp3player;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.TreeMap;

public class MusicActivity extends Activity {

    private int[] _ids;
    private int position;
    private String _titles[] = null;
    private String artists[] = null;
    private Uri uri;
    private ImageButton playBtn = null;//���š���ͣ
    //private Button stopBtn = null;//ֹͣ
    private ImageButton latestBtn = null;//��һ��
    private ImageButton nextBtn = null;//��һ��
    private ImageButton forwardBtn = null;//���
    private ImageButton rewindBtn = null;//����
    private TextView lrcText = null;//����ı�
    private TextView playtime = null;//�Ѳ���ʱ��
    private TextView durationTime = null;//����ʱ��
    private SeekBar seekbar = null;//��������
    private SeekBar soundBar = null;//��������
    private Handler handler = null;//���ڽ�����
    private Handler fHandler = null;//���ڿ��
    private int currentPosition;//��ǰ����λ��
    private int duration;
    private DBHelper dbHelper = null;
    private TextView name = null;
    private GestureDetector gestureDetector;

    private TreeMap<Integer, LRCbean> lrc_map = new TreeMap<Integer, LRCbean>();
    private Cursor myCur;
    private static final String MUSIC_CURRENT = "com.alex.currentTime";
    private static final String MUSIC_DURATION = "com.alex.duration";
    private static final String MUSIC_NEXT = "com.alex.next";
    private static final String MUSIC_UPDATE = "com.alex.update";
    private static final int MUSIC_PLAY = 1;
    private static final int MUSIC_PAUSE = 2;
    private static final int MUSIC_STOP = 3;
    private static final int PROGRESS_CHANGE = 4;
    private static final int MUSIC_REWIND = 5;
    private static final int MUSIC_FORWARD = 6;

    private static final int STATE_PLAY = 1;
    private static final int STATE_PAUSE = 2;
    private int flag;

    //���������ı���
    private AudioManager mAudioManager = null;
    private int maxVolume;//�������
    private int currentVolume;//��ǰ����


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        //ȥ��title
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.music_play);
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        _ids = bundle.getIntArray("_ids");
        _titles = bundle.getStringArray("_titles");
        artists = bundle.getStringArray("artists");
        position = bundle.getInt("position");
        lrcText = (TextView) findViewById(R.id.lrc);
        name = (TextView) findViewById(R.id.name);
        playtime = (TextView) findViewById(R.id.playtime);//�Ѿ����ŵ�ʱ��
        durationTime = (TextView) findViewById(R.id.duration);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);//����������
        currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);//��õ�ǰ����
        gestureDetector = new GestureDetector(new ChangeGestureDetector(this));    //����ʶ��
        playBtn = (ImageButton) findViewById(R.id.playBtn);
        playBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (flag) {
                    case STATE_PLAY:
                        pause();
                        break;

                    case STATE_PAUSE:
                        play();
                        break;
                }
            }
        });

        seekbar = (SeekBar) findViewById(R.id.seekbar);
        seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                play();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                pause();
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                if (fromUser) {
                    seekbar_change(progress);
                }
            }
        });

        rewindBtn = (ImageButton) findViewById(R.id.rewindBtn);
        rewindBtn.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        pause();
                        rewind();
                        break;
                    case MotionEvent.ACTION_UP:
                        play();
                        break;
                }
                return true;
            }
        });

        forwardBtn = (ImageButton) findViewById(R.id.forwardBtn);
        forwardBtn.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        pause();
                        forward();
                        break;
                    case MotionEvent.ACTION_UP:
                        play();
                        break;
                }
                return true;
            }
        });

        latestBtn = (ImageButton) findViewById(R.id.latestBtn);
        latestBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                latestOne();
            }
        });

        nextBtn = (ImageButton) findViewById(R.id.nextBtn);
        nextBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                nextOne();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        setup();
        play();

    }

    private void loadClip() {
        seekbar.setProgress(0);
        //int pos = _ids[position];
        name.setText(_titles[position] + " - " + artists[position]);//���ø�����
        Intent intent = new Intent();
        intent.putExtra("_ids", _ids);
        intent.putExtra("position", position);
        intent.setAction("com.moore.mp3player.MUSIC_SERVICE");
        startService(intent);
    }

    private void init() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(MUSIC_CURRENT);
        filter.addAction(MUSIC_DURATION);
        filter.addAction(MUSIC_NEXT);
        filter.addAction(MUSIC_UPDATE);
        registerReceiver(musicReceiver, filter);
    }

    private void setup() {
        refreshView();
        loadClip();
        init();
    }

    /**
     * ���ֲ���
     */
    private void play() {
        flag = STATE_PLAY;
        playBtn.setBackgroundResource(R.drawable.pause_selecor);
        Intent intent = new Intent();
        intent.setAction("com.moore.mp3player.MUSIC_SERVICE");
        intent.putExtra("op", MUSIC_PLAY);
        startService(intent);
    }

    /**
     * ������ͣ
     */
    private void pause() {
        flag = STATE_PAUSE;
        playBtn.setBackgroundResource(R.drawable.play_selecor);
        Intent intent = new Intent();
        intent.setAction("com.moore.mp3player.MUSIC_SERVICE");
        intent.putExtra("op", MUSIC_PAUSE);
        startService(intent);
    }

    /**
     * ����ֹͣ
     */
    private void stop() {
        unregisterReceiver(musicReceiver);
        Intent intent = new Intent();
        intent.setAction("com.moore.mp3player.MUSIC_SERVICE");
        intent.putExtra("op", MUSIC_STOP);
        startService(intent);
    }

    /**
     * �û��϶�������
     */
    private void seekbar_change(int progress) {
        Intent intent = new Intent();
        intent.setAction("com.moore.mp3player.MUSIC_SERVICE");
        intent.putExtra("op", PROGRESS_CHANGE);
        intent.putExtra("progress", progress);
        startService(intent);
    }

    /**
     * ����
     */
    private void rewind() {
        Intent intent = new Intent();
        intent.setAction("com.moore.mp3player.MUSIC_SERVICE");
        intent.putExtra("op", MUSIC_REWIND);
        startService(intent);
    }

    /**
     * ���
     */
    private void forward() {
        Intent intent = new Intent();
        intent.setAction("com.moore.mp3player.MUSIC_SERVICE");
        intent.putExtra("op", MUSIC_FORWARD);
        startService(intent);
    }

    /**
     * ��һ��
     */

    public void latestOne() {
        if (position == 0) {
            position = _ids.length - 1;
        } else if (position > 0) {
            position--;
        }
        stop();
        setup();
        play();
    }

    /**
     * ��һ��
     */
    public void nextOne() {
        if (_ids.length == 1) {
            position = position;
            Intent intent = new Intent();
            intent.setAction("com.moore.mp3player.MUSIC_SERVICE");
            intent.putExtra("length", 1);
            startService(intent);
            play();
            return;

        } else if (position == _ids.length - 1) {
            position = 0;
        } else if (position < _ids.length - 1) {
            position++;
        }
        stop();
        setup();
        play();
    }

    /**
     * ����musicReceiver,����MusicService���͵Ĺ㲥
     */
    protected BroadcastReceiver musicReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(MUSIC_CURRENT)) {
                currentPosition = intent.getExtras().getInt("currentTime");//��õ�ǰ����λ��
                playtime.setText(toTime(currentPosition));
                seekbar.setProgress(currentPosition);//���ý�����
                Iterator<Integer> iterator = lrc_map.keySet().iterator();
                while (iterator.hasNext()) {
                    Object o = iterator.next();
                    LRCbean val = lrc_map.get(o);
                    if (val != null) {

                        if (currentPosition > val.getBeginTime()
                                && currentPosition < val.getBeginTime() + val.getLineTime()) {
                            lrcText.setText(val.getLrcBody());
                            break;
                        }
                    }
                }
            } else if (action.equals(MUSIC_DURATION)) {
                duration = intent.getExtras().getInt("duration");
                seekbar.setMax(duration);
                durationTime.setText(toTime(duration));

            } else if (action.equals(MUSIC_NEXT)) {
                nextOne();
            } else if (action.equals(MUSIC_UPDATE)) {
                position = intent.getExtras().getInt("position");
                //refreshView();
                //name.setText([position]);
                setup();
            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(musicReceiver);


    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == event.KEYCODE_BACK) {
            Intent intent = new Intent();
            intent.setClass(this, MusicBoxActivity.class);
            startActivity(intent);
            finish();
        }
        return true;
    }

    /**
     * ��������
     */
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_UP) {
                    if (currentVolume < maxVolume) {
                        currentVolume = currentVolume + 1;
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
                    } else {
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
                    }
                }
                return false;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_UP) {
                    if (currentVolume > 0) {
                        currentVolume = currentVolume - 1;
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
                    } else {
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
                    }
                }
                return false;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    /**
     * �������
     *
     * @param path
     */
    private void read(String path) {
        lrc_map.clear();
        TreeMap<Integer, LRCbean> lrc_read = new TreeMap<Integer, LRCbean>();
        String data = "";
        BufferedReader br = null;
        File file = new File(path);
        if (!file.exists()) {
            lrcText.setText("Song text not exit ...");
            return;
        }
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file);
            br = new BufferedReader(new InputStreamReader(
                    stream, "GBK"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            while ((data = br.readLine()) != null) {
                if (data.length() > 6) {
                    if (data.charAt(3) == ':' && data.charAt(6) == '.') {//�Ӹ�����Ŀ�ʼ
                        data = data.replace("[", "");
                        data = data.replace("]", "@");
                        data = data.replace(".", ":");
                        String lrc[] = data.split("@");
                        String lrcContent = null;
                        if (lrc.length == 2) {
                            lrcContent = lrc[lrc.length - 1];//���
                        } else {
                            lrcContent = "";
                        }
                        String lrcTime[] = lrc[0].split(":");

                        int m = Integer.parseInt(lrcTime[0]);//��
                        int s = Integer.parseInt(lrcTime[1]);//��
                        int ms = Integer.parseInt(lrcTime[2]);//����

                        int begintime = (m * 60 + s) * 1000 + ms;//ת���ɺ���
                        LRCbean lrcbean = new LRCbean();
                        lrcbean.setBeginTime(begintime);//���ø�ʿ�ʼʱ��
                        lrcbean.setLrcBody(lrcContent);//���ø�ʵ�����
                        lrc_read.put(begintime, lrcbean);
                    }
                }
            }
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //����ÿ������Ҫ��ʱ��
        lrc_map.clear();
        data = "";
        Iterator<Integer> iterator = lrc_read.keySet().iterator();
        LRCbean oldval = null;
        int i = 0;
        while (iterator.hasNext()) {
            Object ob = iterator.next();
            LRCbean val = lrc_read.get(ob);
            if (oldval == null) {
                oldval = val;
            } else {
                LRCbean item1 = new LRCbean();
                item1 = oldval;
                item1.setLineTime(val.getBeginTime() - oldval.getBeginTime());
                lrc_map.put(new Integer(i), item1);
                i++;
                oldval = val;
            }
        }

    }

    /**
     * ��ȡsd�����
     */
    public void refreshView() {
        myCur = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.DISPLAY_NAME}, "_id=?",
                new String[]{_ids[position] + ""}, null);
        myCur.moveToFirst();

        try {
            String name = myCur.getString(4).substring(0,
                    myCur.getString(4).lastIndexOf("."));
            read("/sdcard/" + name + ".lrc");
        } catch (Exception e) {
            // TODO: handle exception
        }

    }

    /**
     * ʱ���ʽת������
     *
     * @param time
     * @return
     */
    public String toTime(int time) {

        time /= 1000;
        int minute = time / 60;
        int hour = minute / 60;
        int second = time % 60;
        minute %= 60;
        return String.format("%02d:%02d", minute, second);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            switch (flag) {
                case STATE_PLAY:
                    pause();
                    break;

                case STATE_PAUSE:
                    play();
                    break;
            }
        }
        return gestureDetector.onTouchEvent(event);
    }


}
