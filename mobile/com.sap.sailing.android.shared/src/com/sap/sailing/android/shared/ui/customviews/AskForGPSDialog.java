package com.sap.sailing.android.shared.ui.customviews;


import com.sap.sailing.android.shared.R;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

/**
 * This Class can be used to prompt the user to enable GPS.
 * <p>
 * The user will be given the choice to deny or go to his location settings.
 * 
 * @author adrian.riedel@sap.com (D064867)
 *
 */
public class AskForGPSDialog {
    private static Builder dialog;
    /**
     * Shows a dialog where the user is prompted to enable GPS.
     * Requires the context of an active activity.
     * 
     * @param context
     */

    public static void showPrompt (final Context context){
        dialog = new AlertDialog.Builder(context);
        
        dialog.setMessage(context.getString(R.string.gps_required_message));
        dialog.setPositiveButton(context.getString(R.string.gps_required_accept), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                context.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
         });
         dialog.setNegativeButton(context.getString(R.string.gps_required_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                //no-op
            }
        });
        dialog.show();
    }

}
