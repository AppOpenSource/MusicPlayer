package com.abt.player.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.abt.player.R;

import java.util.HashMap;

public class AlbumListAdapter extends BaseAdapter {

    private Context myCon;
    private String[] albums;
    private HashMap<String, String> myMap;

    public AlbumListAdapter(Context con, String[] str1, HashMap<String, String> map) {
        myCon = con;
        albums = str1;
        myMap = map;
    }

    @Override
    public int getCount() {
        return albums.length;
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
        convertView = LayoutInflater.from(myCon).inflate(R.layout.albumslist,
                null);
        TextView album = (TextView) convertView.findViewById(R.id.album);
        if (albums[position].length() > 24) {
            try {
                String albumName = bSubstring(albums[position], 24);
                album.setText(albumName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            album.setText(albums[position]);
        }

        TextView artist = (TextView) convertView.findViewById(R.id.mysinger);
        if (myMap.get(albums[position]).equals("<unknown>")) {
            artist.setText("Unkonw artist");
        } else {
            artist.setText(myMap.get(albums[position]));
        }
        ImageView albumsItem = (ImageView) convertView.findViewById(R.id.Albumsitem);
        albumsItem.setImageResource(R.drawable.album);
        return convertView;
    }

    public static String bSubstring(String s, int length) throws Exception {
        byte[] bytes = s.getBytes("Unicode");
        int n = 0;
        int i = 2;
        for (; i < bytes.length && n < length; i++) {
            if (i % 2 == 1) {
                n++;
            } else {
                if (bytes[i] != 0) {
                    n++;
                }
            }
        }
        if (i % 2 == 1) {
            if (bytes[i - 1] != 0)
                i = i - 1;
            else
                i = i + 1;
        }
        return new String(bytes, 0, i, "Unicode");
    }

}
