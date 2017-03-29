package com.tiger.sgmusic;

import java.util.List;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ListViewAdapter extends BaseAdapter {

    private int defaultSelection = -1;
    private Context mContext;
    private List<String> list;
    private int text_selected_color;
    private int bg_selected_color;
    private ColorStateList colors;

    public ListViewAdapter(Context mContext, List<String> list) {
        // TODO Auto-generated constructor stub
        this.mContext = mContext;
        this.list = list;
        Resources resources = mContext.getResources();
        text_selected_color = resources.getColor(R.color.colorWhite);// 文字选中的颜色
//        bg_selected_color = resources.getColor(R.color.bg_selected);// 背景选中的颜色
        colors = mContext.getResources().getColorStateList(
                R.color.colorBlack);// 文字未选中状态的selector
        resources = null;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list == null ? 0 : list.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return list.get(position);

    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.listview_item, parent, false);
            viewHolder.txt_item = (TextView) convertView
                    .findViewById(R.id.txt_item);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.txt_item.setText(getItem(position).toString());

        if (position == defaultSelection) {// 选中时设置单纯颜色
            viewHolder.txt_item.setTextColor(text_selected_color);
//            convertView.setBackgroundColor(bg_selected_color);
        } else {// 未选中时设置selector
            viewHolder.txt_item.setTextColor(colors);
//            convertView.setBackgroundResource(R.drawable.list_item_bg_selected);
        }
        return convertView;
    }

    class ViewHolder {
        TextView txt_item;
    }

    /**
     * @param position
     *            设置高亮状态的item
     */
    public void setSelectPosition(int position) {
        if (!(position < 0 || position > list.size())) {
            defaultSelection = position;
            notifyDataSetChanged();
        }
    }

}
