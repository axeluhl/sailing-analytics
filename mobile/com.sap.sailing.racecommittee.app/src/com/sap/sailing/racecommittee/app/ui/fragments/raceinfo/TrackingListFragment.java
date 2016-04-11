package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Loader;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.sap.sailing.android.shared.util.AppUtils;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.abstractlog.race.CompetitorResult;
import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.impl.CompetitorResultImpl;
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
import com.sap.sailing.racecommittee.app.domain.impl.CompetitorResultWithIdImpl;
import com.sap.sailing.racecommittee.app.ui.adapters.CompetitorAdapter;
import com.sap.sailing.racecommittee.app.ui.adapters.FinishListAdapter;
import com.sap.sailing.racecommittee.app.ui.adapters.StringArraySpinnerAdapter;
import com.sap.sailing.racecommittee.app.ui.comparators.CompetitorSailIdComparator;
import com.sap.sailing.racecommittee.app.ui.comparators.NaturalNamedComparator;
import com.sap.sailing.racecommittee.app.ui.layouts.CompetitorEditLayout;
import com.sap.sailing.racecommittee.app.ui.layouts.HeaderLayout;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class TrackingListFragment extends BaseFragment
    implements CompetitorAdapter.CompetitorClick, FinishListAdapter.FinishEvents, View.OnClickListener, AdapterView.OnItemSelectedListener {

    private RecyclerViewDragDropManager mDragDropManager;
    private RecyclerViewSwipeManager mSwipeManager;
    private RecyclerViewTouchActionGuardManager mGuardManager;
    private RecyclerView mFinishView;
    private Button mConfirm;

    private RecyclerView.Adapter<FinishListAdapter.ViewHolder> mFinishedAdapter;
    private CompetitorAdapter mCompetitorAdapter;
    private List<CompetitorResultWithIdImpl> mFinishedData;
    private List<Competitor> mCompetitorData;
    private int mId = 0;
    private HeaderLayout mHeader;
    private TextView mPageTitle;
    private Spinner mSortSpinner;

    private Comparator<Competitor> mComparator;
    private List<Comparator<Competitor>> mComparators;

    public TrackingListFragment() {
        mCompetitorData = Collections.synchronizedList(new ArrayList<Competitor>());
    }

    public static TrackingListFragment newInstance(Bundle args, int startMode) {
        TrackingListFragment fragment = new TrackingListFragment();
        args.putInt(START_MODE, startMode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_tracking_list, container, false);

        mHeader = ViewHelper.get(layout, R.id.header);

        mDots = new ArrayList<>();
        mPanels = new ArrayList<>();

        ImageView dot;
        dot = ViewHelper.get(layout, R.id.dot_1);
        if (dot != null) {
            mDots.add(dot);
        }
        dot = ViewHelper.get(layout, R.id.dot_2);
        if (dot != null) {
            mDots.add(dot);
        }

        ImageView btnPrev = ViewHelper.get(layout, R.id.nav_prev);
        if (btnPrev != null) {
            btnPrev.setOnClickListener(this);
        }

        ImageView btnNext = ViewHelper.get(layout, R.id.nav_next);
        if (btnNext != null) {
            btnNext.setOnClickListener(this);
        }

        if (mHeader != null) {
            mHeader.setHeaderOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendIntent(AppConstants.INTENT_ACTION_CLEAR_TOGGLE);
                    sendIntent(AppConstants.INTENT_ACTION_SHOW_SUMMARY_CONTENT);
                }
            });
        }

        if (getArguments().getInt(START_MODE, 0) != 0) {
            mPageTitle = ViewHelper.get(layout, R.id.page_title);
            if (mPageTitle != null) {
                mPageTitle.setVisibility(View.VISIBLE);
            }

            if (mHeader != null) {
                mHeader.setVisibility(View.GONE);
            }

            View buttonBar = ViewHelper.get(layout, R.id.bottom_bar);
            if (buttonBar != null) {
                buttonBar.setVisibility(View.GONE);
            }
        }

        mSortSpinner = ViewHelper.get(layout, R.id.sort_spinner);
        if (mSortSpinner != null) {
            StringArraySpinnerAdapter adapter = new StringArraySpinnerAdapter(getResources().getStringArray(R.array.sort_by_values));
            mSortSpinner.setAdapter(adapter);
            mSortSpinner.setOnItemSelectedListener(new StringArraySpinnerAdapter.SpinnerSelectedListener(adapter, this));
            mSortSpinner.setSelection(0);
        }

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mComparators = new ArrayList<>();
        mComparators.add(new CompetitorSailIdComparator());
        mComparators.add(new NaturalNamedComparator<Competitor>());
        mComparator = mComparators.get(0);

        mFinishedData = initializeFinishList();
        loadCompetitors();

        if (getView() != null) {
            RecyclerView competitorView = (RecyclerView) getView().findViewById(R.id.list_positioning_all);
            if (competitorView != null) {
                LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                mCompetitorAdapter = new CompetitorAdapter(getActivity(), mCompetitorData);
                mCompetitorAdapter.setListener(this);

                competitorView.setLayoutManager(layoutManager);
                competitorView.setAdapter(mCompetitorAdapter);

                mPanels.add(competitorView);
            }

            mFinishView = (RecyclerView) getView().findViewById(R.id.list_positioning_chosen);
            if (mFinishView != null) {
                mGuardManager = new RecyclerViewTouchActionGuardManager();
                mGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
                mGuardManager.setEnabled(true);

                mDragDropManager = new RecyclerViewDragDropManager();
                NinePatchDrawable drawable = (NinePatchDrawable) ContextCompat.getDrawable(getActivity(), R.drawable.material_shadow_z3);
                mDragDropManager.setDraggingItemShadowDrawable(drawable);

                mSwipeManager = new RecyclerViewSwipeManager();

                LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                FinishListAdapter adapter = new FinishListAdapter(getActivity(), mFinishedData);
                adapter.setListener(this);

                @SuppressWarnings("unchecked") RecyclerView.Adapter<FinishListAdapter.ViewHolder> dragManager = mDragDropManager
                    .createWrappedAdapter(adapter);
                mFinishedAdapter = dragManager;

                @SuppressWarnings("unchecked") RecyclerView.Adapter<FinishListAdapter.ViewHolder> swipeManager = mSwipeManager
                    .createWrappedAdapter(mFinishedAdapter);
                mFinishedAdapter = swipeManager;

                mFinishView.setLayoutManager(layoutManager);
                mFinishView.setAdapter(mFinishedAdapter);
                mFinishView.setItemAnimator(new SwipeDismissItemAnimator());

                mGuardManager.attachRecyclerView(mFinishView);
                mDragDropManager.attachRecyclerView(mFinishView);
                mSwipeManager.attachRecyclerView(mFinishView);
                mPanels.add(mFinishView);
            }

            mConfirm = (Button) getView().findViewById(R.id.confirm);
            if (mConfirm != null) {
                mConfirm.setEnabled(mDots.size() == 0);
                mConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getRaceState().setFinishPositioningConfirmed(MillisecondsTimePoint.now());
                        sendIntent(AppConstants.INTENT_ACTION_CLEAR_TOGGLE);
                        sendIntent(AppConstants.INTENT_ACTION_SHOW_SUMMARY_CONTENT);
                    }
                });
            }

            if (getArguments().getInt(START_MODE, 0) == 0) {
                onClick(ViewHelper.get(getView(), R.id.nav_next));
            } else {
                viewPanel(MOVE_NONE);
            }
        }

        Util.addAll(getRace().getCompetitors(), mCompetitorData);
        sortCompetitors();
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
        mCompetitorData.clear();
        mCompetitorData.addAll(data);
        sortCompetitors();
        deleteCompetitorsFromFinishedList(data);
        deleteCompetitorsFromCompetitorList();
        mCompetitorAdapter.notifyDataSetChanged();
    }

    private void deleteCompetitorsFromFinishedList(Collection<Competitor> validCompetitors) {
        List<CompetitorResultWithIdImpl> toBeDeleted = new ArrayList<>();
        for (CompetitorResultWithIdImpl item : mFinishedData) {
            if (!validCompetitors.contains(getCompetitorStore().getExistingCompetitorById(item.getCompetitorId()))) {
                toBeDeleted.add(item);
            }
        }
        mFinishedData.removeAll(toBeDeleted);
    }

    private void deleteCompetitorsFromCompetitorList() {
        for (CompetitorResultWithIdImpl item : mFinishedData) {
            Competitor competitor = getCompetitorStore().getExistingCompetitorById(item.getCompetitorId());
            mCompetitorData.remove(competitor);
        }
    }

    private CompetitorStore getCompetitorStore() {
        return DataManager.create(getActivity()).getDataStore().getDomainFactory().getCompetitorStore();
    }

    private List<CompetitorResultWithIdImpl> initializeFinishList() {
        List<CompetitorResultWithIdImpl> positioning = Collections.synchronizedList(new ArrayList<CompetitorResultWithIdImpl>());
        if (getRaceState() != null && getRaceState().getFinishPositioningList() != null) {
            for (CompetitorResult results : getRaceState().getFinishPositioningList()) {
                positioning.add(new CompetitorResultWithIdImpl(mId, results));
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
        String name = "";
        if (competitor.getBoat() != null) {
            name += competitor.getBoat().getSailID();
        }
        name += " - " + competitor.getName();
        mFinishedData.add(new CompetitorResultWithIdImpl(mId, competitor.getId(), name, mFinishedData.size() + 1, MaxPointsReason.NONE,
                    /* score */ null, /* finishingTime */ null, /* comment */ null));
        mId++;
        mFinishedAdapter.notifyDataSetChanged();
        if (mDots.size() > 0) {
            Toast.makeText(getActivity(), getString(R.string.added_to_result_list, name, mFinishedData.size()), Toast.LENGTH_SHORT).show();
        }
    }

    private void removeCompetitorFromList(Competitor competitor) {
        mCompetitorData.remove(competitor);
        sortCompetitors();
        mCompetitorAdapter.notifyDataSetChanged();
    }

    @Override
    public void afterMoved() {
        getRaceState().setFinishPositioningListChanged(MillisecondsTimePoint.now(), getCompetitorResults());
    }

    @Override
    public void onItemRemoved(CompetitorResultWithIdImpl item) {
        Competitor competitor = getCompetitorStore().getExistingCompetitorById(item.getCompetitorId());
        if (competitor != null) {
            addNewCompetitorToCompetitorList(competitor);
        }
        getRaceState().setFinishPositioningListChanged(MillisecondsTimePoint.now(), getCompetitorResults());
    }

    private void addNewCompetitorToCompetitorList(Competitor competitor) {
        mCompetitorData.add(competitor);
        sortCompetitors();
        mCompetitorAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLongClick(final CompetitorResultWithIdImpl item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_AlertDialog);
        final CharSequence[] maxPointsReasons = getAllMaxPointsReasons();
        builder.setTitle(R.string.select_penalty_reason).setItems(maxPointsReasons, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
                setMaxPointsReasonForItem(item, maxPointsReasons[position]);
                mFinishedAdapter.notifyDataSetChanged();
            }
        });
        builder.show();
    }

    @Override
    public void onEditItem(final CompetitorResultWithIdImpl item, int position) {
        Context context = getActivity();
        if (context instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) context).getSupportActionBar();
            if (actionBar != null) {
                context = actionBar.getThemedContext();
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppTheme_AlertDialog);
        builder.setTitle(item.getCompetitorDisplayName());
        final CompetitorEditLayout layout = new CompetitorEditLayout(getActivity(), getRace().getState()
            .getFinishingTime(), item, position, mFinishedAdapter.getItemCount());
        builder.setView(layout);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CompetitorResultWithIdImpl newItem = layout.getValue();
                int index = mFinishedData.indexOf(item);
                if (item.getMaxPointsReason() != newItem.getMaxPointsReason() && item.getMaxPointsReason() == MaxPointsReason.NONE) {
                    mFinishedData.remove(item);
                    mFinishedData.add(mFinishedData.size(), newItem);
                } else if (item.getOneBasedRank() != newItem.getOneBasedRank()) {
                    int newPos = newItem.getOneBasedRank();
                    if (newPos > 0) {
                        newPos -= 1;
                    }
                    mFinishedData.remove(item);
                    mFinishedData.add(newPos, newItem);
                } else {
                    replaceItemInPositioningList(index, item, newItem);
                }
                mFinishedAdapter.notifyDataSetChanged();
                getRaceState().setFinishPositioningListChanged(MillisecondsTimePoint.now(), getCompetitorResults());
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        AlertDialog dialog = builder.create();
        dialog.show();
        if (AppUtils.with(getActivity()).isTablet()) {
            dialog.getWindow().setLayout(getResources().getDimensionPixelSize(R.dimen.competitor_dialog_width), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private CharSequence[] getAllMaxPointsReasons() {
        List<CharSequence> result = new ArrayList<>();
        for (MaxPointsReason reason : MaxPointsReason.values()) {
            result.add(reason.name());
        }
        return result.toArray(new CharSequence[result.size()]);
    }

    protected void setMaxPointsReasonForItem(CompetitorResultWithIdImpl item, CharSequence maxPointsReasonName) {
        MaxPointsReason maxPointsReason = MaxPointsReason.valueOf(maxPointsReasonName.toString());
        CompetitorResultWithIdImpl newItem = new CompetitorResultWithIdImpl(item.getId(), item.getCompetitorId(), item
            .getCompetitorDisplayName(), item.getOneBasedRank(), maxPointsReason, item.getScore(), item.getFinishingTime(), item.getComment());
        int currentIndexOfItem = mFinishedData.indexOf(item);
        replaceItemInPositioningList(currentIndexOfItem, item, newItem);

        if (!maxPointsReason.equals(MaxPointsReason.NONE)) {
            setCompetitorToBottomOfPositioningList(newItem);
        }
        getRaceState().setFinishPositioningListChanged(MillisecondsTimePoint.now(), getCompetitorResults());
    }

    private void replaceItemInPositioningList(int index, CompetitorResultWithIdImpl item, CompetitorResultWithIdImpl newItem) {
        mFinishedData.remove(item);
        mFinishedData.add(index, newItem);
    }

    private void setCompetitorToBottomOfPositioningList(CompetitorResultWithIdImpl item) {
        int lastIndex = Util.size(getCompetitorResults()) - 1;
        mFinishedData.remove(item);
        mFinishedData.add(lastIndex, item);
    }

    private CompetitorResults getCompetitorResults() {
        CompetitorResults result = new CompetitorResultsImpl();
        int oneBasedRank = 1;
        for (CompetitorResultWithIdImpl item : mFinishedData) {
            result
                .add(new CompetitorResultImpl(item.getCompetitorId(), item.getCompetitorDisplayName(), oneBasedRank++, item.getMaxPointsReason(), item
                    .getScore(), item.getFinishingTime(), item.getComment()));
        }
        return result;
    }

    @Override
    public void onClick(View v) {
        if (v != null) {
            switch (v.getId()) {
                case R.id.nav_prev:
                    viewPanel(MOVE_DOWN);
                    break;

                case R.id.nav_next:
                    viewPanel(MOVE_UP);
                    break;
            }
        }
        if (mHeader != null) {
            switch (mActivePage) {
                case 0:
                    if (mSortSpinner != null) {
                        mSortSpinner.setVisibility(View.VISIBLE);
                    }
                    mHeader.setHeaderText(R.string.tracking_list_01);
                    break;

                default:
                    if (mSortSpinner != null) {
                        mSortSpinner.setVisibility(View.GONE);
                    }
                    mHeader.setHeaderText(R.string.tracking_list_02);
            }
            if (mPageTitle != null) {
                mPageTitle.setText(mHeader.getHeaderText());
            } else if (getView() != null) {
                View header = getView().findViewById(R.id.spinner_header);
                if (header != null) {
                    if (mSortSpinner.getVisibility() != View.VISIBLE) {
                        header.setVisibility(View.GONE);
                    } else {
                        header.setVisibility(View.VISIBLE);
                    }
                }
            }

        }
        mConfirm.setEnabled(mActivePage != 0 || mDots.size() == 0);
    }

    private void sortCompetitors() {
        Collections.sort(mCompetitorData, mComparator);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mComparator = mComparators.get(position);
        sortCompetitors();
        mCompetitorAdapter.notifyDataSetChanged();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        mComparator = mComparators.get(0);
        sortCompetitors();
        mCompetitorAdapter.notifyDataSetChanged();
    }
}
