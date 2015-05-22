package com.sap.sailing.racecommittee.app.ui.fragments.dialogs;

import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.logging.LogEvent;

public class AbortTypeSelectionDialog extends RaceDialogFragment {
    private ImageButton apFlag;
    private ImageButton novemberFlag;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.race_choose_ap_november_view, container);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getDialog().setTitle(getText(R.string.ap_or_november));

        apFlag = (ImageButton) getView().findViewById(R.id.choose_ap_flag_button);
        novemberFlag = (ImageButton) getView().findViewById(R.id.choose_november_flag_button);

        apFlag.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                ExLog.i(getActivity(), LogEvent.RACE_CHOOSE_ABORT_AP, getRace().getId().toString());
                showAbortModeDialog(Flags.AP, "dialogAPFlag");
            }

        });

        novemberFlag.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                ExLog.i(getActivity(), LogEvent.RACE_CHOOSE_ABORT_NOVEMBER, getRace().getId().toString());
                showAbortModeDialog(Flags.NOVEMBER, "dialogNovemberFlag");
            }

        });
    }

    private void showAbortModeDialog(Flags abortingFlag, String dialogName) {
        FragmentManager fragmentManager = getFragmentManager();

        RaceDialogFragment fragment = new AbortModeSelectionDialog();

        Bundle args = getParameterBundle();
        args.putString(AppConstants.FLAG_KEY, abortingFlag.name());
        fragment.setArguments(args);

        fragment.show(fragmentManager, dialogName);
        this.dismiss();
    }

    @Override
    public void notifyTick() {
        
    }
}
