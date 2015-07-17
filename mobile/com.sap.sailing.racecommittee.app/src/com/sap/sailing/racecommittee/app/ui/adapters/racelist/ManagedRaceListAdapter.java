package com.sap.sailing.racecommittee.app.ui.adapters.racelist;

import java.text.SimpleDateFormat;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.ViewHolder;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.StartTimeFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.StartTimeFinderResult;
import com.sap.sailing.domain.abstractlog.race.state.RaceState;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.FlagPoleState;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.common.racelog.FlagPole;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.AndroidRaceLogResolver;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.ui.adapters.racelist.RaceFilter.FilterSubscriber;
import com.sap.sailing.racecommittee.app.ui.utils.FlagsResources;
import com.sap.sailing.racecommittee.app.utils.BitmapHelper;
import com.sap.sailing.racecommittee.app.utils.RaceHelper;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class ManagedRaceListAdapter extends ArrayAdapter<RaceListDataType> implements FilterSubscriber {

    private final static String TAG = ManagedRaceListAdapter.class.getName();
    private final static int FLAG_SIZE = 48;
    private final Object mLockObject = new Object();
    private List<RaceListDataType> mAllViewItems;
    private RaceFilter mFilter;
    private LayoutInflater mInflater;
    private Resources mResources;
    private List<RaceListDataType> mShownViewItems;
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

    public ManagedRaceListAdapter(Context context, List<RaceListDataType> viewItems) {
        super(context, 0);

        mAllViewItems = viewItems;
        mShownViewItems = viewItems;
        mInflater = LayoutInflater.from(getContext());
        mResources = getContext().getResources();
        dateFormat = new SimpleDateFormat("kk:mm", getContext().getResources().getConfiguration().locale);
    }

    @Override
    public int getCount() {
        synchronized (mLockObject) {
            return mShownViewItems != null ? mShownViewItems.size() : 0;
        }
    }

    @Override
    public RaceFilter getFilter() {
        if (mFilter == null) {
            mFilter = new RaceFilter(mAllViewItems, this);
        }
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
        return (getItem(position) instanceof RaceListDataTypeHeader ? ViewType.HEADER.index : ViewType.RACE.index);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RaceListDataType raceListElement;
        raceListElement = getItem(position);

        int type = getItemViewType(position);
        TimePoint now = MillisecondsTimePoint.now();

        if (convertView == null) {
            if (type == ViewType.HEADER.index) {
                convertView = mInflater.inflate(R.layout.race_list_area_header, parent, false);
            } else if (type == ViewType.RACE.index) {
                convertView = mInflater.inflate(R.layout.race_list_area_item, parent, false);
            }
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
            fleet_series.setText(RaceHelper.getFleetSeries(header.getFleet(), header.getSeries()));
            protest_image.setImageDrawable(FlagsResources.getFlagDrawable(getContext(), Flags.BRAVO.name(), FLAG_SIZE));
            protest_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(AppConstants.INTENT_ACTION_SHOW_PROTEST);
                    intent.putExtra(AppConstants.INTENT_ACTION_EXTRA, header.toString());
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
                }
            });
        } else if (type == ViewType.RACE.index) {
            final RaceListDataTypeRace race = (RaceListDataTypeRace) raceListElement;

            if (convertView != null) {
                if (mSelectedRace != null && mSelectedRace.equals(race)) {
                    setMarker(1 - getLevel());
                    convertView.setBackgroundColor(ThemeHelper.getColor(getContext(), R.attr.sap_gray_black_20));

                    if (race.isUpdateIndicatorVisible()) {
                        race.setUpdateIndicatorVisible(false);
                    }
                } else {
                    convertView.setBackgroundColor(ThemeHelper.getColor(getContext(), R.attr.sap_gray));

                    if (race.isUpdateIndicatorVisible()) {
                        update_badge.setVisibility(View.VISIBLE);
                    }
                }
            }

            if (!TextUtils.isEmpty(race.getRaceName())) {
                race_name.setText(race.getRaceName());
            }
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
                        time.setTextSize(textSize);
                    }
                    StartTimeFinder stf = new StartTimeFinder(new AndroidRaceLogResolver(), race.getRace().getRaceLog());
                    StartTimeFinderResult result = stf.analyze();
                    if (result != null && result.isDependentStartTime()) {
                        has_dependent_races.setVisibility(View.VISIBLE);
                    }
                }
                if (state.getFinishedTime() != null) {
                    time.setVisibility(View.GONE);
                    race_finished.setVisibility(View.VISIBLE);
                    race_finished.setText(mResources.getString(R.string.race_finished, dateFormat.format(state.getFinishedTime().asDate())));
                }
                if (state.getStartTime() == null && state.getFinishedTime() == null) {
                    switch (race.getRace().getStatus()) {
                        case PRESCHEDULED:
                            race_scheduled.setVisibility(View.GONE);
                            race_unscheduled.setVisibility(View.GONE);
                            if (depends_on != null) {
                                StartTimeFinder stf = new StartTimeFinder(new AndroidRaceLogResolver(), race.getRace().getRaceLog());
                                StartTimeFinderResult result = stf.analyze();
                                if (result != null && result.isDependentStartTime()) {
                                    SimpleRaceLogIdentifier identifier = Util.get(result.getRacesDependingOn(), 0);
                                    ManagedRace tmp = DataManager.create(getContext()).getDataStore().getRace(identifier);
                                    depends_on.setText(getContext()
                                        .getString(R.string.minutes_after_long, result.getStartTimeDiff().asMinutes(), RaceHelper
                                            .getRaceName(tmp, " / ")));
                                    depends_on.setVisibility(View.VISIBLE);
                                }
                            }
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

            updateFlag(race.getRace(), now);
        }
        return convertView;
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
    public void onResult(List<RaceListDataType> filtered) {
        synchronized (mLockObject) {
            mShownViewItems = filtered;
            notifyDataSetChanged();
        }
    }

    public void setSelectedRace(RaceListDataType id) {
        mSelectedRace = id;
    }

    private void findViews(View layout) {
        marker = ViewHolder.get(layout, R.id.race_marker);
        current_flag = ViewHolder.get(layout, R.id.current_flag);
        update_badge = ViewHolder.get(layout, R.id.update_badge);
        race_flag = ViewHolder.get(layout, R.id.race_flag);
        time = ViewHolder.get(layout, R.id.time);
        race_name = ViewHolder.get(layout, R.id.race_name);
        race_finished = ViewHolder.get(layout, R.id.race_finshed);
        race_started = ViewHolder.get(layout, R.id.race_started);
        race_scheduled = ViewHolder.get(layout, R.id.race_scheduled);
        race_unscheduled = ViewHolder.get(layout, R.id.race_unscheduled);
        flag_timer = ViewHolder.get(layout, R.id.flag_timer);
        protest_image = ViewHolder.get(layout, R.id.protest_image);
        boat_class = ViewHolder.get(layout, R.id.boat_class);
        fleet_series = ViewHolder.get(layout, R.id.fleet_series);
        has_dependent_races = ViewHolder.get(layout, R.id.has_dependent_races);
        depends_on = ViewHolder.get(layout, R.id.depends_on);
    }

    private void resetValues(View layout) {
        if (layout != null) {
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
                        flag = FlagsResources.getFlagDrawable(getContext(), currentFlag.name(), FLAG_SIZE);
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
                flag = FlagsResources.getFlagDrawable(getContext(), Flags.XRAY.name(), FLAG_SIZE);
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
