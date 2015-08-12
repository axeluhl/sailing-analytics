package com.sap.sailing.racecommittee.app.ui.fragments;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.activities.PreferenceActivity;
import com.sap.sailing.racecommittee.app.ui.activities.SystemInformationActivity;
import com.sap.sailing.racecommittee.app.ui.fragments.preference.GeneralPreferenceFragment;

public class LoginBackdrop extends Fragment {

    private static final String TAG = LoginBackdrop.class.getName();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.login_backdrop, container, false);

        ImageView settings = ViewHelper.get(layout, R.id.settings_button);
        if (settings != null) {
            settings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openSettings();
                }
            });
        }

        ImageView info = ViewHelper.get(layout, R.id.technical_info);
        if (info != null) {
            info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openInfo();
                }
            });
        }

        ImageView refresh = ViewHelper.get(layout, R.id.refresh_data);
        if (refresh != null) {
            refresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refreshData();
                }
            });
        }

        ImageView more = ViewHelper.get(layout, R.id.more);
        if (more != null) {
            more.setOnClickListener(new View.OnClickListener() {
                
                //Because of massive usage of reflection (try {} catch ())
                //Don't know how to fix the warning a better way
                @SuppressWarnings("unchecked")
                @Override
                public void onClick(View view) {
                    PopupMenu popupMenu = new PopupMenu(getActivity(), view);
                    popupMenu.inflate(R.menu.login_menu);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.technical_info:
                                    openInfo();
                                    break;

                                case R.id.settings_button:
                                    openSettings();
                                    break;

                                default:
                                    refreshData();
                            }
                            return true;
                        }
                    });
                    popupMenu.show();

                    // Try to force some vertical offset
                    try {
                        Object menuHelper;
                        Field fMenuHelper = PopupMenu.class.getDeclaredField("mPopup");
                        fMenuHelper.setAccessible(true);
                        menuHelper = fMenuHelper.get(popupMenu);
                        Field fListPopup = menuHelper.getClass().getDeclaredField("mPopup");
                        fListPopup.setAccessible(true);
                        Object listPopup = fListPopup.get(menuHelper);
                        Class<?> listPopupClass = listPopup.getClass();

                        int height = view.getHeight();
                        // Invoke setVerticalOffset() with the negative height to move up by that distance
                        Method setVerticalOffset = listPopupClass.getDeclaredMethod("setVerticalOffset", int.class);
                        setVerticalOffset.invoke(listPopup, -height);

                        int width = (Integer) listPopupClass.getDeclaredMethod("getWidth").invoke(listPopup);
                        width -= view.getWidth();
                        // Invoke setHorizontalOffset() with the negative height to move up by that distance
                        Method setHorizontalOffset = listPopupClass.getDeclaredMethod("setHorizontalOffset", int.class);
                        setHorizontalOffset.invoke(listPopup, -width);

                        // Invoke show() to update the window's position
                        Method show = listPopupClass.getDeclaredMethod("show");
                        show.invoke(listPopup);
                    } catch (Exception e) {
                        // an exception here indicates a programming error rather than an exceptional condition
                        // at runtime
                        ExLog.w(getActivity(), TAG, "Unable to force offset" + e.getLocalizedMessage());
                    }
                }
            });
        }

        return layout;
    }

    private void refreshData() {
        Intent intent = new Intent(AppConstants.INTENT_ACTION_RESET);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }

    private void openInfo() {
        Intent intent = new Intent(getActivity(), SystemInformationActivity.class);
        startActivity(intent);
    }

    private void openSettings() {
        Intent intent = new Intent(getActivity(), PreferenceActivity.class);
        intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, GeneralPreferenceFragment.class.getName());
        startActivity(intent);
    }
}
