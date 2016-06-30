package com.gemtek.modulecloud;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.SystemClock;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import com.gemtek.general_cmd.HACommand;

import com.cloudAgent.CloudAgentCommand;
import com.cloudAgent.callback.SendCommandCallback;

import org.json.JSONException;
import org.json.JSONObject;

public class FirmwareUpgrade extends Activity {
    private String peerId;

    private TextView mDeveloperTrigger;
    private Button mFirmwareDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firmwareupgrade);
        peerId = getIntent().getExtras().getString("mLocalID");

        mDeveloperTrigger = (TextView)findViewById(R.id.developer_trigger);
        mDeveloperTrigger.setOnClickListener(new View.OnClickListener() {
            int clickCounter = 0;
            long currentClickTime = 0;
            long previousClickTime = 0;
            boolean developer_mode = false;

            @Override
            public void onClick(View view) {
                currentClickTime = SystemClock.uptimeMillis();
                if (currentClickTime - previousClickTime <= 400) {
                    clickCounter++;
                    if (clickCounter == 7) {
                        clickCounter = 0;
                        developer_mode = !developer_mode;
                        SetDeveloperMode(developer_mode);
                    }
                } else {
                    clickCounter = 1;
                }
                previousClickTime = currentClickTime;
            }
        });

        mFirmwareDownload = (Button)findViewById(R.id.firmware_download);
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
       UITools.blockUI(FirmwareUpgrade.this, message);
    }

    private void toast(final String message) {
        toast(message, Toast.LENGTH_SHORT);
    }

    private void toast(final String message, final int duration) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(FirmwareUpgrade.this, message, duration).show();
            }
        });
    }

    private void alert(final String title, final String message) {
        UITools.showAlertDialog(FirmwareUpgrade.this, title, message);
    }

    private void SetDeveloperMode(boolean developer_mode) {
        if (developer_mode) {
            synchronized (ModuleCloud.RCCLock) {
                ModuleCloud.mReceivingCommandCallback = new ReceivingCommandCallback() {
                    @Override
                    public void onReceivingCommand(String peerId2, CloudAgentCommand cloudAgentCommand) {
                        if (!peerId2.equals(peerId)) return;

                        String recv_msg = null;
                        try {
                            String customer_tag = "result";
                            JSONObject obj = new JSONObject(cloudAgentCommand.getVal());
                            recv_msg = obj.getString(customer_tag);
                        } catch (JSONException jsonException) {
                        } catch (Exception e) {
                        }

                        if (recv_msg == null || recv_msg.equals("")) {
                            Log.d(ModuleCloud.TAG, "onReceivingCommand fail");
                            return;
                        }

                        boolean success = recv_msg.equals("new firmware downloaded successfully");

                        String recv_msg2 = null;
                        try {
                            String customer_tag = "version";
                            JSONObject obj = new JSONObject(cloudAgentCommand.getVal());
                            recv_msg2 = obj.getString(customer_tag);
                        } catch (JSONException jsonException) {
                        } catch (Exception e) {
                        }

                        if ((recv_msg2 == null || recv_msg2.equals("")) && success) {
                            Log.d(ModuleCloud.TAG, "onReceivingCommand fail");
                            return;
                        }

                        if (cloudAgentCommand.getCmdType() == HACommand.HA_MODULE_BINDING_CMD_TYPE &&
                                cloudAgentCommand.getClasses() == HACommand.HA_MODULE_FIRMWARE_CHECK_CLASS) {
                            if (success) alert("Recieve " + cloudAgentCommand.getCmd() + " Command", recv_msg + " (" + recv_msg2 + ")");
                            else alert("Recieve " + cloudAgentCommand.getCmd() + " Command", recv_msg);
                        } else Log.d(ModuleCloud.TAG, "onReceivingCommand fail");
                    };
                };
            }

            mDeveloperTrigger.setText(getResources().getString(R.string.developer_switch_title) + " (Dev)");
            mFirmwareDownload.setVisibility(View.VISIBLE);
        } else {
            synchronized (ModuleCloud.RCCLock) {
                ModuleCloud.mReceivingCommandCallback = null;
            }

            mDeveloperTrigger.setText(getResources().getString(R.string.developer_switch_title));
            mFirmwareDownload.setVisibility(View.GONE);
        }
    }

    private void show_upgrade_option_dialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FirmwareUpgrade.this);
        alertDialogBuilder
        .setTitle("Firmware Upgrade")
        .setMessage("Do you wanna upgrade firmware?")
        .setCancelable(false)
        .setPositiveButton("Yes",
          new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                block("SendFirmwareUpgrade");
                ModuleCloud.mCloudAgent.sendCommand(
                    peerId,
                    HACommand.UpgradeFirmware(),
                    new SendCommandCallback() {
                        @Override
                        public void onSendOut(String peerId, CloudAgentCommand command) {
                        }
                        @Override
                        public void onResponse(String peerId, CloudAgentCommand command) {
                            block(null);
                            if (command.getCode() == 200) {
                                toast("upgrading ...", Toast.LENGTH_LONG);
                            } else if (command.getCode() == 401) {
                                toast("the version is up to date", Toast.LENGTH_LONG);
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
          })
        .setNegativeButton("No",
          new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
          }).show();
    }

    /****************************************************************************************
        all functions
    ******************************************************************************************/
    public void sendfirmwaredownload(View v) {
        block("SendFirmwareDownload");
        ModuleCloud.mCloudAgent.sendCommand(
            peerId,
            HACommand.DownloadFirmware(),
            new SendCommandCallback() {
                @Override
                public void onSendOut(String peerId, CloudAgentCommand command) {
                }
                @Override
                public void onResponse(String peerId, CloudAgentCommand command) {
                    block(null);
                    if (command.getCode() == 200) {
                        toast("downloading ...", Toast.LENGTH_LONG);
                    } else if (command.getCode() == 401) {
                        toast("start to download unsuccessfully", Toast.LENGTH_LONG);
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

    public void sendfirmwarecheck(View v) {
        block("SendFirmwareCheck");
        ModuleCloud.mCloudAgent.sendCommand(
            peerId,
            HACommand.CheckFirmware(),
            new SendCommandCallback() {
                @Override
                public void onSendOut(String peerId, CloudAgentCommand command) {
                }
                @Override
                public void onResponse(String peerId, CloudAgentCommand command) {
                    block(null);
                    try {
                        JSONObject obj = new JSONObject(command.getVal());
                        String customer_tag = "version";
                        String recv_msg = obj.getString(customer_tag);
                        if (command.getCode() == 204) {
                            toast("the version(" + recv_msg + ") " + "is up to date", Toast.LENGTH_LONG);
                        } else if (command.getCode() == 200) {
                            customer_tag = "new_version";
                            String recv_msg2 = obj.getString(customer_tag);
                            toast("this version is " + recv_msg + ", and there is a new available firmware("
                                    + recv_msg2 + ")", Toast.LENGTH_LONG);
                        } else {
                            toast("Recieve Module Cloud Command " + command.getCmd() +
                                    " Response\n" + command.getVal() + "(" + command.getCode() + ")", Toast.LENGTH_LONG);
                        }
                    } catch (JSONException jsonException) {
                        toast("Recieve Module Cloud Command " + command.getCmd() +
                                " Response\n" + command.getVal() + "(" + command.getCode() + ")", Toast.LENGTH_LONG);
                    } catch (Exception e) {
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

    public void sendfirmwareupgrade(View v) {
        block("SendFirmwareCheck");
        ModuleCloud.mCloudAgent.sendCommand(
            peerId,
            HACommand.CheckFirmware(),
            new SendCommandCallback() {
                @Override
                public void onSendOut(String peerId, CloudAgentCommand command) {
                }
                @Override
                public void onResponse(String peerId, CloudAgentCommand command) {
                    block(null);
                    if (command.getCode() == 200) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                show_upgrade_option_dialog();
                            }
                        });
                    } else if (command.getCode() == 204) {
                        toast("the version is up to date", Toast.LENGTH_LONG);
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

