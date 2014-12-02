package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.unscheduled;

import java.util.ArrayList;
import java.util.Set;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.adapters.unscheduled.StartMode;
import com.sap.sailing.racecommittee.app.ui.adapters.unscheduled.StartModeAdapter;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.utils.NextFragmentListener;

public class StartModeFragment extends RaceFragment {

    private NextFragmentListener mListener;
    private ListView mListView;

    public StartModeFragment() {
        
    }
    
    public StartModeFragment(NextFragmentListener listener) {
        mListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.race_schedule_mode, container, false);

        Button confirm = (Button) view.findViewById(R.id.confirm);
        if (confirm != null) {
            confirm.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mListener.nextFragment();
                }
            });
        }

        mListView = (ListView) view.findViewById(R.id.listMode);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }
    
    @Override
    public void onResume() {
        super.onResume();

        ArrayList<StartMode> startMode = new ArrayList<StartMode>();
        Set<Flags> flags = preferences.getRRS26StartmodeFlags();
        Boolean checked = true;

        for (Flags flag : flags) {
            startMode.add(new StartMode(flag.name(), checked));
            checked = false;
        }

        mListView.setAdapter(new StartModeAdapter(getActivity(), startMode));
    }
}
