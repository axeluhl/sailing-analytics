package com.sap.sailing.racecommittee.app.ui.fragments.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.racelog.state.RaceState;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.logging.LogEvent;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class AbortModeSelectionDialog extends RaceDialogFragment {
    Flags abortFlag;

    ImageButton abortFlagOnly;
    ImageButton abortFlagOverHotel;
    ImageButton abortFlagOverAlpha;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.race_choose_abort_mode_view, container);

        return view;
    }

    private void initializeAPDialog() {
        getDialog().setTitle(getText(R.string.flag_ap));
        abortFlagOnly.setImageResource(R.drawable.ap_flag);
        abortFlagOverHotel.setImageResource(R.drawable.ap_over_hotel);
        abortFlagOverAlpha.setImageResource(R.drawable.ap_over_alpha);

    }

    private void initializeNovemberDialog() {
        getDialog().setTitle(getText(R.string.flag_november));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Flags anAbortFlag = Flags.valueOf(getArguments().getString(AppConstants.FLAG_KEY));

        if (!(anAbortFlag == Flags.AP || anAbortFlag == Flags.NOVEMBER)) {
            throw new IllegalArgumentException(
                    "An abort dialog can only be instantiated with flags NOVEMBER or AP, but was " + anAbortFlag);
        }
        abortFlag = anAbortFlag;

        abortFlagOnly = (ImageButton) getView().findViewById(R.id.abortFlagOnly);
        abortFlagOverHotel = (ImageButton) getView().findViewById(R.id.abortFlagOverHotel);
        abortFlagOverAlpha = (ImageButton) getView().findViewById(R.id.abortFlagOverAlpha);

        abortFlagOverAlpha.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                ExLog.i(getActivity(), LogEvent.RACE_CHOOSE_ABORT_ALPHA, getRace().getId().toString());
                signalAbort(Flags.ALPHA);
            }

        });

        abortFlagOverHotel.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                ExLog.i(getActivity(), LogEvent.RACE_CHOOSE_ABORT_HOTEL, getRace().getId().toString());
                signalAbort(Flags.HOTEL);
            }

        });

        abortFlagOnly.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                ExLog.i(getActivity(), LogEvent.RACE_CHOOSE_ABORT_NONE, getRace().getId().toString());
                signalAbort(Flags.NONE);
            }

        });

        if (this.abortFlag == Flags.NOVEMBER) {
            initializeNovemberDialog();
        } else {
            initializeAPDialog();
        }

    }

    private void signalAbort(Flags additionalFlag) {
        TimePoint now = MillisecondsTimePoint.now();
        RaceState state = getRaceState();
        if (this.abortFlag.equals(Flags.AP)) {
            state.setAborted(now, /* postponed */ true, additionalFlag);
            
        } else if (this.abortFlag.equals(Flags.NOVEMBER)) {
            state.setAborted(now, /* postponed */ false, additionalFlag);
        }
        state.setAdvancePass(now);
        this.dismiss();
    }

    @Override
    public void notifyTick() {
        
    }
}
