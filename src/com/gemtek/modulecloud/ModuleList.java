package com.gemtek.modulecloud;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.gemtek.modulecloud.adapter.ModuleListAdapter;
import com.gemtek.listview_custom.ListViewCustom;
import com.gemtek.listview_custom.OverScrollCallback;
import com.gemtek.general_cmd.HACommand;

import com.cloudAgent.CloudAgent;
import com.cloudAgent.CloudAgentCommand;
import com.bee.callback.ConnectCallback;

// modify for library latency
import com.bee.callback.GetDeviceListCallbackJ;
import com.google.gson.Gson;

public class ModuleList extends Activity {
    private Context mContext;
    private ListViewCustom mListViewCustom;
    private List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
    private static ModuleListAdapter mListAdapter;
    private ModuleInfo mDeviceItems;
    private static List<ModuleInfo> data = new ArrayList<ModuleInfo>();

    // modify for library latency
    /*private ArrayList<String> ModuleMACList;
    private ArrayList<String> ModuleSMIDList;*/

    public static final String ConnectingStatus = "Connecting";
    public static final String ConnectedStatus = "Connected";
    public static final String DisconnectedStatus = "Disconnected";

    // mofify for library latency
    private String OverScrollLock = "unlocked";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        ModuleCloud.mDisconnectionCallback = new DisconnectionCallback() {
            @Override
            public void onDisconnection(String peerId) {
                Log.d(ModuleCloud.TAG, "onDisconnection");

                // modify for library latency
                /*RefreshStatus(peerId, DisconnectedStatus);
                RefreshListView();*/
            };
        };

        // modify for library latency
        /*Intent intent = getIntent();
        ModuleMACList = intent.getStringArrayListExtra("MACList");
        ModuleSMIDList = intent.getStringArrayListExtra("SMIDList");*/
        mContext = this.getApplicationContext();
        mListViewCustom = (ListViewCustom)findViewById(R.id.mlist);
        mListViewCustom.setChoiceMode(ListViewCustom.CHOICE_MODE_SINGLE);
        /*synchronized (data) {
            try {
                data.clear();
                for (int i = 0; i < ModuleMACList.size(); i++) {
                    mDeviceItems = new ModuleInfo();
                    mDeviceItems.mac = ModuleMACList.get(i);
                    mDeviceItems.sm_id = ModuleSMIDList.get(i);
                    mDeviceItems.status = DisconnectedStatus;
                    data.add(mDeviceItems);
                }
            } catch (Exception e) {
                Log.e(ModuleCloud.TAG, e.toString());
            }
        }*/

        mListViewCustom.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                synchronized (data) {
                    Log.d(ModuleCloud.TAG, "select " + position + ": " + data.get(position).mac);

                    // modify for library latency
                    /*if (data.get(position).status.equals(ConnectedStatus)) {
                        Intent i = new Intent(ModuleList.this, Sending.class);
                        i.putExtra("SMID", data.get(position).sm_id);
                        startActivity(i);
                    } else toast("please connected with " + data.get(position).sm_id + " first");*/

                    ConnetToModule(data.get(position).sm_id);
                }
            }
        });

        mListViewCustom.setOverScrollCallback(new OverScrollCallback() {
            @Override
            public void onOverScrolled() {
                Log.d(ModuleCloud.TAG, "onOverScrolled");

                // modify for library latency
                //if (ConnectingStatusCheck() == true) return;
                RefreshList();
            }
        });

        mListAdapter = new ModuleListAdapter(mContext, (ArrayList<Map<String, Object>>)list);

        // modify for library latency
        //RefreshListView();
        mListViewCustom.setAdapter(mListAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        synchronized (ModuleCloud.RCCLock) {
            ModuleCloud.mReceivingCommandCallback = new ReceivingCommandCallback() {
                @Override
                public void onReceivingCommand(String peerId, CloudAgentCommand cloudAgentCommand) {
                    if (cloudAgentCommand.getCmdType() == HACommand.HA_CONSOLE_CMD_TYPE &&
                            cloudAgentCommand.getClasses() == HACommand.HA_CONSOLE_CLASS) {
                        alert("Recieve " + cloudAgentCommand.getCmd() + " Command", cloudAgentCommand.getVal());
                    } else if (cloudAgentCommand.getCmdType() == HACommand.HA_CUSTOMER_CMD_TYPE &&
                            cloudAgentCommand.getClasses() == HACommand.HA_CONSOLE_CLASS) {
                        String B64DString = HACommand.Base64Decode(cloudAgentCommand.getVal());
                        if (B64DString == null) return;

                        String recv_msg = null;
                        try {
                            String customer_tag = "data";
                            JSONObject obj = new JSONObject(B64DString);
                            recv_msg = obj.getString(customer_tag);
                        } catch (JSONException jsonException) {
                        } catch (Exception e) {
                        }

                        if (recv_msg == null || recv_msg.equals("")) {
                            Log.d(ModuleCloud.TAG, "onReceivingCommand fail");
                            return;
                        }

                        alert("Recieve " + cloudAgentCommand.getCmd() + " Command", recv_msg);
                    } else Log.d(ModuleCloud.TAG, "onReceivingCommand fail");
                };
            };
        }

        // mofify for library latency
        ModuleCloud.mServiceManagerCallback = new ServiceManagerCallback() {
            @Override
            public void onServiceManager(String command) {
                Log.d(ModuleCloud.TAG, "onServiceManager");

                if (command.equals("offline") || command.equals("online") ||
                        command.equals("add") || command.equals("del")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            RefreshList();
                        }
                    });
                }
            };
        };

        RefreshList();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // mofify for library latency
        ModuleCloud.mServiceManagerCallback = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ModuleCloud.mDisconnectionCallback = null;
    }

    // mofify for library latency
    private void block(final String message) {
       UITools.blockUI(ModuleList.this, message);
    }

    // mofify for library latency
    /*private void toast(final String message) {
        toast(message, Toast.LENGTH_SHORT);
    }*/

    private void toast(final String message, final int duration) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ModuleList.this, message, duration).show();
            }
        });
    }

    private void alert(final String title, final String message) {
        UITools.showAlertDialog(ModuleList.this, title, message);
    }

    private class ResponseJsonObject {
        Device[] device_list;
    }

    private void RefreshList() {
        // modify for library latency
        /*toast("refresh list ...");
        String peerId_temp = null;
        synchronized (data) {
            for (int i = 0; i < data.size(); i++) {
                peerId_temp = data.get(i).sm_id;
                RefreshStatus(peerId_temp, ConnectingStatus);
                RefreshListView();
                ConnetToModule(peerId_temp);
            }
        }*/

        synchronized (OverScrollLock) {
            if (OverScrollLock.equals("unlocked")) {
                OverScrollLock = "locked";
                block("GetModuleList");
            } else return;
        }

        ModuleCloud.mCloudAgent.getDeviceListFromCloud(new GetDeviceListCallbackJ() {
            @Override
            public void onSuccess(String rawJSON) {
                ResponseJsonObject jsonObj = new Gson().fromJson(rawJSON, ResponseJsonObject.class);
                if (jsonObj.device_list.length == 0) {
                    Log.e(ModuleCloud.TAG, "empty");
                    toast("module list is empty", Toast.LENGTH_SHORT);
                    finish();
                } else {
                    int i = 0;
                    synchronized (data) {
                        data.clear();
                        for (Device device : jsonObj.device_list) {
                            Log.i(ModuleCloud.TAG, String.format("%d. %s", i++, device));
                            mDeviceItems = new ModuleInfo();
                            mDeviceItems.mac = device.mac;
                            mDeviceItems.sm_id = device.gid;
                            if (device.isOnline == true)
                                mDeviceItems.status = ConnectedStatus;
                            else
                                mDeviceItems.status = DisconnectedStatus;
                            data.add(mDeviceItems);
                        }
                    }

                    RefreshListView();
                }

                synchronized (OverScrollLock) {
                    if (OverScrollLock.equals("locked")) {
                        OverScrollLock = "unlocked";
                        block(null);
                    } else return;
                }
            }
            @Override
            public void onFailure(int errorCode, String message) {
                Log.i(ModuleCloud.TAG, "Get Device List Failure!(" + message + ")");
                toast("get module list unsuccessfully(" + message + ")", Toast.LENGTH_SHORT);
                finish();

                synchronized (OverScrollLock) {
                    if (OverScrollLock.equals("locked")) {
                        OverScrollLock = "unlocked";
                        block(null);
                    }
                    else return;
                }
            }
        });
    }

    private void ConnetToModule(String peerId) {
        // modify for library latency
        synchronized (OverScrollLock) {
            if (OverScrollLock.equals("unlocked")) {
                OverScrollLock = "locked";
                block("connect with " + peerId + " ...");
            } else return;
        }

        ModuleCloud.mCloudAgent.disconnect(peerId, null);
        ModuleCloud.mCloudAgent.connect(peerId, new ConnectCallback() {
            @Override
            public void onFailure(String peerId, int connectionCode) {
                // modify for library latency
                synchronized (OverScrollLock) {
                    if (OverScrollLock.equals("locked")) {
                        OverScrollLock = "unlocked";
                        block(null);
                    } else return;
                }

                if (connectionCode == CloudAgent.ERR_IN_PROGRESS) {
                    Log.d(ModuleCloud.TAG, "connection in progress");
                    return;
                }

                if (connectionCode == CloudAgent.ERR_MQTT_DISCONNECTED)
                    Log.d(ModuleCloud.TAG, "please login for remote connecting");
                else
                    Log.d(ModuleCloud.TAG, String.format("connect with %s unsuccessfully: %d", peerId, connectionCode));
                /*RefreshStatus(peerId, DisconnectedStatus);
                RefreshListView();*/
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        RefreshList();
                    }
                });
            }
            @Override
            public void onSuccess(String peerId, final int cid) {
                Log.d(ModuleCloud.TAG, (String.format("connect with %s successfully", peerId)));
                // modify for library latency
                /*RefreshStatus(peerId, ConnectedStatus);
                RefreshListView();*/

                synchronized (OverScrollLock) {
                    if (OverScrollLock.equals("locked")) {
                        Intent i = new Intent(ModuleList.this, Sending.class);
                        i.putExtra("SMID", peerId);
                        startActivity(i);
                        OverScrollLock = "unlocked";
                        block(null);
                    } else return;
                }
            }
        });
    }

    // mofify for library latency
    /*private void RefreshStatus(String peerId, String status) {
        synchronized (data) {
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i).sm_id.equals(peerId)) {
                    data.get(i).status = status;
                    break;
                }
            }
        }
    }*/

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

    // mofify for library latency
    /*private boolean ConnectingStatusCheck() {
        synchronized (data) {
            for (int i = 0; i < data.size(); i++)
                if (data.get(i).status.equals(ConnectingStatus))
                    return true;
            return false;
        }
    }*/
}

