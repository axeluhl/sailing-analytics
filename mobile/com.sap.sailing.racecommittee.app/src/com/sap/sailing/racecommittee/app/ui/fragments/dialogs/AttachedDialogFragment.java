package com.sap.sailing.racecommittee.app.ui.fragments.dialogs;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

import com.sap.sailing.android.shared.logging.ExLog;

public abstract class AttachedDialogFragment extends LoggableDialogFragment {
    private final static String TAG = AttachedDialogFragment.class.getName();

    protected abstract CharSequence getNegativeButtonLabel();

    protected abstract CharSequence getPositiveButtonLabel();

    protected abstract Builder createDialog(AlertDialog.Builder builder);

    protected abstract DialogListenerHost getHost();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return createDialog(
                new Builder(getActivity()).setNegativeButton(getNegativeButtonLabel(), new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        onNegativeButton();
                    }
                }).setPositiveButton(getPositiveButtonLabel(), new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        onPositiveButton();
                    }
                })).create();
    }

    protected void onNegativeButton() {
        if (getHost() != null) {
            getHost().getListener().onDialogNegativeButton(this);
        } else {
            ExLog.w(getActivity(), TAG, "Dialog host was null.");
        }
    }

    protected void onPositiveButton() {
        if (getHost() != null) {
            getHost().getListener().onDialogPositiveButton(this);
        } else {
            ExLog.w(getActivity(), TAG, "Dialog host was null.");
        }
    }

}
