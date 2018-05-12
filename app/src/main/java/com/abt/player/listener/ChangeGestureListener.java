package com.abt.player.listener;

import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;

import com.abt.player.activity.MusicActivity;


public class ChangeGestureListener extends SimpleOnGestureListener {

    private MusicActivity activity;

    public ChangeGestureListener(MusicActivity activity) {
        this.activity = activity;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        final int FLING_MIN_DISTANCE = 100;//X����y�����ƶ��ľ���(����)
        final int FLING_MIN_VELOCITY = 200;//x����y���ϵ��ƶ��ٶ�(����/��)
        if ((e1.getX() - e2.getX()) > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
            activity.nextOne();
        } else if ((e2.getX() - e1.getX()) > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
            activity.latestOne();
        }
        return super.onFling(e1, e2, velocityX, velocityY);
    }

}
