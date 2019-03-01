package com.sap.sailing.racecommittee.app.ui.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;
import com.sap.sailing.racecommittee.app.domain.configuration.impl.PreferencesDeviceConfigurationLoader;
import com.sap.sailing.racecommittee.app.ui.activities.PasswordActivity;
import com.sap.sailing.racecommittee.app.ui.fragments.preference.CourseDesignerPreferenceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.preference.GeneralPreferenceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.preference.RegattaPreferenceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.preference.RegattaSpecificPreferenceFragment;
import com.sap.sailing.racecommittee.app.ui.views.decoration.PreferenceMarginItemDecoration;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class MainPreferenceFragment extends LoggableFragment {

    private static final String TAG = MainPreferenceFragment.class.getName();

    public MainPreferenceFragment() {
        // nothing
    }

    public static MainPreferenceFragment newInstance() {
        return new MainPreferenceFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.preference_header, container, false);

        RecyclerView recyclerView = ViewHelper.get(layout, R.id.preference_header);
        if (recyclerView != null) {
            ArrayList<PreferenceItem> items = new ArrayList<>();
            items.add(new PreferenceItem(getString(R.string.settings_category_general),
                    R.drawable.ic_more_horiz_yellow_24dp, GeneralPreferenceFragment.class.getName()));
            items.add(new PreferenceItem(getString(R.string.settings_category_regatta_defaults),
                    R.drawable.ic_boat_yellow_24dp, RegattaPreferenceFragment.class.getName()));
            items.add(new PreferenceItem(getString(R.string.settings_category_regattas), R.drawable.ic_boat_yellow_24dp,
                    RegattaSpecificPreferenceFragment.class.getName()));
            items.add(new PreferenceItem(getString(R.string.settings_category_course_designer),
                    R.drawable.ic_pin_drop_yellow_24dp, CourseDesignerPreferenceFragment.class.getName()));
            items.add(new PreferenceItem(getString(R.string.reload_config), R.drawable.ic_autorenew_yellow_24dp,
                    new Runnable() {
                        @Override
                        public void run() {
                            String configurationName = AppPreferences.on(getActivity()).getDeviceConfigurationName();
                            LoaderManager.LoaderCallbacks<?> configurationLoader = DataManager.create(getActivity())
                                    .createConfigurationLoader(configurationName, new LoadClient<DeviceConfiguration>() {

                                        @Override
                                        public void onLoadFailed(Exception reason) {
                                            if (reason instanceof FileNotFoundException) {
                                                Toast.makeText(getActivity(),
                                                        getString(R.string.loading_configuration_not_found),
                                                        Toast.LENGTH_LONG).show();
                                                ExLog.w(getActivity(), TAG, String.format(
                                                        "There seems to be no configuration for this device: %s",
                                                        reason.toString()));
                                            } else {
                                                Toast.makeText(getActivity(),
                                                        getString(R.string.loading_configuration_failed),
                                                        Toast.LENGTH_LONG).show();
                                                ExLog.ex(getActivity(), TAG, reason);
                                            }
                                        }

                                        @Override
                                        public void onLoadSucceeded(DeviceConfiguration configuration,
                                                boolean isCached) {
                                            // this is our 'global' configuration, let's store it in app preferences
                                            PreferencesDeviceConfigurationLoader
                                                    .wrap(configuration, AppPreferences.on(getActivity())).store();

                                            Toast.makeText(getActivity(),
                                                    getString(R.string.loading_configuration_succeded),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });

                            getLoaderManager().initLoader(0, null, configurationLoader).forceLoad();
                        }
                    }));
            items.add(new PreferenceItem(getString(R.string.logout), R.drawable.ic_logout_yellow_24dp, new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(requireContext())
                            .setTitle(R.string.logout_dialog_title)
                            .setMessage(getString(R.string.logout_dialog_message))
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    AppPreferences.on(getActivity()).setAccessToken(null);
                                    startActivity(new Intent(getActivity(), PasswordActivity.class));
                                    getActivity().finish();
                                }
                            }).setNegativeButton(android.R.string.cancel, null).show();
                }
            }));
            recyclerView.addItemDecoration(new PreferenceMarginItemDecoration(getActivity(),
                    getResources().getDimensionPixelSize(R.dimen.preference_margin)));
            recyclerView.setLayoutManager(
                    new GridLayoutManager(getActivity(), getResources().getInteger(R.integer.preference_columns)));
            recyclerView.setAdapter(new PreferenceAdapter(getActivity(), items));
        }

        return layout;
    }

    private class PreferenceAdapter extends RecyclerView.Adapter<PreferenceViewHolder> {

        private Context mContext;
        private List<PreferenceItem> mItems;

        public PreferenceAdapter(Context context, List<PreferenceItem> items) {
            mContext = context;
            mItems = items;
        }

        @Override
        public PreferenceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View layout = LayoutInflater.from(mContext).inflate(R.layout.pref_header, parent, false);
            return new PreferenceViewHolder(layout);
        }

        @Override
        public void onBindViewHolder(PreferenceViewHolder holder, int position) {
            holder.item = mItems.get(position);
            holder.textView.setText(holder.item.title);
            holder.textView.setCompoundDrawablesWithIntrinsicBounds(holder.item.drawable, null, null, null);

            int count = mItems.size() - 1;
            int columns = getResources().getInteger(R.integer.preference_columns);
            holder.line.setVisibility((position > count - columns) ? View.GONE : View.VISIBLE);
        }

        @Override
        public int getItemCount() {
            return (mItems != null) ? mItems.size() : 0;
        }
    }

    private class PreferenceViewHolder extends RecyclerView.ViewHolder {

        public PreferenceItem item;
        public TextView textView;
        public View line;

        public PreferenceViewHolder(final View itemView) {
            super(itemView);

            textView = (TextView) itemView.findViewById(R.id.preference_title);
            line = itemView.findViewById(R.id.preference_line);
            itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (TextUtils.isEmpty(item.clazz)) {
                        Handler handler = new Handler();
                        handler.post(item.runnable);
                    } else {
                        try {
                            Fragment fragment = (Fragment) Class.forName(item.clazz).newInstance();
                            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(item.title);
                            getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment)
                                    .addToBackStack(null).commit();
                        } catch (ClassNotFoundException ex) {
                            ExLog.ex(getActivity(), TAG, ex);
                        } catch (java.lang.InstantiationException ex) {
                            ExLog.ex(getActivity(), TAG, ex);
                        } catch (IllegalAccessException ex) {
                            ExLog.ex(getActivity(), TAG, ex);
                        }
                    }
                }
            });
        }
    }

    private class PreferenceItem {

        public String title;
        public Drawable drawable;
        public String clazz;
        public Runnable runnable;

        private PreferenceItem(String title, @DrawableRes int drawable) {
            this.title = title;
            this.drawable = ContextCompat.getDrawable(getActivity(), drawable);
        }

        public PreferenceItem(String title, @DrawableRes int drawable, String clazz) {
            this(title, drawable);
            this.clazz = clazz;
        }

        public PreferenceItem(String title, @DrawableRes int drawable, Runnable runnable) {
            this(title, drawable);
            this.runnable = runnable;
        }
    }
}
