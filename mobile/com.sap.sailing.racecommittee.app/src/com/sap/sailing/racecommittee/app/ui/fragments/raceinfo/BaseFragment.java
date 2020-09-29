package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.support.v4.app.FragmentTransaction;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.NavigationEvents;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class BaseFragment extends RaceFragment {
    private final static String TAG = BaseFragment.class.getSimpleName();

    @IntDef({START_MODE_PRESETUP, START_MODE_PLANNED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface START_MODE_VALUES {
    }

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
        FragmentTransaction transaction = requireFragmentManager().beginTransaction();
        transaction.replace(viewId, fragment);
        transaction.commit();
    }

    protected void goHome() {
        if (getArguments() != null && getArguments().getInt(START_MODE, START_MODE_PRESETUP) == START_MODE_PRESETUP) {
            openMainScheduleFragment();
        } else {
            sendIntent(AppConstants.ACTION_SHOW_MAIN_CONTENT);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ExLog.i(getActivity(), TAG, "attach fragment " + this.getClass().getSimpleName());
        NavigationEvents.INSTANCE.attach(this);
    }

    @Override
    public void onDetach() {
        ExLog.i(getActivity(), TAG, "detach fragment " + this.getClass().getSimpleName());
        NavigationEvents.INSTANCE.detach(this);
        super.onDetach();
    }

    public boolean onBackPressed() {
        return false;
    }
}
