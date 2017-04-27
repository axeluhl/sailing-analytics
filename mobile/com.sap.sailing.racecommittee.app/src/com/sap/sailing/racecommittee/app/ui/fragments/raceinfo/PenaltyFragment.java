package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import static com.sap.sailing.racecommittee.app.ui.adapters.PenaltyAdapter.ItemListener;
import static com.sap.sailing.racecommittee.app.ui.adapters.PenaltyAdapter.OrderBy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.AppUtils;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.abstractlog.race.CompetitorResult;
import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.impl.CompetitorResultImpl;
import com.sap.sailing.domain.abstractlog.race.impl.CompetitorResultsImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.OnlineDataManager;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;
import com.sap.sailing.racecommittee.app.domain.impl.CompetitorResultEditableImpl;
import com.sap.sailing.racecommittee.app.domain.impl.CompetitorResultWithIdImpl;
import com.sap.sailing.racecommittee.app.ui.adapters.PenaltyAdapter;
import com.sap.sailing.racecommittee.app.ui.layouts.CompetitorEditLayout;
import com.sap.sailing.racecommittee.app.ui.views.SearchView;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class PenaltyFragment extends BaseFragment implements PopupMenu.OnMenuItemClickListener, ItemListener, SearchView.SearchTextWatcher {

    private static final int COMPETITOR_LOADER = 0;
    private static final int START_ORDER_LOADER = 1;
    private View mButtonBar;
    private Button mPublishButton;
    private PenaltyAdapter mAdapter;
    private TextView mEntryCount;
    private List<CompetitorResultEditableImpl> mCompetitorResults;

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

        mCompetitorResults = new ArrayList<>();

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
                    popupMenu.setOnMenuItemClickListener(PenaltyFragment.this);
                    popupMenu.show();
                    ThemeHelper.positioningPopupMenu(getActivity(), popupMenu, v);
                }
            });
        }

        mButtonBar = ViewHelper.get(layout, R.id.button_bar);
        mButtonBar.setVisibility(View.GONE);

        Button penaltyButton = ViewHelper.get(layout, R.id.button_penalty);
        if (penaltyButton != null) {
            penaltyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AppTheme_AlertDialog));
                    final CharSequence[] maxPointsReasons = getAllMaxPointsReasons();
                    builder.setTitle(R.string.select_penalty_reason);
                    builder.setItems(maxPointsReasons, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int position) {
                            for (CompetitorResultEditableImpl item : mCompetitorResults) {
                                if (item.isChecked()) {
                                    item.setMaxPointsReason(MaxPointsReason.valueOf(maxPointsReasons[position].toString()));
                                    item.setChecked(false);
                                    item.setDirty(true);
                                }
                            }
                            mAdapter.notifyDataSetChanged();
                            setPublishButton();
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        loadCompetitors();
    }

    @Override
    public void onStop() {
        super.onStop();

        sendUnconfirmed();
    }

    private void sendUnconfirmed() {
        getRaceState().setFinishPositioningListChanged(MillisecondsTimePoint.now(), getCompetitorResults());
    }

    private void confirmData() {
        sendUnconfirmed();
        getRaceState().setFinishPositioningConfirmed(MillisecondsTimePoint.now());
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

    private void loadStartOrder() {
        ReadonlyDataManager dataManager = OnlineDataManager.create(getActivity());
        final Loader<?> startOrderLoader = getLoaderManager()
            .initLoader(START_ORDER_LOADER, null, dataManager.createStartOrderLoader(getRace(), new LoadClient<Collection<Competitor>>() {
                @Override
                public void onLoadFailed(Exception reason) {

                }

                @Override
                public void onLoadSucceeded(Collection<Competitor> data, boolean isCached) {
                    ExLog.i(getActivity(), "asd", data.toString());
                }
            }));
        startOrderLoader.forceLoad();
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

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        OrderBy orderBy;
        switch (item.getItemId()) {
            case R.id.by_boat:
                orderBy = OrderBy.SAILING_NUMBER;
                break;

            case R.id.by_name:
                orderBy = OrderBy.COMPETITOR_NAME;
                break;

            case R.id.by_start:
                orderBy = OrderBy.START_LINE;
                break;

            case R.id.by_goal:
                orderBy = OrderBy.FINISH_LINE;
                break;

            default:
                orderBy = OrderBy.SAILING_NUMBER;

        }
        mAdapter.setOrderedBy(orderBy);
        return true;
    }

    @Override
    public void onCheckedChanged(CompetitorResultEditableImpl competitor, boolean isChecked) {
        for (CompetitorResultEditableImpl item : mCompetitorResults) {
            if (item.equals(competitor)) {
                item.setChecked(isChecked);
                break;
            }
        }
        setPublishButton();
    }

    private void setPublishButton() {
        int count = 0;
        boolean isChecked = false;
        for (CompetitorResultEditableImpl item : mCompetitorResults) {
            if (item.isChecked()) {
                isChecked = true;
            }
            if (!MaxPointsReason.NONE.equals(item.getMaxPointsReason())) {
                count++;
            }
        }
        String text = getString(R.string.publish_button_empty);
        // FIXME needs redefine later
//        if (count != 0) {
//            text = getString(R.string.publish_button_other, count);
//        }
        mPublishButton.setText(text);
        mPublishButton.setEnabled(count != 0);
        mButtonBar.setVisibility((count != 0 || isChecked) ? View.VISIBLE : View.GONE);
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
        final CompetitorEditLayout layout = new CompetitorEditLayout(getActivity(), item);
        builder.setView(layout);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CompetitorResultWithIdImpl item = layout.getValue();
                if (!Util.equalsWithNull(competitor.getMaxPointsReason(), item.getMaxPointsReason())) {
                    competitor.setMaxPointsReason(item.getMaxPointsReason());
                    competitor.setDirty(true);
                }
                if (!Util.equalsWithNull(competitor.getComment(), item.getComment())) {
                    competitor.setComment(item.getComment());
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

    private CompetitorResults getCompetitorResults() {
        CompetitorResults results = new CompetitorResultsImpl();
        for (CompetitorResultEditableImpl item : mCompetitorResults) {
            if (!MaxPointsReason.NONE.equals(item.getMaxPointsReason())) {
                CompetitorResult result = new CompetitorResultImpl(item.getCompetitorId(), item.getCompetitorDisplayName(), item
                    .getOneBasedRank(), item.getMaxPointsReason(), item.getScore(), item.getFinishingTime(), item.getComment());
                results.add(result);
            }
        }
        return results;
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
