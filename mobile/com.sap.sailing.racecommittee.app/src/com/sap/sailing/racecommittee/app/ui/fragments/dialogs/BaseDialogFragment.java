package com.sap.sailing.racecommittee.app.ui.fragments.dialogs;

import com.sap.sailing.racecommittee.app.logging.ExLog;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

public abstract class BaseDialogFragment extends DialogFragment {
    private final static String TAG = BaseDialogFragment.class.getName();

    protected abstract CharSequence getNegativeButtonLabel();
    protected abstract CharSequence getPositiveButtonLabel();
    protected abstract Builder createDialog(AlertDialog.Builder builder);

    protected abstract DialogFragmentButtonListener getHost();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return createDialog(new Builder(getActivity())
        .setNegativeButton(getNegativeButtonLabel(), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                onNegativeButton();
            }
        })
        .setPositiveButton(getPositiveButtonLabel(), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                onPositiveButton();
            }
        })
                ).create();
    }

    protected void onNegativeButton() {
        if (getHost() != null) {
            getHost().onDialogNegativeButton();
        } else {
            ExLog.w(TAG, "Dialog host was null.");
        }
    }

    protected void onPositiveButton() {
        if (getHost() != null) {
            getHost().onDialogPositiveButton();
        } else {
            ExLog.w(TAG, "Dialog host was null.");
        }
    }

}
