package com.tiger.sgmusic;

import android.content.ContentResolver;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
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
        MediaPlayer.OnCompletionListener, View.OnTouchListener ,
        MediaPlayer.OnPreparedListener{
	private static final String TAG =MusicPlaybackFragment.class.getSimpleName();

	//private VideoView video1;
//	private CustomVideoView mMusicPlayer;
    private MediaPlayer mMusicPlayer;
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

    private TextView mTrackName;
    private TextView mArtistName;
    private TextView mAlbumName;
    private NotificationManage mNotification;
   //public String mediaPlayPath="/storage/哦呢.mp4";vechiel
//    public String mediaPlayPath="storage/sdcard0/哦呢.mp4";   //storage/sdcard0/哦呢.mp4
    public static MainActivity mActivity;
    private  List<String> mediaFilelist = new ArrayList<String>();//new
    private  List<String> songNamelist=new ArrayList<String>();
    private   int listPosion=0;
    private Boolean isFisrtStart=true;
    private ImageView mPlayPause;
    private ImageView mPlaymode;

    private AudioManager audioManager;
    int mNum; //页号
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
//        mMusicPlayer=(CustomVideoView)view.findViewById(R.id.videoView1);
        mMusicPlayer =  new  MediaPlayer();
//                mMusicPlayer.setVideoPath(mediaPlayPath); //vechiel
//        mMusicPlayer.setScreenFull(true);
//        mMusicPlayer.requestFocus();
        mMusicPlayer.setOnCompletionListener(this);
//        mMusicPlayer.setOnTouchListener(this);
        mMusicPlayer.setOnPreparedListener(this);

        audioManager= (AudioManager) mActivity.getSystemService(mActivity.AUDIO_SERVICE);
        audioManager.requestAudioFocus(afChangeListener,
                // Use the music stream.
                AudioManager.STREAM_MUSIC, // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);

        mNotification=new NotificationManage();
//        mNotification.updateNotification("The fox");
    }

    @Override
    public void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
//        audioManager.abandonAudioFocus(afChangeListener);
//        mNotification.hideNotification();
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
                    mMusicPlayer.start();
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
    public static MainActivity  getMainActivity(){
        if (mActivity==null)
            mActivity=getMainActivity();
        return mActivity;
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
    private SeekBar.OnSeekBarChangeListener mSeekBarChanged = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int arg1, boolean fromuser) {
            if(mMusicPlayer.getDuration() <= 0){
                seekBar.setProgress(0);
                return;
            }
            if(!fromuser){
                return;
            }
            showBars(DEFAULT_TIMEOUT);
            long newpostion = seekBar.getProgress();
            mMusicPlayer.seekTo((int)newpostion*1000);
            mCurrentTime.setText(makeTimeString( newpostion));
            Log.i(TAG, "newposition = "+newpostion);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            showBars(DEFAULT_TIMEOUT);
            if(mMusicPlayer.getDuration() <= 0){
                return;
            }
            mDragging = true;
            mHandler.removeMessages(MSG_UPDATE_PROGRESS);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            showBars(DEFAULT_TIMEOUT);
            if (mMusicPlayer.getDuration() <= 0) {
                seekBar.setProgress(0);
                return;
            }
            mDragging = false;
            mHandler.sendEmptyMessage(MSG_UPDATE_PROGRESS);
        }
    };
    private int setProgress(){
        if (mMusicPlayer == null || mDragging) {
            return 0;
        }
        int position = mMusicPlayer.getCurrentPosition();
        int duration = mMusicPlayer.getDuration();
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
    private void updatePlayBtnState() {
        if(mMusicPlayer.isPlaying()) {
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
                    if (mMusicPlayer.isPlaying()) {
//                        notifyMediaInfo();
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
    @Override//一个media播放完成调用
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "=onCompletion");
        playNext();//自动播放下一首
    }
    /***
     *播放控制
     */
    public  void doPlayNew(String videoPath)
    {


        //更新ID3INfo
        mTrackName.setText(getTrackName(videoPath));
        mArtistName.setText(getArtistName(videoPath));
        mAlbumName.setText(getAlbumName(videoPath));
//        mCursor.close();
        mCursor=null;
//        fullScreen();
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


//        mHandler.sendEmptyMessage(MSG_NOTIFY_POSITION);
    }
    public  void stop(){
        mMusicPlayer.stop();
        mNotification.hideNotification();
        updatePlayBtnState();
    }
    public  void pause(){
        mMusicPlayer.pause();
        updatePlayBtnState();
    }

    public  void playNext(){

        mediaFilelist=mActivity.getPlaylist();
        if(mediaFilelist.isEmpty()) return;
        listPosion=mActivity.getlistPosion();
//        Log.e(TAG,"size:"+mediaFilelist.size());
        if(PlaybackService.mPlayMode==PlaybackService.PLAY_MODE_REPEAT_ALL)  //列表循环
        listPosion++;

        listPosion=listPosion%(mediaFilelist.size());
        mActivity.setlistPosion(listPosion);

        songNamelist=mActivity.getSongNamelist();//name
        mNotification.updateNotification(songNamelist.get(listPosion));

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
        songNamelist=mActivity.getSongNamelist();//name
        mNotification.updateNotification(songNamelist.get(listPosion));
        doPlayNew(mediaFilelist.get(listPosion));
    }
    /**
     *
     *
     */
    private void fullScreen(){
        layout_toolbar.setVisibility(View.VISIBLE);
//        mMusicPlayer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
    }


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
                mNotification.hideNotification();
                break;

            case R.id.img_previous:
                playPrevious();
                break;
            case R.id.img_play:
                if (mMusicPlayer.isPlaying()) {
                    mMusicPlayer.pause();
                }
                else {
                    mMusicPlayer.start();
                }
                updatePlayBtnState();
                break ;
            case R.id.img_next:
                playNext();
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
    @Override//开始播放一个media之前调用
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.d(TAG, "=onPrepared");
        int maxValue = mediaPlayer.getDuration() / 1000;
        Log.e("duration_video",String.valueOf(maxValue));//秒
        mHandler.removeMessages(MSG_UPDATE_PROGRESS);
        mHandler.sendEmptyMessage(MSG_UPDATE_PROGRESS);

        if (layout_Loding.getVisibility() == View.VISIBLE) {
            layout_Loding.setVisibility(View.GONE);
        }

        if(maxValue > 0) {
            // reset progress bar;
            mSeekBar.setProgress(0);
            mSeekBar.setMax(maxValue);
            mTotalTime.setText(makeTimeString(maxValue));
        }


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
            ContentResolver resolver = getActivity().getContentResolver();
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
