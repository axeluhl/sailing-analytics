package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.adapters.PostponeFlagsAdapter;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;

public class PostponeFlagsFragment extends RaceFragment implements PostponeFlagsAdapter.PostponeFlagItemClick {

    private PostponeFlagsAdapter mAdapter;

    public PostponeFlagsFragment() {

    }

    public static PostponeFlagsFragment newInstance() {
        PostponeFlagsFragment fragment = new PostponeFlagsFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.flag_list, container, false);

        ListView listView = (ListView) layout.findViewById(R.id.listView);
        if (listView != null) {
            mAdapter = new PostponeFlagsAdapter(getActivity());
            listView.setAdapter(mAdapter);
        }

        return layout;
    }

    @Override
    public void onClick(PostponeFlagsAdapter.PostponeFlag flag) {
        mAdapter.notifyDataSetChanged();
    }
}
