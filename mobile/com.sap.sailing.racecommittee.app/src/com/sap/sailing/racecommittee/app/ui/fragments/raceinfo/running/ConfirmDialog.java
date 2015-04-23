package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.running;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.sap.sailing.racecommittee.app.R;

public class ConfirmDialog extends DialogFragment {
    private ConfirmRecallListener confirmRecallListener;

    public interface ConfirmRecallListener {
        public void returnAddedElementToPicker(boolean recall);
    }
    
    public void setCallback(ConfirmRecallListener callback) {
        this.confirmRecallListener = callback;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_AlertDialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.confirm_dialog, null);
        builder.setView(view).setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                returnData();
            }
        }).setNegativeButton(R.string.abort, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ConfirmDialog.this.getDialog().cancel();
            }
        });

        return builder.create();
    }

    private void returnData() {
        if (confirmRecallListener != null) {
            confirmRecallListener.returnAddedElementToPicker(true);
        }
    }
}