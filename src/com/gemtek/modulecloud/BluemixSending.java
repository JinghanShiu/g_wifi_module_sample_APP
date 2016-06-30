package com.gemtek.modulecloud;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.gemtek.general_cmd.HACommand;

import com.cloudAgent.CloudAgentCommand;
import com.cloudAgent.callback.SendCommandCallback;

import org.json.JSONException;
import org.json.JSONObject;

public class BluemixSending extends Activity {
    private SharedPreferences mSharedPreferences;
    private final String mSharedPreferencesFile = "BluemixInfo", sh_org = "org", sh_type = "type",
            sh_id = "id", sh_authmethod = "authmethod", sh_authtoken = "authtoken";
    private String m_org, m_type, m_id, m_authmethod, m_authtoken;

    private String peerId;
    private EditText mBmOrg, mBmType, mBmID, mBmAuthMethod, mBmAuthToken;
    private Spinner mInterfaceSpinner, mLogTriggerSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluemixsending);
        peerId = getIntent().getExtras().getString("mLocalID");

        mBmOrg = (EditText)findViewById(R.id.BmOrg);
        mBmType = (EditText)findViewById(R.id.BmType);
        mBmID = (EditText)findViewById(R.id.BmID);
        mBmAuthMethod = (EditText)findViewById(R.id.BmAuthMethod);
        mBmAuthToken = (EditText)findViewById(R.id.BmAuthToken);
        mInterfaceSpinner = (Spinner)findViewById(R.id.interface_spinner);
        mLogTriggerSpinner = (Spinner)findViewById(R.id.log_trigger_spinner);

        mSharedPreferences = this.getSharedPreferences(mSharedPreferencesFile, Context.MODE_PRIVATE);
        m_org = mSharedPreferences.getString(sh_org, "");
        m_type = mSharedPreferences.getString(sh_type, "");
        m_id = mSharedPreferences.getString(sh_id, "");
        m_authmethod = mSharedPreferences.getString(sh_authmethod, "");
        m_authtoken = mSharedPreferences.getString(sh_authtoken, "");

        if (!m_org.equals("") && !m_type.equals("") && !m_id.equals("") &&
                !m_authmethod.equals("") && !m_authtoken.equals("")) {
            mBmOrg.setText(m_org);
            mBmType.setText(m_type);
            mBmID.setText(m_id);
            mBmAuthMethod.setText(m_authmethod);
            mBmAuthToken.setText(m_authtoken);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void block(final String message) {
       UITools.blockUI(BluemixSending.this, message);
    }

    private void toast(final String message) {
        toast(message, Toast.LENGTH_SHORT);
    }

    private void toast(final String message, final int duration) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BluemixSending.this, message, duration).show();
            }
        });
    }

    /****************************************************************************************
        all functions
    ******************************************************************************************/
    public void sendbluemixsetting(View v) {
        String BmOrg_temp = mBmOrg.getText().toString();
        String BmType_temp = mBmType.getText().toString();
        String BmID_temp = mBmID.getText().toString();
        String BmAuthMethod_temp = mBmAuthMethod.getText().toString();
        String BmAuthToken_temp = mBmAuthToken.getText().toString();

        if (BmOrg_temp.isEmpty() || BmType_temp.isEmpty() || BmID_temp.isEmpty() ||
                BmAuthMethod_temp.isEmpty() || BmAuthToken_temp.isEmpty()) {
            toast("please fill Bluemix informations");
            return;
        }

        mSharedPreferences.edit().putString(sh_org, BmOrg_temp).commit();
        mSharedPreferences.edit().putString(sh_type, BmType_temp).commit();
        mSharedPreferences.edit().putString(sh_id, BmID_temp).commit();
        mSharedPreferences.edit().putString(sh_authmethod, BmAuthMethod_temp).commit();
        mSharedPreferences.edit().putString(sh_authtoken, BmAuthToken_temp).commit();

        block("SendBluemixSetting");
        ModuleCloud.mCloudAgent.sendCommand(
            peerId,
            HACommand.SetBluemix(BmOrg_temp, BmType_temp, BmID_temp, BmAuthMethod_temp, BmAuthToken_temp),
            new SendCommandCallback() {
                @Override
                public void onSendOut(String peerId, CloudAgentCommand command) {
                }
                @Override
                public void onResponse(String peerId, CloudAgentCommand command) {
                    block(null);
                    if (command.getCode() == 200)
                        toast("set Bluemix successfully", Toast.LENGTH_LONG);
                    else
                        toast("Recieve Module Cloud Command " + command.getCmd() +
                                " Response\n" + command.getVal() + "(" + command.getCode() + ")", Toast.LENGTH_LONG);
                }
                @Override
                public void onError(String peerId, CloudAgentCommand command, int error) {
                    block(null);
                    if (command == null)
                        toast("command format is wrong");
                    else
                        toast("Module Cloud Command " + command.getCmd() + " Failure\n" +
                                ModuleCloud.mCloudAgent.getDescription(error), Toast.LENGTH_LONG);
                }
        });
    }

    public void sendinterfacesetting(View v) {
        String interface_temp = mInterfaceSpinner.getSelectedItem().toString();

        if (interface_temp.isEmpty()) {
            toast("please select a interface in module");
            return;
        }

        block("SendInterfaceSetting");
        ModuleCloud.mCloudAgent.sendCommand(
            peerId,
            HACommand.SetModuleInterface(interface_temp),
            new SendCommandCallback() {
                @Override
                public void onSendOut(String peerId, CloudAgentCommand command) {
                }
                @Override
                public void onResponse(String peerId, CloudAgentCommand command) {
                    block(null);
                    if (command.getCode() == 200)
                        toast("set interface successfully", Toast.LENGTH_LONG);
                    else
                        toast("Recieve Module Cloud Command " + command.getCmd() +
                                " Response\n" + command.getVal() + "(" + command.getCode() + ")", Toast.LENGTH_LONG);
                }
                @Override
                public void onError(String peerId, CloudAgentCommand command, int error) {
                    block(null);
                    if (command == null)
                        toast("command format is wrong");
                    else
                        toast("Module Cloud Command " + command.getCmd() + " Failure\n" +
                                ModuleCloud.mCloudAgent.getDescription(error), Toast.LENGTH_LONG);
                }
        });
    }

    public void sendlogtriggerstatus(View v) {
        String log_trigger_status = mLogTriggerSpinner.getSelectedItem().toString();

        if (log_trigger_status.isEmpty()) {
            toast("please select a status of log trigger for module");
            return;
        }

        block("SendLogTriggerSetting");
        ModuleCloud.mCloudAgent.sendCommand(
            peerId,
            HACommand.SetBluemixLog(log_trigger_status),
            new SendCommandCallback() {
                @Override
                public void onSendOut(String peerId, CloudAgentCommand command) {
                }
                @Override
                public void onResponse(String peerId, CloudAgentCommand command) {
                    block(null);
                    if (command.getCode() == 200) {
                        try {
                            String customer_tag = "msg";
                            JSONObject obj = new JSONObject(command.getVal());
                            String recv_msg = obj.getString(customer_tag);
                            toast(recv_msg, Toast.LENGTH_LONG);
                        } catch (JSONException jsonException) {
                            toast("set log trigger status successfully", Toast.LENGTH_LONG);
                        } catch (Exception e) {
                            toast("set log trigger status successfully", Toast.LENGTH_LONG);
                        }
                    } else {
                        toast("Recieve Module Cloud Command " + command.getCmd() +
                                " Response\n" + command.getVal() + "(" + command.getCode() + ")", Toast.LENGTH_LONG);
                    }
                }
                @Override
                public void onError(String peerId, CloudAgentCommand command, int error) {
                    block(null);
                    if (command == null)
                        toast("command format is wrong");
                    else
                        toast("Module Cloud Command " + command.getCmd() + " Failure\n" +
                                ModuleCloud.mCloudAgent.getDescription(error), Toast.LENGTH_LONG);
                }
        });
    }
}

