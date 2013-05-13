package com.sap.sailing.racecommittee.app.ui.fragments.dialogs;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.logging.ExLog;

public class LoginDialog extends ActivityDialogFragment {

    private static final LoginType DefaultLoginType = LoginType.OFFICER;
    public enum LoginType {
        OFFICER, VIEWER;
    }

    private CharSequence[] loginTypeDescriptions;
    private LoginType selectedLoginType = DefaultLoginType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loginTypeDescriptions = new CharSequence[2];
        loginTypeDescriptions[0] = getString(R.string.login_type_officer);
        loginTypeDescriptions[1] = getString(R.string.login_type_viewer);
    }

    public LoginType getSelectedLoginType() {
        return selectedLoginType;
    }

    @Override
    protected CharSequence getNegativeButtonLabel() {
        return "Cancel";
    }

    @Override
    protected CharSequence getPositiveButtonLabel() {
        return "Login";
    }

    @Override
    protected Builder createDialog(Builder builder) {
        return builder
                .setTitle("Login onto course area")
                .setIcon(R.drawable.ic_menu_login)
                .setSingleChoiceItems(loginTypeDescriptions, 0, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                        case 0:
                            selectedLoginType = LoginType.OFFICER;
                            break;
                        case 1:
                            selectedLoginType = LoginType.VIEWER;
                            break;
                        default:
                            throw new IllegalStateException("Unknown login type selected.");
                        }
                    }
                });
    }

    @Override
    protected void onNegativeButton() {
        selectedLoginType = DefaultLoginType;
        ExLog.i(ExLog.LOGIN_BUTTON_NEGATIVE, String.valueOf(selectedLoginType), getActivity());
        super.onNegativeButton();
    }

    @Override
    protected void onPositiveButton() {
        ExLog.i(ExLog.LOGIN_BUTTON_POSITIVE, String.valueOf(selectedLoginType), getActivity());
        super.onPositiveButton();
    }

}
