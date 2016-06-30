package com.gemtek.wifi_setting.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gemtek.wifi_setting.WifiRouter;
import com.gemtek.modulecloud.R;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListAdapter extends BaseAdapter {

    private LayoutInflater myInflater;
    private ArrayList< Map<String, Object> > list = null;
    private SparseBooleanArray mSelectedItemsIds;
    private Context mContext;

    public ListAdapter(final Context context, ArrayList< Map<String, Object> > list){
        mContext = context;
        myInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mSelectedItemsIds = new SparseBooleanArray();
        this.list = list;
    }

    public int getCount() {
        return list.size();
    }

    public Object getItem(int position) {
        return list.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewTag viewTag;
        if(convertView == null){
            convertView = myInflater.inflate(R.layout.list_adapter, null);

            viewTag = new ViewTag(
                    (TextView) convertView.findViewById(R.id.camera_title),
                    (ImageView) convertView.findViewById(R.id.camera_status)
                    );

            convertView.setTag(viewTag);
        } else {
            viewTag = (ViewTag) convertView.getTag();
        }

        viewTag.title.setText(list.get(position).get("ssid").toString());
        Log.d("ansel", ">>"+list.get(position).get("ssid").toString()+"/"+list.get(position).get("security").toString());
        //viewTag.title.setTextColor(Color.WHITE);

        return convertView;
    }

    public void LoadingData(List<WifiRouter> data){
        list.clear();
        list = new ArrayList<Map<String, Object>>();
        synchronized(data){
            for(int i=0; i<data.size(); i++){
                Map<String, Object> item = new HashMap<String, Object>();
                item.put("ssid", data.get(i).ssid);
                item.put("security", data.get(i).security);
                item.put("signal", data.get(i).signal);
                list.add(item);
                Log.d("ansel", "Loading >>"+data.get(i).ssid+"/"+data.get(i).security);
            }
        }
    }

    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }
    public void selectView(int position, boolean value) {
        if(value)
            mSelectedItemsIds.put(position, value);
        else
            mSelectedItemsIds.delete(position);
        notifyDataSetChanged();
    }
    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }
    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }

    public class ViewTag{
        TextView title;
        ImageView signal;

        public ViewTag(TextView title, ImageView status){
            this.title = title;
            this.signal = status;
        }
    }
}
