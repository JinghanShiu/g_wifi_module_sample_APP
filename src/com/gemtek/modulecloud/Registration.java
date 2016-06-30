package com.gemtek.modulecloud;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.bee.callback.RegistrationCallback;

public class Registration extends Activity {
    private EditText mRegistration_user, mRegistration_email, mRegistration_pw, mRegistration_pw_confirming;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);

        mRegistration_user = (EditText) findViewById(R.id.reg_user);
        mRegistration_email = (EditText) findViewById(R.id.reg_email);
        mRegistration_pw = (EditText) findViewById(R.id.reg_pwd);
        mRegistration_pw_confirming = (EditText) findViewById(R.id.reg_pwd_confirming);
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
       UITools.blockUI(Registration.this, message);
    }

    private void toast(final String message, final int duration) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Registration.this, message, duration).show();
            }
        });
    }

    /****************************************************************************************
        all functions
    ******************************************************************************************/
    public void submit(View v) {
        if (mRegistration_user.getText().toString().isEmpty() ||
                mRegistration_email.getText().toString().isEmpty() ||
                mRegistration_pw.getText().toString().isEmpty() ||
                mRegistration_pw_confirming.getText().toString().isEmpty()) {
            toast("please set your registration information", Toast.LENGTH_SHORT);
            return;
        }

        if (!mRegistration_pw.getText().toString().equals(mRegistration_pw_confirming.getText().toString())) {
            toast("password confirming fail", Toast.LENGTH_SHORT);
            return;
        }

        block("RegisterAccount");
        ModuleCloud.mCloudAgent.userRegistrationByEmail(
            mRegistration_user.getText().toString(),
            mRegistration_pw.getText().toString(),
            mRegistration_email.getText().toString(),
            "CVR",
            new RegistrationCallback() {
                @Override
                public void onSuccess(final String userName, final String keyStore) {
                    block(null);
                    toast("[Registration Success] userName:" + userName + ", keyStore:" + keyStore, Toast.LENGTH_SHORT);
                    finish();
                }
                @Override
                public void onFailure(final String userName, final int errorCode, final String message) {
                    block(null);
                    toast("[Registration Failure] userName:" + userName +
                            ", error = " + ModuleCloud.mCloudAgent.getDescription(errorCode) +
                            " (" + errorCode + "), message:" + message, Toast.LENGTH_SHORT);
                }
        });
    }
}

