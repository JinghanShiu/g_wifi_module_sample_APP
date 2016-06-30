package com.gemtek.modulecloud;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.gemtek.general_cmd.HACommand;
import com.gemtek.wifi_setting.WifiList;
import com.gemtek.modulecloud.adapter.ModuleWiFiSettingListAdapter;
import com.gemtek.listview_custom.ListViewCustom;

import com.bee.callback.ConnectCallback;
import com.cloudAgent.CloudAgent;
import com.cloudAgent.CloudAgentCommand;
import com.cloudAgent.callback.SendCommandCallback;

public class ModuleWiFiSettingList extends Activity {
    private Context mContext;
    private ListViewCustom mListViewCustom;
    private List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
    private ModuleWiFiSettingListAdapter mListAdapter;
    private ModuleInfo mDeviceItems;
    private List<ModuleInfo> data;
    private ArrayList<String> ModuleMACList;
    private ArrayList<String> ModuleSMIDList;
    private String MACTemp, SMIDTemp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Intent intent = getIntent();
        ModuleMACList = intent.getStringArrayListExtra("MACList");
        ModuleSMIDList = intent.getStringArrayListExtra("SMIDList");
        mContext = this.getApplicationContext();
        mListViewCustom = (ListViewCustom)findViewById(R.id.mlist);
        mListViewCustom.setChoiceMode(ListViewCustom.CHOICE_MODE_SINGLE);
        try {
            data = new ArrayList<ModuleInfo>();
            for (int i = 0; i < ModuleMACList.size(); i++) {
                mDeviceItems = new ModuleInfo();
                mDeviceItems.mac = ModuleMACList.get(i);
                mDeviceItems.sm_id = ModuleSMIDList.get(i);
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
                SMIDTemp = data.get(position).sm_id;
                Log.d(ModuleCloud.TAG, "select " + position + ": " + MACTemp);
                ConnectWithPeer(SMIDTemp);
            }
        });

        mListAdapter = new ModuleWiFiSettingListAdapter(mContext, (ArrayList<Map<String, Object>>)list);
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
       UITools.blockUI(ModuleWiFiSettingList.this, message);
    }

    private void toast(final String message) {
        toast(message, Toast.LENGTH_SHORT);
    }

    private void toast(final String message, final int duration) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ModuleWiFiSettingList.this, message, duration).show();
            }
        });
    }

    private void ConnectWithPeer(String peerId) {
        ModuleCloud.mCloudAgent.disconnect(peerId, null);
        block(String.format("connect with %s ...", peerId));
        ModuleCloud.mCloudAgent.connect(peerId, new ConnectCallback() {
            @Override
            public void onFailure(String peerId, int connectionCode) {
                block(null);
                if (connectionCode == CloudAgent.ERR_MQTT_DISCONNECTED) {
                    toast("please login for remote connecting", Toast.LENGTH_SHORT);
                } else {
                    toast(String.format("connect with %s unsuccessfully: %d", peerId, connectionCode),
                            Toast.LENGTH_SHORT);
                }
            }
            @Override
            public void onSuccess(String peerId, final int cid) {
                block(null);
                toast(String.format("connect with %s successfully", peerId));
                settingwifi_flow(peerId);
            }
        });
    }

    private void settingwifi_flow(String peerId) {
        block("SendGetScanList");
        ModuleCloud.mCloudAgent.sendCommand(
            peerId,
            HACommand.getScanList(),
            new SendCommandCallback() {
                @Override
                public void onSendOut(String peerId, CloudAgentCommand command) {}
                @Override
                public void onResponse(String peerId, CloudAgentCommand command) {
                    block(null);
                    Intent i = new Intent(ModuleWiFiSettingList.this, WifiList.class);
                    i.putExtra("wifi_list", command.getVal());
                    i.putExtra("mLocalID", peerId);
                    startActivity(i);
                    finish();
                }
                @Override
                public void onError(String peerId, CloudAgentCommand command, int error) {
                    block(null);
                    toast("Module Cloud Command " + command.getCmd() + " Failure\n" +
                            ModuleCloud.mCloudAgent.getDescription(error), Toast.LENGTH_LONG);
                    finish();
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

