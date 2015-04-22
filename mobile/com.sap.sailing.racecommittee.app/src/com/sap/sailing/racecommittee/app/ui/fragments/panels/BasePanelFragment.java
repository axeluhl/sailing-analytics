package com.sap.sailing.racecommittee.app.ui.fragments.panels;

import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.view.View;
import android.widget.ImageView;
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
                if (level < 0) {
                    level = 0;
                }
                retValue = setMarkerLevel(v, resId, 1 - level);
            }
        }

        return retValue;
    }

    protected int setMarkerLevel(View v, @IdRes int resId, int level) {
        int retValue = -1;

        if (isAdded()) {
            int offset = 0;
            if (AppConstants.LIGHT_THEME.equals(AppPreferences.on(getActivity()).getTheme())) {
                offset = 10;
            }

            ImageView view = (ImageView) v.findViewById(resId);
            if (view != null) {
                Drawable drawable = view.getDrawable();
                if (drawable != null) {
                    drawable.setLevel(level + offset);
                    retValue = drawable.getLevel() - offset;
                    switch (retValue) {
                    case 1:
                        v.setBackgroundColor(ColorHelper.getThemedColor(getActivity(), R.attr.sap_gray_black_20));
                        break;

                    default:
                        v.setBackgroundColor(ColorHelper.getThemedColor(getActivity(), R.attr.sap_gray));
                        break;
                    }
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

    protected void resetFragment(View lockIcon, @IdRes int idRes, Class cls) {
        if (lockIcon != null && lockIcon.getVisibility() == View.VISIBLE) {
            Fragment fragment = getFragmentManager().findFragmentById(idRes);
            if (fragment != null) {
                if (cls.getCanonicalName().equals(fragment.getClass().getCanonicalName())) {
                    sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
                }
            }
        }
    }

    protected void replaceFragment(RaceFragment fragment) {
        replaceFragment(fragment, R.id.race_frame);
    }

    protected void replaceFragment(RaceFragment fragment, @IdRes int idRes) {
        Bundle args = getRecentArguments();
        if (fragment.getArguments() != null) {
            args.putAll(fragment.getArguments());
        }
        fragment.setArguments(args);
        getFragmentManager()
                .beginTransaction()
                .replace(idRes, fragment)
                .commit();
    }
}
