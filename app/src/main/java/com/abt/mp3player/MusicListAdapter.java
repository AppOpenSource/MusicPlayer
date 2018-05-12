package com.abt.mp3player;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MusicListAdapter extends BaseAdapter {

    private Context mContext;
    private Cursor mCursor;
    private int pos = -1;

    public MusicListAdapter(Context con, Cursor cur) {
        mContext = con;
        mCursor = cur;
    }

    @Override
    public int getCount() {
        return mCursor.getCount();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(mContext).inflate(
                R.layout.music_list, null);
        mCursor.moveToPosition(position);
        TextView musicName = (TextView) convertView
                .findViewById(R.id.music_name);
        if (mCursor.getString(0).length() > 24) {
            try {
                String musicTitle = bSubstring(mCursor.getString(0).trim(), 24);
                musicName.setText(musicTitle);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            musicName.setText(mCursor.getString(0).trim());
        }
        TextView musicSinger = (TextView) convertView.findViewById(R.id.singer);
        if (mCursor.getString(2).equals("<unknown>")) {
            musicSinger.setText("unknown");
        } else {
            musicSinger.setText(mCursor.getString(2));
        }
        TextView musicTime = (TextView) convertView.findViewById(R.id.time);
        musicTime.setText(toTime(mCursor.getInt(1)));
        ImageView img = (ImageView) convertView.findViewById(R.id.list_item_img);
        if (position == pos) {
            img.setImageResource(R.drawable.isplaying);
        } else {
            img.setImageResource(R.drawable.item);
        }
        return convertView;
    }

    public void setItemIcon(int position) {
        pos = position;
    }

    /**
     * ʱ���ʽת��
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

    /**
     * �ַ����ü�
     *
     * @param s
     * @param length
     * @return
     * @throws Exception
     */
    public static String bSubstring(String s, int length) throws Exception {

        byte[] bytes = s.getBytes("Unicode");
        int n = 0; // ��ʾ��ǰ���ֽ���
        int i = 2; // Ҫ��ȡ���ֽ������ӵ�3���ֽڿ�ʼ
        for (; i < bytes.length && n < length; i++) {
            // ����λ�ã���3��5��7�ȣ�ΪUCS2�����������ֽڵĵڶ����ֽ�
            if (i % 2 == 1) {
                n++; // ��UCS2�ڶ����ֽ�ʱn��1
            } else {
                // ��UCS2����ĵ�һ���ֽڲ�����0ʱ����UCS2�ַ�Ϊ���֣�һ�������������ֽ�
                if (bytes[i] != 0) {
                    n++;
                }
            }
        }
        // ���iΪ����ʱ�������ż��
        if (i % 2 == 1)

        {
            // ��UCS2�ַ��Ǻ���ʱ��ȥ�������һ��ĺ���
            if (bytes[i - 1] != 0)
                i = i - 1;
                // ��UCS2�ַ�����ĸ�����֣��������ַ�
            else
                i = i + 1;
        }

        return new String(bytes, 0, i, "Unicode");
    }

}
