package com.gemtek.modulecloud;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Configuration extends Activity {
    private SharedPreferences mSharedPreferences;
    private String mServerUrl, mApiKey, mApiSecret;
    private EditText mServer_url, mAPI_key, mAPI_secret;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configuration);

        mServer_url = (EditText) findViewById(R.id.server_url);
        mAPI_key = (EditText) findViewById(R.id.api_key);
        mAPI_secret = (EditText) findViewById(R.id.api_secret);

        mSharedPreferences = this.getSharedPreferences(ModuleCloud.mSharedPreferencesFile, Context.MODE_PRIVATE);
        mServerUrl = mSharedPreferences.getString(ModuleCloud.sh_surl, "");
        mApiKey = mSharedPreferences.getString(ModuleCloud.sh_akey, "");
        mApiSecret = mSharedPreferences.getString(ModuleCloud.sh_asecret, "");

        if (!mServerUrl.equals("") && !mApiKey.equals("") && !mApiSecret.equals("")) {
            mServer_url.setText(mServerUrl);
            mAPI_key.setText(mApiKey);
            mAPI_secret.setText(mApiSecret);
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

    private void toast(final String message, final int duration) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Configuration.this, message, duration).show();
            }
        });
    }

    /****************************************************************************************
        all functions
    ******************************************************************************************/
    public void submit(View v) {
        if (mServer_url.getText().toString().isEmpty() ||
                mAPI_key.getText().toString().isEmpty() ||
                mAPI_secret.getText().toString().isEmpty()) {
            toast("please fill cloud informations", Toast.LENGTH_SHORT);
            return;
        }

        mSharedPreferences.edit().putString(ModuleCloud.sh_surl, mServer_url.getText().toString()).commit();
        mSharedPreferences.edit().putString(ModuleCloud.sh_akey, mAPI_key.getText().toString()).commit();
        mSharedPreferences.edit().putString(ModuleCloud.sh_asecret, mAPI_secret.getText().toString()).commit();
        toast("fill cloud informations successfully", Toast.LENGTH_SHORT);
        finish();
    }
}

