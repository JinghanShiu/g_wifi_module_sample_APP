package com.gemtek.modulecloud;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.gemtek.modulecloud.adapter.ModuleUnbindingListAdapter;
import com.gemtek.listview_custom.ListViewCustom;

import com.bee.callback.RemoveDeviceCallback;

public class ModuleUnbindingList extends Activity {
    private Context mContext;
    private ListViewCustom mListViewCustom;
    private List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
    private ModuleUnbindingListAdapter mListAdapter;
    private ModuleInfo mDeviceItems;
    private List<ModuleInfo> data;
    private ArrayList<String> ModuleMACList;
    private String MACTemp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Intent intent = getIntent();
        ModuleMACList = intent.getStringArrayListExtra("MACList");
        mContext = this.getApplicationContext();
        mListViewCustom = (ListViewCustom)findViewById(R.id.mlist);
        mListViewCustom.setChoiceMode(ListViewCustom.CHOICE_MODE_SINGLE);
        try {
            data = new ArrayList<ModuleInfo>();
            for (int i = 0; i < ModuleMACList.size(); i++) {
                mDeviceItems = new ModuleInfo();
                mDeviceItems.mac = ModuleMACList.get(i);
                data.add(mDeviceItems);
            }
        } catch (Exception e) {
            Log.e(ModuleCloud.TAG, e.toString());
        }

        mListViewCustom.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                MACTemp = data.get(position).mac;
                Log.d(ModuleCloud.TAG, "select " + position + ": " + MACTemp);
                ShowDialog();
            }
        });

        mListAdapter = new ModuleUnbindingListAdapter(mContext, (ArrayList<Map<String, Object>>)list);
        RefreshListView();
        mListViewCustom.setAdapter(mListAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void block(final String message) {
       UITools.blockUI(ModuleUnbindingList.this, message);
    }

    private void toast(final String message) {
        toast(message, Toast.LENGTH_SHORT);
    }

    private void toast(final String message, final int duration) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ModuleUnbindingList.this, message, duration).show();
            }
        });
    }

    private void ShowDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                ModuleUnbindingList.this);
        alertDialogBuilder.setTitle("Unbind " + MACTemp);

        alertDialogBuilder
        .setCancelable(false)
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
         public void onClick(DialogInterface dialog, int id) {
             unbindingAPI();
         }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
         public void onClick(DialogInterface dialog, int id) {
          dialog.cancel();
         }
        })
        .show();
    }

    private void unbindingAPI() {
        block("Unbind " + MACTemp);
        ModuleCloud.mCloudAgent.removeDeviceFromCloud(MACTemp, new RemoveDeviceCallback() {
            @Override
            public void onSuccess(String deviceMac) {
                toast(String.format("remove module %s successfully", deviceMac));
                block(null);
                finish();
            }
            @Override
            public void onFailure(String deviceMac, int errorCode, String message) {
                toast(String.format("remove module %s unsuccessfully(%s)", deviceMac, message));
                block(null);
            }
        });
    }

    private void RefreshListView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (data) {
                    mListAdapter.LoadingData(data);
                    mListAdapter.notifyDataSetChanged();
                }
            }
        });
    }
}

