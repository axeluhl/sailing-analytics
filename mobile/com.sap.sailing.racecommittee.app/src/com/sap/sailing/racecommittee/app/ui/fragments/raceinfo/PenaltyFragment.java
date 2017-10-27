package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.AppUtils;
import com.sap.sailing.android.shared.util.BitmapHelper;
import com.sap.sailing.android.shared.util.BroadcastManager;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.abstractlog.race.CompetitorResult;
import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.impl.CompetitorResultImpl;
import com.sap.sailing.domain.abstractlog.race.impl.CompetitorResultsImpl;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.line.ConfigurableStartModeFlagRacingProcedure;
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
import com.sap.sailing.racecommittee.app.domain.impl.CompetitorWithRaceRankImpl;
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

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.view.ContextThemeWrapper;
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

public class PenaltyFragment extends BaseFragment implements PopupMenu.OnMenuItemClickListener, ItemListener, SearchView.SearchTextWatcher {
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
    private CompetitorResultsList<CompetitorResultEditableImpl> mLastSend;
    private CompetitorResults mLastPublished;
    private View mListButtonLayout;
    private ImageView mListButton;
    private HeaderLayout mHeader;

    public static PenaltyFragment newInstance() {
        Bundle args = new Bundle();
        PenaltyFragment fragment = new PenaltyFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_penalty_fragment, container, false);
        mCompetitorResults = new CompetitorResultsList<>(new ArrayList<CompetitorResultEditableImpl>());
        mLastSend = new CompetitorResultsList<>(new ArrayList<CompetitorResultEditableImpl>());
        mLastPublished = new CompetitorResultsImpl();
        SearchView searchView = ViewHelper.get(layout, R.id.competitor_search);
        if (searchView != null) {
            searchView.setSearchTextWatcher(this);
        }
        mHeader = ViewHelper.get(layout, R.id.header);
        mListButtonLayout = ViewHelper.get(layout, R.id.list_button_layout);
        if (mListButtonLayout != null) {
            mListButtonLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
            mPenaltyDropDown.setOnItemSelectedListener(new StringArraySpinnerAdapter.SpinnerSelectedListener(mPenaltyAdapter));
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AppTheme_AlertDialog));
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
                    confirmData();
                }
            });
        }
        setPublishButton();
        mEntryCount = ViewHelper.get(layout, R.id.competitor_entry_count);
        mAdapter = new PenaltyAdapter(getActivity(), this);
        RecyclerView recyclerView = ViewHelper.get(layout, R.id.competitor_list);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(mAdapter);
        }
        return layout;
    }

    private void setReason(String reason) {
        for (CompetitorResultEditableImpl item : mCompetitorResults) {
            if (item.isChecked()) {
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

        if (getRaceState().getFinishPositioningList() != null) {
            for (CompetitorResult item : getRaceState().getFinishPositioningList()) {
                mLastSend.add(new CompetitorResultEditableImpl(item));
            }
        }
        if (getRaceState().getConfirmedFinishPositioningList() != null) {
            for (CompetitorResult item : getRaceState().getConfirmedFinishPositioningList()) {
                mLastPublished.add(new CompetitorResultImpl(item.getCompetitorId(), item.getCompetitorDisplayName(), item.getOneBasedRank(), item
                    .getMaxPointsReason(), item.getScore(), item.getFinishingTime(), item.getComment()));
            }
        }
        loadCompetitors();
    }

    @Override
    public void onStart() {
        super.onStart();

        Intent intent = new Intent(AppConstants.INTENT_ACTION_ON_LIFECYCLE);
        intent.putExtra(AppConstants.INTENT_ACTION_EXTRA_LIFECYCLE, AppConstants.INTENT_ACTION_EXTRA_START);
        intent.putExtra(AppConstants.INTENT_ACTION_EXTRA, AppConstants.INTENT_ACTION_TOGGLE_COMPETITOR);
        BroadcastManager.getInstance(getActivity()).addIntent(intent);
    }

    @Override
    public void onStop() {
        super.onStop();

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
        CompetitorResults results = getCompetitorResultsDiff();
        getRaceState().setFinishPositioningListChanged(MillisecondsTimePoint.now(), results);
        for (CompetitorResultEditableImpl item : mCompetitorResults) {
            item.setDirty(false);
        }
    }

    private void confirmData() {
        sendUnconfirmed();
        getRaceState().setFinishPositioningConfirmed(MillisecondsTimePoint.now());
        Toast.makeText(getActivity(), R.string.publish_clicked, Toast.LENGTH_SHORT).show();
        loadCompetitors();
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
                    ExLog.ex(getActivity(), TAG, reason);
                }

                @Override
                public void onLoadSucceeded(LeaderboardResult data, boolean isCached) {
                    onLoadLeaderboardResultSucceeded(data);
                }
            }));
        leaderboardResultLoader.forceLoad();
    }

    private void onLoadCompetitorsSucceeded(Collection<Competitor> data) {
        mCompetitorResults.clear();
        for (Competitor item : data) { // add loaded competitors
            String name = "";
            if (item.getBoat() != null) {
                name += item.getBoat().getSailID();
            }
            name += " - " + item.getName();
            CompetitorResult result = new CompetitorResultImpl(item.getId(), name, 0, MaxPointsReason.NONE,
                    /* score */ null, /* finishingTime */ null, /* comment */ null);
            mCompetitorResults.add(new CompetitorResultEditableImpl(result));
        }
        if (getRaceState() != null && getRaceState().getFinishPositioningList() != null) { // mix with finish position list
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
        mAdapter.setCompetitor(mCompetitorResults);
        setPublishButton();
    }

    private void onLoadLeaderboardResultSucceeded(LeaderboardResult data) {
        final String raceName = getRace().getName();
        List<CompetitorWithRaceRankImpl> sortByRank = data.getCompetitors();
        Collections.sort(sortByRank, new Comparator<CompetitorWithRaceRankImpl>() {
            @Override
            public int compare(CompetitorWithRaceRankImpl left, CompetitorWithRaceRankImpl right) {
                return (int) left.getRaceRank(raceName) - (int) right.getRaceRank(raceName);
            }
        });
        List<CompetitorResultEditableImpl> sortedList = new ArrayList<>();
        for (CompetitorWithRaceRankImpl item : sortByRank) {
            for (CompetitorResultEditableImpl competitor : mCompetitorResults) {
                if (competitor.getCompetitorId().toString().equals(item.getId())) {
                    sortedList.add(competitor);
                    break;
                }
            }
        }
        mCompetitorResults.clear();
        mCompetitorResults.addAll(sortedList);
        mAdapter.setCompetitor(mCompetitorResults);
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

    private void setPublishButton() {
        Set<Serializable> changed = new HashSet<>();
        boolean isChecked = false;

        for (CompetitorResultEditableImpl item : mCompetitorResults) {
            if (item.isChecked()) {
                isChecked = true;
            }
        }
        for (CompetitorResult item : getCompetitorResultsDiff()) {
            boolean found = false;
            for (CompetitorResult server : mLastPublished) {
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
        mPublishButton.setEnabled(changed.size() != 0);
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
        CompetitorResultWithIdImpl item = new CompetitorResultWithIdImpl(0, competitor.getCompetitorId(), competitor
            .getCompetitorDisplayName(), competitor.getOneBasedRank(), competitor.getMaxPointsReason(), competitor.getScore(), competitor
            .getFinishingTime(), competitor.getComment());
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppTheme_AlertDialog);
        builder.setTitle(item.getCompetitorDisplayName());
        final CompetitorEditLayout layout = new CompetitorEditLayout(getActivity(), item, mCompetitorResults.getFirstRankZeroPosition() +
                /* allow for setting rank as the new last in the list in case the competitor did not have a rank so far */
            (item.getOneBasedRank() == 0 ? 1 : 0));
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
                if (competitor.isDirty()) {
                    mAdapter.notifyDataSetChanged();
                }
                setPublishButton();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        AlertDialog dialog = builder.create();
        dialog.show();
        if (AppUtils.with(getActivity()).isTablet()) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(getResources().getDimensionPixelSize(R.dimen.competitor_dialog_width), ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        }
    }

    private CompetitorResults getCompetitorResultsDiff() {
        CompetitorResultsImpl result = new CompetitorResultsImpl();

        for (CompetitorResultEditableImpl item : mCompetitorResults) {
            if (item.isDirty()) {
                result.add(new CompetitorResultImpl(item.getCompetitorId(), item.getCompetitorDisplayName(), item.getOneBasedRank(), item
                    .getMaxPointsReason(), item.getScore(), item.getFinishingTime(), item.getComment()));
            }
        }

        for (CompetitorResult item : mLastSend) {
            boolean found = false;
            for (CompetitorResult dirtyItem : result) {
                if (item.getCompetitorId().equals(dirtyItem.getCompetitorId())) {
                    found = true;
                }
            }
            if (!found) {
                result.add(new CompetitorResultImpl(item.getCompetitorId(), item.getCompetitorDisplayName(), item.getOneBasedRank(), item
                    .getMaxPointsReason(), item.getScore(), item.getFinishingTime(), item.getComment()));
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
}
