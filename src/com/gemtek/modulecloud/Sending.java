package com.gemtek.modulecloud;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.gemtek.general_cmd.HACommand;

import com.cloudAgent.CloudAgentCommand;
import com.cloudAgent.callback.SendCommandCallback;

public class Sending extends Activity {
    private String peerId;
    private EditText mCommandET, mStringET;
    private Spinner mPowerTriggerSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sending);
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

        peerId = getIntent().getExtras().getString("SMID");

        mCommandET = (EditText)findViewById(R.id.CommandET);
        mStringET = (EditText)findViewById(R.id.StringET);
        mPowerTriggerSpinner = (Spinner)findViewById(R.id.power_trigger_spinner);
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
       UITools.blockUI(Sending.this, message);
    }

    private void toast(final String message) {
        toast(message, Toast.LENGTH_SHORT);
    }

    private void toast(final String message, final int duration) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Sending.this, message, duration).show();
            }
        });
    }

    private void alert(final String title, final String message) {
        UITools.showAlertDialog(Sending.this, title, message);
    }

    /****************************************************************************************
        all functions
    ******************************************************************************************/
    public void sendcommand(View v) {
        String command_temp = mCommandET.getText().toString();
        if (command_temp.isEmpty()) {
            toast("please set your command");
            return;
        }

        block("SendCommnad");
        ModuleCloud.mCloudAgent.sendCommand(
            peerId,
            HACommand.CustomerCommandString(command_temp), new SendCommandCallback() {
                @Override
                public void onSendOut(String peerId, CloudAgentCommand command) {
                }
                @Override
                public void onResponse(String peerId, CloudAgentCommand command) {
                    block(null);
                    if (command.getCmdType() == HACommand.HA_CUSTOMER_CMD_TYPE) {
                        alert("Recieve Module Cloud Command " + command.getCmd() +
                                " Response", command.getVal() + ", \nDecoded data = " +
                                HACommand.Base64DecodeToString(command.getVal()));
                    } else {
                        alert("Recieve Module Cloud Command " + command.getCmd() + " Response",
                                "HACommand.HA_CUSTOMER_CMD_TYPE parsing error");
                    }
                }
                @Override
                public void onError(String peerId, CloudAgentCommand command, int error) {
                    block(null);
                    if (command == null)
                        toast("command format is wrong");
                    else
                        alert("Module Cloud Command " + command.getCmd() + " Failure",
                            ModuleCloud.mCloudAgent.getDescription(error));
                }
        });
    }

    public void sendstring(View v) {
        String string_temp = mStringET.getText().toString();
        if (string_temp.isEmpty()) {
            toast("please set your string");
            return;
        }

        block("SendString");
        ModuleCloud.mCloudAgent.sendCommand(
            peerId,
            HACommand.ConsoleString(string_temp.getBytes()), new SendCommandCallback() {
                @Override
                public void onSendOut(String peerId, CloudAgentCommand command) {
                }
                @Override
                public void onResponse(String peerId, CloudAgentCommand command) {
                    block(null);
                    if (command.getCmdType() == HACommand.HA_CONSOLE_CMD_TYPE) {
                        String recv_title = String.format("[IOT] received message from %s.\n", peerId);
                        alert("Recieve Module Cloud Command " + command.getCmd() + " Response",
                                recv_title + command.getVal());
                    } else {
                        alert("Recieve Module Cloud Command " + command.getCmd() + " Response",
                                "HACommand.HA_CONSOLE_CMD_TYPE parsing error");
                    }
                }
                @Override
                public void onError(String peerId, CloudAgentCommand command, int error) {
                    block(null);
                    alert("Module Cloud Command " + command.getCmd() + " Failure",
                            ModuleCloud.mCloudAgent.getDescription(error));
                }
        });
    }

    public void sendpowertriggerstatus(View v) {
        String power_trigger_status = mPowerTriggerSpinner.getSelectedItem().toString();

        if (power_trigger_status.isEmpty()) {
            toast("please select a status of log trigger for module");
            return;
        }

        block("SendPowerTriggerSetting");
        ModuleCloud.mCloudAgent.sendCommand(
            peerId,
            HACommand.SetPower(power_trigger_status),
            new SendCommandCallback() {
                @Override
                public void onSendOut(String peerId, CloudAgentCommand command) {
                }
                @Override
                public void onResponse(String peerId, CloudAgentCommand command) {
                    block(null);
                    if (command.getCode() == 200)
                        toast("set power trigger status successfully", Toast.LENGTH_LONG);
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
}

