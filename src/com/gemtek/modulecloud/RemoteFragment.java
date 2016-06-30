package com.gemtek.modulecloud;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Context;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import com.gemtek.general_cmd.HACommand;
import com.cloudAgent.CloudAgentCommand;

public class RemoteFragment extends Fragment {
    Context mContext;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frg_remote, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        synchronized (ModuleCloud.RCCLock) {
            ModuleCloud.mReceivingCommandCallback = null;
        }
    }

    @Override
    public void onStart() {
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
    }

    private void alert(final String title, final String message) {
        UITools.showAlertDialog((Activity)mContext, title, message);
    }
}
