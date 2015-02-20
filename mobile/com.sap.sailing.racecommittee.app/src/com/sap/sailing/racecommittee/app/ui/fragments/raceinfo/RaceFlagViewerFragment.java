package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.sap.sailing.domain.abstractlog.race.state.RaceState;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.FlagPoleState;
import com.sap.sailing.domain.common.racelog.FlagPole;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.utils.FlagsResources;
import com.sap.sailing.racecommittee.app.utils.TickListener;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import java.util.Calendar;
import java.util.List;

public class RaceFlagViewerFragment extends RaceFragment {

    private ImageView flagLeft;
    private ImageView flagRight;
    private TextView textLeft;
    private TextView textRight;
    private View leftUpArrow;
    private View leftDownArrow;
    private View rightUpArrow;
    private View rightDownArrow;
    private View leftPlace;
    private View rightPlace;
    private View middleLine;

    public RaceFlagViewerFragment() {

    }

    public static RaceFlagViewerFragment newInstance() {
        RaceFlagViewerFragment fragment = new RaceFlagViewerFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_flags, container, false);

        middleLine = layout.findViewById(R.id.middle_line);
        flagLeft = (ImageView) layout.findViewById(R.id.left_flag);
        flagRight = (ImageView) layout.findViewById(R.id.right_flag);
        textLeft = (TextView) layout.findViewById(R.id.left_text);
        textRight = (TextView) layout.findViewById(R.id.right_text);
        leftUpArrow = layout.findViewById(R.id.left_arrow_up);
        leftDownArrow = layout.findViewById(R.id.left_arrow_down);
        rightUpArrow = layout.findViewById(R.id.right_arrow_up);
        rightDownArrow = layout.findViewById(R.id.right_arrow_down);
        leftPlace = layout.findViewById(R.id.left_place);
        rightPlace = layout.findViewById(R.id.right_place);

        return layout;
    }

    @Override
    public void notifyTick() {
        super.notifyTick();

        updateFlags();
    }

    private void updateFlags() {
        if (getRace() == null || getRaceState() == null || getRaceState().getStartTime() == null) {
            return;
        }

        FlagPoleState flagPoleState = getRaceState().getTypedRacingProcedure().getActiveFlags(getRaceState().getStartTime(),
                MillisecondsTimePoint.now());
        List<FlagPole> flagChanges = flagPoleState.computeUpcomingChanges();
        if (!flagChanges.isEmpty()) {
            TimePoint changeAt = flagPoleState.getNextStateValidFrom();
            FlagPole changePole = FlagPoleState.getMostInterestingFlagPole(flagChanges);

            flagLeft.setImageDrawable(FlagsResources.getFlagDrawable(getActivity(), changePole.getUpperFlag().name(), 96));
            String text = getDuration(changeAt.asDate(), Calendar.getInstance().getTime());
            textLeft.setText(text.replace("-", ""));
            leftDownArrow.setVisibility(View.GONE);
            leftUpArrow.setVisibility(View.GONE);
            if (changePole.isDisplayed()) {
                leftUpArrow.setVisibility(View.VISIBLE);
            } else {
                leftDownArrow.setVisibility(View.VISIBLE);
            }
        }
    }
}
