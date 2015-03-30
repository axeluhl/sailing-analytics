package com.sap.sailing.racecommittee.app.ui.fragments.panels;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedure;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.utils.ColorHelper;

public abstract class BasePanelFragment extends RaceFragment {

    protected int toggleMarker(View v, @IdRes int resId) {
        int retValue = -1;
        ImageView view = (ImageView) v.findViewById(resId);
        if (view != null) {
            Drawable drawable = view.getDrawable();
            if (drawable != null) {
                int level = drawable.getLevel();
                if (AppPreferences.on(getActivity()).getTheme().equals(AppConstants.LIGHT_THEME)) {
                    level -= 10;
                }
                retValue = setMarkerLevel(v, resId, 1 - level);
            }
        }

        return retValue;
    }

    protected int setMarkerLevel(View v, @IdRes int resId, int level) {
        int retValue = -1;

        int offset = 0;
        if (AppPreferences.on(getActivity()).getTheme().equals(AppConstants.LIGHT_THEME)) {
            offset = 10;
        }

        ImageView view = (ImageView) v.findViewById(resId);
        if (view != null) {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            Drawable drawable = view.getDrawable();
            if (drawable != null) {
                drawable.setLevel(level + offset);
                retValue = drawable.getLevel() - offset;
                switch (retValue) {
                    case 1:
//                        if (layoutParams != null) {
//                            layoutParams.height = getResources().getDimensionPixelSize(R.dimen.thick_line);
//                            view.invalidate();
//                        }
                        v.setBackgroundColor(ColorHelper.getThemedColor(getActivity(), R.attr.sap_gray_black_20));
                        break;

                    default:
//                        if (layoutParams != null) {
//                            layoutParams.height = getResources().getDimensionPixelSize(R.dimen.thin_line);
//                            view.invalidate();
//                        }
                        v.setBackgroundColor(ColorHelper.getThemedColor(getActivity(), R.attr.sap_gray));
                        break;
                }
            }
        }
        return retValue;
    }

    protected void changeVisibility(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    protected void resetFragment(View lockIcon, Class cls) {
        if (lockIcon != null && lockIcon.getVisibility() == View.VISIBLE) {
            Fragment fragment = getFragmentManager().findFragmentById(R.id.race_frame);
            if (fragment != null) {
                //TODO Reset only if current fragment will be invalid
//                replaceFragment(RaceFlagViewerFragment.newInstance());
            }
        }
    }

    protected void replaceFragment(RaceFragment fragment) {
        Bundle args = getRecentArguments();
        if (fragment.getArguments() != null) {
            args.putAll(fragment.getArguments());
        }
        fragment.setArguments(args);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.race_frame, fragment)
                .commit();
    }
}
