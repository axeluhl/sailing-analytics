package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import android.app.FragmentTransaction;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;

import com.sap.sailing.android.shared.util.AppUtils;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;

public class BaseFragment extends RaceFragment {

    @IntDef({START_MODE_PRESETUP, START_MODE_PLANNED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface START_MODE_VALUES {}

    /**
     * Argument for new Fragment Instance
     */
    protected final static String START_MODE = "startMode";

    /**
     * Start mode from MainScheduleFragment
     */
    public final static int START_MODE_PRESETUP = 0;

    /**
     * Start mode, if race state > UNSCHEDULED
     */
    public final static int START_MODE_PLANNED = 1;

    protected void openMainScheduleFragment() {
        replaceFragment(MainScheduleFragment.newInstance());
    }

    public void replaceFragment(RaceFragment fragment) {
        replaceFragment(fragment, R.id.racing_view_container);
    }

    public void replaceFragment(RaceFragment fragment, @IdRes int viewId) {
        if (fragment.getArguments() == null) {
            fragment.setArguments(getRecentArguments());
        } else {
            fragment.getArguments().putAll(getRecentArguments());
        }
        if (getArguments() != null) {
            fragment.getArguments().putAll(getArguments());
        }
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(viewId, fragment).commit();
    }

    protected void goHome() {
        if (getArguments() != null && getArguments().getInt(START_MODE, START_MODE_PRESETUP) == START_MODE_PRESETUP) {
            openMainScheduleFragment();
        } else {
            sendIntent(AppConstants.INTENT_ACTION_CLEAR_TOGGLE);
            sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
        }
    }
}
