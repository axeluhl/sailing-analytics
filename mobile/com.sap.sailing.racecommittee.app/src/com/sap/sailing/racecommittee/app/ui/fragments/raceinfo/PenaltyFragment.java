package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.Loader;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.abstractlog.race.CompetitorResult;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.OnlineDataManager;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;
import com.sap.sailing.racecommittee.app.domain.impl.CompetitorResultWithIdImpl;
import com.sap.sailing.racecommittee.app.ui.adapters.PenaltyAdapter;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;

public class PenaltyFragment extends BaseFragment implements PopupMenu.OnMenuItemClickListener, PenaltyAdapter.ItemListener {

    private static final String TAG = "PenaltyFragment";

    private View mSortByButton;
    private Button mPenaltyButton;
    private Button mPublishButton;
    private PenaltyAdapter mAdapter;
    private List<CompetitorResult> mCheckedCompetitors;

    public static PenaltyFragment newInstance() {
        Bundle args = new Bundle();
        PenaltyFragment fragment = new PenaltyFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_penalty_fragment, container, false);

        mCheckedCompetitors = new ArrayList<>();

        mSortByButton = ViewHelper.get(layout, R.id.competitor_sort);
        if (mSortByButton != null) {
            mSortByButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        popupMenu = new PopupMenu(getActivity(), v, Gravity.RIGHT);
                    } else {
                        popupMenu = new PopupMenu(getActivity(), v);
                    }
                    popupMenu.inflate(R.menu.sort_menu);
                    popupMenu.setOnMenuItemClickListener(PenaltyFragment.this);
                    popupMenu.show();
                    ThemeHelper.positioningPopupMenu(getActivity(), popupMenu, v);
                }
            });
        }

        mPenaltyButton = ViewHelper.get(layout, R.id.button_penalty);
        mPublishButton = ViewHelper.get(layout, R.id.button_publish);
        setPublishButton();

        mAdapter = new PenaltyAdapter(this);
        RecyclerView recyclerView = ViewHelper.get(layout, R.id.competitor_list);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(mAdapter);
        }

        mCheckedCompetitors = new ArrayList<>();

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        loadCompetitors();
    }

    @Override
    public void onStop() {
        super.onStop();

        // TODO save data
    }

    private void loadCompetitors() {
        // invalidate all competitors of this race
        ReadonlyDataManager dataManager = OnlineDataManager.create(getActivity());
        SharedDomainFactory domainFactory = dataManager.getDataStore().getDomainFactory();
        for (Competitor competitor : getRace().getCompetitors()) {
            domainFactory.getCompetitorStore().allowCompetitorResetToDefaults(competitor);
        }

        final Loader<?> competitorLoader = getLoaderManager()
            .initLoader(0, null, dataManager.createCompetitorsLoader(getRace(), new LoadClient<Collection<Competitor>>() {

                @Override
                public void onLoadFailed(Exception reason) {
                    Toast.makeText(getActivity(), getString(R.string.competitor_load_error, reason.toString()), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onLoadSucceeded(Collection<Competitor> data, boolean isCached) {
                    if (isAdded() && !isCached) {
                        onLoadCompetitorsSucceeded(data);
                    }
                }
            }));

        // Force load to get non-cached remote competitors...
        competitorLoader.forceLoad();
    }

    protected void onLoadCompetitorsSucceeded(Collection<Competitor> data) {

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Toast.makeText(getActivity(), "Clicked: " + item.getTitle(), Toast.LENGTH_LONG).show();
        return false;
    }

    @Override
    public void onCheckedChanged(CompetitorResultWithIdImpl competitor, boolean isChecked) {
        if (isChecked) {
            mCheckedCompetitors.add(competitor);
        } else {
            mCheckedCompetitors.remove(competitor);
        }
        setPublishButton();
    }

    private void setPublishButton() {
        int count = mCheckedCompetitors.size();
        String text = getString(R.string.publish_button_empty);
        if (count != 0) {
            text = getString(R.string.publish_button_other, count);
        }
        mPublishButton.setText(text);
        mPublishButton.setEnabled(count != 0);
    }

    @Override
    public void onEditClicked(CompetitorResultWithIdImpl competitor) {

    }
}
