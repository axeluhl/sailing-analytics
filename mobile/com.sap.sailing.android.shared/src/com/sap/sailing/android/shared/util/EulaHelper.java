package com.sap.sailing.android.shared.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import com.sap.sailing.android.shared.R;

public class EulaHelper {

    private static final String EULA_PREFERENCES = "eula.preferences";
    private static final String EULA_CONFIRMED = "eula.confirmed";

    public static void showTrackingEulaDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.eula_title);
        builder.setMessage(R.string.eula_message);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                storeEulaAccepted(context);
            }
        });
        builder.show();
    }

    public static void showEulaDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.eula_title);
        builder.setMessage(R.string.eula_message);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                storeEulaAccepted(context);
            }
        });
        builder.setNegativeButton(R.string.skip, null);
        builder.show();
    }

    private static void storeEulaAccepted(Context context) {
        SharedPreferences preferences = context.getApplicationContext().getSharedPreferences(EULA_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(EULA_CONFIRMED, true);
        editor.apply();
    }

    public static boolean isEulaAccepted(Context context) {
        boolean accepted = false;
        SharedPreferences preferences = context.getApplicationContext().getSharedPreferences(EULA_PREFERENCES, Context.MODE_PRIVATE);
        if (preferences.contains(EULA_CONFIRMED)) {
            accepted = preferences.getBoolean(EULA_CONFIRMED, false);
        }
        return accepted;
    }

    public static void openEulaPage(Context context) {
        String url = context.getString(R.string.eula_url);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent);
    }
}
