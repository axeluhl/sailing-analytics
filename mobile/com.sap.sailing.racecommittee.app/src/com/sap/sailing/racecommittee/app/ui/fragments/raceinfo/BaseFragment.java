package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.app.FragmentTransaction;
import android.support.annotation.IdRes;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;

public class BaseFragment extends RaceFragment {

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
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(viewId, fragment).commit();
    }
}
