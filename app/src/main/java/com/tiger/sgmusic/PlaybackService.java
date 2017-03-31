package com.tiger.sgmusic;

import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.tiger.sgmusic.MusicPlaybackFragment.mActivity;

public class PlaybackService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {


    List<String> mediaFilelist=new ArrayList<String>();
    int listPosion;

    public static final String TAG = "MyService";
    public static final int PLAY_MODE_REPEAT_ALL = 0;
    public static final int PLAY_MODE_REPEAT_ONE = 1;
    public static int mPlayMode = PLAY_MODE_REPEAT_ALL;


    private static final int MSG_UPDATE_PROGRESS = 0x01;
    private static final int MSG_NOTIFY_POSITION = 0x02;

    private LocalBroadcastManager localBroadcastManager;
    private final String ACTION_MUSIC_INFO_UPDATED = "action_music_info_updated";
    private final String ACTION_MUSIC_CHANGED = "action_music_changed";
    private final String ACTION_MUSIC_START = "action_music_start";
    private final String ACTION_MUSIC_UPDAE_ID3INFO = "action_music_update_id3info";
    private final String ACTION_MUSIC_PAUSE = "action_music_pause";
    private final String ACTION_MUSIC_PLAYINGSTORE_EJECT = "action_music_eject";

    private boolean isFirstPlay=true;

    private MediaPlayer  mMusicPlayer;

    private NotificationManage mNotification;

    private AudioManager audioManager;



    public MediaPlaybackInterface.Stub mBinder=new MediaPlaybackInterface.Stub() {

        @Override
        public void play(int index) throws RemoteException {

            mediaFilelist=mActivity.getPlaylist();
            if(mediaFilelist.isEmpty()) {
                return;
            }
            mActivity.setlistPosion(index);
            doPlayNew(mediaFilelist.get(index));

            Log.e(TAG,TAG+"play()__");
        }

        @Override
        public void seekTo(int position) throws RemoteException{
                mMusicPlayer.seekTo(position);
        }

        @Override
        public void pause() throws RemoteException{
            mMusicPlayer.pause();
            if (mHandler.hasMessages(MSG_UPDATE_PROGRESS)) {
                mHandler.removeMessages(MSG_UPDATE_PROGRESS);
            }
            notifyState(ACTION_MUSIC_PAUSE);
            Log.e(TAG,TAG+"pause()__");
        }

        @Override
        public void start() throws RemoteException{
            mMusicPlayer.start();
            if (!mHandler.hasMessages(MSG_UPDATE_PROGRESS)) {
                mHandler.sendEmptyMessage(MSG_UPDATE_PROGRESS);
            }
            notifyState(ACTION_MUSIC_START);
            Log.e(TAG,TAG+"__start()__");
        }

        @Override
        public void next() throws RemoteException{
            playNext();
        }

        @Override
        public void previous() throws RemoteException{
            playPrevious();
        }

        @Override
        public boolean isPlaying() throws RemoteException{

            return  mMusicPlayer.isPlaying();
        }

        @Override
        public void getLatestInfo() throws RemoteException{

        }

        @Override
        public int getMediaDuration() throws RemoteException {

            return mMusicPlayer.getDuration();
        }

        @Override
        public int getMediaCurDuration() throws RemoteException {
            return mMusicPlayer.getCurrentPosition();
        }

        @Override
        public void setIsFirstPlay(boolean bool) throws RemoteException {
            isFirstPlay=bool;
        }

        @Override
        public boolean getIsFirstPlay() throws RemoteException {

            return isFirstPlay;
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();

        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        mMusicPlayer =  new  MediaPlayer();
        mMusicPlayer.setOnCompletionListener(this);
        mMusicPlayer.setOnPreparedListener(this);

        mNotification=new NotificationManage(this);


        Log.e(TAG, "__onCreate()__");

        audioManager= (AudioManager) this.getSystemService(this.AUDIO_SERVICE);
        audioManager.requestAudioFocus(afChangeListener,       // Use the music stream.
                AudioManager.STREAM_MUSIC, // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e(TAG, "__onStartCommand() executed__");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "__onDestroy() executed__");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override//一个media播放完成调用
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "=onCompletion");
        playNext();//自动播放下一首
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        mHandler.removeMessages(MSG_UPDATE_PROGRESS);
        mHandler.sendEmptyMessage(MSG_UPDATE_PROGRESS);

    }

    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch(msg.what){
                case MSG_UPDATE_PROGRESS:
                    updateSeekBar();
                    msg = obtainMessage(MSG_UPDATE_PROGRESS);
                    sendMessageDelayed(msg, 1000);//每隔1s更新进度条
                    break;
                default:
                    break;
            }
        }
    };

    private void updateSeekBar() {

        Intent intent=new Intent(ACTION_MUSIC_INFO_UPDATED);
        Bundle b = new Bundle();
        b.putLong("duration", mMusicPlayer.getDuration());
        b.putLong("current", mMusicPlayer.getCurrentPosition());
        intent.putExtra("info",b);
        localBroadcastManager.sendBroadcast(intent);

    }

    private void updateID3info(String videoPath) {
        String songName=getTrackName(videoPath);
        String artistName=getArtistName(videoPath);
        String albumName=(getAlbumName(videoPath));
        Intent intent=new Intent(ACTION_MUSIC_UPDAE_ID3INFO);
        Bundle b = new Bundle();
        b.putString("songName",songName);
        b.putString("artistName",artistName);
        b.putString("albumName",albumName);
        intent.putExtra("info",b);
        localBroadcastManager.sendBroadcast(intent);

    }
    /*
     *
     */
    private void notifyState(String action) {
        Intent intent;

        if(action.equals(ACTION_MUSIC_START)){
            intent = new Intent(ACTION_MUSIC_START);
            localBroadcastManager.sendBroadcast(intent);
        }
        else if(action.equals(ACTION_MUSIC_PAUSE)){
            intent = new Intent(ACTION_MUSIC_PAUSE);
            localBroadcastManager.sendBroadcast(intent);
        }
        else if(action.equals(ACTION_MUSIC_CHANGED)){
            intent = new Intent(ACTION_MUSIC_CHANGED);
            localBroadcastManager.sendBroadcast(intent);
        }
        else if(action.equals(ACTION_MUSIC_PLAYINGSTORE_EJECT)){
            intent = new Intent(ACTION_MUSIC_PLAYINGSTORE_EJECT);
            localBroadcastManager.sendBroadcast(intent);
        }

    }

    /**
     *
     * @return
     */
    AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                if (mMusicPlayer.isPlaying()) {
//                   pause();
                    stop();
                }

            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                if (mMusicPlayer == null) {
                    playNext();
                } else if (!mMusicPlayer.isPlaying()) {
//                    mMusicPlayer.start();
                    playNext();
                }
                // Resume playback
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                if (mMusicPlayer.isPlaying()) {
                    stop();
                }
                audioManager.abandonAudioFocus(afChangeListener);
                // Stop playback
            } else if (focusChange == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                if (mMusicPlayer.isPlaying()) {
                    stop();
                }

            } else if (focusChange == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
                if (mMusicPlayer.isPlaying()) {
                    stop();
                }
            }
        }
    };

    class MyBinder extends Binder {

        public void startDownload() {
            Log.d("TAG", " __onBind__");
            // 执行具体的任务
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

    /***
     *播放控制
     */
    public  void doPlayNew(String videoPath)
    {

        mCursor=null;
        if(mMusicPlayer!=null)
            mMusicPlayer.stop();
        mMusicPlayer.reset();
        try {
            mMusicPlayer.setDataSource(videoPath);
            mMusicPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMusicPlayer.start();
        //更新ID3INfo
        mNotification.updateNotification(getTrackName(videoPath)); //同步Notification歌名
        updateID3info(videoPath);
//        mHandler.sendEmptyMessage(MSG_NOTIFY_POSITION);
    }

    public  void stop(){
        mMusicPlayer.stop();
        mNotification.hideNotification();
//        updatePlayBtnState();
    }
    public  void pause(){
        mMusicPlayer.pause();
        mNotification.hideNotification();
//        updatePlayBtnState();
    }




    public  void playNext(){


        mediaFilelist=mActivity.getPlaylist();
        if(mediaFilelist.isEmpty()) return;
        listPosion=mActivity.getlistPosion();

        if(PlaybackService.mPlayMode==PlaybackService.PLAY_MODE_REPEAT_ALL)  //列表循环
            listPosion++;

        listPosion=listPosion%(mediaFilelist.size());
        mActivity.setlistPosion(listPosion);
        doPlayNew(mediaFilelist.get(listPosion));

    }

    public void playPrevious(){
        mediaFilelist=mActivity.getPlaylist();
        listPosion=mActivity.getlistPosion();
//        Log.e(TAG,"size:"+mediaFilelist.size());
        if(PlaybackService.mPlayMode==PlaybackService.PLAY_MODE_REPEAT_ALL)  //列表循环
            if(listPosion>0)
                listPosion--;
        listPosion=listPosion%(mediaFilelist.size());
        mActivity.setlistPosion(listPosion);

        doPlayNew(mediaFilelist.get(listPosion));
    }


    /**
     * mp3 info
     */

    private Cursor mCursor;
    private boolean mRequestToken;
    String[] mCursorCols = new String[] {
            "audio._id AS _id", // index must match IDCOLIDX below
            MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.BOOKMARK
    };
    private void tryToGetPlayingId3Info(String path) {
        if (mCursor == null && !TextUtils.isEmpty(path)) {
            ContentResolver resolver = this.getContentResolver();
            Uri uri = MediaStore.Audio.Media.getContentUriForPath(path);
            String where = MediaStore.Audio.Media.DATA + "=?";
            String[] selectionArgs = new String[] {
                    path
            };
            try {
                mCursor = resolver.query(uri, mCursorCols, where,
                        selectionArgs, null);
                if (mCursor != null) {
                    if (mCursor.getCount() != 1) {
                        mCursor.close();
                        mCursor = null;
                    } else {
                        mCursor.moveToNext();
                    }
                }
            } catch (UnsupportedOperationException ex) {
            } catch (IllegalStateException ex) {
                ex.printStackTrace();
            }
        }
    }
    //
    public String getArtistName(String mFilePath) {
        synchronized (this) {
            if (mCursor == null) {
                tryToGetPlayingId3Info(mFilePath);
                if (mCursor == null) {
                    return null;
                }
            }
            return mCursor.getString(mCursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
        }
    }
    //
    public long getArtistId(String mFilePath) {
        synchronized (this) {
            if (mCursor == null) {
                tryToGetPlayingId3Info(mFilePath);
                if (mCursor == null) {
                    return -1;
                }
            }
            return mCursor.getLong(mCursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID));
        }
    }
    //
    public String getAlbumName(String mFilePath) {
        synchronized (this) {
            if (mCursor == null) {
                tryToGetPlayingId3Info(mFilePath);
                if (mCursor == null) {
                    return null;
                }
            }
            return mCursor.getString(mCursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
        }
    }
    //
    public long getAlbumId(String mFilePath) {
        synchronized (this) {
            if (mCursor == null) {
                tryToGetPlayingId3Info(mFilePath);
                if (mCursor == null) {
                    return -1;
                }
            }
            return mCursor.getLong(mCursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
        }
    }
    //
    public String getTrackName(String mFilePath) {
        synchronized (this) {
            if (mCursor == null) {
                tryToGetPlayingId3Info(mFilePath);
                if (mCursor == null) {
                    if (!TextUtils.isEmpty(mFilePath)) {
                        return new File(mFilePath).getName();
                    }
                    return null;
                }
            }
            return mCursor.getString(mCursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
        }
    }
    /*

    */

}