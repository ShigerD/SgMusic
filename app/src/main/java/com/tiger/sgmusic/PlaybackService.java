package com.tiger.sgmusic;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class PlaybackService extends Service {

    private MainActivity mainActivity;
    public static final String TAG = "MyService";
//   private MainActivity mainActivity=(MainActivity)getA;


    @Override
    public void onCreate() {
        super.onCreate();

        Log.e(TAG, "onCreate() executed");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//        new MyAsyncTask().execute("hello");
        Log.e(TAG, "onStartCommand() executed");
//        MainActivity.initMedia();
        MusicPlaybackFragment.getMainActivity().playNext();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() executed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

     class MyBinder extends Binder {

        public void startDownload() {
            Log.d("TAG", " onBind");
            // 执行具体的下载任务
        }

    }
    public class MyAsyncTask extends AsyncTask<String,String,String>{


        @Override
        protected String doInBackground(String... strings) {
            for(int i=100;i>0;i--){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.e(TAG, "倒计时 ...."+String.valueOf(i));
            }
            return null;
        }
    }
}