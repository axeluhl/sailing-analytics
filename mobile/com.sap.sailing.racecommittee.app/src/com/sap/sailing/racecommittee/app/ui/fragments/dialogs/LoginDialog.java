package com.sap.sailing.racecommittee.app.ui.fragments.dialogs;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.racecommittee.app.AppConstants;
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
    protected AlertDialog.Builder createDialog(AlertDialog.Builder builder) {
        return builder.setTitle(getString(R.string.login)).setIcon(R.drawable.ic_assignment_ind_grey600_36dp)
            .setSingleChoiceItems(loginTypeDescriptions, -1, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                    // see loginTypeDescriptions for the indices of the login types
                    case 0:
                        selectedLoginType = LoginType.OFFICER;
                        author = new LogEventAuthorImpl(AppConstants.AUTHOR_TYPE_OFFICER_START, 0);
                        break;
                    case 1:
                        selectedLoginType = LoginType.OFFICER;
                        author = new LogEventAuthorImpl(AppConstants.AUTHOR_TYPE_OFFICER_FINISH, 1);
                        break;
                    case 2:
                        selectedLoginType = LoginType.OFFICER;
                        author = new LogEventAuthorImpl(AppConstants.AUTHOR_TYPE_SHORE_CONTROL, 2);
                        break;
                    case 3:
                        selectedLoginType = LoginType.VIEWER;
                        author = new LogEventAuthorImpl(AppConstants.AUTHOR_TYPE_VIEWER, 3);
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
