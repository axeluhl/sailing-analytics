package com.sap.sailing.racecommittee.app.ui.fragments.panels;

import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceListFragment;
import com.sap.sailing.racecommittee.app.ui.utils.FlagsResources;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FinishedSubmitFragment extends BasePanelFragment {

    public static FinishedSubmitFragment newInstance(Bundle args) {
        FinishedSubmitFragment fragment = new FinishedSubmitFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_finished_desc, container, false);

        TextView protest = ViewHelper.get(layout, R.id.protest_button);
        if (protest != null) {
            int size = getActivity().getResources().getInteger(R.integer.flag_size);
            Drawable drawable = FlagsResources.getFlagDrawable(getActivity(), Flags.BRAVO.name(), size);
            protest.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
            protest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RaceListFragment.showProtest(getActivity(), getRace());
                }
            });
        }

        return layout;
    }
}
