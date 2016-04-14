package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.sap.sailing.android.shared.data.http.HttpJsonPostRequest;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.AppUtils;
import com.sap.sailing.android.shared.util.BroadcastManager;
import com.sap.sailing.android.shared.util.NetworkHelper;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.common.impl.RaceColumnConstants;
import com.sap.sailing.domain.common.racelog.RaceLogServletConstants;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.layouts.HeaderLayout;

public class RaceFactorFragment extends BaseFragment implements View.OnClickListener {

    private static final String TAG = RaceFactorFragment.class.getName();

    private HeaderLayout mHeader;
    private EditText mFactor;

    public static RaceFactorFragment newInstance(@START_MODE_VALUES int startMode) {
        RaceFactorFragment fragment = new RaceFactorFragment();
        Bundle args = new Bundle();
        args.putInt(START_MODE, startMode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View layout = LayoutInflater.from(getActivity()).inflate(R.layout.race_column_factor, container, false);

        mHeader = ViewHelper.get(layout, R.id.header);
        if (mHeader != null) {
            mHeader.setHeaderOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goHome();
                }
            });
        }

        mFactor = ViewHelper.get(layout, R.id.column_factor);

        Button button = ViewHelper.get(layout, R.id.set_column_factor);
        if (button != null) {
            button.setOnClickListener(this);
        }

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getView() != null && getArguments() != null) {
            switch (getArguments().getInt(START_MODE, START_MODE_PRESETUP)) {
                case START_MODE_PLANNED:
                    if (AppUtils.with(getActivity()).isLand()) {
                        mHeader.setVisibility(View.GONE);
                    }
                    break;

                default:
                    break;
            }
        }

        if (mFactor != null) {
            DecimalFormat format = new DecimalFormat("#.####", new DecimalFormatSymbols(Locale.US));
            String explicitFactor = null;
            if (getRace().getExplicitFactor() != null) {
                explicitFactor = format.format(getRace().getExplicitFactor());
            }
            mFactor.setText(explicitFactor);
        }
    }

    @Override
    public void onClick(final View v) {
        v.setEnabled(false);

        final String factor = mFactor.getText().toString();
        Uri.Builder uri = Uri.parse(preferences.getServerBaseURL()).buildUpon();
        uri.appendPath("sailingserver");
        uri.appendPath("api");
        uri.appendPath("v1");
        uri.appendPath("leaderboards");
        uri.appendPath(getRace().getRaceGroup().getName());
        uri.appendPath("racecolumnfactors");
        uri.appendQueryParameter(RaceLogServletConstants.PARAMS_RACE_COLUMN_NAME, getRace().getName());
        uri.appendQueryParameter(RaceColumnConstants.EXPLICIT_FACTOR, factor);

        if (preferences.isSendingActive()) {
            try {
                final HttpJsonPostRequest request = new HttpJsonPostRequest(getActivity(), new URL(uri.toString()));
                NetworkHelper.getInstance(getActivity()).executeHttpJsonRequestAsync(request, new NetworkHelper.NetworkHelperSuccessListener() {

                    @Override
                    public void performAction(JSONObject response) {
                        updateData(v, factor);
                    }

                }, new NetworkHelper.NetworkHelperFailureListener() {

                    @Override
                    public void performAction(NetworkHelper.NetworkHelperError e) {
                        v.setEnabled(true);
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_AlertDialog);
                        builder.setTitle(R.string.race_factor);
                        builder.setMessage(getActivity().getString(R.string.set_factor_error, e.getMessage()));
                        builder.setPositiveButton(android.R.string.ok, null);
                        ExLog.e(getActivity(), TAG, e.getMessage());
                    }
                });
            } catch (MalformedURLException e) {
                ExLog.e(getActivity(), TAG, e.getMessage());
            }
        } else {
            updateData(v, factor);
        }
    }

    private void updateData(View v, String factor) {
        v.setEnabled(true);
        if (!TextUtils.isEmpty(factor)) {
            getRace().setExplicitFactor(Double.parseDouble(factor));
        }
        if (isAdded()) {
            BroadcastManager.getInstance(getActivity()).addIntent(new Intent(AppConstants.INTENT_ACTION_CLEAR_TOGGLE));
            BroadcastManager.getInstance(getActivity()).addIntent(new Intent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT));
            BroadcastManager.getInstance(getActivity()).addIntent(new Intent(AppConstants.INTENT_ACTION_UPDATE_SCREEN));
        }
    }
}
