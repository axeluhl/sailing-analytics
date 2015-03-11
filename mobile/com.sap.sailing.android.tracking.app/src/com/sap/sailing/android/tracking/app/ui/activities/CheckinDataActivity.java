package com.sap.sailing.android.tracking.app.ui.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;

import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.utils.CheckinManager;
import com.sap.sailing.android.tracking.app.valueobjects.CheckinData;

public abstract class CheckinDataActivity extends BaseActivity implements CheckinManager.CheckinDataHandler{

    @Override
    public abstract void onCheckinDataAvailable(CheckinData data);

    /**
     * Shows a pop-up-dialog that informs the user than an DB-operation has failed.
     */
    public void displayDatabaseError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.notify_user_db_operation_failed));
        builder.setCancelable(true);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }

        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
