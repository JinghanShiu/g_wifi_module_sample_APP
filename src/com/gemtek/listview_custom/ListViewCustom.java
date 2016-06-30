package com.gemtek.listview_custom;

import android.content.Context;
import android.widget.ListView;
import android.util.AttributeSet;

public class ListViewCustom extends ListView {
    private OverScrollCallback mOverScrollCallback;

    public ListViewCustom(Context context) {
        super(context);
    }

    public ListViewCustom(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ListViewCustom(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
        if (mOverScrollCallback != null) mOverScrollCallback.onOverScrolled();
    }

    public boolean setOverScrollCallback(OverScrollCallback mOverScrollCallback) {
        if (mOverScrollCallback == null) return false;
        this.mOverScrollCallback = mOverScrollCallback;
        return true;
    }
}

