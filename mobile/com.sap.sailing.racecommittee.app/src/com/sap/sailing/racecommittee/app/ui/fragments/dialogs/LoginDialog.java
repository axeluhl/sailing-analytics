package com.sap.sailing.racecommittee.app.ui.fragments.dialogs;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.AbstractLogEventAuthorImpl;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.logging.LogEvent;

public class LoginDialog extends ActivityAttachedDialogFragment {

    private static final LoginType DefaultLoginType = LoginType.NONE;
    public enum LoginType {
        OFFICER, VIEWER, NONE;
    }

    private CharSequence[] loginTypeDescriptions;
    private LoginType selectedLoginType;
    private AbstractLogEventAuthor author;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loginTypeDescriptions = new CharSequence[] { getString(R.string.login_type_officer_on_start_vessel),
                                                     getString(R.string.login_type_officer_on_finish_vessel),
                                                     getString(R.string.login_type_shore_control),
                                                     getString(R.string.login_type_viewer) };
        selectedLoginType = DefaultLoginType;
    }

    public LoginType getSelectedLoginType() {
        return selectedLoginType;
    }
    
    public AbstractLogEventAuthor getAuthor() {
        return author;
    }

    @Override
    protected CharSequence getNegativeButtonLabel() {
        return getString(R.string.cancel);
    }

    @Override
    protected CharSequence getPositiveButtonLabel() {
        return getString(R.string.login);
    }

    @Override
    protected Builder createDialog(Builder builder) {
        return builder
                .setTitle(getString(R.string.login))
                .setIcon(R.drawable.ic_menu_login)
                .setSingleChoiceItems(loginTypeDescriptions, -1, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                        // see loginTypeDescriptions for the indices of the login types
                        case 0:
                            selectedLoginType = LoginType.OFFICER;
                            author = new AbstractLogEventAuthorImpl("Race Officer on Start Vessel", 0);
                            break;
                        case 1:
                            selectedLoginType = LoginType.OFFICER;
                            author = new AbstractLogEventAuthorImpl("Race Officer on Finish Vessel", 1);
                            break;
                        case 2:
                            selectedLoginType = LoginType.OFFICER;
                            author = new AbstractLogEventAuthorImpl("Shore Control", 2);
                            break;
                        case 3:
                            selectedLoginType = LoginType.VIEWER;
                            author = new AbstractLogEventAuthorImpl("Viewer", 3);
                            break;
                        default:
                            selectedLoginType = LoginType.NONE;
                            break;
                        }
                    }
                });
    }

    @Override
    protected void onNegativeButton() {
        selectedLoginType = DefaultLoginType;
        ExLog.i(getActivity(), LogEvent.LOGIN_BUTTON_NEGATIVE, String.valueOf(selectedLoginType));
        super.onNegativeButton();
    }

    @Override
    protected void onPositiveButton() {
        ExLog.i(getActivity(), LogEvent.LOGIN_BUTTON_POSITIVE, String.valueOf(selectedLoginType));
        super.onPositiveButton();
    }

}
