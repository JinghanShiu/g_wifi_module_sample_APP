package com.gemtek.modulecloud;

import android.util.Log;

public class Device {
    public String mac;
    public String gid;
    public String name;
    public String type;
    public String key;
    public String time;
    public boolean isOnline;

    @Override
    public String toString() {
        return String.format("mac = %s, gid = %s, name = %s, type = %s, key = %s, time = %s, isOnline = %b",
                mac, gid, name, type, key, time, isOnline);
    }

    public void showInfo() {
        Log.i(ModuleCloud.TAG, " Mac = " + mac);
        Log.i(ModuleCloud.TAG, " Gid = " + gid);
        Log.i(ModuleCloud.TAG, "Name = " + name);
        Log.i(ModuleCloud.TAG, "Type = " + type);
        Log.i(ModuleCloud.TAG, " Key = " + key);
        Log.i(ModuleCloud.TAG, "Time = " + time);
        Log.i(ModuleCloud.TAG, "IsOnline = " + isOnline);
    }
}

