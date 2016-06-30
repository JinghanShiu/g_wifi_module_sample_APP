package com.gemtek.modulecloud;

import java.util.List;
import java.util.ArrayList;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;

import com.bee.Peer;
import com.bee.utility.Log;
import com.bee.callback.GetDeviceListCallback;
import com.bee.callback.LogoutCallback;
import com.serviceManager.Device;

public class MainPage extends FragmentActivity {
    private ArrayList<String> ModuleMACList = new ArrayList<String>();
    private ArrayList<String> ModuleSMIDList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_fragment);

        FragmentTabHost tabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);

        tabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

        tabHost.addTab(tabHost.newTabSpec("Remote Control").setIndicator("Remote Control"),
                RemoteFragment.class,
                null);
        tabHost.addTab(tabHost.newTabSpec("Local Setting").setIndicator("Local Setting"),
                LocalFragment.class,
                null);

        updateTab(tabHost);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ModuleCloud.mCloudAgent.logout(new LogoutCallback() {
            @Override
            public void onCompleted() {
                Log.e(ModuleCloud.TAG, "Logout Success");
                toast("log out successfully", Toast.LENGTH_SHORT);
            }
        });
    }

    private void block(final String message) {
       UITools.blockUI(MainPage.this, message);
    }

    private void toast(final String message) {
        toast(message, Toast.LENGTH_SHORT);
    }

    private void toast(final String message, final int duration) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainPage.this, message, duration).show();
            }
        });
    }

    private void updateTab(final FragmentTabHost tabHost) {
        tabHost.getTabWidget().setShowDividers(0);

        for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
            tabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.tab_indicator_holo);
            TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextSize(13);
            tv.setTextColor(0xe5ffffff);
        }
    }

    /****************************************************************************************
        all functions
    ******************************************************************************************/
    public void getmodulelist(View v) {
        Log.d(ModuleCloud.TAG, "getmodulelist");

        // modify for library latency
        Intent i = new Intent(MainPage.this, ModuleList.class);
        startActivity(i);

        /*block("GetModuleList");
        ModuleCloud.mCloudAgent.getDeviceListFromCloud(new GetDeviceListCallback() {
            @Override
            public void onSuccess(Device[] deviceList) {
                Log.i(ModuleCloud.TAG, "Device List: ");
                Log.i(ModuleCloud.TAG, "===============================================");
                if (deviceList.length == 0) {
                    Log.e(ModuleCloud.TAG, "empty");
                    toast("module list is empty", Toast.LENGTH_SHORT);
                } else {
                    toast(deviceList.length + " module(s) in " + ModuleCloud.sUserName, Toast.LENGTH_SHORT);

                    ModuleMACList.clear();
                    ModuleSMIDList.clear();
                    int index = 1;
                    for (Device device : deviceList) {
                        Log.i(ModuleCloud.TAG, "%d. %s", index++, device);
                        ModuleMACList.add(device.mac);
                        ModuleSMIDList.add(device.gid);
                    }

                    Intent i = new Intent(MainPage.this, ModuleList.class);
                    i.putExtra("MACList", ModuleMACList);
                    i.putExtra("SMIDList", ModuleSMIDList);
                    startActivity(i);
                }
                Log.i(ModuleCloud.TAG, "===============================================");
                block(null);
            }
            @Override
            public void onFailure(int errorCode, String message) {
                Log.i(ModuleCloud.TAG, "Get Device List Failure!(" + message + ")");
                toast("get module list unsuccessfully(" + message + ")", Toast.LENGTH_SHORT);
                block(null);
            }
        });*/
    }

    public void bindmodule(View v) {
        Log.d(ModuleCloud.TAG, "bindmodule");

        block("GetModuleList(LOCAL)");
        List<Peer> list = ModuleCloud.mCloudAgent.getPeerListInCurrentLAN();
        ModuleMACList.clear();
        ModuleSMIDList.clear();
        for (Peer peer : list) {
            if (peer.deviceType.equals("CLOUD_MODULE")) {
                ModuleMACList.add(peer.name);
                ModuleSMIDList.add(peer.id);
            }
        }
        block(null);

        if (ModuleMACList.size() == 0) {
            toast("can't fetch cloud module from local network");
            return;
        }

        Intent i = new Intent(MainPage.this, ModuleBindingList.class);
        i.putExtra("MACList", ModuleMACList);
        i.putExtra("SMIDList", ModuleSMIDList);
        startActivity(i);
    }

    public void unbindmodule(View v) {
        Log.d(ModuleCloud.TAG, "unbindmodule");
        block("GetModuleList");
        ModuleCloud.mCloudAgent.getDeviceListFromCloud(new GetDeviceListCallback() {
            @Override
            public void onSuccess(Device[] deviceList) {
                Log.i(ModuleCloud.TAG, "Device List: ");
                Log.i(ModuleCloud.TAG, "===============================================");
                if (deviceList.length == 0) {
                    Log.e(ModuleCloud.TAG, "empty");
                    toast("module list is empty", Toast.LENGTH_SHORT);
                } else {
                    toast(deviceList.length + " module(s) in " + ModuleCloud.sUserName, Toast.LENGTH_SHORT);

                    ModuleMACList.clear();
                    int index = 1;
                    for (Device device : deviceList) {
                        Log.i(ModuleCloud.TAG, "%d. %s", index++, device);
                        ModuleMACList.add(device.mac);
                    }

                    Intent i = new Intent(MainPage.this, ModuleUnbindingList.class);
                    i.putExtra("MACList", ModuleMACList);
                    startActivity(i);
                }
                Log.i(ModuleCloud.TAG, "===============================================");
                block(null);
            }
            @Override
            public void onFailure(int errorCode, String message) {
                Log.i(ModuleCloud.TAG, "Get Device List Failure!(" + message + ")");
                toast("get module list unsuccessfully(" + message + ")", Toast.LENGTH_SHORT);
                block(null);
            }
        });
    }

    public void setmodulewifi(View v) {
        Log.d(ModuleCloud.TAG, "setmodulewifi");

        block("GetModuleList(LOCAL)");
        List<Peer> list = ModuleCloud.mCloudAgent.getPeerListInCurrentLAN();
        ModuleMACList.clear();
        ModuleSMIDList.clear();
        for (Peer peer : list) {
            if (peer.deviceType.equals("CLOUD_MODULE")) {
                ModuleMACList.add(peer.name);
                ModuleSMIDList.add(peer.id);
            }
        }
        block(null);

        if (ModuleMACList.size() == 0) {
            toast("can't fetch cloud module from local network");
            return;
        }

        Intent i = new Intent(MainPage.this, ModuleWiFiSettingList.class);
        i.putExtra("MACList", ModuleMACList);
        i.putExtra("SMIDList", ModuleSMIDList);
        startActivity(i);
    }

    public void setbluemix(View v) {
        Log.d(ModuleCloud.TAG, "setbluemix");

        block("GetModuleList(LOCAL)");
        List<Peer> list = ModuleCloud.mCloudAgent.getPeerListInCurrentLAN();
        ModuleMACList.clear();
        ModuleSMIDList.clear();
        for (Peer peer : list) {
            if (peer.deviceType.equals("CLOUD_MODULE")) {
                ModuleMACList.add(peer.name);
                ModuleSMIDList.add(peer.id);
            }
        }
        block(null);

        if (ModuleMACList.size() == 0) {
            toast("can't fetch cloud module from local network");
            return;
        }

        Intent i = new Intent(MainPage.this, ModuleBluemixSettingList.class);
        i.putExtra("MACList", ModuleMACList);
        i.putExtra("SMIDList", ModuleSMIDList);
        startActivity(i);
    }

    public void upgradefirmware(View v) {
        Log.d(ModuleCloud.TAG, "upgradefirmware");

        block("GetModuleList(LOCAL)");
        List<Peer> list = ModuleCloud.mCloudAgent.getPeerListInCurrentLAN();
        ModuleMACList.clear();
        ModuleSMIDList.clear();
        for (Peer peer : list) {
            if (peer.deviceType.equals("CLOUD_MODULE")) {
                ModuleMACList.add(peer.name);
                ModuleSMIDList.add(peer.id);
            }
        }
        block(null);

        if (ModuleMACList.size() == 0) {
            toast("can't fetch cloud module from local network");
            return;
        }

        Intent i = new Intent(MainPage.this, ModuleFirmwareUpgradeList.class);
        i.putExtra("MACList", ModuleMACList);
        i.putExtra("SMIDList", ModuleSMIDList);
        startActivity(i);
    }
}

