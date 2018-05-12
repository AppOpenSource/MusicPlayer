package com.abt.player.receiver;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.MediaStore;
import android.widget.Toast;

import com.abt.player.activity.MainActivity;

public class ScanSdReceiver extends BroadcastReceiver {

    private AlertDialog.Builder builder = null;
    private AlertDialog ad = null;
    private int count1;
    private int count2;
    private int count;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)) {
            Cursor c1 = context.getContentResolver()
                    .query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            new String[]{MediaStore.Audio.Media.TITLE,
                                    MediaStore.Audio.Media.DURATION,
                                    MediaStore.Audio.Media.ARTIST,
                                    MediaStore.Audio.Media._ID,
                                    MediaStore.Audio.Media.DISPLAY_NAME},
                            null, null, null);
            count1 = c1.getCount();
            System.out.println("count:" + count);
            builder = new AlertDialog.Builder(context);
            builder.setMessage("scanning sdcard...");
            ad = builder.create();
            // hwq aa
            //context.unregisterReceiver(MainActivity.mScanSdReceiver);
            ad.show();

        } else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
            Cursor c2 = context.getContentResolver()
                    .query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            new String[]{MediaStore.Audio.Media.TITLE,
                                    MediaStore.Audio.Media.DURATION,
                                    MediaStore.Audio.Media.ARTIST,
                                    MediaStore.Audio.Media._ID,
                                    MediaStore.Audio.Media.DISPLAY_NAME},
                            null, null, null);
            count2 = c2.getCount();
            count = count2 - count1;
            ad.cancel();
            context.unregisterReceiver(MainActivity.mScanSdReceiver);
            if (count >= 0) {
                Toast.makeText(context, "add" +
                        count + "song", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "delete" +
                        count + "song", Toast.LENGTH_LONG).show();
            }
        }
    }
}
