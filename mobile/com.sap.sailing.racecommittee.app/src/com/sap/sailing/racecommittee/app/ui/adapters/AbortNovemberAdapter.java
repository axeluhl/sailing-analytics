package com.sap.sailing.racecommittee.app.ui.adapters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.sap.sailing.domain.abstractlog.race.analyzing.impl.StartTimeFinderResult;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.utils.RaceHelper;
import com.sap.sse.common.Util;

public class AbortNovemberAdapter extends RecyclerView.Adapter<AbortNovemberAdapter.ViewHolder> {

    private List<ManagedRace> mRaces = new ArrayList<>();
    private List<ManagedRace> mSelected = new ArrayList<>();
    private ManagedRace mCurrentRace;

    public AbortNovemberAdapter(LinkedHashMap<String, ManagedRace> races, ManagedRace currentRace) {
        mCurrentRace = currentRace;
        mSelected.add(mCurrentRace);
        Iterator<String> iterator = races.keySet().iterator();
        ManagedRace race;
        while (iterator.hasNext()) {
            race = races.get(iterator.next());
            mRaces.add(race);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.race_november_item, parent, false);
        return new ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final ManagedRace race = mRaces.get(position);
        boolean selected = (getRace(race) != null);

        holder.race.setText(RaceHelper.getShortReverseRaceName(race, " / ", mCurrentRace));
        holder.race.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.selected.setChecked(!holder.selected.isChecked());
            }
        });
        holder.selected.setChecked(selected);
        holder.selected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                remove(race);
                if (isChecked) {
                    int pos = getParentPos(race);
                    if (pos == mSelected.size()) {
                        pos = getFirstChild(race);
                    }
                    mSelected.add(pos, race);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mRaces.size();
    }

    public List<ManagedRace> getSelected() {
        return mSelected;
    }

    private int getFirstChild(ManagedRace race) {
        int pos = 0;
        for (ManagedRace item : mSelected) {
            StartTimeFinderResult result = item.getState().getStartTimeFinderResult();
            if (result.isDependentStartTime()) {
                if (RaceHelper.getSimpleRaceLogIdentifier(race).equals(Util.get(result.getRacesDependingOn(), 0))) {
                    return pos;
                }
            }
            pos++;
        }
        return pos;
    }

    private int getParentPos(ManagedRace race) {
        int pos = 0;
        StartTimeFinderResult result = race.getState().getStartTimeFinderResult();
        if (result.isDependentStartTime()) {
            for (ManagedRace item : mSelected) {
                pos++;
                if (RaceHelper.getSimpleRaceLogIdentifier(item).equals(Util.get(result.getRacesDependingOn(), 0))) {
                    return pos;
                }
            }
        }
        return pos;
    }

    private boolean remove(ManagedRace race) {
        for (ManagedRace item : mSelected) {
            if (item.equals(race)) {
                mSelected.remove(item);
                return true;
            }
        }
        return false;
    }

    private ManagedRace getRace(ManagedRace race) {
        for (ManagedRace item : mSelected) {
            if (item.equals(race)) {
                return item;
            }
        }
        return null;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView race;
        public CheckBox selected;

        public ViewHolder(View itemView) {
            super(itemView);

            race = (TextView) itemView.findViewById(R.id.abort_text);
            selected = (CheckBox) itemView.findViewById(R.id.abort_checkbox);
        }
    }
}
