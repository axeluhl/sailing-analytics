package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.IdRes;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;

public class ScheduleFragment extends RaceFragment {

    protected void openMainScheduleFragment() {
        replaceFragment(MainScheduleFragment.newInstance());
    }

    public void replaceFragment(RaceFragment fragment) {
        replaceFragment(fragment, R.id.racing_view_container);
    }

    public void replaceFragment(RaceFragment fragment, @IdRes int viewId) {
        Bundle args = getRecentArguments();
        if (fragment.getArguments() != null) {
            args.putAll(fragment.getArguments());
        }
        fragment.setArguments(args);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(viewId, fragment).commit();

        sendIntent(R.string.intent_update_ui);
    }
}
