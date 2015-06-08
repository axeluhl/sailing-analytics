package com.sap.sailing.racecommittee.app.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.services.RaceStateService;
import com.sap.sailing.racecommittee.app.ui.activities.LoginActivity;

public class DataHelper {

    public static void logout(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AppTheme_AlertDialog);
        builder.setTitle(activity.getString(R.string.data_reload_title));
        builder.setMessage(activity.getString(R.string.data_reload_message));
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intentService = new Intent(activity.getBaseContext(), RaceStateService.class);
                intentService.setAction(AppConstants.INTENT_ACTION_CLEAR_RACES);
                activity.getBaseContext().startService(intentService);

                Intent intentActivity = new Intent(activity.getBaseContext(), LoginActivity.class);
                intentActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intentActivity  .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.getBaseContext().startActivity(intentActivity);
            }
        });
        builder.setCancelable(false);
        builder.show();
    }
}
