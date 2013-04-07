package com.sap.sailing.racecommittee.app.ui.adapters.racelist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.comparators.RaceListDataTypeElementComparator;
import com.sap.sailing.racecommittee.app.ui.comparators.RaceListDataTypeTitleComparator;

public class RaceListAdapter extends ArrayAdapter<RaceListDataType> {

    public interface JuryFlagClickedListener {
        public void onJuryFlagClicked(BoatClassSeriesDataFleet clickedItem);
    }

    public static final int VIEW_TYPE_COUNT = 2;
    public static final int VIEW_BOAT_GROUP = 0;
    public static final int VIEW_RACE = 1;

    private LayoutInflater mInflater;
    private JuryFlagClickedListener juryListener;
    private RaceFilter filter;
    private final Object mLock = new Object();

    private List<RaceListDataType> items;
    private List<RaceListDataType> originalItems;

    public RaceListAdapter(Context context, int textViewResourceId, 
            List<RaceListDataType> objects, JuryFlagClickedListener juryListener) {
        super(context, textViewResourceId, objects);
        this.items = objects;
        this.originalItems = new ArrayList<RaceListDataType>();

        this.juryListener = juryListener;
        mInflater = (LayoutInflater) (getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE));
    }

    @Override
    public int getCount() {
        synchronized(mLock) {
            return items!=null ? items.size() : 0;  
        }
    }

    @Override
    public RaceListDataType getItem(int position) {
        RaceListDataType element = null;
        synchronized(mLock) {
            element = items!=null ? items.get(position) : null;
        }
        return element;
    }

    @Override
    public int getItemViewType(int position) {
        return (getItem(position) instanceof RaceListDataTypeTitle ? VIEW_BOAT_GROUP
                : VIEW_RACE);
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public boolean isEnabled(int position) {
        return (getItem(position) instanceof RaceListDataTypeElement);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RaceListDataType raceListElement = null;

        raceListElement = getItem(position);

        ViewHolder holder = null;
        int type = getItemViewType(position);

        if (convertView == null) {
            holder = new ViewHolder();
            if (type == VIEW_BOAT_GROUP) {
                convertView = mInflater.inflate(R.layout.welter_one_row_no_image_label, null);
                holder.line1 = (TextView) convertView.findViewById(R.id.Welter_Cell_OneRowNoImageLabel_txtTitle);
                holder.button1 = (ImageButton) convertView.findViewById(R.id.Welter_Cell_OneRowNoImageLabel_btnJury);
            } else if (type == VIEW_RACE) {
                convertView = mInflater.inflate(R.layout.welter_two_row_no_image, null);
                holder.line1 = (TextView) convertView.findViewById(R.id.Welter_Cell_TwoRowNoImage_lineOne);
                holder.line2 = (TextView) convertView.findViewById(R.id.Welter_Cell_TwoRowNoImage_lineTwo);
                holder.updateLabel = (ImageView) convertView.findViewById(R.id.Welter_Cell_UpdateLabel);
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (type == VIEW_BOAT_GROUP) {
            final RaceListDataTypeTitle title = (RaceListDataTypeTitle) raceListElement;
            holder.line1.setText(title.toString());
            holder.button1.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    juryListener.onJuryFlagClicked(title.getBoatClassSeriesDataFleet());
                }
            });
        } else if (type == VIEW_RACE) {
            RaceListDataTypeElement element = (RaceListDataTypeElement) raceListElement;
            holder.line1.setText(element.getRaceName());
            holder.line2.setText(element.getStatus());

            if (element.isUpdateIndicatorVisible()) {
                holder.updateLabel.setVisibility(View.VISIBLE);
            }
        }

        return convertView;
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new RaceFilter();
        }
        return filter;
    }

    public List<RaceListDataType> getItems() {
        return items;
    }

    static class ViewHolder {
        TextView line1, line2;
        ImageButton button1;
        ImageView updateLabel;
    }

    private class RaceFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            // Initiate our results object
            FilterResults results = new FilterResults();
            List<RaceListDataType> dataTypeList = new ArrayList<RaceListDataType>();

            if (originalItems.isEmpty()) {
                originalItems = new ArrayList<RaceListDataType>(items.size());
                for (RaceListDataType type : items) {
                    originalItems.add(type);
                }
            }

            if (originalItems.isEmpty()) {
                results.values = items;
                results.count = items.size();
            } else {
                Map<RaceListDataTypeTitle, List<RaceListDataTypeElement>> elementMap
                = new TreeMap<RaceListDataTypeTitle, List<RaceListDataTypeElement>>(new RaceListDataTypeTitleComparator());
                RaceListDataTypeTitle currentTitle = null;

                for (RaceListDataType element : originalItems) {
                    if (element instanceof RaceListDataTypeTitle) {
                        currentTitle = (RaceListDataTypeTitle) element;
                        elementMap.put(currentTitle, new ArrayList<RaceListDataTypeElement>());
                    } else if (element instanceof RaceListDataTypeElement) {
                        RaceListDataTypeElement listElement = (RaceListDataTypeElement) element;
                        if (elementMap.containsKey(currentTitle)) {
                            elementMap.get(currentTitle).add(listElement);
                        }
                    }
                }

                for (RaceListDataTypeTitle title : elementMap.keySet()) {
                    dataTypeList.add(title);
                    List<RaceListDataTypeElement> elementList = elementMap.get(title);
                    Collections.sort(elementList, new RaceListDataTypeElementComparator());
                    RaceListDataTypeElement lastFinishedRace = null;
                    int numberOfUnscheduledRaces = 0;

                    for (RaceListDataTypeElement dataElement : elementList) {
                        if (dataElement.getRace().getStatus().equals(RaceLogRaceStatus.FINISHED)) {
                            lastFinishedRace = dataElement;
                        }
                    }

                    for (RaceListDataTypeElement dataElement : elementList) {
                        if (dataElement.getRace().getStatus().equals(RaceLogRaceStatus.UNSCHEDULED)) {
                            numberOfUnscheduledRaces++;
                            if (numberOfUnscheduledRaces > 1)
                                continue;
                        } else if (dataElement.getRace().getStatus().equals(RaceLogRaceStatus.FINISHED)) {
                            if (!dataElement.equals(lastFinishedRace)) {
                                continue;
                            }
                        }
                        dataTypeList.add(dataElement);

                    }
                }

                results.values = dataTypeList;
                results.count = dataTypeList.size();
            }
            return results;	
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            synchronized(mLock) {
                items = (List<RaceListDataType>) results.values;
                notifyDataSetChanged();
            }
        }

    }

}
