package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.adapters.AbandonFlagsAdapter;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;

public class AbandonFlagsFragment extends RaceFragment implements AbandonFlagsAdapter.AbandonFlagItemClick {

    private AbandonFlagsAdapter mAdapter;

    public static AbandonFlagsFragment newInstance() {
        AbandonFlagsFragment fragment = new AbandonFlagsFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.flag_list, container, false);

        ListView listView = (ListView) layout.findViewById(R.id.listView);
        if (listView != null) {
            mAdapter = new AbandonFlagsAdapter(getActivity());
            listView.setAdapter(mAdapter);
        }

        return layout;
    }

    @Override
    public void onClick(AbandonFlagsAdapter.AbandonFlag flag) {
        mAdapter.notifyDataSetChanged();
    }
}
