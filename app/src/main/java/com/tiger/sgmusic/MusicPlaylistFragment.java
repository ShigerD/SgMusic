package com.tiger.sgmusic;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * 用于创建Fragment对象，作为ViewPager的叶片

 *
 */
public class MusicPlaylistFragment extends Fragment {
    private final String TAG = MusicPlaylistFragment.class.getSimpleName();
    private ListView listView;//new
    private   List<String> mediaFilelist = new ArrayList<String>();//new
    public  List<String> songNamelist  =new ArrayList<String>();//new
    public  List<String> mediaFilelistHd = new ArrayList<String>();//new
    public  List<String> mediaFilelistUdisk = new ArrayList<String>();//new
    public  List<String> mediaFilelistSdcard = new ArrayList<String>();//new
    public  List<String> mediaDisplayList = new ArrayList<String>();//new

    public  int listPosion=0;
    private Boolean isFisrtStart=true;
    private MainActivity mActivity;
    int mNum; //页号
    public int getlistPosion(){
        return  this.listPosion;
    }
    public  void setlistPosion(int listPosion){
        this.listPosion=listPosion;
    }
    public List<String> getMediaList(){
        return this.mediaFilelist;
    }

    private ListViewAdapter mlistviewAdapter;
    MainActivity activity;

    public static MusicPlaylistFragment newInstance(int num) {
        MusicPlaylistFragment fragment = new MusicPlaylistFragment();
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
        View view = inflater.inflate(R.layout.playback_list_layout, container, false);
        listView = (ListView)view.findViewById(R.id.main_list);
        listView.setOnItemClickListener(mOnItemClickListener);
        activity=(MainActivity) getActivity();
//        mlistviewAdapter=new ListViewAdapter(getActivity(), mediaFilelist);
        mlistviewAdapter=new ListViewAdapter(getActivity(), mediaDisplayList);//mediaDisplayList
        listView.setAdapter(mlistviewAdapter);

        updatePlaylist(MainActivity.externalStoragePath);
//        new ListFileTask(MainActivity.udiskPath, mediaFilelistUdisk).execute("Hello2");
        Log.e("filepath.isEmpty()",mediaFilelistUdisk.toString());

        return view;
    }

    public void updatePlaylist(String filepath){
        try{

            mediaFilelist.clear();
            mediaDisplayList.clear();
            new ListFileTask(filepath, mediaDisplayList).execute("Hello");
            Toast.makeText(getActivity(),filepath,Toast.LENGTH_LONG).show();

        }
        catch (Exception ex){
            Log.e("filepath.isEmpty()","filepath.isEmpty()");
            Toast.makeText(getActivity(),filepath+"不存在或没有视频",Toast.LENGTH_SHORT).show();
        }

    }
    //input: absolutePath  eg:  /sdcard

    //
    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
         //   VideoTabActivity activity = (VideoTabActivity)getActivity();
         //   activity.play(arg2);
//            Log.e("+++++","arg2:"+arg2);//第几个
            Log.e("+++++","filepath"+mediaFilelist.get(arg2));

            activity.play(mediaFilelist.get(arg2));
            setlistPosion(arg2);
            mlistviewAdapter.setSelectPosition(arg2);

        }
    };

    public void setSongListSelectState(int posion){
        mlistviewAdapter.setSelectPosition(posion);
    }

    private class ListFileTask extends AsyncTask<String, Integer, List<String>> {

        private List<String> newGetfileDir(String Path ) {

            File file=new File(Path);

            if(file.list()==null)
                return mediaFilelist;
            File[] subFile=file.listFiles();
            String name = "";
            String type = "";
            String filepath="";
            for (File f : subFile) {
                // temFilelist.add(f.getPath());
                if(f.isDirectory())
                {
                    newGetfileDir(f.getPath());
                }
                else{//是一个wenjian
                    name = f.getName().toLowerCase();
                    type = name.substring(name.lastIndexOf(".") + 1);
                    filepath=f.getPath().substring(0,getFilepathIndex(f.getPath()) + 1);
//                    try {
//                        filepath=f.getCanonicalPath();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                    if(type.equals("mp3")) {
                        mediaFilelist.add(f.getPath());
                        songNamelist.add(f.getName());//获取对应歌名
                        mediaDisplayList.add(f.getName()+"\n"+filepath);
                        Log.e("#########",f.getPath());
                        i++;
                        publishProgress(i);
                    }

                    }
                }

            return mediaFilelist;

            }
        private int getFilepathIndex(String path){
            int outIndex=0;
            byte[] tembyte=path.getBytes();
            for(int i=0;i<tembyte.length;i++)
                if(tembyte[i]=='/'){
                    outIndex=i;
                }

            return outIndex;
        }
        private int i=0;

        private String filepath_as;

        public ListFileTask(String filepath,List<String> listArry){
            this.filepath_as=filepath;
        }

        @Override
        protected List<String> doInBackground(String... strings) {
            Log.e("+++++","doInback");
            //if(!mediaFilelist.isEmpty())
            //mediaFilelist.clear();
            mediaFilelist=(newGetfileDir(filepath_as)) ;
            return mediaFilelist;
            //listArry=(getfileDir(listArry,filepath_as)) ;
            //return listArry;
        }

        protected void onProgressUpdate(Integer... values) {
            // TODO Auto-generated method stub
//            listView.setAdapter(new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,mediaFilelist));
            Log.e("#########",values[0].toString());
//

            if(isFisrtStart)
            {
                activity.hideLoading();
                activity.play(mediaFilelist.get(0));
                activity.setSongListSelectState(0);
                new NotificationManage(songNamelist.get(0));
                isFisrtStart=false;
            }
            Log.e("+++++","onProgressUpdate");
            //super.Integer(values);
            listView.setAdapter(mlistviewAdapter);//更新UI
        }

        @Override
        protected void onPreExecute() {

            Log.e("+++++","onPreExrcute");
            super.onPreExecute();
        }
        @Override//finished commit
        protected void onPostExecute(List<String> result) {
            Log.e("+++++","onPostExecute");

            listView.setAdapter(mlistviewAdapter);//结束更新UI
//            mActivity.makeText("扫描到"+"首歌曲");//异常
            Toast.makeText(getActivity(),"扫描到"+mediaFilelist.size()+"首歌曲",Toast.LENGTH_SHORT).show();
//            mediaFilelist=result;
        }
    }
    /**
     * unused
     */
    private class PlayAsyncTask extends AsyncTask<String, Integer, String>
    {
        MainActivity activity=(MainActivity)getActivity();
        int i=0;
        public PlayAsyncTask() {
            Log.e("~~~~~~~~~","onStruct");
        }

        @Override
        protected void onPostExecute(String result) {
            Log.e("~~~~~~~~~","onPostExecute");
//            activity.hideLoading();
//            activity.play(activity.getPlaylist().get(0));
            super.onPostExecute(result);
        }

        //在PreExcute执行后被启动AysncTask的后台线程调用，将结果返回给UI线程
        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            Log.e("~~~~~~~~~~~","ondoInback_begin");

            while(true)
                if(activity.getPlaylist().size()==0)

                    try {
                        Thread.sleep(1000);
                        Log.e("~~~~~~~~~~~",activity.getPlaylist().toString());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                else
                    return null;

        }
    }
}
