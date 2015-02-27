package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.adapters.RecallFlagsAdapter;
import com.sap.sailing.racecommittee.app.ui.adapters.RecallFlagsAdapter.RecallFlag;
import com.sap.sailing.racecommittee.app.ui.adapters.RecallFlagsAdapter.RecallFlagItemClick;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;

public class RecallFlagsFragment extends RaceFragment implements RecallFlagItemClick{

    private RecallFlagsAdapter mAdapter;

    public RecallFlagsFragment() {

    }

    public static RecallFlagsFragment newInstance() {
        RecallFlagsFragment fragment = new RecallFlagsFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.flag_list, container, false);

        ListView listView = (ListView) layout.findViewById(R.id.listView);
        if (listView != null) {
            mAdapter = new RecallFlagsAdapter(getActivity(), this);
            listView.setAdapter(mAdapter);
        }

        return layout;
    }

    @Override
    public void onClick(RecallFlag flag) {
        mAdapter.notifyDataSetChanged();
        Toast.makeText(getActivity(), flag.file_name, Toast.LENGTH_SHORT).show();
    }
}
