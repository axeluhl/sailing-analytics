package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sap.sailing.android.shared.logging.ExLog;
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
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.line.ConfigurableStartModeFlagRacingProcedure;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.OnlineDataManager;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;
import com.sap.sailing.racecommittee.app.domain.impl.CompetitorResultEditableImpl;
import com.sap.sailing.racecommittee.app.domain.impl.CompetitorResultWithIdImpl;
import com.sap.sailing.racecommittee.app.domain.impl.LeaderboardResult;
import com.sap.sailing.racecommittee.app.ui.adapters.CompetitorResultsList;
import com.sap.sailing.racecommittee.app.ui.adapters.PenaltyAdapter;
import com.sap.sailing.racecommittee.app.ui.adapters.PenaltyAdapter.ItemListener;
import com.sap.sailing.racecommittee.app.ui.adapters.PenaltyAdapter.OrderBy;
import com.sap.sailing.racecommittee.app.ui.adapters.StringArraySpinnerAdapter;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.layouts.CompetitorEditLayout;
import com.sap.sailing.racecommittee.app.ui.layouts.HeaderLayout;
import com.sap.sailing.racecommittee.app.ui.views.SearchView;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PenaltyFragment extends BaseFragment
        implements PopupMenu.OnMenuItemClickListener, ItemListener, SearchView.SearchTextWatcher {
    private static final String TAG = PenaltyFragment.class.getName();

    private static final int COMPETITOR_LOADER = 0;
    private static final int LEADERBOARD_ORDER_LOADER = 2;

    private View mButtonBar;
    private Spinner mPenaltyDropDown;
    private StringArraySpinnerAdapter mPenaltyAdapter;
    private Button mPublishButton;
    private PenaltyAdapter mAdapter;
    private TextView mEntryCount;
    private CompetitorResultsList<CompetitorResultEditableImpl> mCompetitorResults;
    private CompetitorResults mDraftData;
    private CompetitorResults mConfirmedData;
    private View mListButtonLayout;
    private ImageView mListButton;
    private HeaderLayout mHeader;
    private StateChangeListener mStateChangeListener;
    private SearchView mSearchView;

    public static PenaltyFragment newInstance() {
        Bundle args = new Bundle();
        PenaltyFragment fragment = new PenaltyFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_penalty_fragment, container, false);

        mStateChangeListener = new StateChangeListener(this);

        mCompetitorResults = new CompetitorResultsList<>(new ArrayList<CompetitorResultEditableImpl>());
        mDraftData = new CompetitorResultsImpl();
        mConfirmedData = new CompetitorResultsImpl();
        mSearchView = ViewHelper.get(layout, R.id.competitor_search);
        if (mSearchView != null) {
            mSearchView.setSearchTextWatcher(this);
        }
        mHeader = ViewHelper.get(layout, R.id.header);
        mListButtonLayout = ViewHelper.get(layout, R.id.list_button_layout);
        if (mListButtonLayout != null) {
            mListButtonLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendUnconfirmed();
                    RaceFragment fragment = TrackingListFragment.newInstance(new Bundle(), 1);
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
        mListButton = ViewHelper.get(layout, R.id.list_button);
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
                    popupMenu.setOnMenuItemClickListener(PenaltyFragment.this);
                    popupMenu.show();
                    ThemeHelper.positioningPopupMenu(getActivity(), popupMenu, v);
                }
            });
        }
        mButtonBar = ViewHelper.get(layout, R.id.button_bar);
        mButtonBar.setVisibility(View.GONE);
        mPenaltyDropDown = ViewHelper.get(layout, R.id.spinner_penalty);
        if (mPenaltyDropDown != null) {
            mPenaltyAdapter = new StringArraySpinnerAdapter(getAllMaxPointsReasons());
            mPenaltyDropDown.setAdapter(mPenaltyAdapter);
            mPenaltyDropDown
                    .setOnItemSelectedListener(new StringArraySpinnerAdapter.SpinnerSelectedListener(mPenaltyAdapter));
        }
        View applyButton = ViewHelper.get(layout, R.id.button_apply);
        if (applyButton != null) {
            applyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setReason((String) mPenaltyDropDown.getSelectedItem());
                }
            });
        }
        View penaltyButton = ViewHelper.get(layout, R.id.button_penalty);
        if (penaltyButton != null) {
            penaltyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    final CharSequence[] maxPointsReasons = getAllMaxPointsReasons();
                    builder.setTitle(R.string.select_penalty_reason);
                    builder.setItems(maxPointsReasons, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int position) {
                            setReason(maxPointsReasons[position].toString());
                        }
                    });
                    builder.show();
                }
            });
        }
        mPublishButton = ViewHelper.get(layout, R.id.button_publish);
        if (mPublishButton != null) {
            mPublishButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendConfirmed();
                }
            });
        }
        mEntryCount = ViewHelper.get(layout, R.id.competitor_entry_count);
        return layout;
    }

    private void setReason(String reason) {
        for (CompetitorResultEditableImpl item : mCompetitorResults) {
            if (item.isChecked()) {
                item.setMergeState(MergeState.OK);
                item.setMaxPointsReason(MaxPointsReason.valueOf(reason));
                item.setChecked(false);
                item.setDirty(true);
            }
        }
        mAdapter.notifyDataSetChanged();
        setPublishButton();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new PenaltyAdapter(getActivity(), this,
                getRace().getRaceGroup().canBoatsOfCompetitorsChangePerRace());
        RecyclerView recyclerView = ViewHelper.get(getView(), R.id.competitor_list);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(mAdapter);
        }

        if (mPenaltyDropDown != null) {
            int selection = mPenaltyAdapter.getPosition(MaxPointsReason.OCS.name());
            RacingProcedure procedure = getRaceState().getRacingProcedure();
            if (procedure instanceof ConfigurableStartModeFlagRacingProcedure) {
                ConfigurableStartModeFlagRacingProcedure racingProcedure = getRaceState().getTypedRacingProcedure();
                switch (racingProcedure.getStartModeFlag()) {
                case BLACK:
                    selection = mPenaltyAdapter.getPosition(MaxPointsReason.BFD.name());
                    break;

                case UNIFORM:
                    selection = mPenaltyAdapter.getPosition(MaxPointsReason.UFD.name());
                    break;

                default:
                    // nothing
                    break;
                }
            }
            if (getRaceState().getStatus() == RaceLogRaceStatus.FINISHED) {
                selection = mPenaltyAdapter.getPosition(MaxPointsReason.DNF.name());
            }
            mPenaltyDropDown.setSelection(selection);
        }
        switch (getRaceState().getStatus()) {
        case FINISHED:
            if (mHeader != null) {
                if (AppUtils.with(getActivity()).isPhone()) {
                    mHeader.setVisibility(View.VISIBLE);
                    mHeader.setHeaderOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendIntent(AppConstants.INTENT_ACTION_CLEAR_TOGGLE);
                            sendIntent(AppConstants.INTENT_ACTION_SHOW_SUMMARY_CONTENT);
                        }
                    });
                } else {
                    mHeader.setVisibility(View.GONE);
                }
            }
        case FINISHING:
            if (mListButton != null) {
                mListButton.setImageDrawable(BitmapHelper.getAttrDrawable(getActivity(), R.attr.list_both_24dp));
            }
            if (mListButtonLayout != null) {
                mListButtonLayout.setVisibility(View.VISIBLE);
            }
            break;

        default:
            if (mListButtonLayout != null) {
                mListButtonLayout.setVisibility(View.GONE);
            }
        }
        if (mListButtonLayout != null) {
            mSearchView.isEditSmall(mListButtonLayout.getVisibility() == View.VISIBLE);
        }
        initLocalData();
        loadCompetitors();
    }

    private void initLocalData() {
        mDraftData.clear();
        if (getRaceState().getFinishPositioningList() != null) {
            for (CompetitorResult item : getRaceState().getFinishPositioningList()) {
                mDraftData.add(new CompetitorResultImpl(item.getCompetitorId(), item.getCompetitorDisplayName(),
                        item.getOneBasedRank(), item.getMaxPointsReason(), item.getScore(), item.getFinishingTime(),
                        item.getComment(), item.getMergeState()));
            }
        }

        initConfirmedData();
    }

    private void initConfirmedData() {
        mConfirmedData.clear();
        if (getRaceState().getConfirmedFinishPositioningList() != null) {
            for (CompetitorResult item : getRaceState().getConfirmedFinishPositioningList()) {
                mConfirmedData.add(new CompetitorResultImpl(item.getCompetitorId(), item.getCompetitorDisplayName(),
                        item.getOneBasedRank(), item.getMaxPointsReason(), item.getScore(), item.getFinishingTime(),
                        item.getComment(), item.getMergeState()));
            }
        }
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

        boolean dirty = false;
        for (CompetitorResultEditableImpl item : mCompetitorResults) {
            if (item.isDirty()) {
                dirty = true;
                break;
            }
        }
        if (dirty) {
            sendUnconfirmed();
        }

        Intent intent = new Intent(AppConstants.INTENT_ACTION_ON_LIFECYCLE);
        intent.putExtra(AppConstants.INTENT_ACTION_EXTRA_LIFECYCLE, AppConstants.INTENT_ACTION_EXTRA_STOP);
        intent.putExtra(AppConstants.INTENT_ACTION_EXTRA, AppConstants.INTENT_ACTION_TOGGLE_COMPETITOR);
        BroadcastManager.getInstance(getActivity()).addIntent(intent);
    }

    private void sendUnconfirmed() {
        CompetitorResultsImpl results = getCompetitorResultsDiff(mDraftData);
        if (results.size() > 0) {
            getRaceState().setFinishPositioningListChanged(MillisecondsTimePoint.now(), results);
        }
    }

    private void sendConfirmed() {
        CompetitorResultsImpl results = getCompetitorResultsDiff(mDraftData);
        getRaceState().setFinishPositioningConfirmed(MillisecondsTimePoint.now(), results);
        Toast.makeText(getActivity(), R.string.publish_clicked, Toast.LENGTH_SHORT).show();
        initLocalData();
        setPublishButton();
    }

    private String[] getAllMaxPointsReasons() {
        List<String> result = new ArrayList<>();
        for (MaxPointsReason reason : MaxPointsReason.values()) {
            result.add(reason.name());
        }
        return result.toArray(new String[result.size()]);
    }

    private void loadCompetitors() {
        // invalidate all competitors of this race
        ReadonlyDataManager dataManager = OnlineDataManager.create(getActivity());
        SharedDomainFactory domainFactory = dataManager.getDataStore().getDomainFactory();
        for (Competitor competitor : getRace().getCompetitors()) {
            domainFactory.getCompetitorAndBoatStore().allowCompetitorResetToDefaults(competitor);
        }
        final Loader<?> competitorLoader = getLoaderManager().initLoader(COMPETITOR_LOADER, null,
                dataManager.createCompetitorsLoader(getRace(), new LoadClient<Map<Competitor, Boat>>() {
                    @Override
                    public void onLoadFailed(Exception reason) {
                        Toast.makeText(getActivity(), getString(R.string.competitor_load_error, reason.toString()),
                                Toast.LENGTH_LONG).show();
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
        final Loader<?> leaderboardResultLoader = getLoaderManager().initLoader(LEADERBOARD_ORDER_LOADER, null,
                dataManager.createLeaderboardLoader(getRace(), new LoadClient<LeaderboardResult>() {
                    @Override
                    public void onLoadFailed(Exception reason) {
                        ExLog.ex(getActivity(), TAG, reason);
                    }

                    @Override
                    public void onLoadSucceeded(LeaderboardResult data, boolean isCached) {
                        onLoadLeaderboardResultSucceeded(data);
                    }
                }));
        leaderboardResultLoader.forceLoad();
    }

    private void onLoadCompetitorsSucceeded(Map<Competitor, Boat> data) {
        mCompetitorResults.clear();
        for (Competitor item : data.keySet()) { // add loaded competitors
            String name = "";
            if (item.getShortInfo() != null) {
                name += item.getShortInfo() + " - ";
            }
            name += item.getName();
            CompetitorResult result = new CompetitorResultImpl(item.getId(), name, 0, MaxPointsReason.NONE,
                    /* score */ null, /* finishingTime */ null, /* comment */ null, MergeState.OK);
            mCompetitorResults.add(new CompetitorResultEditableImpl(result));
        }
        if (getRaceState() != null && getRaceState().getFinishPositioningList() != null) { // mix with finish position
                                                                                           // list
            for (CompetitorResult result : getRaceState().getFinishPositioningList()) {
                int pos = 0;
                for (int i = 0; i < mCompetitorResults.size(); i++) {
                    if (mCompetitorResults.get(i).getCompetitorId().equals(result.getCompetitorId())) {
                        mCompetitorResults.remove(i);
                        break;
                    }
                    pos++;
                }
                mCompetitorResults.add(pos, new CompetitorResultEditableImpl(result));
            }
        }
        mAdapter.setCompetitor(mCompetitorResults, data);
        setPublishButton();
    }

    private void onLoadLeaderboardResultSucceeded(LeaderboardResult data) {
        final String raceName = getRace().getName();
        List<Util.Pair<Long, String>> sortByRank = data.getResult(raceName);
        List<CompetitorResultEditableImpl> sortedList = new ArrayList<>();
        if (sortByRank != null) {
            for (Util.Pair<Long, String> item : sortByRank) {
                for (CompetitorResultEditableImpl competitor : mCompetitorResults) {
                    if (competitor.getCompetitorId().toString().equals(item.getB())) {
                        sortedList.add(competitor);
                        break;
                    }
                }
            }
        }
        mCompetitorResults.clear();
        mCompetitorResults.addAll(sortedList);
        mAdapter.setCompetitor(mCompetitorResults, null);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        OrderBy orderBy = OrderBy.SAILING_NUMBER;
        switch (item.getItemId()) {
        case R.id.by_name:
            orderBy = OrderBy.COMPETITOR_NAME;
            break;

        case R.id.by_start:
            orderBy = OrderBy.START_LINE;
            break;

        case R.id.by_goal:
            orderBy = OrderBy.FINISH_LINE;
            loadLeaderboardResult();
            break;

        default:
            break;

        }
        mAdapter.setOrderedBy(orderBy);
        return true;
    }

    @Override
    public void onCheckedChanged(CompetitorResultEditableImpl competitor, boolean isChecked) {
        setPublishButton();
    }

    private void mergeData(CompetitorResults results, boolean checkDirty) {
        Map<Serializable, String> changedCompetitor = new HashMap<>();
        for (CompetitorResult result : results) {
            CompetitorResultEditableImpl item = null;
            CompetitorResult draft = null;
            for (CompetitorResultEditableImpl edited : mCompetitorResults) {
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
                if (checkDirty & item.isDirty()) {
                    // check max point reason
                    if (!item.getMaxPointsReason().equals(result.getMaxPointsReason())) {
                        if (draft != null) {
                            if (item.getMaxPointsReason().equals(draft.getMaxPointsReason())) {
                                setMergeState(item, MergeState.WARNING);
                            } else {
                                setMergeState(item, MergeState.ERROR);
                            }
                        } else {
                            setMergeState(item, MergeState.ERROR);
                        }
                        item.setMaxPointsReason(result.getMaxPointsReason());
                        changedCompetitor.put(item.getCompetitorId(), item.getCompetitorDisplayName());
                    }

                    // check score
                    if (item.getScore() != null) {
                        if (!item.getScore().equals(result.getScore())) {
                            if (draft != null) {
                                if (item.getScore().equals(draft.getScore())) {
                                    setMergeState(item, MergeState.WARNING);
                                } else {
                                    setMergeState(item, MergeState.ERROR);
                                }
                            } else {
                                setMergeState(item, MergeState.ERROR);
                            }
                            item.setScore(result.getScore());
                            changedCompetitor.put(item.getCompetitorId(), item.getCompetitorDisplayName());
                        }
                    } else if (result.getScore() != null) {
                        setMergeState(item, MergeState.ERROR);
                        item.setScore(result.getScore());
                        changedCompetitor.put(item.getCompetitorId(), item.getCompetitorDisplayName());
                    }

                    // check score
                    if (item.getComment() != null) {
                        if (!item.getComment().equals(result.getComment())) {
                            if (draft != null) {
                                if (item.getComment().equals(draft.getComment())) {
                                    setMergeState(item, MergeState.WARNING);
                                } else {
                                    setMergeState(item, MergeState.ERROR);
                                }
                            } else {
                                setMergeState(item, MergeState.ERROR);
                            }
                            item.setComment(item.getComment() + " ## " + result.getComment());
                            changedCompetitor.put(item.getCompetitorId(), item.getCompetitorDisplayName());
                        }
                    } else if (result.getComment() != null) {
                        setMergeState(item, MergeState.ERROR);
                        item.setComment(result.getComment());
                        changedCompetitor.put(item.getCompetitorId(), item.getCompetitorDisplayName());
                    }

                    // check merge state
                    if (!item.getMergeState().equals(result.getMergeState())) {
                        setMergeState(item, result.getMergeState());
                        changedCompetitor.put(item.getCompetitorId(), item.getCompetitorDisplayName());
                    }
                } else {
                    item.setValue(result);
                }
            } else { // unknown result, so it will be added
                mCompetitorResults.add(new CompetitorResultEditableImpl(result));
            }
        }
        mAdapter.notifyDataSetChanged();
        setPublishButton();
        if (changedCompetitor.size() > 0) { // show message to user
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle(R.string.refresh_title);
            StringBuilder string = new StringBuilder(1024);
            for (String competitor : changedCompetitor.values()) {
                string.append(competitor);
                string.append("\n");
            }
            builder.setMessage(getString(R.string.refresh_message, string.toString()));
            builder.setPositiveButton(R.string.refresh_positive, null);
            builder.show();
        }
    }

    private void setMergeState(CompetitorResultEditableImpl item, MergeState newState) {
        switch (item.getMergeState()) {
        case ERROR:
            if (newState != MergeState.ERROR) {
                break;
            }
            item.setDirty(true);
            item.setMergeState(newState);
            break;

        case WARNING:
            if (newState != MergeState.WARNING && newState != MergeState.ERROR) {
                break;
            }
            item.setDirty(true);
            item.setMergeState(newState);
            break;

        case OK:
            item.setDirty(true);
            item.setMergeState(newState);
            break;
        }
    }

    private void setPublishButton() {
        Set<Serializable> changed = new HashSet<>();
        boolean isChecked = false;
        boolean hasError = false;

        for (CompetitorResultEditableImpl item : mCompetitorResults) {
            if (item.isChecked()) {
                isChecked = true;
            }
            if (item.getMergeState() != MergeState.OK) {
                hasError = true;
            }
        }
        CompetitorResults list = getCompetitorResultsDiff(mDraftData);
        for (CompetitorResult item : list) {
            boolean found = false;
            for (CompetitorResult server : mConfirmedData) {
                if (server.equals(item)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                changed.add(item.getCompetitorId());
            }
        }
        String text = getString(R.string.publish_button_empty);
        if (changed.size() != 0) {
            text = getString(R.string.publish_button_other, changed.size());
        }
        mPublishButton.setText(text);
        int warningSign = hasError ? R.drawable.ic_warning_red_small : 0;
        mPublishButton.setCompoundDrawablesWithIntrinsicBounds(warningSign, 0, 0, 0);
        mPublishButton.setEnabled(changed.size() != 0 && !hasError);
        mButtonBar.setVisibility((changed.size() != 0 || isChecked) ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onEditClicked(final CompetitorResultEditableImpl competitor) {
        Context context = getActivity();
        if (context instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) context).getSupportActionBar();
            if (actionBar != null) {
                context = actionBar.getThemedContext();
            }
        }
        competitor.setMergeState(MergeState.OK);
        CompetitorResultWithIdImpl item = new CompetitorResultWithIdImpl(0, getBoat(competitor.getCompetitorId()),
                competitor.getCompetitorId(), competitor.getCompetitorDisplayName(), competitor.getOneBasedRank(),
                competitor.getMaxPointsReason(), competitor.getScore(), competitor.getFinishingTime(),
                competitor.getComment(), competitor.getMergeState());
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(item.getCompetitorDisplayName());
        final CompetitorEditLayout layout = new CompetitorEditLayout(getActivity(), item,
                mCompetitorResults.getFirstRankZeroPosition() +
                /*
                 * allow for setting rank as the new last in the list in case the competitor did not have a rank so far
                 */
                        (item.getOneBasedRank() == 0 ? 1 : 0),
                competitor.getMergeState() == MergeState.ERROR);
        builder.setView(layout);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CompetitorResultWithIdImpl item = layout.getValue();
                // no need to compare rank as long as the dialog doesn't allow the user to edit it
                if (!Util.equalsWithNull(competitor.getMaxPointsReason(), item.getMaxPointsReason())) {
                    competitor.setMaxPointsReason(item.getMaxPointsReason());
                    competitor.setDirty(true);
                }
                if (!Util.equalsWithNull(competitor.getComment(), item.getComment())) {
                    competitor.setComment(TextUtils.isEmpty(item.getComment()) ? null : item.getComment());
                    competitor.setDirty(true);
                }
                if (!Util.equalsWithNull(competitor.getScore(), item.getScore())) {
                    competitor.setScore(item.getScore());
                    competitor.setDirty(true);
                }
                mAdapter.notifyDataSetChanged();
                setPublishButton();
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
                setPublishButton();
                mAdapter.notifyDataSetChanged();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        if (AppUtils.with(getActivity()).isTablet()) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(getResources().getDimensionPixelSize(R.dimen.competitor_dialog_width),
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        }
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

    private CompetitorResultsImpl getCompetitorResultsDiff(CompetitorResults results) {
        CompetitorResultsImpl result = new CompetitorResultsImpl();

        for (CompetitorResultEditableImpl item : mCompetitorResults) {
            boolean found = false;
            for (CompetitorResult published : results) {
                if (item.getCompetitorId().equals(published.getCompetitorId())) {
                    result.add(new CompetitorResultImpl(item.getCompetitorId(), item.getCompetitorDisplayName(),
                            item.getOneBasedRank(), item.getMaxPointsReason(), item.getScore(), item.getFinishingTime(),
                            item.getComment(), item.getMergeState()));
                    found = true;
                    break;
                }
            }
            if (!found && item.isDirty()) {
                result.add(new CompetitorResultImpl(item.getCompetitorId(), item.getCompetitorDisplayName(),
                        item.getOneBasedRank(), item.getMaxPointsReason(), item.getScore(), item.getFinishingTime(),
                        item.getComment(), item.getMergeState()));
            }
        }

        for (CompetitorResult item : results) {
            boolean found = false;
            for (CompetitorResult current : mCompetitorResults) {
                if (item.getCompetitorId().equals(current.getCompetitorId())) {
                    found = true;
                }
            }
            if (!found) {
                result.add(new CompetitorResultImpl(item.getCompetitorId(), item.getCompetitorDisplayName(),
                        item.getOneBasedRank(), item.getMaxPointsReason(), item.getScore(), item.getFinishingTime(),
                        item.getComment(), item.getMergeState()));
            }
        }

        return result;
    }

    @Override
    public void onTextChanged(String text) {
        if (mAdapter != null) {
            mAdapter.setFilter(text);
            int count = mAdapter.getItemCount();
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

    private static class StateChangeListener extends BaseRaceStateChangedListener {

        private WeakReference<PenaltyFragment> mReference;

        StateChangeListener(PenaltyFragment fragment) {
            mReference = new WeakReference<>(fragment);
        }

        @Override
        public void onFinishingPositionsConfirmed(ReadonlyRaceState state) {
            super.onFinishingPositionsConfirmed(state);

            PenaltyFragment fragment = mReference.get();
            if (fragment != null) {
                fragment.initConfirmedData();
                fragment.mergeData(state.getConfirmedFinishPositioningList(), true);
                fragment.setPublishButton();
            }
        }

        @Override
        public void onFinishingPositioningsChanged(ReadonlyRaceState state) {
            super.onFinishingPositioningsChanged(state);

            PenaltyFragment fragment = mReference.get();
            if (fragment != null) {
                fragment.mergeData(state.getFinishPositioningList(), true);
            }
        }
    }
}
