package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Loader;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.ItemShadowDecorator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.impl.CompetitorResultsImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorStore;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.data.OnlineDataManager;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;
import com.sap.sailing.racecommittee.app.domain.impl.CompetitorsWithIdImpl;
import com.sap.sailing.racecommittee.app.ui.adapters.CompetitorAdapter;
import com.sap.sailing.racecommittee.app.ui.adapters.FinishListAdapter;
import com.sap.sailing.racecommittee.app.ui.comparators.NaturalNamedComparator;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PositioningFragment extends BaseFragment
    implements CompetitorAdapter.CompetitorClick, FinishListAdapter.FinishEvents {

    private RecyclerViewDragDropManager mDragDropManager;
    private RecyclerViewSwipeManager mSwipeManager;
    private RecyclerViewTouchActionGuardManager mGuardManager;
    private RecyclerView mFinishView;
    private RecyclerView mCompetitorView;
    private Button mConfirm;

    private RecyclerView.Adapter mFinishedAdapter;
    private CompetitorAdapter mCompetitorAdapter;
    private ArrayList<CompetitorsWithIdImpl> mFinishedData;
    private ArrayList<Competitor> mCompetitorData;
    private long mId = 0;

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
        return layout;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mFinishedData = initializeFinishList();
        loadCompetitors();

        if (getView() != null) {
            mFinishView = (RecyclerView) getView().findViewById(R.id.list_positioning_chosen);
            if (mFinishView != null) {
                mGuardManager = new RecyclerViewTouchActionGuardManager();
                mGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
                mGuardManager.setEnabled(true);

                mDragDropManager = new RecyclerViewDragDropManager();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mDragDropManager.setDraggingItemShadowDrawable(
                        (NinePatchDrawable) getActivity().getDrawable(R.drawable.material_shadow_z3));
                } else {
                    mDragDropManager.setDraggingItemShadowDrawable(
                        (NinePatchDrawable) getResources().getDrawable(R.drawable.material_shadow_z3));
                }

                mSwipeManager = new RecyclerViewSwipeManager();

                LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                FinishListAdapter adapter = new FinishListAdapter(getActivity(), mFinishedData);
                adapter.setListener(this);
                mFinishedAdapter = mDragDropManager.createWrappedAdapter(adapter);
                mFinishedAdapter = mSwipeManager.createWrappedAdapter(mFinishedAdapter);

                mFinishView.setLayoutManager(layoutManager);
                mFinishView.setAdapter(mFinishedAdapter);
                mFinishView.setItemAnimator(new SwipeDismissItemAnimator());

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    mFinishView.addItemDecoration(new ItemShadowDecorator(
                        (NinePatchDrawable) getResources().getDrawable(R.drawable.material_shadow_z1)));
                }

                mGuardManager.attachRecyclerView(mFinishView);
                mDragDropManager.attachRecyclerView(mFinishView);
                mSwipeManager.attachRecyclerView(mFinishView);
            }

            mCompetitorView = (RecyclerView) getView().findViewById(R.id.list_positioning_all);
            if (mCompetitorView != null) {
                LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                mCompetitorAdapter = new CompetitorAdapter(getActivity(), mCompetitorData);
                mCompetitorAdapter.setListener(this);

                mCompetitorView.setLayoutManager(layoutManager);
                mCompetitorView.setAdapter(mCompetitorAdapter);
            }

            mConfirm = (Button) getView().findViewById(R.id.confirm);
            if (mConfirm != null) {
                mConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getRaceState().setFinishPositioningConfirmed(MillisecondsTimePoint.now());
                        sendIntent(AppConstants.INTENT_ACTION_CLEAR_TOGGLE);
                        sendIntent(AppConstants.INTENT_ACTION_SHOW_SUMMARY_CONTENT);
                    }
                });
            }
        }

        Util.addAll(getRace().getCompetitors(), mCompetitorData);
        Collections.sort(mCompetitorData, new NaturalNamedComparator());
        mCompetitorAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        mDragDropManager.cancelDrag();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mDragDropManager != null) {
            mDragDropManager.release();
            mDragDropManager = null;
        }

        if (mSwipeManager != null) {
            mSwipeManager.release();
            mSwipeManager = null;
        }

        if (mGuardManager != null) {
            mGuardManager.release();
            mGuardManager = null;
        }

        if (mFinishView != null) {
            mFinishView.setItemAnimator(null);
            mFinishView.setAdapter(null);
            mFinishView = null;
        }

        if (mFinishedAdapter != null) {
            WrapperAdapterUtils.releaseAll(mFinishedAdapter);
            mFinishedAdapter = null;
        }

        super.onDestroy();
    }

    private void loadCompetitors() {
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
                    String toastText = getString(R.string.marks_w_placeholder);
                    Toast.makeText(getActivity(), String.format(toastText, reason.toString()), Toast.LENGTH_LONG)
                        .show();
                }

                @Override
                public void onLoadSucceeded(Collection<Competitor> data, boolean isCached) {
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
        deleteCompetitorsFromFinishedList(data);
        deleteCompetitorsFromCompetitorList();
        mCompetitorAdapter.notifyDataSetChanged();
        mFinishedAdapter.notifyDataSetChanged();
    }

    private void deleteCompetitorsFromFinishedList(Collection<Competitor> validCompetitors) {
        List<Util.Triple<Serializable, String, MaxPointsReason>> toBeDeleted = new ArrayList<>();
        for (CompetitorsWithIdImpl item : mFinishedData) {
            Util.Triple<Serializable, String, MaxPointsReason> positionedItem = item.getData();
            if (!validCompetitors.contains(getCompetitorStore().getExistingCompetitorById(positionedItem.getA()))) {
                toBeDeleted.add(positionedItem);
            }
        }
        mFinishedData.removeAll(toBeDeleted);
    }

    private void deleteCompetitorsFromCompetitorList() {
        for (CompetitorsWithIdImpl item : mFinishedData) {
            Util.Triple<Serializable, String, MaxPointsReason> positionedItem = item.getData();
            Competitor competitor = getCompetitorStore().getExistingCompetitorById(positionedItem.getA());
            mCompetitorData.remove(competitor);
        }
    }

    private CompetitorStore getCompetitorStore() {
        return DataManager.create(getActivity()).getDataStore().getDomainFactory().getCompetitorStore();
    }

    private ArrayList<CompetitorsWithIdImpl> initializeFinishList() {
        ArrayList<CompetitorsWithIdImpl> positioning = new ArrayList<>();
        if (getRaceState() != null && getRaceState().getFinishPositioningList() != null) {
            for (Util.Triple results : getRaceState().getFinishPositioningList()) {
                positioning.add(new CompetitorsWithIdImpl(mId, results));
                mId++;
            }
        }
        return positioning;
    }

    @Override
    public void onCompetitorClick(Competitor competitor) {
        moveCompetitorToFinishList(competitor);
        removeCompetitorFromList(competitor);
        getRaceState().setFinishPositioningListChanged(MillisecondsTimePoint.now(), getCompetitorResults());
    }

    private void moveCompetitorToFinishList(Competitor competitor) {
        mFinishedData
            .add(new CompetitorsWithIdImpl(mId, competitor.getId(), competitor.getName(), MaxPointsReason.NONE));
        mId++;
        mFinishedAdapter.notifyDataSetChanged();
    }

    private void removeCompetitorFromList(Competitor competitor) {
        mCompetitorData.remove(competitor);
        Collections.sort(mCompetitorData, new NaturalNamedComparator());
        mCompetitorAdapter.notifyDataSetChanged();
    }

    @Override
    public void afterMoved() {
        getRaceState().setFinishPositioningListChanged(MillisecondsTimePoint.now(), getCompetitorResults());
    }

    @Override
    public void onItemRemoved(CompetitorsWithIdImpl item) {
        Competitor competitor = getCompetitorStore().getExistingCompetitorById(item.getKey());
        if (competitor != null) {
            addNewCompetitorToCompetitorList(competitor);
        }
        getRaceState().setFinishPositioningListChanged(MillisecondsTimePoint.now(), getCompetitorResults());
    }

    private void addNewCompetitorToCompetitorList(Competitor competitor) {
        mCompetitorData.add(competitor);
        Collections.sort(mCompetitorData, new NaturalNamedComparator());
        mCompetitorAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLongClick(final CompetitorsWithIdImpl item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final CharSequence[] maxPointsReasons = getAllMaxPointsReasons();
        builder.setTitle(R.string.select_penalty_reason)
            .setItems(maxPointsReasons, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int position) {
                    setMaxPointsReasonForItem(item, maxPointsReasons[position]);
                    mFinishedAdapter.notifyDataSetChanged();
                }
            });
        builder.create().show();
    }

    private CharSequence[] getAllMaxPointsReasons() {
        List<CharSequence> result = new ArrayList<>();
        for (MaxPointsReason reason : MaxPointsReason.values()) {
            result.add(reason.name());
        }
        return result.toArray(new CharSequence[result.size()]);
    }

    protected void setMaxPointsReasonForItem(CompetitorsWithIdImpl item, CharSequence maxPointsReasonName) {
        MaxPointsReason maxPointsReason = MaxPointsReason.valueOf(maxPointsReasonName.toString());
        CompetitorsWithIdImpl newItem = new CompetitorsWithIdImpl(item.getId(), item.getKey(), item.getText(),
            maxPointsReason);
        int currentIndexOfItem = mFinishedData.indexOf(item);
        replaceItemInPositioningList(currentIndexOfItem, item, newItem);

        if (!maxPointsReason.equals(MaxPointsReason.NONE)) {
            setCompetitorToBottomOfPositioningList(newItem);
        }
        getRaceState().setFinishPositioningListChanged(MillisecondsTimePoint.now(), getCompetitorResults());
    }

    private void replaceItemInPositioningList(int index, CompetitorsWithIdImpl item, CompetitorsWithIdImpl newItem) {
        mFinishedData.remove(item);
        mFinishedData.add(index, newItem);
    }

    private void setCompetitorToBottomOfPositioningList(CompetitorsWithIdImpl item) {
        int lastIndex = getCompetitorResults().size() - 1;
        mFinishedData.remove(item);
        mFinishedData.add(lastIndex, item);
    }

    private CompetitorResults getCompetitorResults() {
        CompetitorResults result = new CompetitorResultsImpl();
        for (CompetitorsWithIdImpl item : mFinishedData) {
            result.add(new Util.Triple<>(item.getKey(), item.getText(), item.getReason()));
        }
        return result;
    }
}
