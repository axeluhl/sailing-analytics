package com.sap.sailing.racecommittee.app.ui.adapters.racelist;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.BitmapHelper;
import com.sap.sailing.android.shared.util.BroadcastManager;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.StartTimeFinderResult;
import com.sap.sailing.domain.abstractlog.race.state.RaceState;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.FlagPoleState;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.base.SeriesBase;
import com.sap.sailing.domain.base.racegroup.CurrentRaceComparator;
import com.sap.sailing.domain.base.racegroup.RaceGroupSeries;
import com.sap.sailing.domain.base.racegroup.RaceGroupSeriesComparator;
import com.sap.sailing.domain.common.racelog.FlagPole;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.ui.adapters.racelist.RaceFilter.FilterSubscriber;
import com.sap.sailing.racecommittee.app.ui.utils.FlagsResources;
import com.sap.sailing.racecommittee.app.utils.RaceHelper;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ManagedRaceListAdapter extends ArrayAdapter<RaceListDataType> implements FilterSubscriber {

    private final static String TAG = ManagedRaceListAdapter.class.getName();
    private final Object mLockObject = new Object();
    private final RaceFilter mFilter;
    private LayoutInflater mInflater;
    private Resources mResources;
    private final List<RaceListDataType> mShownViewItems;
    private final Map<ManagedRace, RaceListDataTypeRace> viewItemsRaces;
    private final Map<SeriesBase, RaceListDataTypeHeader> viewItemsSeriesHeaders;
    private ImageView marker;
    private ImageView update_badge;
    private LinearLayout race_flag;
    private TextView time;
    private TextView race_started;
    private TextView race_finished;
    private LinearLayout race_scheduled;
    private TextView race_unscheduled;
    private TextView depends_on;
    private ImageView current_flag;
    private TextView race_name;
    private TextView flag_timer;
    private TextView boat_class;
    private TextView fleet_series;
    private ImageView protest_image;
    private ImageView has_dependent_races;
    private SimpleDateFormat dateFormat;
    private RaceListDataType mSelectedRace;
    private ViewGroup panel_left;
    private ViewGroup panel_right;
    private TextView explicit_factor;
    private int flag_size;
    private DecimalFormat factor_format;
    private final Set<ManagedRace> mAllRaces;

    public ManagedRaceListAdapter(Context context, Set<ManagedRace> allRaces) {
        super(context, 0);
        mAllRaces = allRaces;
        mShownViewItems = new ArrayList<>();
        this.viewItemsRaces = new HashMap<>();
        this.viewItemsSeriesHeaders = new HashMap<>();
        createViewItemsForRacesAndSeries();
        mFilter = new RaceFilter(allRaces, this);
        mInflater = LayoutInflater.from(getContext());
        mResources = getContext().getResources();
        dateFormat = new SimpleDateFormat("kk:mm", getContext().getResources().getConfiguration().locale);
        flag_size = getContext().getResources().getInteger(R.integer.flag_size);
        factor_format = new DecimalFormat(context.getString(R.string.race_factor_format));
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        createViewItemsForRacesAndSeries();
        getFilter().filter("");
    }

    private void createViewItemsForRacesAndSeries() {
        viewItemsRaces.clear();
        viewItemsSeriesHeaders.clear();
        for (final ManagedRace race : mAllRaces) {
            viewItemsRaces.put(race, new RaceListDataTypeRace(race, mInflater));
            final SeriesBase series = race.getSeries();
            if (!viewItemsSeriesHeaders.containsKey(series)) {
                viewItemsSeriesHeaders.put(series, new RaceListDataTypeHeader(new RaceGroupSeries(race), mInflater));
            }
        }
    }

    @Override
    public int getCount() {
        synchronized (mLockObject) {
            return mShownViewItems != null ? mShownViewItems.size() : 0;
        }
    }

    @Override
    public RaceFilter getFilter() {
        return mFilter;
    }

    @Override
    public RaceListDataType getItem(int position) {
        synchronized (mLockObject) {
            return mShownViewItems != null ? mShownViewItems.get(position) : null;
        }
    }

    public List<RaceListDataType> getItems() {
        return mShownViewItems;
    }

    @Override
    public int getItemViewType(int position) {
        return (getItem(position) instanceof RaceListDataTypeHeader ? ViewType.HEADER.index :
                getItem(position) instanceof RaceListDataTypeRace ? ViewType.RACE.index : -1);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final RaceListDataType raceListElement = getItem(position);

        int type = getItemViewType(position);
        TimePoint now = MillisecondsTimePoint.now();

        if (convertView == null) {
            convertView = raceListElement.getView(parent);
        }
        findViews(convertView);
        resetValues(convertView);

        if (type == ViewType.HEADER.index) {
            final RaceListDataTypeHeader header = (RaceListDataTypeHeader) raceListElement;
            String regatta = header.getRaceGroup().getDisplayName();
            if (TextUtils.isEmpty(regatta)) {
                regatta = header.getRaceGroup().getName();
            }
            boat_class.setText(regatta);
            fleet_series.setText(RaceHelper.getSeriesName(header.getSeries(), ""));
            if (fleet_series.getText().length() == 0) {
                fleet_series.setVisibility(View.GONE);
            } else {
                fleet_series.setVisibility(View.VISIBLE);
            }
            protest_image.setImageDrawable(FlagsResources.getFlagDrawable(getContext(), Flags.BRAVO.name(), flag_size));
            protest_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(AppConstants.INTENT_ACTION_SHOW_PROTEST);
                    intent.putExtra(AppConstants.INTENT_ACTION_EXTRA, header.toString());
                    BroadcastManager.getInstance(getContext()).addIntent(intent);
                }
            });
        } else if (type == ViewType.RACE.index) {
            final RaceListDataTypeRace race = (RaceListDataTypeRace) raceListElement;

            if (convertView != null) {
                if (mSelectedRace != null && mSelectedRace.equals(race)) {
                    setMarker(1 - getLevel());
                    if (race.isUpdateIndicatorVisible()) {
                        race.setUpdateIndicatorVisible(false);
                    }
                } else {
                    if (race.isUpdateIndicatorVisible()) {
                        update_badge.setVisibility(View.VISIBLE);
                    }
                }
            }
            race_name.setText(RaceHelper.getReverseRaceFleetName(race.getRace()));
            RaceState state = race.getRace().getState();
            if (state != null) {
                if (state.getStartTime() != null) {
                    int startRes = R.string.race_started;
                    if (state.getFinishedTime() == null) {
                        startRes = R.string.race_start;
                    }
                    String startTime = mResources.getString(startRes, dateFormat.format(state.getStartTime().asDate()));
                    race_started.setText(startTime);
                    if (state.getFinishedTime() == null) {
                        String duration = TimeUtils.formatDuration(now, state.getStartTime());
                        time.setText(duration);
                        float textSize = getContext().getResources().getDimension(R.dimen.textSize_40);
                        if (!TextUtils.isEmpty(duration) && duration.length() >= 6) {
                            textSize = getContext().getResources().getDimension(R.dimen.textSize_32);
                        }
                        time.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                    }
                }
                if (state.getFinishedTime() != null) {
                    time.setVisibility(View.GONE);
                    race_finished.setVisibility(View.VISIBLE);
                    race_finished.setText(mResources.getString(R.string.race_finished, dateFormat.format(state.getFinishedTime().asDate())));
                }
                setDependingText(race);
                if (state.getStartTime() == null && state.getFinishedTime() == null) {
                    switch (race.getRace().getStatus()) {
                        case PRESCHEDULED:
                            panel_right.setVisibility(View.GONE);
                            race_scheduled.setVisibility(View.GONE);
                            race_unscheduled.setVisibility(View.GONE);
                            break;

                        default:
                            race_scheduled.setVisibility(View.GONE);
                            race_unscheduled.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (race_name != null) {
                        race_name.setTextColor(ThemeHelper.getColor(getContext(), R.attr.white));
                    }
                }
            }

            Double factor = race.getRace().getExplicitFactor();
            if (factor != null) {
                explicit_factor.setText(factor_format.format(factor));
                explicit_factor.setVisibility(View.VISIBLE);
            }

            updateFlag(race.getRace(), now);
        }
        return convertView;
    }

    private void setDependingText(RaceListDataTypeRace race) {
        if (depends_on != null) {
            StartTimeFinderResult result = race.getRace().getState().getStartTimeFinderResult();
            if (result != null && result.isDependentStartTime()) {
                SimpleRaceLogIdentifier identifier = Util.get(result.getDependingOnRaces(), 0);
                ManagedRace depending_race = DataManager.create(getContext()).getDataStore().getRace(identifier);
                depends_on.setText(getContext().getString(R.string.minutes_after_long, result.getStartTimeDiff().asMinutes(), RaceHelper
                    .getShortReverseRaceName(depending_race, " / ", race.getRace())));
                depends_on.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getViewTypeCount() {
        return ViewType.values().length;
    }

    @Override
    public boolean isEnabled(int position) {
        return (getItem(position) instanceof RaceListDataTypeRace);
    }

    @Override
    public void onResult(List<ManagedRace> filteredRaces) {
        synchronized (mLockObject) {
            mShownViewItems.clear();
            mShownViewItems.addAll(getShownViewItems(filteredRaces));
            notifyDataSetChanged();
        }
    }

    /**
     * For the {@code filteredRaces} ensure that exactly the required view items are in {@link #mShownViewItems}. This
     * encompasses the header items for all non-empty series and the race items for all races in their series.
     */
    private List<RaceListDataType> getShownViewItems(List<ManagedRace> races) {
        final Map<RaceListDataTypeHeader, List<RaceListDataTypeRace>> raceItemsByHeader = getRaceItemsGroupedBySeriesHeader(races);
        final List<RaceListDataType> result = serializeItems(raceItemsByHeader);
        return result;
    }

    private List<RaceListDataType> serializeItems(final Map<RaceListDataTypeHeader, List<RaceListDataTypeRace>> raceItemsByHeader) {
        final List<RaceListDataType> result = new ArrayList<>();
        final List<RaceListDataTypeHeader> headers = new ArrayList<>(raceItemsByHeader.keySet());
        Collections.sort(headers, new Comparator<RaceListDataTypeHeader>() {
            private final RaceGroupSeriesComparator c = new RaceGroupSeriesComparator();

            @Override
            public int compare(RaceListDataTypeHeader lhs, RaceListDataTypeHeader rhs) {
                return c.compare(lhs.getRegattaSeries(), rhs.getRegattaSeries());
            }
        });
        for (final RaceListDataTypeHeader header : headers) {
            result.add(header);
            final List<RaceListDataTypeRace> raceItems = new ArrayList<>(raceItemsByHeader.get(header));
            Collections.sort(raceItems, new Comparator<RaceListDataTypeRace>() {
                final CurrentRaceComparator c = new CurrentRaceComparator();

                @Override
                public int compare(RaceListDataTypeRace lhs, RaceListDataTypeRace rhs) {
                    return c.compare(lhs.getRace(), rhs.getRace());
                }
            });
            for (final RaceListDataTypeRace raceItem : raceItems) {
                result.add(raceItem);
            }
        }
        return result;
    }

    private Map<RaceListDataTypeHeader, List<RaceListDataTypeRace>> getRaceItemsGroupedBySeriesHeader(final List<ManagedRace> filteredRaces) {
        final Map<RaceListDataTypeHeader, List<RaceListDataTypeRace>> raceItemsByHeader = new HashMap<>();
        for (final ManagedRace race : filteredRaces) {
            final SeriesBase series = race.getSeries();
            final RaceListDataTypeHeader seriesHeader = viewItemsSeriesHeaders.get(series);
            List<RaceListDataTypeRace> raceItemsInSeries = raceItemsByHeader.get(seriesHeader);
            if (raceItemsInSeries == null) {
                raceItemsInSeries = new ArrayList<>();
                raceItemsByHeader.put(seriesHeader, raceItemsInSeries);
            }
            raceItemsInSeries.add(viewItemsRaces.get(race));
        }
        return raceItemsByHeader;
    }

    public void setSelectedRace(RaceListDataType id) {
        mSelectedRace = id;
    }

    private void findViews(View layout) {
        panel_left = ViewHelper.get(layout, R.id.panel_left);
        panel_right = ViewHelper.get(layout, R.id.panel_right);
        marker = ViewHelper.get(layout, R.id.race_marker);
        current_flag = ViewHelper.get(layout, R.id.current_flag);
        update_badge = ViewHelper.get(layout, R.id.update_badge);
        race_flag = ViewHelper.get(layout, R.id.race_flag);
        time = ViewHelper.get(layout, R.id.time);
        race_name = ViewHelper.get(layout, R.id.race_name);
        race_finished = ViewHelper.get(layout, R.id.race_finished);
        race_started = ViewHelper.get(layout, R.id.race_started);
        race_scheduled = ViewHelper.get(layout, R.id.race_scheduled);
        race_unscheduled = ViewHelper.get(layout, R.id.race_unscheduled);
        flag_timer = ViewHelper.get(layout, R.id.flag_timer);
        protest_image = ViewHelper.get(layout, R.id.protest_image);
        boat_class = ViewHelper.get(layout, R.id.boat_class);
        fleet_series = ViewHelper.get(layout, R.id.fleet_series);
        has_dependent_races = ViewHelper.get(layout, R.id.has_dependent_races);
        depends_on = ViewHelper.get(layout, R.id.depends_on);
        explicit_factor = ViewHelper.get(layout, R.id.explicit_factor);
    }

    private void resetValues(View layout) {
        if (layout != null) {
            if (panel_left != null) {
                panel_left.setVisibility(View.VISIBLE);
            }
            if (panel_right != null) {
                panel_right.setVisibility(View.VISIBLE);
            }
            if (update_badge != null) {
                update_badge.setVisibility(View.GONE);
            }
            if (race_flag != null) {
                race_flag.setVisibility(View.GONE);
            }
            if (time != null) {
                time.setVisibility(View.VISIBLE);
            }
            if (race_started != null) {
                race_started.setText("");
            }
            if (race_finished != null) {
                race_finished.setVisibility(View.GONE);
            }
            if (race_scheduled != null) {
                race_scheduled.setVisibility(View.VISIBLE);
            }
            if (race_unscheduled != null) {
                race_unscheduled.setVisibility(View.GONE);
            }
            if (race_name != null) {
                race_name.setTextColor(ThemeHelper.getColor(getContext(), R.attr.sap_light_gray));
            }
            if (has_dependent_races != null) {
                has_dependent_races.setVisibility(View.GONE);
            }
            if (depends_on != null) {
                depends_on.setTextColor(ThemeHelper.getColor(getContext(), R.attr.sap_light_gray));
                depends_on.setVisibility(View.GONE);
            }
            if (explicit_factor != null) {
                explicit_factor.setVisibility(View.GONE);
            }
            setMarker(0);
        }
    }

    private void updateFlag(ManagedRace race, TimePoint now) {
        RaceState state = race.getState();
        if (state == null || state.getStartTime() == null) {
            return;
        }

        RacingProcedure procedure = state.getTypedRacingProcedure();
        LayerDrawable flag = null;
        Drawable arrow = null;
        String timer = null;
        if (!procedure.isIndividualRecallDisplayed()) {
            FlagPoleState poleState = state.getRacingProcedure().getActiveFlags(state.getStartTime(), now);
            List<FlagPole> currentState = poleState.getCurrentState();
            List<FlagPole> upcoming = poleState.computeUpcomingChanges();
            FlagPole nextPole = FlagPoleState.getMostInterestingFlagPole(upcoming);
            TimePoint change = poleState.getNextStateValidFrom();
            Flags currentFlag;

            if (change != null) {
                for (FlagPole pole : currentState) {
                    int isNext = 0;

                    currentFlag = pole.getUpperFlag();
                    if (isNextFlag(currentFlag, nextPole)) {
                        isNext = 1;
                    } else {
                        currentFlag = pole.getLowerFlag();
                        if (!Flags.NONE.equals(currentFlag)) {
                            if (isNextFlag(currentFlag, nextPole)) {
                                isNext = 2;
                            }
                        }
                    }

                    if (isNext != 0) {
                        flag = FlagsResources.getFlagDrawable(getContext(), currentFlag.name(), flag_size);
                        switch (isNext) {
                            case 1:
                                if (nextPole.isDisplayed()) {
                                    arrow = BitmapHelper.getAttrDrawable(getContext(), R.attr.arrow_up);
                                } else {
                                    arrow = BitmapHelper.getAttrDrawable(getContext(), R.attr.arrow_down);
                                }
                                break;

                            case 2:
                                arrow = BitmapHelper.getAttrDrawable(getContext(), R.attr.arrow_up);
                                break;

                            default:
                                ExLog.i(getContext(), TAG, "unknown flag");
                        }
                        timer = TimeUtils.formatDuration(now, poleState.getNextStateValidFrom());
                    }
                }
            }
        } else {
            TimePoint flagDown = procedure.getIndividualRecallRemovalTime();
            if (now.before(flagDown)) {
                flag = FlagsResources.getFlagDrawable(getContext(), Flags.XRAY.name(), flag_size);
                arrow = BitmapHelper.getAttrDrawable(getContext(), R.attr.arrow_down);
                timer = TimeUtils.formatDuration(now, flagDown);
            }
        }
        if (timer != null) {
            timer = timer.replace("-", "");
        }
        showFlag(flag, arrow, timer);
    }

    private boolean isNextFlag(Flags flag, FlagPole pole) {
        return pole != null && flag.equals(pole.getUpperFlag());
    }

    private void showFlag(LayerDrawable flag, Drawable arrow, String timer) {
        if (flag != null && arrow != null && timer != null) {
            current_flag.setImageDrawable(flag);
            flag_timer.setText(timer);
            flag_timer.setCompoundDrawablesWithIntrinsicBounds(arrow, null, null, null);
            race_flag.setVisibility(View.VISIBLE);
        }
    }

    private void setMarker(int level) {
        if (marker != null) {
            Drawable drawable = marker.getDrawable();
            if (drawable != null) {
                drawable.setLevel(level);
            }
        }
    }

    private int getLevel() {
        int level = 0;
        if (marker != null) {
            Drawable drawable = marker.getDrawable();
            if (drawable != null) {
                level = drawable.getLevel();
            }
        }
        return level;
    }

    private enum ViewType {
        HEADER(0), RACE(1);

        public final int index;

        ViewType(int index) {
            this.index = index;
        }
    }
}
