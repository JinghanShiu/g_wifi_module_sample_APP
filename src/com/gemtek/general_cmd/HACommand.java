package com.gemtek.general_cmd;

import android.util.Base64;

import com.bee.utility.Log;
import com.cloudAgent.CloudAgentCommand;

public class HACommand {
    static private final String TAG = "Command";
    static public final int HA_WIFI_SETTING_CMD_TYPE = 0x0001;
    static public final int HA_WIFI_SETTING_CLASS    = 0x0008;
    static public final int HA_CUSTOMER_CMD_TYPE     = 0X003a;
    static public final int HA_CUSTOMER_CLASS        = 0x000a;
    static public final int HA_CONSOLE_CMD_TYPE      = 0X003b;
    static public final int HA_CONSOLE_CLASS         = 0x000b;

    static public final int HA_MODULE_BINDING_CMD_TYPE     = 0x0001;
    static public final int HA_MODULE_POWERING_CMD_TYPE    = 0x0002;
    static public final int HA_MODULE_POWERING_CLASS       = 0x0001;
    static public final int HA_MODULE_BINDING_CLASS        = 0x0009;
    static public final int HA_MODULE_FIRMWARE_CHECK_CLASS = 0x0001;

    // power on 01 06 40 00 00 01 5D CA

    static public final CloudAgentCommand DownloadFirmware() {
        CloudAgentCommand command = new CloudAgentCommand().setCmdType(HA_MODULE_BINDING_CMD_TYPE).setClasses(HA_MODULE_FIRMWARE_CHECK_CLASS);
        return command.setCmd("get_new_version");
    }

    static public final CloudAgentCommand UpgradeFirmware() {
        CloudAgentCommand command = new CloudAgentCommand().setCmdType(HA_MODULE_BINDING_CMD_TYPE).setClasses(HA_MODULE_FIRMWARE_CHECK_CLASS);
        return command.setCmd("firmware_upgrade");
    }

    static public final CloudAgentCommand CheckFirmware() {
        CloudAgentCommand command = new CloudAgentCommand().setCmdType(HA_MODULE_BINDING_CMD_TYPE).setClasses(HA_MODULE_FIRMWARE_CHECK_CLASS);
        return command.setCmd("firmware_check");
    }

    static public final CloudAgentCommand SetPower(String mPowerTriggerStatus) {
        if (mPowerTriggerStatus == null || mPowerTriggerStatus.equals("")) return null;

        String power_trigger_setting_value = "{\"power\":\"" + mPowerTriggerStatus + "\"}";
        CloudAgentCommand command = new CloudAgentCommand().setCmdType(HA_MODULE_POWERING_CMD_TYPE).setClasses(HA_MODULE_POWERING_CLASS);
        return command.setCmd("set_power").setVal(power_trigger_setting_value);
    }

    static public final CloudAgentCommand SetBluemixLog(String mLogTriggerStatus) {
        if (mLogTriggerStatus == null || mLogTriggerStatus.equals("")) return null;

        String log_trigger_setting_value = "{\"status\":\"" + mLogTriggerStatus + "\"}";
        CloudAgentCommand command = new CloudAgentCommand().setCmdType(HA_MODULE_BINDING_CMD_TYPE).setClasses(HA_CUSTOMER_CLASS);
        return command.setCmd("set_bluemix_log").setVal(log_trigger_setting_value);
    }

    static public final CloudAgentCommand SetModuleInterface(String mInterface) {
        if (mInterface == null || mInterface.equals("")) return null;

        String module_interface_setting_value = "{\"interface\":\"" + mInterface + "\"}";
        CloudAgentCommand command = new CloudAgentCommand().setCmdType(HA_MODULE_BINDING_CMD_TYPE).setClasses(HA_CUSTOMER_CLASS);
        return command.setCmd("set_interface").setVal(module_interface_setting_value);
    }

    static public final CloudAgentCommand SetBluemix(String org, String type, String id, String authmethod, String authtoken) {
        if (org == null || org.equals("") || type == null || type.equals("") ||
                id == null || id.equals("") || authmethod == null || authmethod.equals("") ||
                authtoken == null || authtoken.equals("")) return null;

        String bluemix_setting_value = "{\"org\":\"" + org + "\"," + "\"type\":\"" + type + "\"," + "\"id\":\"" + id +
                "\"," + "\"authmethod\":\"" + authmethod + "\"," + "\"authtoken\":\"" + authtoken + "\"}";
        CloudAgentCommand command = new CloudAgentCommand().setCmdType(HA_MODULE_BINDING_CMD_TYPE).setClasses(HA_CUSTOMER_CLASS);
        return command.setCmd("set_bluemix").setVal(bluemix_setting_value);
    }

    static public final CloudAgentCommand BindModule(String uid) {
        if (uid == null || uid.equals("")) return null;

        String binding_value = "{\"user_id\":\"" + uid + "\"}";
        CloudAgentCommand command = new CloudAgentCommand().setCmdType(HA_MODULE_BINDING_CMD_TYPE).setClasses(HA_MODULE_BINDING_CLASS);
        return command.setCmd("cloud_module_binding").setVal(binding_value);
    }

    //13. Wi-Fi Get current scan list
    static public final CloudAgentCommand getScanList() {
        CloudAgentCommand command = new CloudAgentCommand().setCmdType(HA_WIFI_SETTING_CMD_TYPE).setClasses(HA_WIFI_SETTING_CLASS);
        return command.setCmd("scan_list");
    }

    //14. Wi-Fi connect
    static public final CloudAgentCommand Connect(String ssid, String security, String pw) {
        CloudAgentCommand command = new CloudAgentCommand().setCmdType(HA_WIFI_SETTING_CMD_TYPE).setClasses(HA_WIFI_SETTING_CLASS);
        String connect_value;
        if (pw == null)
            connect_value = "{\"wifi_ap\":{\"ssid\":\""+ssid+"\",\"security\":\""+security+"\"}}";
        else
            connect_value = "{\"wifi_ap\":{\"ssid\":\""+ssid+
                    "\",\"security\":\""+security+"\",\"password\":\""+pw+"\"}}";
        return command.setCmd("connect_wifi").setVal(connect_value);
    }

    //Customer command
    static public final CloudAgentCommand CustomerCommand(byte[] cmd) {
        if (cmd == null) return null;

        String encodedBytes = Base64.encodeToString(cmd, Base64.NO_WRAP);
        CloudAgentCommand command = new CloudAgentCommand().setCmdType(HA_CUSTOMER_CMD_TYPE).setClasses(HA_CUSTOMER_CLASS);
        //Base64DecodeToString(encodedBytes);
        return command.setCmd("customer").setVal(encodedBytes);
    }

    //Console string
    static public final CloudAgentCommand ConsoleString(byte[] str) {
        //String encodedBytes = Base64.encodeToString(str, Base64.NO_WRAP);
        CloudAgentCommand command = new CloudAgentCommand().setCmdType(HA_CONSOLE_CMD_TYPE).setClasses(HA_CONSOLE_CLASS);
        //Base64DecodeToString(encodedBytes);
        return command.setCmd("console").setVal(new String(str));
    }

    static public final CloudAgentCommand CustomerCommandString(String string_cmd){
        return CustomerCommand(HexStringToByteArray(string_cmd));
    }

    private static byte[] HexStringToByteArray(String hexString){
        String[] sentArray = hexString.split(" ");
        int byte_len = sentArray.length;
        if (byte_len == 0) return null;

        byte[] byte_array = new byte[byte_len];
        int val = 0;
        for (int i=0;i<byte_len; i++){
            try {
                val = Integer.parseInt( sentArray[i], 16 );
                byte_array[i] = (byte) val;
            } catch (Exception e) {
                Log.i(TAG, "encoding error: " + e.toString());
                return null;
            }
        }
        return byte_array;
    }

    public static String Base64DecodeToString(String base64_cmd){
        String out = "";
        try{
            byte[] tmp2 = Base64.decode(base64_cmd, Base64.NO_WRAP);
            int byte_len = tmp2.length;
            for (int i=0;i<byte_len; i++){
                out += String.format("0x%02X", tmp2[i]);
                if (i != byte_len-1) out += " ";
            }
        }catch(Exception e){
            out = "Decode error";
        }
        Log.i(TAG,"Decoded data = "+out);
        return out;
    }

    public static String InnerBase64DecodeToString(String base64_cmd){
        String out = "";
        try{
            byte[] tmp2 = Base64.decode(base64_cmd, Base64.NO_WRAP);
            int byte_len = tmp2.length;
            if (byte_len != 15) {
                out = "Decode error";
                return out;
            }
            for (int i=0;i<byte_len; i++){
                if (i % 14 == 0 && i != 0)
                    out = out +" "+String.format("%d. 0x%02X", i+1, tmp2[i]);
                else
                    out = out +" "+String.format("%d. 0x%02X\n", i+1, tmp2[i]);
            }
        }catch(Exception e){
            out = "Decode error";
        }
        Log.i(TAG,"Decoded data = "+out);
        return out;
    }

    public static String Base64Decode(String base64_cmd){
        String out = null;
        try {
            byte[] tmp2 = Base64.decode(base64_cmd, Base64.NO_WRAP);
            if (tmp2.length <= 0)
                out = null;
            else
                out = new String(tmp2);
        } catch(Exception e) {
            out = null;
        }
        return out;
    }
}
