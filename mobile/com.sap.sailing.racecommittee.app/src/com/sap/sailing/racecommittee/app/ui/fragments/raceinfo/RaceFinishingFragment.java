package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.sap.sailing.android.shared.util.ViewHolder;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.utils.FlagsResources;
import com.sap.sailing.racecommittee.app.utils.BitmapHelper;

import java.text.SimpleDateFormat;

public class RaceFinishingFragment extends BaseFragment {

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

        ImageView flag = ViewHolder.get(layout, R.id.flag);
        if (flag != null) {
            flag.setImageDrawable(FlagsResources.getFlagDrawable(getActivity(), Flags.BLUE.name(), 96));
        }

        Button down = ViewHolder.get(layout, R.id.flag_down);
        if (down != null) {
            down.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    replaceFragment(MoreFlagsFragment.FinishTimeFragment.newInstance(1), R.id.race_frame);
                }
            });
        }

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getView() != null) {
            TextView first_vessel = ViewHolder.get(getView(), R.id.first_vessel);
            if (first_vessel != null && getRaceState() != null && getRaceState().getFinishingTime() != null) {
                String vessel_time = getString(R.string.finishing_started);
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", getResources().getConfiguration().locale);
                vessel_time = vessel_time.replace("#TIME#", dateFormat.format(getRaceState().getFinishingTime().asDate()));
                first_vessel.setText(vessel_time);
            }
        }
    }
}
