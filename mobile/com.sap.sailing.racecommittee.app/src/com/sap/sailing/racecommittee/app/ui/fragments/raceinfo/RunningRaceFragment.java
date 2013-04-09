package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.AbortTypeSelectionDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.IndividualRecallUiListener;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceDialogFragment;

public class RunningRaceFragment extends RaceFragment implements IndividualRecallUiListener {
    
    private TextView countUpTextView;
    private ImageView individualRecallFlag;
    private TextView individualRecallLabel;
    ImageButton abortingFlagButton;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.race_running_view, container, false);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        countUpTextView = (TextView) getView().findViewById(R.id.raceCountUp);
        
        individualRecallFlag = (ImageView) getView().findViewById(R.id.xrayFlag);
        individualRecallLabel = (TextView) getView().findViewById(R.id.individualRecallLabel);
        
        ImageButton blueFlagButton = (ImageButton) getView().findViewById(R.id.blueFlagButton);
        blueFlagButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDisplayBlueFlagDialog();
            }
        });
        
        abortingFlagButton = (ImageButton) getView().findViewById(R.id.abortingFlagButton);
        abortingFlagButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showChooseAPNovemberDialog();
            }
        });
        
        ImageButton generalRecallFlagButton = (ImageButton) getView().findViewById(R.id.firstSubstituteButton);
        generalRecallFlagButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDisplayGeneralRecallDialog();
            }
        });
        
        
        ImageButton individualRecallButton = (ImageButton) getView().findViewById(R.id.individualRecallButton);
        individualRecallButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                TimePoint now = MillisecondsTimePoint.now();
                if (getRace().getState().getIndividualRecallDisplayedTime() == null) {
                    getRace().getState().getStartProcedure().setIndividualRecall(now);
                } else {
                    getRace().getState().getStartProcedure().setIndividualRecallRemoval(now);
                }
            }
        });
    }

    private void showDisplayGeneralRecallDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getActivity().getResources().getString(R.string.confirmation_first_substitute_display))
        .setCancelable(true)
        .setPositiveButton(getActivity().getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ExLog.i(ExLog.RACE_RUNNING_GENERAL_RECALL_YES, getRace().getId().toString(), getActivity());
                getRace().getState().getStartProcedure().setGeneralRecall(MillisecondsTimePoint.now());
            }
        })
        .setNegativeButton(getActivity().getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ExLog.i(ExLog.RACE_RUNNING_GENERAL_RECALL_NO, getRace().getId().toString(), getActivity());
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showDisplayBlueFlagDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getActivity().getResources().getString(R.string.confirmation_blue_flag_display))
        .setCancelable(true)
        .setPositiveButton(getActivity().getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ExLog.i(ExLog.FLAG_BLUE_SET, getRace().getId().toString(), getActivity());
                getRace().getState().getStartProcedure().setFinishing(MillisecondsTimePoint.now());
            }
        })
        .setNegativeButton(getActivity().getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ExLog.i(ExLog.FLAG_BLUE_SET_NO, getRace().getId().toString(), getActivity());
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
    
    public void notifyTick() {
        if (getRace().getState().getStartTime() == null)
            return;

        long millisSinceStart = System.currentTimeMillis() - getRace().getState().getStartTime().asMillis();
        setStarttimeCountupLabel(millisSinceStart);
    }


    private void setStarttimeCountupLabel(long millisecondsSinceStart) {
        countUpTextView.setText(String.format(getString(R.string.race_running_since_template), prettyTimeString(millisecondsSinceStart), getRace().getName()));
    }

    protected CharSequence prettyTimeString(long time) {
        int secondsStart = (int) (time / 1000);
        int hours = secondsStart / 3600;
        int minutes = (secondsStart % 3600) / 60;
        int seconds = (secondsStart % 60);
        String timePattern = "%s:%s:%s";
        String secondsString = seconds < 10 ? "0" + seconds : "" + seconds;
        String minutesString = minutes < 10 ? "0" + minutes : "" + minutes;
        String hoursString = hours < 10 ? "0" + hours : "" + hours;
        return String.format(timePattern, hoursString, minutesString, secondsString);
    }

    private void showChooseAPNovemberDialog() {
        FragmentManager fragmentManager = getFragmentManager();

        RaceDialogFragment fragment = new AbortTypeSelectionDialog();

        Bundle args = getParameterBundle();
        fragment.setArguments(args);

        fragment.show(fragmentManager, "dialogAPNovemberMode");
    }

    @Override
    public void removeIndividualRecallFlag() {
        setIndividualRecallRemovedInView();
    }

    @Override
    public void displayIndividualRecallFlag() {
        setIndividualRecallDisplayedInView();
    }

    private void setIndividualRecallDisplayedInView() {
        moveImageUp(individualRecallFlag);
        String unsetXray = getActivity().getResources().getString(R.string.choose_xray_flag_down);
        individualRecallLabel.setText(unsetXray);
    }
    
    private void setIndividualRecallRemovedInView() {
        moveImageDown(individualRecallFlag);
        //setXrayCountdownLabel();
        String setIndividualRecallUp = getActivity().getResources().getString(R.string.choose_xray_flag_up);
        individualRecallLabel.setText(setIndividualRecallUp);
    }

    protected void moveImageUp(ImageView image) {
        moveImage(image, 1, 0);
    }

    protected void moveImageDown(ImageView image) {
        moveImage(image, 0, 1);
    }

    protected void moveImage(ImageView image, int parentTop, int parentBottom) {
        LayoutParams params = (RelativeLayout.LayoutParams) image.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, parentTop);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, parentBottom);
        image.setLayoutParams(params);
    }
}
