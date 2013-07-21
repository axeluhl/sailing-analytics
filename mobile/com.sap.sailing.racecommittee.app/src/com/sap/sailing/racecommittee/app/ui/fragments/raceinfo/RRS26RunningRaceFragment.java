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
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.analyzing.impl.LastFlagsFinder;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.startprocedure.StartModeChoosableStartProcedure;
import com.sap.sailing.racecommittee.app.domain.startprocedure.impl.RRS26RunningRaceEventListener;
import com.sap.sailing.racecommittee.app.domain.startprocedure.impl.RRS26StartProcedure;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.AbortTypeSelectionDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceDialogFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceFinishingTimeDialog;

public class RRS26RunningRaceFragment extends RaceFragment implements RRS26RunningRaceEventListener {
    
    private TextView countUpTextView;
    private ImageView individualRecallFlag;
    private TextView individualRecallLabel;
    ImageButton abortingFlagButton;
    ImageButton individualRecallButton;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.rrs26_race_running_view, container, false);
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
        
        
        individualRecallButton = (ImageButton) getView().findViewById(R.id.individualRecallButton);
        individualRecallButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                TimePoint now = MillisecondsTimePoint.now();
                if (getRace().getState().getStartProcedure() instanceof RRS26StartProcedure) {
                    RRS26StartProcedure rrs26StartProcedure = ((RRS26StartProcedure) getRace().getState().getStartProcedure());
                    if (rrs26StartProcedure.isIndividualRecallDisplayed()) {
                        rrs26StartProcedure.setIndividualRecallRemoval(now);
                    } else {
                        rrs26StartProcedure.setIndividualRecall(now);
                    }
                }
            }
        });
    }
    
    @Override
    public void onStart() {
        super.onStart();
        setupUi();
        getRace().getState().getStartProcedure().setRunningRaceEventListener(this);
        ExLog.i(RRS26RunningRaceFragment.class.getName(), String.format("Fragment %s is now shown", RRS26RunningRaceFragment.class.getName()));
    }
    
    @Override
    public void notifyTick() {
        if (getRace().getState().getStartTime() == null)
            return;

        long millisSinceStart = System.currentTimeMillis() - getRace().getState().getStartTime().asMillis();
        setStarttimeCountupLabel(millisSinceStart);
        //TODO: implement count down label text when individual recall is displayed
    }

    private void setupUi() {
        StartModeChoosableStartProcedure startProcedure = (StartModeChoosableStartProcedure) this.getRace().getState().getStartProcedure();
        if(startProcedure.getCurrentStartModeFlag().equals(Flags.BLACK)){
            removeIndividualRecallButton();
        }
        LastFlagsFinder lastFlagFinder = new LastFlagsFinder(this.getRace().getRaceLog());
        RaceLogFlagEvent lastFlagEvent = LastFlagsFinder.getMostRecent(lastFlagFinder.analyze());
        if(lastFlagEvent != null){
            if(lastFlagEvent.getUpperFlag().equals(Flags.XRAY)){
                if(lastFlagEvent.isDisplayed()){
                    onIndividualRecall();
                } else {
                    onIndividualRecallRemoval();
                }
            }
        }
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
        //TODO handle xray still up
        FragmentManager fragmentManager = getFragmentManager();

        RaceDialogFragment fragment = new RaceFinishingTimeDialog();
        
        Bundle args = getRecentArguments();
        fragment.setArguments(args);
        
        fragment.show(fragmentManager, "dialogFinishingTime");
    }


    private void setStarttimeCountupLabel(long millisecondsSinceStart) {
        countUpTextView.setText(String.format(getString(R.string.race_running_since_template), 
                getRace().getName(),
                prettyTimeString(millisecondsSinceStart)));
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

        Bundle args = getRecentArguments();
        fragment.setArguments(args);

        fragment.show(fragmentManager, "dialogAPNovemberMode");
    }

    @Override
    public void onIndividualRecallRemoval() {
        setIndividualRecallRemovedInView();
    }

    @Override
    public void onIndividualRecall() {
        setIndividualRecallDisplayedInView();
    }

    private void setIndividualRecallDisplayedInView() {
        moveImageUp(individualRecallFlag);
        String unsetXray = getActivity().getResources().getString(R.string.choose_xray_flag_down);
        individualRecallLabel.setText(unsetXray);
    }
    
    private void setIndividualRecallRemovedInView() {
        moveImageDown(individualRecallFlag);
        removeIndividualRecallButton();
    }

    private void removeIndividualRecallButton() {
        individualRecallLabel.setVisibility(View.GONE);
        individualRecallButton.setVisibility(View.GONE);
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
