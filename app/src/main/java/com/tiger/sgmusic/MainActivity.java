package com.tiger.sgmusic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity {

	private static final String TAG =MainActivity.class.getSimpleName();
	
    private ViewPager mViewPager;
//    private MyFragmentPageAdapter mAdapter;
    private FragmentPagerAdapter mAdapter2;
    private List<Fragment> mFragments = new ArrayList<Fragment>();
    public MusicStorageFragment mMusicStorageFragment = new MusicStorageFragment();
    private MusicPlaybackFragment mMusicPlaybackFragment = new MusicPlaybackFragment();
    private MusicPlaylistFragment mMusicPlaylistFragment = new MusicPlaylistFragment();


    public static  String externalStoragePath= Environment.getExternalStorageDirectory().getPath();
    public static  String udiskPath="/mnt/udisk/udisk1";
    public static  String sdpath="/mnt/hd/sdcard";

    private int postion = 0;
    private MediaPlayer mp= new MediaPlayer();
	//private VideoView video1;
		
	//File file=new File("/storage/Aegean_Sea.mp4");
	//public void h
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Log.e(TAG, "++Oncreate++");
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        mViewPager = (ViewPager)findViewById(R.id.viewpager);


        Toast.makeText(MainActivity.this,externalStoragePath,Toast.LENGTH_LONG).show();

        FragmentManager fm = getSupportFragmentManager();
        //初始化自定义适配器

        //绑定自定义适配器
        mFragments.add(mMusicStorageFragment);
        mFragments.add(mMusicPlaybackFragment);
        mFragments.add(mMusicPlaylistFragment);
        mAdapter2 = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return mFragments.size();
            }

            @Override
            public Fragment getItem(int arg0) {
                return mFragments.get(arg0);
            }

            @Override
            public float getPageWidth(int position) {
                if (position == 0)
                    return super.getPageWidth(position) / 3;
                else if (position == 2) {
                    return super.getPageWidth(position) / 3;
                } else {
                    return super.getPageWidth(position);
                }
            }
        };
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(mAdapter2);
        mViewPager.setCurrentItem(1, true);

        //注册广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        //intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        //intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        //intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        intentFilter.addDataScheme("file");
        getApplicationContext().registerReceiver(mReceiver, intentFilter);
        Log.e(TAG, "++End Oncreate++");
    }

    /**
     *
     * @param mediaPath
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean mounted = false;

            if (Intent.ACTION_MEDIA_MOUNTED.equals(intent.getAction())) {
                //U盘/SD插入
                Log.e("BroadcastReceiver"," in ： "+intent.getData().getPath());
                String mediaPath=intent.getData().getPath();
                if(mediaPath==udiskPath)
                {

                }
                else if(mediaPath==sdpath){

                }
                mounted = true;
            } else {
                //U盘/sd拔除
                Log.e("BroadcastReceiver"," out : "+intent.getData().getPath());
                mounted = false;
            }


        }
    };


    @Override
    protected void onDestroy(){
        Log.e(TAG, "++onDestroy++");
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        Log.e(TAG, "++onResume++");
        super.onResume();
    }
    /**
     *
     * @return
     */
    public void play(String mediaPath) {
        mMusicPlaybackFragment.doPlayNew(mediaPath);
    }

    public void playNext(){
        mMusicPlaybackFragment.playNext();
    }

    public void hideLoading()
    {
        mMusicPlaybackFragment.hideLoading();
    }


    public void  setSongListSelectState(int posion){
        mMusicPlaylistFragment.setSongListSelectState(posion);
    }
    public List<String> getPlaylist(){
      return   mMusicPlaylistFragment.getMediaList();
    }
    public List<String> getSongNamelist(){
        return   mMusicPlaylistFragment.songNamelist;
    }
    public int getlistPosion(){
        return  mMusicPlaylistFragment.listPosion;
    }
    public  void setlistPosion(int listPosion){
        mMusicPlaylistFragment.setlistPosion(listPosion);
        setSongListSelectState(listPosion);
    }
    public void updatePlaylist(String filepath){
         mMusicPlaylistFragment.updatePlaylist(filepath);
    }
    public void switchToPage(int page) {
        mViewPager.setCurrentItem(page, true);
    }
    public void makeText(String text_show){
        Toast.makeText(getApplicationContext(),text_show,Toast.LENGTH_SHORT).show();
    }
}
