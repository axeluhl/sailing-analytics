package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.unscheduled;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.activities.RacingActivity;
import com.sap.sailing.racecommittee.app.ui.adapters.unscheduled.StartProcedure;
import com.sap.sailing.racecommittee.app.ui.adapters.unscheduled.StartProcedureAdapter;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;

public class StartProcedureChangeFragment extends RaceFragment {
    
    private static final String TAG = StartProcedureChangeFragment.class.getName();

    private final ArrayList<StartProcedure> startProcedure = new ArrayList<StartProcedure>();
    private StartProcedureAdapter mAdapter;

    public StartProcedureChangeFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.race_schedule_procedure_change, container, false);

        RacingProcedureType[] procedureTypes = RacingProcedureType.validValues();
        for (int i = 0; i < procedureTypes.length; i++) {
            String className;
            switch (procedureTypes[i]) {
            case GateStart:
                className = GateStartFragment.class.getName();
                break;

            default:
                className = LineStartFragment.class.getName();
                break;
            }
            startProcedure.add(new StartProcedure(procedureTypes[i].toString(), (preferences.getDefaultRacingProcedureType() == procedureTypes[i]), className));
        }

        Button confirm = (Button) view.findViewById(R.id.confirm);
        if (confirm != null) {
            confirm.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    for (int i = 0; i < mAdapter.getCount(); i++) {
                        if (mAdapter.getItem(i).isChecked()) {
                            try {
                                RaceFragment fragment = (RaceFragment) Class.forName(mAdapter.getItem(i).getClassName()).newInstance();
                                RacingActivity activity = (RacingActivity) getActivity();
                                fragment.setArguments(getArguments());
                                activity.replaceFragment(fragment);
                            } catch (java.lang.InstantiationException ex) {
                                ExLog.ex(getActivity(), TAG, ex);
                            } catch (IllegalAccessException ex) {
                                ExLog.ex(getActivity(), TAG, ex);
                            } catch (ClassNotFoundException ex) {
                                ExLog.ex(getActivity(), TAG, ex);
                            }
                        }
                    }
                }
            });
        }

        ListView listView = (ListView) view.findViewById(R.id.listView);
        if (listView != null) {
            mAdapter = new StartProcedureAdapter(getActivity(), startProcedure);
            listView.setAdapter(mAdapter);
        }

        return view;
    }
}
