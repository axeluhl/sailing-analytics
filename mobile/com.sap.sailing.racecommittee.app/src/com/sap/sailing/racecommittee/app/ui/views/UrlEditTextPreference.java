package com.sap.sailing.racecommittee.app.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.preference.EditTextPreference;
import android.util.AttributeSet;

public class UrlEditTextPreference extends EditTextPreference {

    public UrlEditTextPreference(Context context) {
        super(context);
    }

    public UrlEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UrlEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public UrlEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setText(String text) {
        super.setText(normalizeUrl(text));
    }

    private String normalizeUrl(String url) {
        // add missing protocol
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }

        // remove trailing slash
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        return url;
    }

    @Override
    public CharSequence getSummary() {
        return getText();
    }
}
