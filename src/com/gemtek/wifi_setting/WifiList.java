package com.gemtek.wifi_setting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.cloudAgent.CloudAgentCommand;
import com.cloudAgent.callback.SendCommandCallback;
import com.gemtek.general_cmd.HACommand;
import com.gemtek.modulecloud.ModuleCloud;
import com.gemtek.modulecloud.R;
import com.gemtek.modulecloud.UITools;
import com.gemtek.wifi_setting.adapter.ListAdapter;

public class WifiList extends Activity{

    private final String TAG = "WifiList";
    private Context mContext;
    private ListView mListView;
    private List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
    private ListAdapter mListAdapter;
    private WifiRouter mDeviceItems;
    private List<WifiRouter> data;
    private String wifi_list, mLocalID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Intent intent = getIntent();
        wifi_list = intent.getExtras().getString("wifi_list");
        mLocalID = intent.getExtras().getString("mLocalID");
        mContext = this.getApplicationContext();
        mListView = (ListView) findViewById(R.id.mlist);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        try{
            data = new ArrayList<WifiRouter>();
            JSONArray wifi_ap =  new JSONObject(wifi_list).optJSONArray("wifi_ap");
            for (int i = 0; i < wifi_ap.length(); i++) {
                JSONObject JsonOb = wifi_ap.getJSONObject(i);
                mDeviceItems = new WifiRouter();
                mDeviceItems.security = JsonOb.getString("security");
                mDeviceItems.signal = JsonOb.getString("signal");
                mDeviceItems.ssid = JsonOb.getString("ssid");
                data.add(mDeviceItems);
            }
        }catch(Exception e){
            Log.e(TAG, "Error exception = "+e);
        }

        mListView.setOnItemClickListener( new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                Log.d("ansel", "select "+position+": "+data.get(position).ssid);
                if (data.get(position).security.equals("open"))
                    ShowNoneDialog(data.get(position).ssid, data.get(position).security);
                else
                    ShowPWDialog(data.get(position).ssid, data.get(position).security);
            }

        });

        mListAdapter = new ListAdapter(mContext, (ArrayList<Map<String, Object>>) list);
        mListAdapter.LoadingData(data);
        mListAdapter.notifyDataSetChanged();
        mListView.setAdapter(mListAdapter);

    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    private void ShowPWDialog(final String ssid, final String security){
        LayoutInflater li = LayoutInflater.from(WifiList.this);
        View promptsView = li.inflate(R.layout.edit_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                WifiList.this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);
        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        alertDialogBuilder
        .setCancelable(false)
        .setPositiveButton("OK",
          new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                // get user input and set it to result
                // edit text
                Log.i(TAG, "The wifi ssid = "+ssid+", pw = "+userInput.getText());
                block("SendConnectWifi");
                ModuleCloud.mCloudAgent.sendCommand(
                    mLocalID,
                    HACommand.Connect(ssid, security, userInput.getText().toString()),
                    new SendCommandCallback() {
                        @Override
                        public void onSendOut(String peerId, CloudAgentCommand command) {}
                        @Override
                        public void onResponse(String peerId, CloudAgentCommand command) {
                            block(null);
                            toast("Recieve Module Cloud Command " + command.getCmd() + " Response\n" + command.getVal());
                            finish();
                        }
                        @Override
                        public void onError(String peerId, CloudAgentCommand command, int error) {
                            block(null);
                            toast("Module Cloud Command " + command.getCmd() + " Failure\n" + ModuleCloud.mCloudAgent.getDescription(error));
                            finish();
                         }
                 });
            }
          })
        .setNegativeButton("Cancel",
          new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
            dialog.cancel();
            }
          }).show();
    }

    private void ShowNoneDialog(final String ssid, final String security){
        new AlertDialog.Builder(WifiList.this)
        .setTitle("Connect to wifi router")
        .setMessage("Would want connect to "+ssid+"?")
        .setCancelable(false)
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                block("SendConnectWifi(without pw)");
                ModuleCloud.mCloudAgent.sendCommand(
                    mLocalID,
                    HACommand.Connect(ssid, security, null),
                    new SendCommandCallback() {
                        @Override
                        public void onSendOut(String peerId, CloudAgentCommand command) {}
                        @Override
                        public void onResponse(String peerId, CloudAgentCommand command) {
                            block(null);
                            toast("Recieve Module Cloud Command " + command.getCmd() + " Response\n" + command.getVal());
                            finish();
                        }
                        @Override
                        public void onError(String peerId, CloudAgentCommand command, int error) {
                            block(null);
                            toast("Module Cloud Command " + command.getCmd() + " Failure\n" + ModuleCloud.mCloudAgent.getDescription(error));
                            finish();
                         }
                 });
            }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        })
        .show();
    }

    private void block(final String message) {
        UITools.blockUI(WifiList.this, message);
    }

    private void toast(final String message) {
        toast(message, Toast.LENGTH_LONG);
    }

    private void toast(final String message, final int duration) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(WifiList.this, message, duration).show();
            }
        });
    }
}

