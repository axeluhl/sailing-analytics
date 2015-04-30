package com.sap.sailing.racecommittee.app.ui.adapters.racelist;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.sap.sailing.android.shared.util.ViewHolder;
import com.sap.sailing.domain.abstractlog.race.state.RaceState;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.FlagPoleState;
import com.sap.sailing.domain.common.racelog.FlagPole;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.impl.BoatClassSeriesFleet;
import com.sap.sailing.racecommittee.app.ui.adapters.racelist.RaceFilter.FilterSubscriber;
import com.sap.sailing.racecommittee.app.ui.utils.FlagsResources;
import com.sap.sailing.racecommittee.app.utils.BitmapHelper;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ManagedRaceListAdapter extends ArrayAdapter<RaceListDataType> implements FilterSubscriber {

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
    private ImageView current_flag;
    private TextView race_name;
    private TextView flag_timer;
    private ImageView arrow_direction;
    private TextView boat_class;
    private TextView fleet_series;
    private ImageView protest_image;
    private SimpleDateFormat dateFormat;
    private RaceListDataType mSelectedRace;

    public ManagedRaceListAdapter(Context context, List<RaceListDataType> viewItems,
        JuryFlagClickedListener juryListener) {
        super(context, 0);

        mAllViewItems = viewItems;
        mShownViewItems = viewItems;
        mInflater = LayoutInflater.from(getContext());
        mResources = getContext().getResources();
        dateFormat = new SimpleDateFormat("HH:mm", getContext().getResources().getConfiguration().locale);
    }

    public String fill2(int value) {
        String erg = String.valueOf(value);

        if (erg.length() < 2) {
            erg = "0" + erg;
        }
        return erg;
    }

    @Override
    public int getCount() {
        synchronized (mLockObject) {
            return mShownViewItems != null ? mShownViewItems.size() : 0;
        }
    }

    public String getDuration(Date date1, Date date2) {
        TimeUnit timeUnit = TimeUnit.SECONDS;

        long diffInMilli = date2.getTime() - date1.getTime();
        long s = timeUnit.convert(diffInMilli, TimeUnit.MILLISECONDS);

        long days = s / (24 * 60 * 60);
        long rest = s - (days * 24 * 60 * 60);
        long hrs = rest / (60 * 60);
        long rest1 = rest - (hrs * 60 * 60);
        long min = rest1 / 60;
        long sec = s % 60;

        String dates = "";
        if (days < 0 || hrs < 0 || min < 0 || sec < 0) {
            dates += "-";
            if (days < 0) {
                days *= -1;
            }
            if (hrs < 0) {
                hrs *= -1;
            }
            if (min < 0) {
                min *= -1;
            }
            if (sec < 0) {
                sec *= -1;
            }
        }
        if (days != 0) {
            dates = days + ":";
        }

        dates += (days != 0 || hrs != 0) ? fill2((int) hrs) + ":" : "";
        dates += fill2((int) min) + ":";
        dates += fill2((int) sec);

        return dates;
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
            boat_class.setText(header.getBoatClass().getName());
            String fleetSeries = "";
            if (header.getFleet() != null && !header.getFleet().getName().equals("Default")) {
                fleetSeries += header.getFleet().getName();
            }
            if (header.getSeries() != null && !header.getSeries().getName().equals("Default")) {
                if (!TextUtils.isEmpty(fleetSeries)) {
                    fleetSeries += " - ";
                }
                fleetSeries += header.getSeries().getName();
            }
            fleet_series.setText(fleetSeries);
            protest_image.setImageDrawable(FlagsResources.getFlagDrawable(getContext(), Flags.BRAVO.name(), 48));
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
                } else {
                    convertView.setBackgroundColor(ThemeHelper.getColor(getContext(), R.attr.sap_gray));
                }
            }

            if (!TextUtils.isEmpty(race.getRaceName())) {
                race_name.setText(race.getRaceName());
            }
            RaceState state = race.getRace().getState();
            if (state != null) {
                if (state.getStartTime() != null) {
                    race_started.setText(
                        mResources.getString(R.string.race_started, dateFormat.format(state.getStartTime().asDate())));
                    if (state.getFinishedTime() == null) {
                        String duration = getDuration(state.getStartTime().asDate(), Calendar.getInstance().getTime());
                        time.setText(duration);
                        float textSize = getContext().getResources().getDimension(R.dimen.textSize_40);
                        if (!TextUtils.isEmpty(duration) && duration.length() >= 6) {
                            textSize = getContext().getResources().getDimension(R.dimen.textSize_32);
                        }
                        time.setTextSize(textSize);
                    }
                }
                if (state.getFinishedTime() != null) {
                    time.setVisibility(View.GONE);
                    race_finished.setVisibility(View.VISIBLE);
                    race_finished.setText(mResources
                        .getString(R.string.race_finished, dateFormat.format(state.getFinishedTime().asDate())));
                }
                if (state.getStartTime() == null && state.getFinishedTime() == null) {
                    race_scheduled.setVisibility(View.GONE);
                    race_unscheduled.setVisibility(View.VISIBLE);
                } else {
                    if (race_name != null) {
                        race_name.setTextColor(ThemeHelper.getColor(getContext(), R.attr.white));
                    }
                }
            }
            updateFlag(race.getRace());
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

    private void findViews(View convertView) {
        marker = ViewHolder.get(convertView, R.id.race_marker);
        arrow_direction = ViewHolder.get(convertView, R.id.arrow_direction);
        current_flag = ViewHolder.get(convertView, R.id.current_flag);
        update_badge = ViewHolder.get(convertView, R.id.update_badge);
        race_flag = ViewHolder.get(convertView, R.id.race_flag);
        time = ViewHolder.get(convertView, R.id.time);
        race_name = ViewHolder.get(convertView, R.id.race_name);
        race_finished = ViewHolder.get(convertView, R.id.race_finshed);
        race_started = ViewHolder.get(convertView, R.id.race_started);
        race_scheduled = ViewHolder.get(convertView, R.id.race_scheduled);
        race_unscheduled = ViewHolder.get(convertView, R.id.race_unscheduled);
        flag_timer = ViewHolder.get(convertView, R.id.flag_timer);
        protest_image = ViewHolder.get(convertView, R.id.protest_image);
        boat_class = ViewHolder.get(convertView, R.id.boat_class);
        fleet_series = ViewHolder.get(convertView, R.id.fleet_series);
    }

    private void resetValues(View convertView) {
        if (convertView != null) {
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
            setMarker(0);
        }
    }

    private void updateFlag(ManagedRace race) {
        RaceState state = race.getState();
        if (state == null || state.getStartTime() == null) {
            return;
        }
        FlagPoleState flagPoleState = state.getTypedRacingProcedure()
            .getActiveFlags(state.getStartTime(), MillisecondsTimePoint.now());
        List<FlagPole> flagChanges = flagPoleState.computeUpcomingChanges();
        if (!flagChanges.isEmpty()) {
            TimePoint changeAt = flagPoleState.getNextStateValidFrom();
            FlagPole changePole = FlagPoleState.getMostInterestingFlagPole(flagChanges);

            if (changeAt != null) {
                current_flag.setImageDrawable(
                    FlagsResources.getFlagDrawable(getContext(), changePole.getUpperFlag().name(), 48));
                String text = getDuration(changeAt.asDate(), Calendar.getInstance().getTime());
                flag_timer.setText(text.replace("-", ""));
                Drawable arrow;
                if (changePole.isDisplayed()) {
                    arrow = BitmapHelper.getAttrDrawable(getContext(), R.attr.arrow_up);
                } else {
                    arrow = BitmapHelper.getAttrDrawable(getContext(), R.attr.arrow_down);
                }
                arrow_direction.setImageDrawable(arrow);
                race_flag.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setMarker(int level) {
        if (marker != null) {
            Drawable drawable = marker.getDrawable();
            if (drawable != null) {
                drawable.setLevel(level + ThemeHelper.getThemeOffset(getContext()));
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

    public interface JuryFlagClickedListener {
        void onJuryFlagClicked(BoatClassSeriesFleet clickedItem);
    }
}
