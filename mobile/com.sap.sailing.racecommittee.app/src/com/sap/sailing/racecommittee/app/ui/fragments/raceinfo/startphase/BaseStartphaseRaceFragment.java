package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.startphase;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.*;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedure;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.BaseRaceInfoRaceFragment;
import com.sap.sailing.racecommittee.app.ui.utils.FlagPoleStateRenderer;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public abstract class BaseStartphaseRaceFragment<ProcedureType extends RacingProcedure> extends BaseRaceInfoRaceFragment<ProcedureType> {

   @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.race_main, container, false);
        ViewStub actionsStub = (ViewStub) view.findViewById(R.id.race_content);
        actionsStub.setLayoutResource(getActionsLayoutId());
        actionsStub.inflate();
        return view;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        nextCountdownTextView = (TextView) getView().findViewById(R.id.race_startphase_base_next_countdown);
//
//        abortButton = (ImageButton) getView().findViewById(R.id.race_startphase_base_abort_button);
//        abortButton.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                RaceDialogFragment fragment = new AbortModeSelectionDialog();
//                Bundle args = getRecentArguments();
//                args.putString(AppConstants.FLAG_KEY, Flags.AP.name());
//                fragment.setArguments(args);
//                fragment.show(getFragmentManager(), "dialogAPMode");
//            }
//        });
//
//        resetTimeButton = (Button) getView().findViewById(R.id.race_startphase_base_reset_time_button);
//        resetTimeButton.setOnClickListener(new OnClickListener() {
//            public void onClick(View v) {
//                infoListener.onResetTime();
//            }
//        });
//
//        raceStartIn5Minutes = (Button) getView().findViewById(R.id.raceStartIn5Minutes);
//        raceStartIn5Minutes.setVisibility(View.INVISIBLE);
//
//        raceStartIn4Minutes = (Button) getView().findViewById(R.id.raceStartIn4Minutes);
//        raceStartIn4Minutes.setVisibility(View.INVISIBLE);
//
//        raceStartIn2Minutes = (Button) getView().findViewById(R.id.raceStartIn1Minute);
//        raceStartIn2Minutes.setVisibility(View.INVISIBLE);
//
//        toggleGroupRacing = (ToggleButton) getView().findViewById(R.id.toggle_group_race_mode);
//        toggleGroupRacing.setVisibility(View.INVISIBLE);
//
//        raceResetCourse = (Button) getView().findViewById(R.id.raceResetCourse);
//        raceResetCourse.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View paramView) {
//                showCourseDesignDialog();
//            }
//        });
//
//        flagRenderer = new FlagPoleStateRenderer(getActivity(), getRace(),
//                (LinearLayout) getView().findViewById(R.id.race_flag_space_up_flags),
//                (LinearLayout) getView().findViewById(R.id.race_flag_space_down_flags));
    }

    protected int getActionsLayoutId() {
        return R.layout.race_running_content;
    }
    
    @Override
    protected void setupUi() {
//        TimePoint startTime = getRaceState().getStartTime();
//        if (startTime != null) {
//            flagRenderer.render(getRacingProcedure().getActiveFlags(startTime, MillisecondsTimePoint.now()));
//        }
    }
}
