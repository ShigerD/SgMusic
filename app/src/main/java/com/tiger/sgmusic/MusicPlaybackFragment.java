package com.tiger.sgmusic;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于创建Fragment对象，作为ViewPager的叶片
 * @author ZHF
 *
 */
public class MusicPlaybackFragment extends Fragment  implements View.OnClickListener,
         View.OnTouchListener
        {
	private static final String TAG =MusicPlaybackFragment.class.getSimpleName();


//    private MediaPlayer mMusicPlayer;
    private View layout_Loding;
    private View layout_toolbar;
    private boolean isfullmode=false;
    //seekbar
    private SeekBar mSeekBar;

    private static final int MSG_UPDATE_PROGRESS = 0x01;
    private static final int MSG_NOTIFY_POSITION = 0x02;
    private static final int MSG_FADE_OUT        = 0x03;
    private static final int MSG_BRAKE_ON        = 0x04;
    private static final int MSG_BRAKE_OFF       = 0x05;
    private static final int MSG_NOTIFY_MEDIA_INFO = 0x06;
    private static final int MSG_UPDATE_PLAY_STATE = 0x07;
    private boolean mDragging = false;
    public static final int DEFAULT_TIMEOUT       = 5000;
    private TextView mCurrentTime ;
    private TextView mTotalTime;


    private final String ACTION_MUSIC_START = "action_music_start";
    private final String ACTION_MUSIC_PAUSE = "action_music_pause";
    private final String ACTION_MUSIC_CHANGED = "action_music_changed";
    private final String ACTION_MUSIC_PLAYINGSTORE_EJECT = "action_music_eject";
    private final String ACTION_MUSIC_INIT="music_first_song_init";
    private final String ACTION_MUSIC_INFO_UPDATED = "action_music_info_updated";
    private final String ACTION_MUSIC_UPDAE_ID3INFO = "action_music_update_id3info";
    private LocalReceiver localReceiver;
    private LocalBroadcastManager localBroadcastManager;

    private TextView mTrackName;
    private TextView mArtistName;
    private TextView mAlbumName;


   //public String mediaPlayPath="/storage/哦呢.mp4";vechiel
//    public String mediaPlayPath="storage/sdcard0/哦呢.mp4";   //storage/sdcard0/哦呢.mp4
    public static MainActivity mActivity;
    private  List<String> mediaFilelist = new ArrayList<String>();//new
    private  List<String> songNamelist=new ArrayList<String>();
    private  int listPosion=0;
    private  Boolean isFisrtStart=true;
    private  ImageView mPlayPause;
    private  ImageView mPlaymode;

    private MediaPlayer fragmentMusicPlayer;

    int mNum; //页号

    private MediaPlaybackInterface mService;
    public void setService(MediaPlaybackInterface service) {
        mService=service;
        Log.e(TAG, "setService");

    }




    public static MusicPlaybackFragment newInstance(int num) {

        MusicPlaybackFragment fragment = new MusicPlaybackFragment();
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("num", num);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.e(TAG, " Oncreate++");
        super.onCreate(savedInstanceState);
        //这里我只是简单的用num区别标签，其实具体应用中可以使用真实的fragment对象来作为叶片
        mNum = getArguments() != null ? getArguments().getInt("num") : 1;
        mActivity = (MainActivity)getActivity();
        Log.e(TAG, "++End Oncreate++");
    }
    /*
     * 
     */
    private void setupView(View view){
        view.findViewById(R.id.img_previous).setOnClickListener(this);
        view.findViewById(R.id.img_next).setOnClickListener(this);
        view.findViewById(R.id.img_dir).setOnClickListener(this);


        mPlaymode=(ImageView) view.findViewById(R.id.img_playmode);
        mPlaymode.setOnClickListener(this);
        view.findViewById(R.id.img_play).setOnClickListener(this);
        mPlayPause = (ImageView)view.findViewById(R.id.img_play);
        mSeekBar = (SeekBar)view.findViewById(R.id.seekBar);
        mSeekBar.setOnSeekBarChangeListener(mSeekBarChanged);
        mCurrentTime = (TextView)view.findViewById(R.id.tv_startTime);
        mTotalTime = (TextView)view.findViewById(R.id.tv_totalTime);

        layout_Loding= view.findViewById(R.id.video_loading);
        layout_toolbar=view.findViewById(R.id.media_controller_bar);

        mTrackName = (TextView) view.findViewById(R.id.trackname);
        mArtistName = (TextView) view.findViewById(R.id.artistname);
        mAlbumName=(TextView)view.findViewById(R.id.albumname);

        localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_MUSIC_INFO_UPDATED);
        intentFilter.addAction(ACTION_MUSIC_CHANGED);
        intentFilter.addAction(ACTION_MUSIC_PAUSE);
        intentFilter.addAction(ACTION_MUSIC_START);
        intentFilter.addAction(ACTION_MUSIC_PLAYINGSTORE_EJECT);
        intentFilter.addAction(ACTION_MUSIC_INIT);
        intentFilter.addAction(ACTION_MUSIC_UPDAE_ID3INFO);
        localReceiver=new LocalReceiver();
        localBroadcastManager.registerReceiver(localReceiver,intentFilter);


    }

    @Override
    public void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
//        audioManager.abandonAudioFocus(afChangeListener);
//        mNotification.hideNotification();
    }




    /**
     * seekbar
     */
    public void showBars(int timeout) {
        Message msg = mHandler.obtainMessage(MSG_FADE_OUT);
        if (timeout != 0) {
            mHandler.removeMessages(MSG_FADE_OUT);
            mHandler.sendMessageDelayed(msg, timeout);
        }

    }
    private int getDuration(){
        int result=0;
        try {
            result=mService.getMediaDuration();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return result;
    }
    //seekbar 点击拖曳
    private SeekBar.OnSeekBarChangeListener mSeekBarChanged = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int arg1, boolean fromuser) {

            if(getDuration() <= 0){
                seekBar.setProgress(0);
                return;
            }

            if(!fromuser){
                return;
            }
            showBars(DEFAULT_TIMEOUT);
            long newpostion = seekBar.getProgress();
//            mMusicPlayer.seekTo((int)newpostion*1000);
            try {
                mService.seekTo((int)newpostion*1000);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mCurrentTime.setText(makeTimeString( newpostion));
            Log.i(TAG, "newposition = "+newpostion);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            showBars(DEFAULT_TIMEOUT);
            if(getDuration() <= 0){
                return;
            }
            mDragging = true;
            mHandler.removeMessages(MSG_UPDATE_PROGRESS);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            showBars(DEFAULT_TIMEOUT);
            if (getDuration()<= 0) {
                seekBar.setProgress(0);
                return;
            }
            mDragging = false;
            mHandler.sendEmptyMessage(MSG_UPDATE_PROGRESS);
        }
    };

    private boolean isPlaying(){
        boolean istrue=false;

        try {
            if(mService.isPlaying())
                 istrue=true;
            else istrue= false;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return istrue;
    }
    private int setProgress(){
        if (!isPlaying()|| mDragging) {
            return 0;
        }
        int position = getCurrentPosition();
        int duration = getDuration();
        if (mSeekBar != null){
            if (duration >= 0) {
                long pos = position/1000 ;
                mSeekBar.setProgress((int)pos);
            } else {
                mSeekBar.setProgress(0);
            }
        }

        if (mCurrentTime != null) {
            mCurrentTime.setText(makeTimeString(position/1000));
        }

        return position;
    }

    private int getCurrentPosition() {
        int dur=0;
        try {
            dur= mService.getMediaCurDuration();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return dur;
    }

    private void updatePlayBtnState() {
        boolean isplay = false;
        try {
            isplay = mService.isPlaying();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if(isplay) {
            mPlayPause.setBackgroundResource(R.drawable.mc_play);
        } else {
            mPlayPause.setBackgroundResource(R.drawable.mc_pause);
        }
    }
    private Handler mHandler = new Handler(){
        /****/
        public void handleMessage(Message msg) {
            int pos;
            switch(msg.what){

                case MSG_UPDATE_PROGRESS:
                    pos = setProgress();
                    msg = obtainMessage(MSG_UPDATE_PROGRESS);
                    sendMessageDelayed(msg, 1000 - (pos % 1000));

                    break;
                case MSG_NOTIFY_POSITION:
//                    notifyPosition();
                    if(!mediaFilelist.isEmpty()){
//                        mTrackName.setText(getTrackName(mediaFilelist.get(listPosion)));
//                        mArtistName.setText(mediaFilelist.get(listPosion));
                    }

                    break;
                case MSG_BRAKE_ON:
//                    mBrakeView.setVisibility(View.GONE);
                    showBars(DEFAULT_TIMEOUT);
                    break;
                case MSG_BRAKE_OFF:
//                    mBrakeView.setVisibility(View.VISIBLE);

                    break;
                case MSG_FADE_OUT:
//                    if (mCurrentPage == 1) {
//                        hideBars();
//                    }
                    break;
                case MSG_NOTIFY_MEDIA_INFO:
                    try {
                        if (mService.isPlaying()) {
    //                        notifyMediaInfo();
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    sendEmptyMessageDelayed(MSG_NOTIFY_MEDIA_INFO, 1000);
                    break;

                default:
                    break;
            }
        };

    };
    /*

     */
    public void hideLoading(){
        if(layout_Loding.getVisibility()==View.VISIBLE)
            layout_Loding.setVisibility(View.GONE);
    }
    /**为Fragment加载布局时调用**/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	Log.e(TAG, " ++OncreateView++");                                                                                                                                                                                                                                                                                       
        View view = inflater.inflate(R.layout.media_play_layout, container, false);
        setupView(view);

        Log.e(TAG, " ++End OncreateView++");
        return view;
    }


    /**
     *
     *
     */


    @Override//按钮点击事件
    public void onClick(View view) {
//        Log.e(TAG,"=====onclick"+view.getId());
        switch(view.getId()){
            case R.id.img_playmode:
                if(PlaybackService.mPlayMode==PlaybackService.PLAY_MODE_REPEAT_ALL)
                {
                    PlaybackService.mPlayMode=PlaybackService.PLAY_MODE_REPEAT_ONE;
                    mPlaymode.setBackgroundResource(R.drawable.mc_repeat_once_normal);
                }
                else if(PlaybackService.mPlayMode==PlaybackService.PLAY_MODE_REPEAT_ONE)
                {
                    PlaybackService.mPlayMode=PlaybackService.PLAY_MODE_REPEAT_ALL;
                    mPlaymode.setBackgroundResource(R.drawable.mc_repeat_all_normal);
                }
                break;

            case R.id.img_previous:
                try {
                    mService.previous();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.img_play:
                try {
                    if (mService.isPlaying()) {
                        mService.pause();
                    }
                    else {
                        mService.start();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                updatePlayBtnState();
                break ;
            case R.id.img_next:
                try {
                    mService.next();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break ;
            case R.id.img_dir:
                mActivity.switchToPage(0);
                break ;
            default :
                break ;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }

    private String makeTimeString( long secs) {
        String result="--:--";
        String[] sTimeArgs = new String[5];

        long[] timeArgs =new long[5];
        timeArgs[0] = secs / 60;//minue
        timeArgs[1] = secs % 60;//second
        if(timeArgs[0]<10) sTimeArgs[0]="0"+String.valueOf(timeArgs[0]);
        else sTimeArgs[0]=String.valueOf(timeArgs[0]);
        if(timeArgs[1]<10) sTimeArgs[1]="0"+String.valueOf(timeArgs[1]);
        else sTimeArgs[1]=String.valueOf(timeArgs[1]);

        result=sTimeArgs[0]+":"+sTimeArgs[1];
        return result;
    }


    class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals(ACTION_MUSIC_INIT)){
                Log.e(TAG, "onReceive: " + "ACTION_MUSIC_INIT");
            }
            else if(intent.getAction().equals(ACTION_MUSIC_INFO_UPDATED)){//seekbar update
//                Log.e(TAG, "onReceive: " + "update");
                Bundle bundle = intent.getBundleExtra("info");
                long current=bundle.getLong("current");
                long total=bundle.getLong("duration");
                current=current/1000;
                total=total/1000;
                mCurrentTime.setText(makeTimeString(current));
                mTotalTime.setText(makeTimeString(total));
                mSeekBar.setMax(((int) total));
                mSeekBar.setProgress(((int) current));
            }
            else if(intent.getAction().equals(ACTION_MUSIC_UPDAE_ID3INFO)){
                Bundle bundle2 = intent.getBundleExtra("info");
                String songName=bundle2.getString("songName");
                String artistName=bundle2.getString("artistName");
                String albumName=bundle2.getString("albumName");
                mTrackName.setText(songName);
                mArtistName.setText(artistName);
                mAlbumName.setText(albumName);
                Log.e(TAG, "onReceive:" + "ACTION_MUSIC_UPDAE_ID3INFO"+"");
            }
            else if(intent.getAction().equals(ACTION_MUSIC_CHANGED)){

                Log.e(TAG, "onReceive: " + "change");
            }
            else if(intent.getAction().equals(ACTION_MUSIC_START)){
                Log.e(TAG, "onReceive: " + "ACTION_MUSIC_START");
            }
            else if(intent.getAction().equals(ACTION_MUSIC_PAUSE)){

            }
            else if(intent.getAction().equals(ACTION_MUSIC_PLAYINGSTORE_EJECT)){

            }

//            switch (intent.getAction()) {
//                case ACTION_MUSIC_INIT:
//                    Log.e(TAG, "onReceive: " + "ACTION_MUSIC_INIT");
//                    break;
//                case ACTION_MUSIC_INFO_UPDATED:
//                    Log.e(TAG, "onReceive: " + "update");
//                    Bundle bundle = intent.getBundleExtra("info");
//                    long current=bundle.getLong("current");
//                    long total=bundle.getLong("duration");
//                    current=current/1000;
//                    total=total/1000;
//                    mCurrentTime.setText(makeTimeString(current));
//                    mTotalTime.setText(makeTimeString(total));
//                    mSeekBar.setMax(((int) total));
//                    mSeekBar.setProgress(((int) current));
//                    break;
//                case ACTION_MUSIC_UPDAE_ID3INFO:
//                    Bundle bundle2 = intent.getBundleExtra("info");
//                    String songName=bundle2.getString("songName");
//                    String artistName=bundle2.getString("artistName");
//                    String albumName=bundle2.getString("albumName");
//                    mTrackName.setText(songName);
//                    mArtistName.setText(artistName);
//                    mAlbumName.setText(albumName);
//                    Log.e(TAG, "onReceive:" + "ACTION_MUSIC_UPDAE_ID3INFO"+"");
//                    break;
//                case ACTION_MUSIC_CHANGED:
//                    Log.e(TAG, "onReceive: " + "change");
////                    int playingIndex=MediaModel.getInstance().getPlayingIndex();
////                    Store store=MediaModel.getInstance().getPlayingStore();
////                    if (store == null) {
////                        break;
////                    }
////                    Uri uri = MediaModel.getInstance().getUriList(store).get(playingIndex);
////                    Log.d(TAG, "onReceive: " + uri);
////                    MusicItem item = MusicUtil.queryMusicFromContentProvider(getContext(), uri);
////
////                    if (item!=null) {
////                        Log.d(TAG, "onReceive: " + item.getFileName());
////                        if (item.getImage() != null) {
////                            imgAlbum.setImageBitmap(item.getImage());
////                        }else {
////                            imgAlbum.setImageResource(R.drawable.music);
////                        }
////                        tvTitle.setText(TextUtils.isEmpty(item.getTitle()) ? item.getFileName() : item.getTitle());
////                        tvArtist.setText(item.getArtist());
////                        tvAlbum.setText(item.getAlbum());
////                    }else {
////                        Log.d(TAG, "onReceive: " + "item is null");
////                    }
//                    break;
//                case ACTION_MUSIC_START:
//
//                    Log.e(TAG, "onReceive: " + "ACTION_MUSIC_START");
//                    break;
//                case ACTION_MUSIC_PAUSE:
//                    Log.e(TAG, "onReceive: " + "ACTION_MUSIC_PAUSE");
//
//                    break;
//                case ACTION_MUSIC_PLAYINGSTORE_EJECT:
//                    Log.e(TAG, "onReceive: " + "eject");
//                    break;
//                default:
//                    break;
//            }

        }
    }

}
