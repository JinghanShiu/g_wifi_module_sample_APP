package com.gemtek.modulecloud;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.view.inputmethod.InputMethodManager;

public class UITools {
    private static ProgressDialog mProgressDialog;
    private static AlertDialog.Builder mAlertDialog;

    // 1. Hide Keyboard
    public static void hideKeyBoard(final Activity activity) {
        InputMethodManager im = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null)
            im.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /* 2. Block UI by Progress Dialog
     *
     *    show dialog with message
     *    set message null to unblock
     */
    public static void blockUI(final Activity activity, final String message) {
       if (message != null) {
           hideKeyBoard(activity);
           mProgressDialog = ProgressDialog.show(activity, "", message, true);
       } else {
           mProgressDialog.dismiss();
       }
    }

    // 3. Show Alert Dialog
    public static void showAlertDialog(final Activity activity, final String title, final String message) {
        mAlertDialog = new AlertDialog.Builder(activity)
                                      .setTitle(title)
                                      .setMessage(message);

        activity.runOnUiThread(new Runnable() {
            public void run() {
                mAlertDialog.show();
            }
        });
    }
}
