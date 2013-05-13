package com.sap.sailing.racecommittee.app.ui.fragments.dialogs;

import android.app.AlertDialog.Builder;
import android.os.Bundle;

import com.sap.sailing.racecommittee.app.R;

public class LoadFailedDialog extends FragmentDialogFragment {
    private static final String ARGS_ERROR_MSG = LoadFailedDialog.class.getName() + ".errorMessage";

    public static LoadFailedDialog create(String errorMessage) {
        LoadFailedDialog dialog = new LoadFailedDialog();

        Bundle args = new Bundle();
        args.putString(ARGS_ERROR_MSG, errorMessage);
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    protected CharSequence getPositiveButtonLabel() {
        return "Retry";
    }

    @Override
    protected CharSequence getNegativeButtonLabel() {
        return "Cancel";
    }

    @Override
    protected Builder createDialog(Builder builder) {
        return builder
                .setMessage(String.format(
                        "There was an error loading the requested data:\n%s\nDo you want to retry?", 
                        getArguments().getString(ARGS_ERROR_MSG)))
                        .setTitle("Load failure")
                        .setIcon(R.drawable.ic_dialog_alert_holo_light);
    }
}
