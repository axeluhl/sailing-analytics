package com.sap.sailing.racecommittee.app.ui.adapters.racelist;

import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.impl.BoatClassSeriesFleet;
import com.sap.sailing.racecommittee.app.ui.adapters.racelist.RaceFilter.FilterSubscriber;

public class ManagedRaceListAdapter extends ArrayAdapter<RaceListDataType> implements FilterSubscriber {

    public interface JuryFlagClickedListener {
        public void onJuryFlagClicked(BoatClassSeriesFleet clickedItem);
    }

    private class ViewHolder {
        TextView line1, line2;
        ImageButton button1;
        ImageView updateLabel;
    }

    private enum ViewType {
        HEADER(0), RACE(1);

        public final int index;

        ViewType(int index) {
            this.index = index;
        }
    }

    private final Object lockObject = new Object();

    private JuryFlagClickedListener juryListener;
    private LayoutInflater mInflater;
    private RaceFilter filter;

    private Collection<RaceListDataType> allViewItems;
    private List<RaceListDataType> shownViewItems;

    public ManagedRaceListAdapter(Context context, List<RaceListDataType> viewItems,
            JuryFlagClickedListener juryListener) {
        super(context, 0);
        this.allViewItems = viewItems;
        this.shownViewItems = viewItems;

        this.juryListener = juryListener;
        this.mInflater = (LayoutInflater) (getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE));
    }

    @Override
    public int getCount() {
        synchronized (lockObject) {
            return shownViewItems != null ? shownViewItems.size() : 0;
        }
    }

    @Override
    public RaceListDataType getItem(int position) {
        synchronized (lockObject) {
            return shownViewItems != null ? shownViewItems.get(position) : null;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (getItem(position) instanceof RaceListDataTypeHeader ? ViewType.HEADER.index : ViewType.RACE.index);
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
    public RaceFilter getFilter() {
        if (filter == null) {
            filter = new RaceFilter(allViewItems, this);
        }
        return filter;
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
                convertView = mInflater.inflate(R.layout.welter_one_row_no_image_label, null);
                holder.line1 = (TextView) convertView.findViewById(R.id.Welter_Cell_OneRowNoImageLabel_txtTitle);
                holder.button1 = (ImageButton) convertView.findViewById(R.id.Welter_Cell_OneRowNoImageLabel_btnJury);
            } else if (type == ViewType.RACE.index) {
                convertView = mInflater.inflate(R.layout.welter_two_row_no_image, null);
                holder.line1 = (TextView) convertView.findViewById(R.id.Welter_Cell_TwoRowNoImage_lineOne);
                holder.line2 = (TextView) convertView.findViewById(R.id.Welter_Cell_TwoRowNoImage_lineTwo);
                holder.updateLabel = (ImageView) convertView.findViewById(R.id.Welter_Cell_UpdateLabel);
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (type == ViewType.HEADER.index) {
            final RaceListDataTypeHeader title = (RaceListDataTypeHeader) raceListElement;
            holder.line1.setText(title.toString());
            holder.button1.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    juryListener.onJuryFlagClicked(title.getBoatClassSeriesDataFleet());
                }
            });
        } else if (type == ViewType.RACE.index) {
            RaceListDataTypeRace element = (RaceListDataTypeRace) raceListElement;
            holder.line1.setText(element.getRaceName());
            holder.line2.setText(element.getStatusText());

            if (element.isUpdateIndicatorVisible()) {
                holder.updateLabel.setVisibility(View.VISIBLE);
            }
        }

        return convertView;
    }

    public List<RaceListDataType> getItems() {
        return shownViewItems;
    }

    @Override
    public void onResult(List<RaceListDataType> filtered) {
        synchronized (lockObject) {
            shownViewItems = filtered;
            notifyDataSetChanged();
        }
    }

}
