package com.sap.sailing.android.shared.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;

import com.sap.sailing.android.shared.R;
import com.sap.sailing.android.shared.util.AppUtils;

public class AboutDialog {
    private Dialog dialog;

    public AboutDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage(AppUtils.getBuildInfo(context)).setTitle(context.getString(R.string.application_version));
        dialog = builder.create();
    }

    public void show() {
        dialog.show();
    }
}
