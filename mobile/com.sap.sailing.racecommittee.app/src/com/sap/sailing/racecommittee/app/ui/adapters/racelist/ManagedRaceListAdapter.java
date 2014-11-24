package com.sap.sailing.racecommittee.app.ui.adapters.racelist;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.sap.sailing.domain.racelog.state.RaceState;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.impl.BoatClassSeriesFleet;
import com.sap.sailing.racecommittee.app.ui.adapters.racelist.RaceFilter.FilterSubscriber;
import com.sap.sailing.racecommittee.app.utils.TickListener;

public class ManagedRaceListAdapter extends ArrayAdapter<RaceListDataType> implements FilterSubscriber {

    public interface JuryFlagClickedListener {
        public void onJuryFlagClicked(BoatClassSeriesFleet clickedItem);
    }

    private class TimeListener implements TickListener {

        private RaceState state;
        private ViewHolder viewHolder;
        
        public TimeListener(ViewHolder viewHolder, RaceState state) {
            this.viewHolder = viewHolder;
            this.state = state;
        }
        
        @Override
        public void notifyTick() {
            if (viewHolder != null && viewHolder.time != null) {
                if (state != null && state.getStartTime() != null) {
                    viewHolder.time.setText(getDuration(state.getStartTime().asDate(), Calendar.getInstance().getTime()));
                }
            }
        }
    }

    private class ViewHolder {
        TextView area_name;
        ImageView current_flag;
        
        TextView flag_timer;
        
        TextView group_name;
        RaceListDataTypeHeader header;
        
        RaceListDataTypeRace race;
        Spinner race_filter;
        TextView race_finished;
        TextView race_name;
        
        TextView race_started;
        TextView time;
        LinearLayout race_scheduled;
        TextView race_unscheduled;
        Boolean unscheduled;
        
        ImageView update_badge;
    }

    private enum ViewType {
        HEADER(0), RACE(1);

        public final int index;

        ViewType(int index) {
            this.index = index;
        }
    }

    private Collection<RaceListDataType> allViewItems;
    private RaceFilter filter;
    private JuryFlagClickedListener juryListener;

    private final Object lockObject = new Object();
    private LayoutInflater mInflater;

    private List<RaceListDataType> shownViewItems;

    public ManagedRaceListAdapter(Context context, List<RaceListDataType> viewItems,
            JuryFlagClickedListener juryListener) {
        super(context, 0);
        this.allViewItems = viewItems;
        this.shownViewItems = viewItems;

        this.juryListener = juryListener;
        this.mInflater = (LayoutInflater) (getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE));
    }

    public String fill2(int value)
    { 
        String erg = String.valueOf(value);
     
        if (erg.length() < 2)
            erg = "0" + erg;            
        return erg;
    }

    @Override
    public int getCount() {
        synchronized (lockObject) {
            return shownViewItems != null ? shownViewItems.size() : 0;
        }
    }

    public String getDuration(Date date1, Date date2)
    {                    
        TimeUnit timeUnit = TimeUnit.SECONDS;
     
        long diffInMilli = date2.getTime() - date1.getTime();
        long s = timeUnit.convert(diffInMilli, TimeUnit.MILLISECONDS);
     
        long days = s / (24 * 60 * 60);
        long rest = s - (days * 24 * 60 * 60);
        long hrs =  rest / (60 * 60);
        long rest1 = rest - (hrs * 60 * 60);
        long min = rest1 / 60;      
        long sec = s % 60;
     
        String dates = "";
        if (days > 0) dates = days + " Days ";
     
        dates += fill2((int) hrs) + "h ";
        dates += fill2((int) min) + "m ";
        dates += fill2((int) sec) + "s ";
     
        return dates;
    }

    @Override
    public RaceFilter getFilter() {
        if (filter == null) {
            filter = new RaceFilter(allViewItems, this);
        }
        return filter;
    }

    @Override
    public RaceListDataType getItem(int position) {
        synchronized (lockObject) {
            return shownViewItems != null ? shownViewItems.get(position) : null;
        }
    }

    public List<RaceListDataType> getItems() {
        return shownViewItems;
    }

    @Override
    public int getItemViewType(int position) {
        return (getItem(position) instanceof RaceListDataTypeHeader ? ViewType.HEADER.index : ViewType.RACE.index);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RaceListDataType raceListElement = null;

        raceListElement = getItem(position);

        ViewHolder holder = null;
        int type = getItemViewType(position);

        if (convertView == null) {
            holder = new ViewHolder();
            if (type == ViewType.HEADER.index) {
                convertView = mInflater.inflate(R.layout.race_list_area_header, parent, false);
                holder.area_name = (TextView) convertView.findViewById(R.id.area_name);
                holder.race_filter = (Spinner) convertView.findViewById(R.id.race_filter);
            } else if (type == ViewType.RACE.index) {
                convertView = mInflater.inflate(R.layout.race_list_area_item, parent, false);
                holder.group_name = (TextView) convertView.findViewById(R.id.group_name);
                holder.race_name = (TextView) convertView.findViewById(R.id.race_name);
                holder.current_flag = (ImageView) convertView.findViewById(R.id.current_flag);
                holder.flag_timer = (TextView) convertView.findViewById(R.id.flag_timer);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                holder.race_started = (TextView) convertView.findViewById(R.id.race_started);
                holder.race_finished = (TextView) convertView.findViewById(R.id.race_finshed);
                holder.race_scheduled = (LinearLayout) convertView.findViewById(R.id.race_scheduled);
                holder.race_unscheduled = (TextView) convertView.findViewById(R.id.race_unscheduled);
                holder.update_badge = (ImageView) convertView.findViewById(R.id.update_badge);
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (type == ViewType.HEADER.index) {
            holder.header = (RaceListDataTypeHeader) raceListElement;
            // TODO
        } else if (type == ViewType.RACE.index) {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm", getContext().getResources().getConfiguration().locale);
            final RaceListDataTypeRace race = (RaceListDataTypeRace) raceListElement;
            holder.race = race;
            resetValues(holder);
            
            if (!TextUtils.isEmpty(race.getRaceName())) {
                holder.race_name.setText(race.getRaceName());
            }
            if (race.getRace().getState().getStartTime() != null) {
                holder.race_started.setText(format.format(race.getRace().getState().getStartTime().asDate()));
            }
            if (race.getRace().getState().getFinishedTime() != null) {
                holder.time.setVisibility(View.GONE);
                holder.race_finished.setVisibility(View.VISIBLE);
                holder.race_finished.setText(format.format(race.getRace().getState().getFinishedTime().asDate()));
            }
            if (race.getRace().getState().getStartTime() == null && race.getRace().getState().getFinishedTime() == null) {
                holder.race_scheduled.setVisibility(View.GONE);
                holder.race_unscheduled.setVisibility(View.VISIBLE);
            }
            if (race.isUpdateIndicatorVisible()) {
                holder.update_badge.setVisibility(View.VISIBLE);
            }
        }

        return convertView;
    }

    private void resetValues(ViewHolder holder) {
        holder.time.setVisibility(View.VISIBLE);
        holder.race_started.setText("");
        holder.race_finished.setText("");
        holder.race_finished.setVisibility(View.GONE);
        holder.race_scheduled.setVisibility(View.VISIBLE);
        holder.race_unscheduled.setVisibility(View.GONE);
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
        synchronized (lockObject) {
            shownViewItems = filtered;
            notifyDataSetChanged();
        }
    }
}
