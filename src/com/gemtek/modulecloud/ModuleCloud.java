package com.gemtek.modulecloud;

import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.net.wifi.WifiManager;
//import org.json.JSONException;
//import org.json.JSONObject;
import java.util.List;

import com.gemtek.wifi_setting.WifiList;
import com.gemtek.general_cmd.HACommand;
import com.bee.callback.LoginCallback;
import com.bee.callback.ConnectCallback;
import com.bee.utility.Log;
import com.bee.Peer;
import com.cloudAgent.CloudAgent;
import com.cloudAgent.CloudAgentCommand;
import com.cloudAgent.callback.CloudAgentCallback;
import com.cloudAgent.callback.SendCommandCallback;

public class ModuleCloud extends Activity implements CloudAgentCallback, Handler.Callback {
    public static final String TAG = "ModuleCloud";

    public static CloudAgent mCloudAgent = null;

    private SharedPreferences mSharedPreferences;
    public static final String mSharedPreferencesFile = "CloudInfo", sh_surl = "surl", sh_akey = "akey", sh_asecret = "asecret";
    public static String sUserName;
    private String mServerUrl, mApiKey, mApiSecret, mLocalID;
    private final String strictWiFi = "\"CLMOU-";
    private EditText mUserName, mUserPassword;

    public static DisconnectionCallback mDisconnectionCallback;

    // modify for library latency
    public static ServiceManagerCallback mServiceManagerCallback;
    public static ReceivingCommandCallback mReceivingCommandCallback;
    public static String RCCLock = "";

    private boolean mStartThread  = false;
    private Handler mHandler = new Handler(this);
    @Override
    public boolean handleMessage(Message msg) {
        List<Peer> list = mCloudAgent.getPeerListInCurrentLAN();
        Log.i(TAG, "SSDP List");
        Log.i(TAG, "------------------------------------------------------------");
        int i = 0;
        boolean checkDT = false;
        for (Peer peer : list) {
            Log.i(TAG, "%d. %s", ++i, peer.toString());
            if (peer.deviceType.equals("CLOUD_MODULE")) {
                mLocalID = peer.id;
                checkDT = true;
            }
        }
        if (!checkDT) mLocalID = null;
        if (i == 0) Log.e(TAG, "empty");
        Log.i(TAG, "------------------------------------------------------------");
        Log.d(TAG, "mLocalID: " + mLocalID);
        LoopForever();
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        mUserName = (EditText)findViewById(R.id.user_name);
        mUserPassword = (EditText)findViewById(R.id.user_password);

        mCloudAgent = new CloudAgent(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mStartThread == false) {
            mStartThread = true;
            LoopForever();
        }

        mSharedPreferences = this.getSharedPreferences(mSharedPreferencesFile, Context.MODE_PRIVATE);
        mServerUrl = mSharedPreferences.getString(sh_surl, "");
        mApiKey = mSharedPreferences.getString(sh_akey, "");
        mApiSecret = mSharedPreferences.getString(sh_asecret, "");

        if (!mServerUrl.equals("") && !mApiKey.equals("") && !mApiSecret.equals("")) {
            mCloudAgent.setCloudServerUrl(mServerUrl);
            mCloudAgent.setService(mApiKey, mApiSecret);
            mCloudAgent.setMobileInfo(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }

    private void LoopForever() {
        Message msg = Message.obtain();
        mHandler.sendMessageDelayed(msg, 3000);
    }

    private void block(final String message) {
       UITools.blockUI(ModuleCloud.this, message);
    }

    private void toast(final String message) {
        toast(message, Toast.LENGTH_SHORT);
    }

    private void toast(final String message, final int duration) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ModuleCloud.this, message, duration).show();
            }
        });
    }

    private void alert(final String title, final String message) {
        UITools.showAlertDialog(ModuleCloud.this, title, message);
    }

    private void settingwifi_flow(String peerId) {
        block("SendGetScanList");
        mCloudAgent.sendCommand(
            peerId,
            HACommand.getScanList(),
            new SendCommandCallback() {
                @Override
                public void onSendOut(String peerId, CloudAgentCommand command) {}
                @Override
                public void onResponse(String peerId, CloudAgentCommand command) {
                    block(null);
                    //alert("Recieve Module Cloud Command " + command.getCmd() + " Response", command.getVal());
                    Intent i = new Intent(ModuleCloud.this, WifiList.class);
                    i.putExtra("wifi_list", command.getVal());
                    i.putExtra("mLocalID", peerId);
                    startActivity(i);
                }
                @Override
                public void onError(String peerId, CloudAgentCommand command, int error) {
                    block(null);
                    alert("Module Cloud Command " + command.getCmd() + " Failure", mCloudAgent.getDescription(error));
                }
        });
    }

    /****************************************************************************************
        all functions
    ******************************************************************************************/
    public void settingwifi(View v) {
        WifiManager wifiManager = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            toast("WifiManager error");
            return;
        }
        if (!wifiManager.getConnectionInfo().getSSID().startsWith(strictWiFi)) {
            toast("current WiFi is " + wifiManager.getConnectionInfo().getSSID() + ", please connected with " + strictWiFi + "*\"");
            return;
        }

        String mLocalID_temp = mLocalID;
        if (mLocalID_temp == null) {
            toast("can't fetch sm_id from module");
            return;
        }
        if (mLocalID_temp.length() != 9) {
            toast("module sm_id isn't 9-Digit number");
            return;
        }

        mCloudAgent.disconnect(mLocalID_temp, null);
        block(String.format("connect with %s ...", mLocalID_temp));
        mCloudAgent.connect(mLocalID_temp, new ConnectCallback() {
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

    public void registration(View v) {
        if (mServerUrl != null && !mServerUrl.equals("") &&
                mApiKey != null && !mApiKey.equals("") &&
                mApiSecret != null && !mApiSecret.equals("")) {
            Intent i = new Intent(ModuleCloud.this, Registration.class);
            startActivity(i);
        } else {
            toast("please set cloud information");
        }
    }

    public void login(View v) {
        if (mServerUrl == null || mServerUrl.equals("") ||
                mApiKey == null || mApiKey.equals("") ||
                mApiSecret == null || mApiSecret.equals("")) {
            toast("please set cloud information");
            return;
        }

        final String username = mUserName.getText().toString();
        final String password = mUserPassword.getText().toString();
        if (username.equals("") || password.equals("")) {
            toast("please set account information");
            return;
        }

        block(String.format("Login %s ...", username));

        mCloudAgent.login(username.toLowerCase(), password, new LoginCallback() {
            @Override
            public void onSuccess(String loginId, LoginResponse response) {
                Log.e(TAG, "Login Success: login id = %s", loginId);
                block(null);
                response.showInfo();
                toast("login successfully");
                sUserName = loginId;
                Intent i = new Intent(ModuleCloud.this, MainPage.class);
                startActivity(i);
            }
            @Override
            public void onFailure(String loginId, int errorCode) {
                block(null);
                Log.e(TAG, "Login Failure: login id = %s, reason = %s", loginId, mCloudAgent.getDescription(errorCode));
                toast("Login Failure " + mCloudAgent.getDescription(errorCode));
            }
        });
    }

    public void configuration(View v) {
        Intent i = new Intent(ModuleCloud.this, Configuration.class);
        startActivity(i);
    }

    /****************************************************************************************
        cloud agent callback
    ******************************************************************************************/
    @Override
    public void onDisconnected(final String peerId, final int cid, final int connectionCode) {
        Log.e(TAG, "onDisconnected: peerId = %s reason = %s", peerId, mCloudAgent.getDescription(connectionCode));
        toast("disconnect from " + peerId);
        if (mDisconnectionCallback != null) mDisconnectionCallback.onDisconnection(peerId);
    }

    @Override
    public void onReceivedCommand(final String peerId, final int cid, final CloudAgentCommand cloudAgentCommand) {
        Log.i(TAG, "onReceivedCommand: peerId = %s", peerId);
        Log.i(TAG, "RECV <- %s", cloudAgentCommand.getCmd());
        cloudAgentCommand.showInfo();

        synchronized (RCCLock) {
            if (mReceivingCommandCallback != null)
                mReceivingCommandCallback.onReceivingCommand(peerId, cloudAgentCommand);
        }
    }

    @Override
    public void onServiceManagerCallback(String type, String command, String value) {
        Log.i(TAG, "onServiceManager: type = %s, command = %s, value = %s", type, command, value);

        // modify for library latency
        if (mServiceManagerCallback != null) mServiceManagerCallback.onServiceManager(command);
    }

    @Override
    public void onLogCallback(String tag, int level, String message) {
        if      (level == CloudAgent.LOG_INFO)  Log.i(tag, message);
        else if (level == CloudAgent.LOG_WARN)  Log.w(tag, message);
        else if (level == CloudAgent.LOG_ERROR) Log.e(tag, message);
        else                                    Log.d(tag, message);
    }

    @Override
    public void onMqttStatusChanged(boolean status) {
        Log.e(TAG, "onMqttStatusChanged: %s MQTT server", status ? "Connected with" : "Disconnect from");
    }

    @Override
    public void onSsdpNeighborChanged() {
        Log.e(TAG, "SSDP list is changed");
    }
}

