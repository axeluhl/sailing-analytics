package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.content.Loader;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.impl.CompetitorResultsImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorStore;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.data.OnlineDataManager;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;
import com.sap.sailing.racecommittee.app.ui.adapters.CompetitorAdapter;
import com.sap.sailing.racecommittee.app.ui.comparators.NaturalNamedComparator;
import com.sap.sailing.racecommittee.app.ui.views.DividerItemDecoration;
import com.sap.sse.common.Util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PositioningFragment extends BaseFragment {

    private RecyclerView mPosition;
    private RecyclerView mCompetitor;

    private CompetitorAdapter mCompetitorAdapter;
    private ArrayList<Competitor> mCompetitorData;

    private CompetitorResults mCompetitorResult;

    public PositioningFragment() {
        mCompetitorData = new ArrayList<>();
    }

    public static PositioningFragment newInstance(Bundle args) {
        PositioningFragment fragment = new PositioningFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_positioning, container, false);

        mPosition = (RecyclerView) layout.findViewById(R.id.list_positioning_chosen);
        if (mPosition != null) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
            mPosition.setLayoutManager(layoutManager);
            mPosition.setAdapter(new RecyclerView.Adapter() {
                @Override
                public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    return null;
                }

                @Override
                public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

                }

                @Override
                public int getItemCount() {
                    return 0;
                }
            });
        }

        mCompetitor = (RecyclerView) layout.findViewById(R.id.list_positioning_all);
        if (mCompetitor != null) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
            mCompetitorAdapter = new CompetitorAdapter(getActivity(), mCompetitorData);

            mCompetitor.setLayoutManager(layoutManager);
            mCompetitor.setAdapter(mCompetitorAdapter);
            mCompetitor.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        }

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mCompetitorResult = initializeFinishPositioningList();
        loadCompetitors();

        Util.addAll(getRace().getCompetitors(), mCompetitorData);
        Collections.sort(mCompetitorData, new NaturalNamedComparator());
        mCompetitorAdapter.notifyDataSetChanged();
    }

    private void loadCompetitors() {
        getActivity().setProgressBarIndeterminateVisibility(true);

        // invalidate all competitors of this race
        ReadonlyDataManager dataManager = OnlineDataManager.create(getActivity());
        SharedDomainFactory domainFactory = dataManager.getDataStore().getDomainFactory();
        for (Competitor competitor : getRace().getCompetitors()) {
            domainFactory.getCompetitorStore().allowCompetitorResetToDefaults(competitor);
        }

        Loader<?> competitorLoaders = getLoaderManager().initLoader(0, null,
            dataManager.createCompetitorsLoader(getRace(), new LoadClient<Collection<Competitor>>() {

                @Override
                public void onLoadFailed(Exception reason) {
                    getActivity().setProgressBarIndeterminateVisibility(false);
                    Toast
                        .makeText(getActivity(), String.format("Competitors: %s", reason.toString()), Toast.LENGTH_LONG)
                        .show();
                }

                @Override
                public void onLoadSucceeded(Collection<Competitor> data, boolean isCached) {
                    if (!isCached) {
                        getActivity().setProgressBarIndeterminateVisibility(false);
                    }
                    onLoadCompetitorsSucceeded(data);
                }

            }));
        // Force load to get non-cached remote competitors...
        competitorLoaders.forceLoad();
    }

    protected void onLoadCompetitorsSucceeded(Collection<Competitor> data) {
        mCompetitorData.clear();
        mCompetitorData.addAll(data);
        Collections.sort(mCompetitorData, new NaturalNamedComparator());
        deleteObsoleteCompetitorsFromPositionedList(data);
        deletePositionedCompetitorsFromUnpositionedList();
        mCompetitorAdapter.notifyDataSetChanged();
    }

    private void deleteObsoleteCompetitorsFromPositionedList(Collection<Competitor> validCompetitors) {
        List<Util.Triple<Serializable, String, MaxPointsReason>> toBeDeleted = new ArrayList<>();
        for (Util.Triple<Serializable, String, MaxPointsReason> positionedItem : mCompetitorResult) {
            if (!validCompetitors.contains(getCompetitorStore().getExistingCompetitorById(positionedItem.getA()))) {
                toBeDeleted.add(positionedItem);
            }
        }
        mCompetitorResult.removeAll(toBeDeleted);
    }

    private void deletePositionedCompetitorsFromUnpositionedList() {
        for (Util.Triple<Serializable, String, MaxPointsReason> positionedItem : mCompetitorResult) {
            Competitor competitor = getCompetitorStore().getExistingCompetitorById(positionedItem.getA());
            mCompetitorData.remove(competitor);
        }
    }

    private CompetitorStore getCompetitorStore() {
        return DataManager.create(getActivity()).getDataStore().getDomainFactory().getCompetitorStore();
    }

    private CompetitorResults initializeFinishPositioningList() {
        CompetitorResults positioning = getRaceState().getFinishPositioningList();
        return positioning == null ? new CompetitorResultsImpl() : positioning;
    }
}
