package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.sap.sailing.android.shared.util.AppUtils;
import com.sap.sailing.android.shared.util.BitmapHelper;
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
import com.sap.sailing.racecommittee.app.domain.impl.CompetitorWithRaceRankImpl;
import com.sap.sailing.racecommittee.app.domain.impl.LeaderboardResult;
import com.sap.sailing.racecommittee.app.ui.adapters.CompetitorAdapter;
import com.sap.sailing.racecommittee.app.ui.adapters.FinishListAdapter;
import com.sap.sailing.racecommittee.app.ui.comparators.CompetitorSailIdComparator;
import com.sap.sailing.racecommittee.app.ui.comparators.NaturalNamedComparator;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.layouts.CompetitorEditLayout;
import com.sap.sailing.racecommittee.app.ui.layouts.HeaderLayout;
import com.sap.sailing.racecommittee.app.ui.views.SearchView;
import com.sap.sailing.racecommittee.app.utils.StringHelper;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Loader;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

public class TrackingListFragment extends BaseFragment
    implements CompetitorAdapter.CompetitorClick, FinishListAdapter.FinishEvents, View.OnClickListener, AdapterView.OnItemSelectedListener,
    PopupMenu.OnMenuItemClickListener, SearchView.SearchTextWatcher {

    private static final int COMPETITOR_LOADER = 0;
    private static final int START_ORDER_LOADER = 1;
    private static final int LEADERBOARD_ORDER_LOADER = 2;

    private RecyclerViewDragDropManager mDragDropManager;
    private RecyclerViewSwipeManager mSwipeManager;
    private RecyclerViewTouchActionGuardManager mGuardManager;
    private RecyclerView mFinishView;
    private Button mConfirm;
    private TextView mEntryCount;

    private FinishListAdapter mAdapter;
    private RecyclerView.Adapter<FinishListAdapter.ViewHolder> mFinishedAdapter;
    private CompetitorAdapter mCompetitorAdapter;
    private List<CompetitorResultWithIdImpl> mFinishedData;
    private List<Competitor> mCompetitorData;
    private List<Competitor> mFilteredCompetitorData;
    private int mId = 0;
    private HeaderLayout mHeader;

    private Comparator<Competitor> mComparator;
    private List<Comparator<Competitor>> mComparators;
    private String mFilter;
    private View mTools;

    public TrackingListFragment() {
        mCompetitorData = Collections.synchronizedList(new ArrayList<Competitor>());
        mFilteredCompetitorData = Collections.synchronizedList(new ArrayList<Competitor>());
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

        mTools = ViewHelper.get(layout, R.id.tools_layout);

        ImageView listButton = ViewHelper.get(layout, R.id.list_button);
        if (listButton != null) {
            listButton.setImageDrawable(BitmapHelper.getAttrDrawable(getActivity(), R.attr.list_single_24dp));
            listButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendUnconfirmed();
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
        mComparators.add(new CompetitorSailIdComparator());
        mComparators.add(new NaturalNamedComparator<Competitor>());
        mComparator = mComparators.get(0);

        mFinishedData = initializeFinishList();
        loadCompetitors();

        if (getView() != null) {
            RecyclerView competitorView = (RecyclerView) getView().findViewById(R.id.list_positioning_all);
            if (competitorView != null) {
                LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                mCompetitorAdapter = new CompetitorAdapter(getActivity(), mFilteredCompetitorData);
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
                mAdapter = new FinishListAdapter(getActivity(), mFinishedData);
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
                mConfirm.setEnabled(mDots.size() == 0);
                mConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendUnconfirmed();
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
        mFilteredCompetitorData.clear();
        mFilteredCompetitorData.addAll(mCompetitorData);
        sortCompetitors();
        mCompetitorAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        mDragDropManager.cancelDrag();
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

        sendUnconfirmed();
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
            case R.id.by_boat:
                mComparator = mComparators.get(0);
                break;

            case R.id.by_name:
                mComparator = mComparators.get(1);
                break;

            case R.id.by_start:
                break;

            case R.id.by_goal:
                loadLeaderboardResult();
                break;

            default:
                mComparator = mComparators.get(0);

        }
        sortCompetitors();
        mCompetitorAdapter.notifyDataSetChanged();
        return true;
    }

    private void sendUnconfirmed() {
        getRaceState().setFinishPositioningListChanged(MillisecondsTimePoint.now(), getCompetitorResults());
    }

    private void loadCompetitors() {
        // invalidate all competitors of this race
        ReadonlyDataManager dataManager = OnlineDataManager.create(getActivity());
        SharedDomainFactory domainFactory = dataManager.getDataStore().getDomainFactory();
        for (Competitor competitor : getRace().getCompetitors()) {
            domainFactory.getCompetitorStore().allowCompetitorResetToDefaults(competitor);
        }

        final Loader<?> competitorLoader = getLoaderManager()
            .initLoader(COMPETITOR_LOADER, null, dataManager.createCompetitorsLoader(getRace(), new LoadClient<Collection<Competitor>>() {

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

    private void loadLeaderboardResult() {
        ReadonlyDataManager dataManager = OnlineDataManager.create(getActivity());

        final Loader<?> leaderboardResultLoader = getLoaderManager()
            .initLoader(LEADERBOARD_ORDER_LOADER, null, dataManager.createLeaderboardLoader(getRace(), new LoadClient<LeaderboardResult>() {
                @Override
                public void onLoadFailed(Exception reason) {

                }

                @Override
                public void onLoadSucceeded(LeaderboardResult data, boolean isCached) {
                    if (isAdded()) {
                        onLoadLeaderboardSucceeded(data);
                    }
                }
            }));

        leaderboardResultLoader.forceLoad();
    }

    protected void onLoadCompetitorsSucceeded(Collection<Competitor> data) {
        mCompetitorData.clear();
        mCompetitorData.addAll(data);
        mFilteredCompetitorData.clear();
        mFilteredCompetitorData.addAll(data);
        sortCompetitors();
        deleteCompetitorsFromFinishedList(data);
        deleteCompetitorsFromCompetitorList();
        mCompetitorAdapter.notifyDataSetChanged();
    }

    protected void onLoadLeaderboardSucceeded(LeaderboardResult data) {
        final String raceName = getRace().getName();
        List<CompetitorWithRaceRankImpl> sortByRank = data.getCompetitors();
        Collections.sort(sortByRank, new Comparator<CompetitorWithRaceRankImpl>() {
            @Override
            public int compare(CompetitorWithRaceRankImpl left, CompetitorWithRaceRankImpl right) {
                return (int)left.getRaceRank(raceName) - (int)right.getRaceRank(raceName);
            }
        });
        List<Competitor> sortedList = new ArrayList<>();
        for (CompetitorWithRaceRankImpl item : sortByRank) {
            for (Competitor competitor : mCompetitorData) {
                if (competitor.getId().toString().equals(item.getId())) {
                    sortedList.add(competitor);
                    break;
                }
            }
        }
        mCompetitorData.clear();
        mCompetitorData.addAll(sortedList);
        mFilteredCompetitorData.clear();
        mFilteredCompetitorData.addAll(sortedList);
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
            mFilteredCompetitorData.remove(competitor);
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
        Collections.sort(positioning, new DefaultCompetitorResultComparator(/* lowPoint TODO where to get this from? */ true));
        return positioning;
    }

    @Override
    public void onCompetitorClick(Competitor competitor) {
        moveCompetitorToFinishList(competitor);
        removeCompetitorFromList(competitor);
    }

    private void moveCompetitorToFinishList(Competitor competitor) {
        String name = "";
        if (competitor.getBoat() != null) {
            name += competitor.getBoat().getSailID();
        }
        name += " - " + competitor.getName();
        int pos = mAdapter.getFirstPenalty();
        // FIXME mFinishedData.size()+1 also counts penalized competitors before which the competitor is to be inserted! I just wonder how the position shown in the app seems correct...
        int greatestOneBasedRankSoFar = 0;
        for (final CompetitorResultWithIdImpl result : mFinishedData) {
            if (result.getOneBasedRank() > greatestOneBasedRankSoFar) {
                greatestOneBasedRankSoFar = result.getOneBasedRank();
            }
        }
        mFinishedData.add(pos, new CompetitorResultWithIdImpl(mId, competitor.getId(), name, greatestOneBasedRankSoFar + 1, MaxPointsReason.NONE,
                    /* score */ null, /* finishingTime */ null, /* comment */ null));
        mId++;
        mFinishedAdapter.notifyDataSetChanged();
        if (mDots.size() > 0) {
            Toast.makeText(getActivity(), getString(R.string.added_to_result_list, name, pos + 1), Toast.LENGTH_SHORT).show();
        }
    }

    private void removeCompetitorFromList(Competitor competitor) {
        mCompetitorData.remove(competitor);
        mFilteredCompetitorData.remove(competitor);
        sortCompetitors();
        mCompetitorAdapter.notifyDataSetChanged();
    }

    @Override
    public void afterMoved() {
        // no-op
    }

    @Override
    public void onItemRemoved(CompetitorResultWithIdImpl item) {
        Competitor competitor = getCompetitorStore().getExistingCompetitorById(item.getCompetitorId());
        if (competitor != null) {
            addNewCompetitorToCompetitorList(competitor);
        }
    }

    private void addNewCompetitorToCompetitorList(Competitor competitor) {
        mCompetitorData.add(competitor);
        mFilteredCompetitorData.add(competitor);
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
    public void onEditItem(final CompetitorResultWithIdImpl item) {
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
            .getFinishingTime(), item, mFinishedAdapter.getItemCount(), false);
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
                    int firstPenalty = mAdapter.getFirstPenalty();
                    if (newPos >= firstPenalty) {
                        newPos = firstPenalty;
                    }
                    mFinishedData.remove(item);
                    mFinishedData.add(newPos, newItem);
                } else {
                    replaceItemInPositioningList(index, item, newItem);
                }
                mFinishedAdapter.notifyDataSetChanged();
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
            result.add(new CompetitorResultImpl(item.getCompetitorId(), item.getCompetitorDisplayName(),
                item.getMaxPointsReason() == MaxPointsReason.NONE ? oneBasedRank++ : 0, item.getMaxPointsReason(), item.getScore(), item
                .getFinishingTime(), item.getComment()));
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
        mConfirm.setEnabled(mActivePage != 0 || mDots.size() == 0);
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
                for (Competitor competitor : mCompetitorData) {
                    String name = "";
                    if (competitor.getBoat() != null) {
                        name += competitor.getBoat().getSailID();
                    }
                    name += " - " + competitor.getName();
                    if (StringHelper.on(getActivity()).containsIgnoreCase(name, mFilter)) {
                        mFilteredCompetitorData.add(competitor);
                    }
                }
            }
        }
    }
}
