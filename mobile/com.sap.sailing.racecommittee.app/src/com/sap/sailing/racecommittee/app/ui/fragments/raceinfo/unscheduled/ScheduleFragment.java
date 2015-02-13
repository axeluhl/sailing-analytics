package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.unscheduled;

import android.os.Bundle;
import android.support.annotation.IdRes;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;

public class ScheduleFragment extends RaceFragment {

    protected void openMainScheduleFragment() {
        openFragment(MainScheduleFragment.newInstance());
    }

    public void openFragment(RaceFragment fragment) {
        openFragment(fragment, R.id.racing_view_container);
    }

    public void openFragment(RaceFragment fragment, @IdRes int viewId) {
        Bundle arguments = fragment.getArguments();
        if (arguments == null) {
            arguments = new Bundle();
        }
        arguments.putAll(getRecentArguments());
        fragment.setArguments(arguments);
        getFragmentManager()
                .beginTransaction()
                .replace(viewId, fragment)
                .commit();
    }
}
