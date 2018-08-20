package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.text.SimpleDateFormat;

import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.utils.FlagsResources;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class RaceFinishingFragment extends BaseFragment {

    private SimpleDateFormat mDateFormat;
    private TextView mFinishingSince;

    public static RaceFinishingFragment newInstance() {
        RaceFinishingFragment fragment = new RaceFinishingFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_finishing, container, false);

        mDateFormat = new SimpleDateFormat("HH:mm:ss", getResources().getConfiguration().locale);
        mFinishingSince = ViewHelper.get(layout, R.id.first_vessel_since);

        ImageView flag = ViewHelper.get(layout, R.id.flag);
        if (flag != null) {
            flag.setImageDrawable(FlagsResources.getFlagDrawable(getActivity(), Flags.BLUE.name(), getResources().getInteger(R.integer.flag_size_xlarge)));
        }

        Button down = ViewHelper.get(layout, R.id.flag_down);
        if (down != null) {
            down.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    replaceFragment(MoreFlagsFragment.FinishTimeFragment.newInstance(1), getFrameId(getActivity(), R.id.race_edit, R.id.race_content, false));
                }
            });
        }

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getView() != null) {
            TextView first_vessel = ViewHelper.get(getView(), R.id.first_vessel);
            if (first_vessel != null && getRaceState() != null && getRaceState().getFinishingTime() != null) {
                first_vessel.setText(getString(R.string.finishing_started, mDateFormat.format(getRaceState().getFinishingTime().asDate())));
            }
        }
        notifyTick(MillisecondsTimePoint.now());
    }

    @Override
    public void notifyTick(TimePoint now) {
        super.notifyTick(now);

        if (mFinishingSince != null && getRaceState().getFinishingTime() != null) {
            String timeDiff = TimeUtils.formatTimeAgo(getActivity(), now.minus(getRaceState().getFinishingTime().asMillis()).asMillis());
            mFinishingSince.setText(getString(R.string.finishing_started_since, timeDiff));
        }
    }
}
