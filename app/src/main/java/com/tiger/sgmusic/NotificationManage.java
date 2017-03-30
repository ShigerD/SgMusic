package com.tiger.sgmusic;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;



public class NotificationManage {
    private Context mContext;
    private static final int NOTIFICATION_FLAG = 1;
    public NotificationManager manager ;
    public Notification myNotify;
    public RemoteViews rv;

    private Context getInstance(){
        return mContext;
    }

    public NotificationManage(String songName,Context mContext){
        // Notification myNotify = new Notification(R.drawable.message,
        // "自定义通知：您有新短信息了，请注意查收！", System.currentTimeMillis());
        this.mContext=mContext;
        myNotify = new Notification();
        myNotify.icon = R.drawable.ic_launcher;
//        myNotify.tickerText = "Welcomt to music！";
        myNotify.when = System.currentTimeMillis();
        myNotify.flags = Notification.FLAG_NO_CLEAR;// 不能够自动清除
        rv = new RemoteViews(mContext.getPackageName(),
                R.layout.my_notification);
        rv.setTextViewText(R.id.tv_songname, songName);
        myNotify.contentView = rv;
        Intent intent = new Intent(Intent.ACTION_MAIN);
//                PendingIntent contentIntent = PendingIntent.getActivity(this, 1,
//                        intent, 1);
//                myNotify.contentIntent = contentIntent;
        manager = (NotificationManager) getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_FLAG, myNotify);
    }

    public NotificationManage(Context mContext){
        // Notification myNotify = new Notification(R.drawable.message,
        // "自定义通知：您有新短信息了，请注意查收！", System.currentTimeMillis());
        this.mContext=mContext;
        myNotify = new Notification();
        myNotify.icon = R.drawable.ic_launcher;
//        myNotify.tickerText = "Welcomt to music！";
        myNotify.when = System.currentTimeMillis();
        myNotify.flags = Notification.FLAG_NO_CLEAR;// 不能够自动清除
        rv = new RemoteViews(mContext.getPackageName(),
                 R.layout.my_notification);
        rv.setTextViewText(R.id.tv_songname, "Loading...");
        myNotify.contentView = rv;
        Intent intent = new Intent(Intent.ACTION_MAIN);
//                PendingIntent contentIntent = PendingIntent.getActivity(this, 1,
//                        intent, 1);
//                myNotify.contentIntent = contentIntent;
        manager = (NotificationManager) getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_FLAG, myNotify);
    }

    public  void updateNotification(String musicName){
        rv.setTextViewText(R.id.tv_songname, musicName);
        manager.notify(NOTIFICATION_FLAG, myNotify);
    }

    public void hideNotification(){
        manager.cancel(NOTIFICATION_FLAG);//cancle the notify while id =NOTIFICATION_FLAG
    }
}
