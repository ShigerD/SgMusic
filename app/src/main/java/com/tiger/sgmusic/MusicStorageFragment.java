package com.tiger.sgmusic;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;


public class MusicStorageFragment extends Fragment{
    private static final String TAG = MusicStorageFragment.class.getSimpleName();
    int mNum; //页号


    private ListView listView;//new
    private List<String> storelist=new ArrayList<String>();
    private String   udiskPath="/mnt/udisk";
    private MainActivity mActivity;
    private ListViewAdapter mlistAdapter;

    public String [] mediaListArry={"媒体列表:","硬盘\n","SD卡\n","U盘\n"};

    public static MusicStorageFragment newInstance(int num) {
    	MusicStorageFragment fragment= new MusicStorageFragment();
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("num", num);
        fragment.setArguments(args);
        return fragment;
    }
  
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "++Oncreate++");
        super.onCreate(savedInstanceState);
        //这里我只是简单的用num区别标签，其实具体应用中可以使用真实的fragment对象来作为叶片
        mNum = getArguments() != null ? getArguments().getInt("num") : 1;
    }
    
    /**为Fragment加载布局时调用**/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.e(TAG, "++OncreateView++");
        View view = inflater.inflate(R.layout.playback_list_layout, null);
        mActivity = (MainActivity)getActivity();
        listView = (ListView)view.findViewById(R.id.main_list);
        listView.setOnItemClickListener(mOnItemClickListener);
        mlistAdapter=new ListViewAdapter(mActivity,storelist);
        listView.setAdapter(mlistAdapter);
        upateStoreList(" ");
        mlistAdapter.setSelectPosition(1);

        return view;
    }

    public void upateStoreList(String tem ){
//        String [] temArry=new String[4];
        for(int i=0;i<mediaListArry.length;i++){
            if(i!=0)
                mediaListArry[i]=mediaListArry[i]+tem;
            storelist.add(mediaListArry[i]);
        }
    }

    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

            Log.e("storelist",storelist.get(arg2));

            switch (arg2){
                case 1:
                    mActivity.updatePlaylist(MainActivity.externalStoragePath);
                    mActivity.switchToPage(2);
                    mlistAdapter.setSelectPosition(arg2);
                    break;
                case 2:
                    mActivity.updatePlaylist(MainActivity.sdpath);
                    mActivity.switchToPage(2);
                    mlistAdapter.setSelectPosition(arg2);
                    break;
                case 3:
                    mActivity.updatePlaylist(MainActivity.udiskPath);
                    mActivity.switchToPage(2);
                    mlistAdapter.setSelectPosition(arg2);
                    break;
                default:
                    break;
            }


        }
    };
}
