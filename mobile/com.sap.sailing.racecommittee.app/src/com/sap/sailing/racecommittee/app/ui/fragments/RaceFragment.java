package com.sap.sailing.racecommittee.app.ui.fragments;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.BitmapHelper;
import com.sap.sailing.android.shared.util.BroadcastManager;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.abstractlog.race.state.RaceState;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.OnlineDataManager;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.ui.activities.BaseActivity;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;
import com.sap.sailing.racecommittee.app.utils.TickListener;
import com.sap.sailing.racecommittee.app.utils.TickSingleton;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.view.View;
import android.widget.ImageView;

public abstract class RaceFragment extends LoggableFragment implements TickListener {

    @IntDef({ MOVE_DOWN, MOVE_NONE, MOVE_UP })
    @Retention(RetentionPolicy.SOURCE)
    protected @interface MOVE_VALUES {
    }

    private static final String TAG = RaceFragment.class.getName();

    protected ManagedRace managedRace;
    protected AppPreferences preferences;

    protected ArrayList<ImageView> mDots;
    protected ArrayList<View> mPanels;
    protected int mActivePage = 0;

    protected final static int MOVE_DOWN = -1;
    protected final static int MOVE_NONE = 0;
    protected final static int MOVE_UP = 1;

    public static Bundle createArguments(ManagedRace race) {
        Bundle arguments = new Bundle();
        arguments.putString(AppConstants.INTENT_EXTRA_RACE_ID, race.getId());
        return arguments;
    }

    public ManagedRace getRace() {
        return managedRace;
    }

    public RaceState getRaceState() {
        return getRace().getState();
    }

    /**
     * Creates a bundle that contains the race id as parameter for the next fragment
     *
     * @return a bundle containing the race id
     */
    protected Bundle getRecentArguments() {
        Bundle args = new Bundle();
        args.putString(AppConstants.INTENT_EXTRA_RACE_ID, managedRace.getId());
        return args;
    }

    @Override
    public void notifyTick(TimePoint now) {
        // see subclasses.
    }

    public boolean isFragmentUIActive() {
        return isAdded() && !isDetached() && !isRemoving();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getArguments() != null) {
            String raceId = getArguments().getString(AppConstants.INTENT_EXTRA_RACE_ID);
            managedRace = OnlineDataManager.create(getActivity()).getDataStore().getRace(raceId);
            if (managedRace == null) {
                throw new IllegalStateException("Unable to obtain ManagedRace " + raceId
                        + " from datastore on start of " + getClass().getName());
            }
        } else {
            ExLog.i(getActivity(), TAG, "no arguments!?");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        BaseActivity baseActivity = (BaseActivity) activity;
        if (baseActivity != null) {
            preferences = baseActivity.getPreferences();
        } else {
            preferences = AppPreferences.on(activity);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        TickSingleton.INSTANCE.registerListener(this);
        notifyTick(MillisecondsTimePoint.now());
    }

    @Override
    public void onStop() {
        super.onStop();

        TickSingleton.INSTANCE.unregisterListener(this);
    }

    protected void sendIntent(String action) {
        sendIntent(action, null, null);
    }

    protected void sendIntent(String action, String extra_key, String extra_value) {
        if (action != null) {
            Intent intent = new Intent(action);
            if (extra_key != null) {
                intent.putExtra(extra_key, extra_value);
            }
            BroadcastManager.getInstance(getActivity()).addIntent(intent);
        }
    }

    protected String getCourseName() {
        String courseName = "";
        if (getRace() != null && getRaceState() != null) {
            CourseBase courseDesign = getRaceState().getCourseDesign();
            if (courseDesign != null) {
                if (Util.isEmpty(courseDesign.getWaypoints())) {
                    courseName = courseDesign.getName();
                } else {
                    courseName = String.format(getString(R.string.course_design_number_waypoints),
                            Util.size(courseDesign.getWaypoints()));
                }
            } else {
                courseName = getString(R.string.no_course_active);
            }
        }
        return courseName;
    }

    protected @IdRes int getFrameId(Activity activity, @IdRes int defaultFrame, @IdRes int fallbackFrame,
            boolean changeVisibility) {
        int frame = 0;
        View view = activity.findViewById(defaultFrame);
        if (view != null) {
            if (changeVisibility) {
                ViewHelper.setSiblingsVisibility(view, View.GONE);
            }
            frame = defaultFrame;
        } else if (activity.findViewById(fallbackFrame) != null) {
            frame = fallbackFrame;
        }
        return frame;
    }

    protected void viewPanel(@MOVE_VALUES int direction) {
        if (mDots.size() == 0 || (mPanels != null && mPanels.size() == 0)) {
            return;
        }

        // find next active page (with overflow)
        mActivePage += direction;
        if (mActivePage < 0) {
            mActivePage = mDots.size() - 1;
        }
        if (mActivePage == mDots.size()) {
            mActivePage = 0;
        }

        // ignore invisible dots
        if (mDots.get(mActivePage).getVisibility() == View.GONE) {
            viewPanel(direction);
        }

        // tint all dots gray
        for (ImageView mDot : mDots) {
            int tint = ThemeHelper.getColor(getActivity(), R.attr.sap_light_gray);
            Drawable drawable = BitmapHelper.getTintedDrawable(getActivity(), R.drawable.ic_dot, tint);
            mDot.setImageDrawable(drawable);
        }

        // tint current dot black
        int tint = ThemeHelper.getColor(getActivity(), R.attr.black);
        Drawable drawable = BitmapHelper.getTintedDrawable(getActivity(), R.drawable.ic_dot, tint);
        mDots.get(mActivePage).setImageDrawable(drawable);

        // hide all panels
        if (mPanels != null) {
            for (View view : mPanels) {
                view.setVisibility(View.GONE);
            }

            // show current panel
            mPanels.get(mActivePage).setVisibility(View.VISIBLE);
        }
    }
}
