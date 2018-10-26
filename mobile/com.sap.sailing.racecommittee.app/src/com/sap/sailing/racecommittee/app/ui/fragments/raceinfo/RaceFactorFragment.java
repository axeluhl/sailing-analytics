package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.UUID;

import com.sap.sailing.android.shared.services.sending.MessageSendingService;
import com.sap.sailing.android.shared.util.AppUtils;
import com.sap.sailing.android.shared.util.BroadcastManager;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.common.impl.RaceColumnConstants;
import com.sap.sailing.domain.common.racelog.RaceLogServletConstants;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.layouts.HeaderLayout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class RaceFactorFragment extends BaseFragment implements View.OnClickListener {
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
                if (AppUtils.with(getActivity()).isLandscape()) {
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

        getActivity().startService(MessageSendingService.createMessageIntent(getActivity(), uri.toString(), null,
                UUID.randomUUID(), "{}", null));

        if (!TextUtils.isEmpty(factor)) {
            getRace().setExplicitFactor(Double.parseDouble(factor));
        } else {
            getRace().setExplicitFactor(null);
        }

        if (isAdded()) {
            BroadcastManager.getInstance(getActivity()).addIntent(new Intent(AppConstants.INTENT_ACTION_CLEAR_TOGGLE));
            BroadcastManager.getInstance(getActivity())
                    .addIntent(new Intent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT));
            BroadcastManager.getInstance(getActivity()).addIntent(new Intent(AppConstants.INTENT_ACTION_UPDATE_SCREEN));
        }
    }
}
