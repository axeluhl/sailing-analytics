/**
 * 
 */
package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.finished;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.sap.sailing.android.shared.util.ViewHolder;
import com.sap.sailing.domain.abstractlog.race.state.RaceStateChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.BaseRaceStateChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.impl.BoatClassSeriesFleet;
import com.sap.sailing.racecommittee.app.ui.activities.ResultsCapturingActivity;
import com.sap.sailing.racecommittee.app.ui.fragments.panels.BasePanelFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.panels.FinishedButtonFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.panels.FinishedSubmitFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.BaseRaceInfoRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.RaceSummaryFragment;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public abstract class BaseFinishedRaceFragment<ProcedureType extends RacingProcedure> extends BaseRaceInfoRaceFragment<ProcedureType> {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_finished, container, false);

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getView() != null) {
            replaceFragment(FinishedButtonFragment.newInstance(getArguments()), R.id.finished_panel_left);
            replaceFragment(FinishedSubmitFragment.newInstance(getArguments()), R.id.finished_panel_right);
            replaceFragment(RaceSummaryFragment.newInstance(getArguments()), R.id.finished_content);
        }
    }

    @Override
    protected void setupUi() {

    }

}
