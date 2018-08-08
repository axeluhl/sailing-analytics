package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.sap.sailing.android.shared.util.AppUtils;
import com.sap.sailing.android.shared.util.BitmapHelper;
import com.sap.sailing.android.shared.util.BroadcastManager;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.abstractlog.race.CompetitorResult;
import com.sap.sailing.domain.abstractlog.race.CompetitorResult.MergeState;
import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.impl.CompetitorResultImpl;
import com.sap.sailing.domain.abstractlog.race.impl.CompetitorResultsImpl;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.BaseRaceStateChangedListener;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorAndBoatStore;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.data.OnlineDataManager;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;
import com.sap.sailing.racecommittee.app.domain.impl.CompetitorResultWithIdImpl;
import com.sap.sailing.racecommittee.app.domain.impl.LeaderboardResult;
import com.sap.sailing.racecommittee.app.ui.adapters.CompetitorAndBoatAdapter;
import com.sap.sailing.racecommittee.app.ui.adapters.CompetitorResultsList;
import com.sap.sailing.racecommittee.app.ui.adapters.FinishListAdapter;
import com.sap.sailing.racecommittee.app.ui.comparators.CompetitorGoalPassingComparator;
import com.sap.sailing.racecommittee.app.ui.comparators.CompetitorNameComparator;
import com.sap.sailing.racecommittee.app.ui.comparators.CompetitorSailIdComparator;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.layouts.CompetitorEditLayout;
import com.sap.sailing.racecommittee.app.ui.layouts.HeaderLayout;
import com.sap.sailing.racecommittee.app.ui.views.SearchView;
import com.sap.sailing.racecommittee.app.utils.StringHelper;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

public class TrackingListFragment extends BaseFragment
    implements CompetitorAndBoatAdapter.CompetitorClick, FinishListAdapter.FinishEvents, View.OnClickListener, AdapterView.OnItemSelectedListener,
    PopupMenu.OnMenuItemClickListener, SearchView.SearchTextWatcher {

    private static final int COMPETITOR_LOADER = 0;
    private static final int LEADERBOARD_ORDER_LOADER = 2;

    private static final int SORT_SAIL_NUMBER = 0;
    private static final int SORT_NAME = 1;
    private static final int SORT_GOAL = 2;
    private static final int SORT_START = 3;

    private RecyclerViewDragDropManager mDragDropManager;
    private RecyclerViewSwipeManager mSwipeManager;
    private RecyclerViewTouchActionGuardManager mGuardManager;
    private RecyclerView mFinishView;
    private Button mConfirm;
    private TextView mEntryCount;

    private FinishListAdapter mAdapter;
    private RecyclerView.Adapter<FinishListAdapter.ViewHolder> mFinishedAdapter;
    private CompetitorAndBoatAdapter mCompetitorAdapter;
    private CompetitorResultsList<CompetitorResultWithIdImpl> mFinishedData;
    private CompetitorResults mDraftData;
    private CompetitorResults mConfirmedData;
    private List<Map.Entry<Competitor, Boat>> mCompetitorData;
    private List<Map.Entry<Competitor, Boat>> mFilteredCompetitorData;
    private int mId = 0;
    private HeaderLayout mHeader;

    private Comparator<Map.Entry<Competitor, Boat>> mComparator;
    private List<Comparator<Map.Entry<Competitor, Boat>>> mComparators;
    private String mFilter;
    private View mTools;

    private StateChangeListener mStateChangeListener;

    public TrackingListFragment() {
        mCompetitorData = Collections.synchronizedList(new ArrayList<Map.Entry<Competitor, Boat>>());
        mFilteredCompetitorData = Collections.synchronizedList(new ArrayList<Map.Entry<Competitor, Boat>>());
    }

    public static TrackingListFragment newInstance(Bundle args, int startMode) {
        TrackingListFragment fragment = new TrackingListFragment();
        args.putInt(START_MODE, startMode);
        fragment.setArguments(args);
        return fragment;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_tracking_list, container, false);

        mStateChangeListener = new StateChangeListener(this);

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
        mTools = ViewHelper.get(layout, R.id.tools_layout);
        ImageView listButton = ViewHelper.get(layout, R.id.list_button);
        if (listButton != null) {
            listButton.setImageDrawable(BitmapHelper.getAttrDrawable(getActivity(), R.attr.list_single_24dp));
            listButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RaceFragment fragment = PenaltyFragment.newInstance();
                    int viewId = R.id.race_content;
                    switch (getRaceState().getStatus()) {
                        case FINISHED:
                            viewId = getFrameId(getActivity(), R.id.finished_edit, R.id.finished_content, true);
                            break;

                        default:
                            break;
                    }
                    replaceFragment(fragment, viewId);
                }
            });
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
            if (mHeader != null) {
                mHeader.setVisibility(View.GONE);
            }
        }
        mEntryCount = ViewHelper.get(layout, R.id.competitor_entry_count);
        SearchView searchView = ViewHelper.get(layout, R.id.competitor_search);
        if (searchView != null) {
            searchView.setSearchTextWatcher(this);
            searchView.isEditSmall(true);
        }
        View sortByButton = ViewHelper.get(layout, R.id.competitor_sort);
        if (sortByButton != null) {
            sortByButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        popupMenu = new PopupMenu(getActivity(), v, Gravity.RIGHT);
                    } else {
                        popupMenu = new PopupMenu(getActivity(), v);
                    }
                    popupMenu.inflate(R.menu.sort_menu);
                    popupMenu.setOnMenuItemClickListener(TrackingListFragment.this);
                    popupMenu.show();
                    ThemeHelper.positioningPopupMenu(getActivity(), popupMenu, v);
                }
            });
        }

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mComparators = new ArrayList<>();
        mComparators.add(SORT_SAIL_NUMBER, new CompetitorSailIdComparator());
        mComparators.add(SORT_NAME, new CompetitorNameComparator());
        mComparators.add(SORT_GOAL, new CompetitorGoalPassingComparator());
        mComparator = mComparators.get(SORT_SAIL_NUMBER);
        mFinishedData = initializeFinishList();
        mConfirmedData = new CompetitorResultsImpl();
        mDraftData = new CompetitorResultsImpl();
        loadCompetitors();
        if (getView() != null) {
            RecyclerView competitorView = (RecyclerView) getView().findViewById(R.id.list_positioning_all);
            if (competitorView != null) {
                LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                mCompetitorAdapter = new CompetitorAndBoatAdapter(getActivity(), mFilteredCompetitorData, getRace().getRaceGroup().canBoatsOfCompetitorsChangePerRace());
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
                mAdapter = new FinishListAdapter(getActivity(), mFinishedData, getRace().getRaceGroup().canBoatsOfCompetitorsChangePerRace());
                mAdapter.setListener(this);
                @SuppressWarnings("unchecked") RecyclerView.Adapter<FinishListAdapter.ViewHolder> dragManager = mDragDropManager
                    .createWrappedAdapter(mAdapter);
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
                mConfirm.setEnabled(true);
                mConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TimePoint now = MillisecondsTimePoint.now();
                        CompetitorResults result = getCompetitorResultsDiff(mConfirmedData);
                        getRaceState().setFinishPositioningListChanged(now, result);
                        getRaceState().setFinishPositioningConfirmed(now, result);
                        initLocalData();
                        Toast.makeText(getActivity(), R.string.publish_clicked, Toast.LENGTH_SHORT).show();
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
        Util.addAll(getRace().getCompetitorsAndBoats().entrySet(), mCompetitorData);
        mFilteredCompetitorData.clear();
        mFilteredCompetitorData.addAll(mCompetitorData);
        loadLeaderboardResult();
        initLocalData();
    }

    private void initLocalData() {
        mDraftData.clear();
        if (getRaceState().getFinishPositioningList() != null) {
            for (CompetitorResult item : getRaceState().getFinishPositioningList()) {
                mDraftData.add(item);
            }
        }

        mConfirmedData.clear();
        if (getRaceState().getConfirmedFinishPositioningList() != null) {
            for (CompetitorResult item : getRaceState().getConfirmedFinishPositioningList()) {
                mConfirmedData.add(new CompetitorResultImpl(item));
            }
        }
    }

    private int getFirstRankZeroPosition() {
        return mAdapter.getFirstRankZeroPosition();
    }

    @Override
    public void onPause() {
        mDragDropManager.cancelDrag();

        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getRace() != null && getRaceState() != null) {
            getRaceState().addChangedListener(mStateChangeListener);
        }

        Intent intent = new Intent(AppConstants.INTENT_ACTION_ON_LIFECYCLE);
        intent.putExtra(AppConstants.INTENT_ACTION_EXTRA_LIFECYCLE, AppConstants.INTENT_ACTION_EXTRA_START);
        intent.putExtra(AppConstants.INTENT_ACTION_EXTRA, AppConstants.INTENT_ACTION_TOGGLE_COMPETITOR);
        BroadcastManager.getInstance(getActivity()).addIntent(intent);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (getRace() != null && getRaceState() != null) {
            getRaceState().removeChangedListener(mStateChangeListener);
        }

        if (isDirty()) {
            sendUnconfirmed();
        }

        Intent intent = new Intent(AppConstants.INTENT_ACTION_ON_LIFECYCLE);
        intent.putExtra(AppConstants.INTENT_ACTION_EXTRA_LIFECYCLE, AppConstants.INTENT_ACTION_EXTRA_STOP);
        intent.putExtra(AppConstants.INTENT_ACTION_EXTRA, AppConstants.INTENT_ACTION_TOGGLE_COMPETITOR);
        BroadcastManager.getInstance(getActivity()).addIntent(intent);
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

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.by_name:
                mComparator = mComparators.get(SORT_NAME);
                break;
            case R.id.by_start:
                mComparator = mComparators.get(SORT_START);
                break;
            case R.id.by_goal:
                mComparator = mComparators.get(SORT_GOAL);
                break;
            case R.id.by_boat:
                mComparator = mComparators.get(SORT_SAIL_NUMBER);
                break;
            default:
                mComparator = mComparators.get(SORT_SAIL_NUMBER);

        }
        sortCompetitors();
        mCompetitorAdapter.notifyDataSetChanged();
        return true;
    }

    private boolean isDirty() {
        CompetitorResultsImpl diff = (CompetitorResultsImpl) getCompetitorResultsDiff(mConfirmedData);
        boolean dirty = diff.size() != mDraftData.size();
        if (!dirty) {
            for (CompetitorResult item : getCompetitorResultsDiff(mConfirmedData)) {
                for (CompetitorResult last : mDraftData) {
                    if (item.getCompetitorId().equals(last) && !item.equals(last)) {
                        dirty = true;
                        break;
                    }
                }
                if (dirty) {
                    break;
                }
            }
        }
        return dirty;
    }

    private void setPublishButton() {
        Set<Integer> positions = new HashSet<>();
        boolean multiplePositions = false;
        Set<Serializable> errors = new HashSet<>();

        for (CompetitorResult item : mFinishedData) {
            // check for multiple positions
            if (item.getOneBasedRank() != 0 && positions.contains(item.getOneBasedRank())) {
                multiplePositions = true;
            }
            positions.add(item.getOneBasedRank());

            // check for merge errors
            if (item.getMergeState() != MergeState.OK) {
                errors.add(item.getCompetitorId());
            }
        }

        boolean error = multiplePositions || errors.size() > 0;
        int warningSign = error ? R.drawable.ic_warning_red_small : 0;
        mConfirm.setEnabled(!error);
        mConfirm.setCompoundDrawablesWithIntrinsicBounds(warningSign, 0, 0, 0);
    }

    private void sendUnconfirmed() {
        getRaceState().setFinishPositioningListChanged(MillisecondsTimePoint.now(), getCompetitorResultsDiff(mConfirmedData));
    }

    private void loadCompetitors() {
        // invalidate all competitors of this race
        ReadonlyDataManager dataManager = OnlineDataManager.create(getActivity());
        SharedDomainFactory domainFactory = dataManager.getDataStore().getDomainFactory();
        for (Competitor competitor : getRace().getCompetitors()) {
            domainFactory.getCompetitorAndBoatStore().allowCompetitorResetToDefaults(competitor);
        }

        final Loader<?> competitorLoader = getLoaderManager()
            .initLoader(COMPETITOR_LOADER, null, dataManager.createCompetitorsLoader(getRace(), new LoadClient<Map<Competitor, Boat>>() {

                @Override
                public void onLoadFailed(Exception reason) {
                    Toast.makeText(getActivity(), getString(R.string.competitor_load_error, reason.toString()), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onLoadSucceeded(Map<Competitor, Boat> data, boolean isCached) {
                    if (isAdded() && !isCached) {
                        onLoadCompetitorsSucceeded(data);
                    }
                }
            }));

        // Force load to get non-cached remote competitors...
        competitorLoader.forceLoad();
    }

    private void loadLeaderboardResult() {
        ReadonlyDataManager dataManager = OnlineDataManager.create(getActivity());

        final Loader<?> leaderboardResultLoader = getLoaderManager()
            .initLoader(LEADERBOARD_ORDER_LOADER, null, dataManager.createLeaderboardLoader(getRace(), new LoadClient<LeaderboardResult>() {
                @Override
                public void onLoadFailed(Exception reason) {

                }

                @Override
                public void onLoadSucceeded(LeaderboardResult data, boolean isCached) {
                    List<Util.Pair<Long, String>> sortByRank = data.getResult(getRace().getName());
                    if (isAdded() && sortByRank != null) {
                        CompetitorGoalPassingComparator comparator = (CompetitorGoalPassingComparator)mComparators.get(SORT_GOAL);
                        comparator.updateWith(sortByRank);
                        sortCompetitors();
                        mCompetitorAdapter.notifyDataSetChanged();
                    }
                }
            }));

        leaderboardResultLoader.forceLoad();
    }

    protected void onLoadCompetitorsSucceeded(Map<Competitor, Boat> data) {
        Collection<Competitor> competitors = data.keySet();
        mCompetitorData.clear();
        Util.addAll(data.entrySet(), mCompetitorData);
        mFilteredCompetitorData.clear();
        Util.addAll(data.entrySet(), mFilteredCompetitorData);
        sortCompetitors();
        deleteCompetitorsFromFinishedList(competitors);
        deleteCompetitorsFromCompetitorList();
        mCompetitorAdapter.notifyDataSetChanged();
    }

    private void deleteCompetitorsFromFinishedList(Collection<Competitor> validCompetitors) {
        List<CompetitorResultWithIdImpl> toBeDeleted = new ArrayList<>();
        for (CompetitorResultWithIdImpl item : mFinishedData) {
            if (!validCompetitors.contains(getCompetitorStore().getExistingCompetitorById(item.getCompetitorId()))) {
                toBeDeleted.add(item);
                continue;
            }
            if (item.getOneBasedRank() == 0 && item.getMaxPointsReason() == MaxPointsReason.NONE) {
                toBeDeleted.add(item);
                continue;
            }
            item.setBoat(getBoat(item.getCompetitorId()));
        }
        mFinishedData.removeAll(toBeDeleted);
        setPublishButton();
        mFinishedAdapter.notifyDataSetChanged();
    }

    private void deleteCompetitorsFromCompetitorList() {
        for (CompetitorResultWithIdImpl item : mFinishedData) {
            Competitor competitor = getCompetitorStore().getExistingCompetitorById(item.getCompetitorId());
            for (Map.Entry<Competitor, Boat> entry : getRace().getCompetitorsAndBoats().entrySet()) {
                if (entry.getKey().equals(competitor)) {
                    mCompetitorData.remove(entry);
                    mFilteredCompetitorData.remove(entry);
                }
            }
        }
    }

    private CompetitorAndBoatStore getCompetitorStore() {
        return DataManager.create(getActivity()).getDataStore().getDomainFactory().getCompetitorAndBoatStore();
    }

    private CompetitorResultsList<CompetitorResultWithIdImpl> initializeFinishList() {
        CompetitorResultsList<CompetitorResultWithIdImpl> positioning = new CompetitorResultsList<>(Collections
            .synchronizedList(new ArrayList<CompetitorResultWithIdImpl>()));
        if (getRaceState() != null && getRaceState().getFinishPositioningList() != null) {
            for (CompetitorResult result : getRaceState().getFinishPositioningList()) {
                Boat boat = getBoat(result.getCompetitorId());
                positioning.add(new CompetitorResultWithIdImpl(mId, boat, result));
                mId++;
            }
        }
        Collections.sort(positioning, new DefaultCompetitorResultComparator(/* lowPoint TODO where to get this from? */ true));
        return positioning;
    }

    @Nullable
    private Boat getBoat(Serializable competitorId) {
        Boat boat = null;
        for (Map.Entry<Competitor, Boat> entry : getRace().getCompetitorsAndBoats().entrySet()) {
            if (entry.getKey().getId().equals(competitorId)) {
                boat = entry.getValue();
                break;
            }
        }
        return boat;
    }

    @Override
    public void onCompetitorClick(Competitor competitor) {
        moveCompetitorToFinishList(competitor);
        for (Map.Entry<Competitor, Boat> entry : mFilteredCompetitorData) {
            if (entry.getKey().equals(competitor)) {
                removeCompetitorFromList(entry);
                break;
            }
        }
    }

    private void moveCompetitorToFinishList(Competitor competitor) {

        int pos = mAdapter.getFirstRankZeroPosition();
        // FIXME mFinishedData.size()+1 also counts penalized competitors before which the competitor is to be inserted! I just wonder how the position shown in the app seems correct...
        int greatestOneBasedRankSoFar = 0;
        for (final CompetitorResultWithIdImpl result : mFinishedData) {
            if (result.getOneBasedRank() > greatestOneBasedRankSoFar) {
                greatestOneBasedRankSoFar = result.getOneBasedRank();
            }
        }
        CompetitorResultWithIdImpl theCompetitor = new CompetitorResultWithIdImpl(mId, getBoat(competitor.getId()), competitor.getId(),
                competitor.getName(), competitor.getShortName(), greatestOneBasedRankSoFar + 1, MaxPointsReason.NONE,
                /* score */ null, /* finishingTime */ null, /* comment */ null, MergeState.OK); 
        mFinishedData.add(pos, theCompetitor);
        mId++;
        setPublishButton();
        mFinishedAdapter.notifyItemInserted(pos);
        if (mDots.size() > 0) {
            Toast.makeText(getActivity(), getString(R.string.added_to_result_list, theCompetitor.getCompetitorDisplayName(), pos + 1), Toast.LENGTH_SHORT).show();
        }
    }

    private void removeCompetitorFromList(Map.Entry<Competitor, Boat> entry) {
        mCompetitorData.remove(entry);
        int pos = mFilteredCompetitorData.indexOf(entry);
        mFilteredCompetitorData.remove(pos);
        mCompetitorAdapter.notifyItemRemoved(pos);
    }

    private void addNewCompetitorToCompetitorList(Map.Entry<Competitor, Boat> entry) {
        mCompetitorData.add(entry);
        mFilteredCompetitorData.add(entry);
        sortCompetitors();
        mCompetitorAdapter.notifyItemInserted(mFilteredCompetitorData.indexOf(entry));
    }

    /**
     * The {@code fromPosition} identifies the position of the item to be moved in the underlying {@link #mFinishedData
     * data model}; it shall be moved to {@code toPosition}. The item will be moved in {@link #mFinishedData} as
     * requested, and all ranks of items affected will be {@link #adjustRanks(int, int) adjusted}. The item moved will
     * only have its rank corrected if it moved to a position before the {@link #getFirstRankZeroPosition()}. Four cases
     * are possible:
     * <ol>
     * <li>Rank set from non-0 to 0: move item to the end of the list, into the "penalized section"; decrement ranks
     * greater than old rank</li>
     * <li>Rank set from 0 to non-0: move from penalized to ranked section; increment ranks greater than or equal to old
     * rank</li>
     * <li>Rank changed from non-0 to other non-0 value: adjust all ranks between and including old and new list
     * position</li>
     * </ol>
     *
     * @param fromPosition the position (zero-based index) where the item currently is in {@link #mFinishedData}
     * @param toPosition   the position (zero-based index <em>after</em> removal of the item from its {@code fromPosition}) where
     *                     to {@link List#add(int, Object)} the item again in {@link #mFinishedData}
     */
    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        CompetitorResultWithIdImpl item = mFinishedData.remove(fromPosition);
        mFinishedData.add(toPosition, item);
        final int firstPositionChanged = Math.min(getFirstRankZeroPosition(), Math.min(fromPosition, toPosition));
        final int lastPositionChanged = Math.min(getFirstRankZeroPosition(), Math.max(fromPosition, toPosition) + 1);
        adjustRanks(firstPositionChanged, lastPositionChanged);
        setPublishButton();
        mFinishedAdapter.notifyItemRangeChanged(firstPositionChanged, lastPositionChanged - firstPositionChanged);
    }

    @Override
    public void onItemRemove(int position) {
        CompetitorResultWithIdImpl item = mFinishedData.get(position);
        if (position >= 0) { // found
            mFinishedData.remove(position);
            adjustRanks(position, getFirstRankZeroPosition());
            setPublishButton();
            mFinishedAdapter.notifyItemRemoved(position);
        }
        for (Map.Entry<Competitor, Boat> entry : getRace().getCompetitorsAndBoats().entrySet()) {
            if (entry.getKey().getId().equals(item.getCompetitorId())) {
                addNewCompetitorToCompetitorList(entry);
                break;
            }
        }
    }

    /**
     * In {@link #mFinishedData}, starting at index {@code fromPosition}, updates all ranks so they equal the
     * position in the list plus one; stop before reaching the entry at list index {@code toPosition} or the
     * {@link #getFirstRankZeroPosition() first penalty position}.
     *
     * @param fromPosition inclusive start index into {@link #mFinishedData} where to start updating the ranks
     * @param toPosition   exclusive end index into {@link #mFinishedData} where to stop updating the ranks; must not be -1
     */
    private void adjustRanks(int fromPosition, int toPosition) {
        final int end = Math.min(toPosition, getFirstRankZeroPosition());
        for (int i = fromPosition; i < end; i++) {
            CompetitorResultWithIdImpl competitorToReplaceWithAdjustedPosition = mFinishedData.get(i);
            final int newOneBasedRank = i + 1;
            mFinishedData.set(i, cloneCompetitorResultAndAdjustRank(competitorToReplaceWithAdjustedPosition, newOneBasedRank));
        }
    }

    private CompetitorResultWithIdImpl cloneCompetitorResultAndAdjustRank(CompetitorResultWithIdImpl competitorToReplaceWithAdjustedPosition,
        final int newOneBasedRank) {
        return new CompetitorResultWithIdImpl(competitorToReplaceWithAdjustedPosition.getId(), getBoat(competitorToReplaceWithAdjustedPosition
            .getCompetitorId()), competitorToReplaceWithAdjustedPosition.getCompetitorId(), competitorToReplaceWithAdjustedPosition
            .getName(), competitorToReplaceWithAdjustedPosition.getShortName(), newOneBasedRank, competitorToReplaceWithAdjustedPosition
            .getMaxPointsReason(), competitorToReplaceWithAdjustedPosition.getScore(), competitorToReplaceWithAdjustedPosition
            .getFinishingTime(), competitorToReplaceWithAdjustedPosition.getComment(), MergeState.OK);
    }

    @Override
    public void onLongClick(final CompetitorResultWithIdImpl item) {
        final CharSequence[] maxPointsReasons = getAllMaxPointsReasons();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_AlertDialog);
        builder.setTitle(R.string.select_penalty_reason);
        builder.setItems(maxPointsReasons, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
                setMaxPointsReasonForItem(item, maxPointsReasons[position]);
                setPublishButton();
                mFinishedAdapter.notifyItemChanged(mFinishedData.indexOf(item));
            }
        });
        builder.show();
    }

    @Override
    public void onItemEdit(final CompetitorResultWithIdImpl item) {
        Context context = getActivity();
        if (context instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) context).getSupportActionBar();
            if (actionBar != null) {
                context = actionBar.getThemedContext();
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppTheme_AlertDialog);
        builder.setTitle(item.getCompetitorDisplayName());
        final CompetitorEditLayout layout = new CompetitorEditLayout(getActivity(), getRace().getState().getFinishingTime(), item,
            mAdapter.getFirstRankZeroPosition() +
            /* allow for setting rank as the new last in the list in case the competitor did not have a rank so far */
                (item.getOneBasedRank() == 0 ? 1 : 0), false, item.getMergeState() == MergeState.ERROR);
        builder.setView(layout);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CompetitorResultWithIdImpl newItem = layout.getValue();
                updateItem(item, newItem);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                CompetitorResultWithIdImpl newItem = new CompetitorResultWithIdImpl(item.getId(),
                        getBoat(item.getCompetitorId()), item.getCompetitorId(), item.getName(), item.getShortName(),
                        item.getOneBasedRank(), item.getMaxPointsReason(), item.getScore(), item.getFinishingTime(),
                        item.getComment(), MergeState.OK);
                updateItem(item, newItem);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        if (AppUtils.with(getActivity()).isTablet()) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(getResources().getDimensionPixelSize(R.dimen.competitor_dialog_width), ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        }
    }

    /**
     * The following cases are possible:
     * <ol>
     * <li>Rank set from non-0 to 0 and MaxPointsReason is not NONE: move item to the end of the list, into the
     * "penalized section"; decrement ranks greater than old rank</li>
     * <li>Rank set from non-0 to 0 and MaxPointsReason is NONE: remove item from finish list</li>
     * <li>Rank set from 0 to non-0: move from penalized to ranked section; increment ranks greater than or equal to old
     * rank</li>
     * <li>Rank changed from non-0 to other non-0 value: adjust all ranks between and including old and new list
     * position</li>
     * </ol>
     */
    private void updateItem(final CompetitorResultWithIdImpl item, CompetitorResultWithIdImpl newItem) {
        int index = -1;
        for (int i = 0; i < mFinishedData.size(); i++) {
            if (mFinishedData.get(i).getCompetitorId().equals(item.getCompetitorId())) {
                index = i;
            }
        }
        if (index >= 0) {
            mFinishedData.set(index, newItem); // update the item in the list already; then check where to move it and adjust other elements
        }
        if (item.getOneBasedRank() != 0 && newItem.getOneBasedRank() == 0 && newItem.getMaxPointsReason() != MaxPointsReason.NONE) {
            // move to the end of the area of "penalized" competitors; may also be an unpenalized competitor that hasn't
            // been removed yet (e.g., in order to force a score correction reset on the server)
            onItemMove(index, mFinishedData.size() - 1); // -1 because first the element is removed, so when inserting the list is one element shorter
        } else if (item.getOneBasedRank() != newItem.getOneBasedRank() && newItem.getOneBasedRank() != 0) {
            onItemMove(index, newItem.getOneBasedRank() - 1);
        } else if (newItem.getOneBasedRank() == 0 && newItem.getMaxPointsReason() == MaxPointsReason.NONE) {
            onItemRemove(index);
        } else {
            mFinishedAdapter.notifyItemChanged(index);
        }
        setPublishButton();
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
        CompetitorResultWithIdImpl newItem = new CompetitorResultWithIdImpl(item.getId(), getBoat(item.getCompetitorId()), item);
        updateItem(item, newItem);
        getRaceState().setFinishPositioningListChanged(MillisecondsTimePoint.now(), getCompetitorResults());
    }

    private CompetitorResults getCompetitorResults() {
        CompetitorResults result = new CompetitorResultsImpl();
        for (CompetitorResultWithIdImpl item : mFinishedData) {
            result.add(new CompetitorResultImpl(item));
        }
        return result;
    }

    private CompetitorResults getCompetitorResultsDiff(CompetitorResults results) {
        CompetitorResults result = new CompetitorResultsImpl();

        // all changed items
        for (CompetitorResult oldItem : results) {
            boolean found = false;
            for (CompetitorResult newItem : mFinishedData) {
                if (oldItem.getCompetitorId().equals(newItem.getCompetitorId())) {
                    CompetitorResult temp = new CompetitorResultImpl(newItem);
                    if (!oldItem.equals(temp)) {
                        result.add(temp);
                    }
                    found = true;
                    break;
                }
            }
            if (!found) {
                result.add(new CompetitorResultImpl(oldItem));
            }
        }

        // all new items
        for (CompetitorResultWithIdImpl newItem : mFinishedData) {
            boolean found = false;
            for (CompetitorResult oldItem : results) {
                if (oldItem.getCompetitorId().equals(newItem.getCompetitorId())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                result.add(new CompetitorResultImpl(newItem));
            }
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
                    mHeader.setHeaderText(R.string.tracking_list_01);
                    break;

                default:
                    mHeader.setHeaderText(R.string.tracking_list_02);
            }
        }
        if (mTools != null) {
            mTools.setVisibility(mActivePage == 0 ? View.VISIBLE : View.GONE);
        }
        mConfirm.setEnabled(true);
    }

    private void sortCompetitors() {
        Collections.sort(mFilteredCompetitorData, mComparator);
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

    @Override
    public void onTextChanged(String text) {
        if (mCompetitorAdapter != null) {
            mFilter = text;
            filterData();
            mCompetitorAdapter.notifyDataSetChanged();
            int count = mCompetitorAdapter.getItemCount();
            if (mEntryCount != null && AppUtils.with(getActivity()).isTablet()) {
                if (!TextUtils.isEmpty(text)) {
                    mEntryCount.setText(getString(R.string.competitor_count, count));
                    mEntryCount.setVisibility(View.VISIBLE);
                } else {
                    mEntryCount.setVisibility(View.GONE);
                }
            }
        }
    }

    private void filterData() {
        mFilteredCompetitorData.clear();
        if (mCompetitorData != null) {
            if (TextUtils.isEmpty(mFilter)) {
                mFilteredCompetitorData.addAll(mCompetitorData);
            } else {
                for (Map.Entry<Competitor, Boat> entry : mCompetitorData) {
                    String name = "";
                    if (entry.getKey().getShortInfo() != null) {
                        name += entry.getKey().getShortInfo() + " - ";
                    }
                    name += entry.getKey().getName();
                    if (StringHelper.on(getActivity()).containsIgnoreCase(name, mFilter)) {
                        mFilteredCompetitorData.add(entry);
                    }
                }
            }
        }
    }

    private void mergeData(CompetitorResults results) {
        Map<Serializable, String> changedCompetitor = new HashMap<>();
        for (CompetitorResult result : results) {
            CompetitorResultWithIdImpl item = null;
            CompetitorResult draft = null;
            CompetitorResultWithIdImpl newItem;
            MergeState state;
            for (CompetitorResultWithIdImpl edited : mFinishedData) {
                if (result.getCompetitorId().equals(edited.getCompetitorId())) {
                    item = edited;
                    break;
                }
            }
            for (CompetitorResult saved : mDraftData) {
                if (result.getCompetitorId().equals(saved.getCompetitorId())) {
                    draft = saved;
                    break;
                }
            }
            if (item != null) { // result is in list
                Boat boat = getBoat(item.getCompetitorId());
                // check one based rank
                if (item.getOneBasedRank() != result.getOneBasedRank()) {
                    if (draft != null) {
                        if (item.getOneBasedRank() == draft.getOneBasedRank()) {
                            state = MergeState.WARNING;
                        } else {
                            state = MergeState.ERROR;
                        }
                    } else {
                        state = MergeState.ERROR;
                    }
                    newItem = new CompetitorResultWithIdImpl(item.getId(), boat, item.getCompetitorId(), item.getName(),
                            item.getShortName(), result.getOneBasedRank(), item.getMaxPointsReason(), item.getScore(),
                            item.getFinishingTime(), item.getComment(), getMergeState(item, state));
                    item = updateChangedItem(changedCompetitor, item, newItem);
                }

                // check max point reasons
                if (!item.getMaxPointsReason().equals(result.getMaxPointsReason())) {
                    if (draft != null) {
                        if (item.getMaxPointsReason().equals(draft.getMaxPointsReason())) {
                            state = MergeState.WARNING;
                        } else {
                            state = MergeState.ERROR;
                        }
                    } else {
                        state = MergeState.ERROR;
                    }
                    newItem = new CompetitorResultWithIdImpl(item.getId(), boat, item.getCompetitorId(), item.getName(),
                            item.getShortName(), item.getOneBasedRank(), result.getMaxPointsReason(), item.getScore(),
                            item.getFinishingTime(), item.getComment(), getMergeState(item, state));
                    item = updateChangedItem(changedCompetitor, item, newItem);
                }

                // check score
                if (item.getScore() != null) {
                    if (!item.getScore().equals(result.getScore())) {
                        if (draft != null) {
                            if (item.getScore().equals(draft.getScore())) {
                                state = MergeState.WARNING;
                            } else {
                                state = MergeState.ERROR;
                            }
                        } else {
                            state = MergeState.ERROR;
                        }
                        newItem = new CompetitorResultWithIdImpl(item.getId(), boat, item.getCompetitorId(),
                                item.getName(), item.getShortName(), item.getOneBasedRank(), item.getMaxPointsReason(),
                                result.getScore(), item.getFinishingTime(), item.getComment(),
                                getMergeState(item, state));
                        item = updateChangedItem(changedCompetitor, item, newItem);
                    }
                } else if (result.getScore() != null) {
                    newItem = new CompetitorResultWithIdImpl(item.getId(), boat, item.getCompetitorId(), item.getName(),
                            item.getShortName(), item.getOneBasedRank(), item.getMaxPointsReason(), result.getScore(),
                            item.getFinishingTime(), item.getComment(), getMergeState(item, MergeState.ERROR));
                    item = updateChangedItem(changedCompetitor, item, newItem);
                }

                // check finishing time
                if (item.getFinishingTime() != null) {
                    if (!item.getFinishingTime().equals(result.getFinishingTime())) {
                        if (draft != null) {
                            if (item.getFinishingTime().equals(draft.getFinishingTime())) {
                                state = MergeState.WARNING;
                            } else {
                                state = MergeState.ERROR;
                            }
                        } else {
                            state = MergeState.ERROR;
                        }
                        newItem = new CompetitorResultWithIdImpl(item.getId(), boat, item.getCompetitorId(),
                                item.getName(), item.getShortName(), item.getOneBasedRank(), item.getMaxPointsReason(),
                                item.getScore(), result.getFinishingTime(), item.getComment(),
                                getMergeState(item, state));
                        item = updateChangedItem(changedCompetitor, item, newItem);
                    }
                } else if (result.getFinishingTime() != null) {
                    newItem = new CompetitorResultWithIdImpl(item.getId(), boat, item.getCompetitorId(), item.getName(),
                            item.getShortName(), item.getOneBasedRank(), item.getMaxPointsReason(), item.getScore(),
                            result.getFinishingTime(), item.getComment(), getMergeState(item, MergeState.ERROR));
                    item = updateChangedItem(changedCompetitor, item, newItem);
                }

                // check comment
                if (item.getComment() != null) {
                    if (!item.getComment().equals(result.getComment())) {
                        if (draft != null) {
                            if (item.getComment().equals(draft.getComment())) {
                                state = MergeState.WARNING;
                            } else {
                                state = MergeState.ERROR;
                            }
                        } else {
                            state = MergeState.ERROR;
                        }
                        newItem = new CompetitorResultWithIdImpl(item.getId(), boat, item.getCompetitorId(),
                                item.getName(), item.getShortName(), item.getOneBasedRank(), item.getMaxPointsReason(),
                                item.getScore(), item.getFinishingTime(),
                                item.getComment() + " ## " + result.getComment(), getMergeState(item, state));
                        item = updateChangedItem(changedCompetitor, item, newItem);
                    }
                } else if (result.getComment() != null) {
                    newItem = new CompetitorResultWithIdImpl(item.getId(), boat, item.getCompetitorId(), item.getName(),
                            item.getShortName(), item.getOneBasedRank(), item.getMaxPointsReason(), item.getScore(),
                            item.getFinishingTime(), result.getComment(), getMergeState(item, MergeState.ERROR));
                    item = updateChangedItem(changedCompetitor, item, newItem);
                }

                // check merge state
                if (!item.getMergeState().equals(result.getMergeState())) {
                    newItem = new CompetitorResultWithIdImpl(item.getId(), boat, item.getCompetitorId(), item.getName(),
                            item.getShortName(), item.getOneBasedRank(), item.getMaxPointsReason(), item.getScore(),
                            item.getFinishingTime(), item.getComment(), getMergeState(item, result.getMergeState()));
                    item = updateChangedItem(changedCompetitor, item, newItem);
                }
            } else { // unknown result, so it will be added
                if (result.getOneBasedRank() != 0 || result.getMaxPointsReason() != MaxPointsReason.NONE) {
                    for (Map.Entry<Competitor, Boat> entry : mCompetitorData) {
                        if (entry.getKey().getId().equals(result.getCompetitorId())) {
                            removeCompetitorFromList(entry);
                            break;
                        }
                    }
                    mFinishedData.add(new CompetitorResultWithIdImpl(mFinishedData.size(), getBoat(result.getCompetitorId()), result));
                    Collections.sort(mFinishedData, new DefaultCompetitorResultComparator(/* lowPoint TODO where to get this from? */ true));
                }
            }
        }
        setPublishButton();
        mAdapter.notifyDataSetChanged();
        if (changedCompetitor.size() > 0) { // show message to user
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_AlertDialog);
            builder.setTitle(R.string.refresh_title);
            StringBuilder string = new StringBuilder(1024);
            for (String competitor : changedCompetitor.values()) {
                string.append(competitor);
                string.append("\n");
            }
            builder.setMessage(getString(R.string.refresh_message, string.toString()));
            builder.setPositiveButton(R.string.refresh_positive, null);
            builder.show();
            // goal passing might have changed too, so update them
            loadLeaderboardResult();
        }
    }

    @NonNull
    private CompetitorResultWithIdImpl updateChangedItem(Map<Serializable, String> changedCompetitor, CompetitorResultWithIdImpl item,
        CompetitorResultWithIdImpl newItem) {
        updateItem(item, newItem);
        changedCompetitor.put(newItem.getCompetitorId(), newItem.getCompetitorDisplayName());
        return newItem;
    }

    private MergeState getMergeState(CompetitorResultWithIdImpl item, MergeState newState) {
        MergeState state = item.getMergeState();
        switch (item.getMergeState()) {
            case ERROR:
                if (newState != MergeState.ERROR) {
                    break;
                }
                state = newState;
                break;

            case WARNING:
                if (newState != MergeState.WARNING && newState != MergeState.ERROR) {
                    break;
                }
                state = newState;
                break;

            case OK:
                state = newState;
                break;
        }
        return state;
    }

    private static class StateChangeListener extends BaseRaceStateChangedListener {

        private WeakReference<TrackingListFragment> mReference;

        StateChangeListener(TrackingListFragment fragment) {
            mReference = new WeakReference<>(fragment);
        }

        @Override
        public void onFinishingPositioningsChanged(ReadonlyRaceState state) {
            super.onFinishingPositioningsChanged(state);

            TrackingListFragment fragment = mReference.get();
            if (fragment != null) {
                fragment.mergeData(state.getFinishPositioningList());
            }
        }
    }
}
