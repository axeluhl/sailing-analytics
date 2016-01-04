package com.sap.sailing.racecommittee.app.ui.fragments.panels;

import android.app.Fragment;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.BaseFragment;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;

public abstract class BasePanelFragment extends RaceFragment {

    /**
     * @param view  container view
     * @param resId resource if of the marker drawable
     * @return new level (0 - normal / 1 - toggled)
     */
    protected int toggleMarker(View view, @IdRes int resId) {
        int retValue = -1;

        ImageView image = (ImageView) view.findViewById(resId);
        if (image != null) {
            Drawable drawable = image.getDrawable();
            if (drawable != null) {
                int level = drawable.getLevel();
                retValue = setMarkerLevel(view, resId, 1 - level);
            }
        }

        return retValue;
    }

    /**
     * @param view  container view
     * @param resId resource id of the marker drawable
     * @return 0 - normal / 1 - toggled
     */
    protected int getMarkerLevel(View view, @IdRes int resId) {
        int retValue = -1;

        if (isAdded()) {
            ImageView image = (ImageView) view.findViewById(resId);
            if (image != null) {
                Drawable drawable = image.getDrawable();
                if (drawable != null) {
                    retValue = drawable.getLevel();
                }
            }
        }

        return retValue;
    }

    /**
     * @param view  container view
     * @param resId resource id of the marker drawable
     * @return is view marked as normal (level == 0)
     */
    protected boolean isNormal(View view, @IdRes int resId) {
        return (getMarkerLevel(view, resId) == 0);
    }

    /**
     * @param view  container view
     * @param resId resource id of the marker drawable
     * @param level 0 - normal / 1 - toggled
     * @return new level, which should be the input level, if everything is correct
     */
    protected int setMarkerLevel(View view, @IdRes int resId, int level) {
        int retValue = -1;

        if (isAdded()) {
            ImageView image = (ImageView) view.findViewById(resId);
            if (image != null) {
                Drawable drawable = image.getDrawable();
                if (drawable != null) {
                    drawable.setLevel(level);
                    retValue = drawable.getLevel();
                    switch (retValue) {
                    case 1: // clicked
                        view.setBackgroundColor(ThemeHelper.getColor(getActivity(), R.attr.sap_gray_black_20));
                        break;

                    default:
                        view.setBackgroundColor(ThemeHelper.getColor(getActivity(), R.attr.sap_gray));
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

    protected void resetFragment(View lockIcon, @IdRes int idRes, Class<? extends BaseFragment> cls) {
        if (lockIcon != null && lockIcon.getVisibility() == View.VISIBLE) {
            if (getFragmentManager() != null) {
                Fragment fragment = getFragmentManager().findFragmentById(idRes);
                if (fragment != null) {
                    if (cls.getCanonicalName().equals(fragment.getClass().getCanonicalName())) {
                        sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
                    }
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
        getFragmentManager().beginTransaction().replace(idRes, fragment).commit();
    }

    protected void showChangeDialog(DialogInterface.OnClickListener positiveButton) {
        showChangeDialog(positiveButton, null);
    }

    protected void showChangeDialog(DialogInterface.OnClickListener positiveButton, DialogInterface.OnClickListener negativeButton) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_AlertDialog);
        builder.setTitle(getString(R.string.change_title));
        builder.setMessage(getString(R.string.change_message));
        builder.setPositiveButton(getString(R.string.change_proceed), positiveButton);
        builder.setNegativeButton(getString(R.string.change_cancel), negativeButton);
        builder.setCancelable(true);
        builder.create().show();
    }

    protected void disableToggle(View container, @IdRes int resId) {
        disableToggle(container, resId, 200);
    }

    protected void disableToggle(final View container, @IdRes final int resId, int delay) {
        container.setClickable(false);
        container.setBackgroundColor(getResources().getColor(R.color.constant_sap_yellow_1));
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                container.setClickable(true);
                setMarkerLevel(container, resId, getMarkerLevel(container, resId));
            }
        }, delay);
    }
}
