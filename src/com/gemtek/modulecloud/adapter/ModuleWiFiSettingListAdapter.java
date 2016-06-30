package com.gemtek.modulecloud.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.content.Context;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gemtek.modulecloud.R;
import com.gemtek.modulecloud.ModuleCloud;
import com.gemtek.modulecloud.ModuleInfo;

public class ModuleWiFiSettingListAdapter extends BaseAdapter {
    private LayoutInflater myInflater;
    private ArrayList<Map<String, Object>> list = null;
    private SparseBooleanArray mSelectedItemsIds;

    public ModuleWiFiSettingListAdapter(final Context context, ArrayList<Map<String, Object>> list) {
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
        if (convertView == null){
            convertView = myInflater.inflate(R.layout.module_list_adapter, null);

            viewTag = new ViewTag((TextView) convertView.findViewById(R.id.camera_title),
                    (ImageView) convertView.findViewById(R.id.camera_status));

            convertView.setTag(viewTag);
        } else {
            viewTag = (ViewTag) convertView.getTag();
        }

        viewTag.title.setText(list.get(position).get("mac").toString());

        Log.d(ModuleCloud.TAG, "[getView] mac: " + list.get(position).get("mac").toString());
        return convertView;
    }

    public void LoadingData(List<ModuleInfo> data) {
        list.clear();
        list = new ArrayList<Map<String, Object>>();

        synchronized(data) {
            for (int i = 0; i < data.size(); i++) {
                Map<String, Object> item = new HashMap<String, Object>();
                item.put("mac", data.get(i).mac);
                list.add(item);

                Log.d(ModuleCloud.TAG, "[LoadingData] mac: " + data.get(i).mac);
            }
        }
    }

    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    public void selectView(int position, boolean value) {
        if (value)
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

    public class ViewTag {
        TextView title;
        ImageView signal;

        public ViewTag(TextView title, ImageView status) {
            this.title = title;
            this.signal = status;
        }
    }
}

