package com.sap.sailing.racecommittee.app.ui.adapters.racelist;

import com.sap.sailing.domain.base.SeriesBase;
import com.sap.sailing.domain.base.racegroup.IsFleetFragment;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.domain.base.racegroup.RaceGroupSeries;
import com.sap.sailing.racecommittee.app.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RaceListDataTypeHeader implements RaceListDataType, IsFleetFragment {
    private RaceGroupSeries data;
    private boolean hasConflict;
    private final LayoutInflater mInflater;

    public RaceListDataTypeHeader(RaceGroupSeries data, LayoutInflater layoutInflater, boolean hasConflicts) {
        this.data = data;
        this.mInflater = layoutInflater;
        this.hasConflict = hasConflicts;
    }

    public RaceGroupSeries getRegattaSeries() {
        return data;
    }

    public RaceGroup getRaceGroup() {
        return data.getRaceGroup();
    }

    public SeriesBase getSeries() {
        return data.getSeries();
    }

    public void setHasConflict(boolean hasConflict) {
        this.hasConflict = hasConflict;
    }

    public boolean hasConflict() {
        return hasConflict;
    }

    @Override
    public String toString() {
        return data.getDisplayName();
    }

    @Override
    public View getView(ViewGroup parent) {
        return mInflater.inflate(R.layout.race_list_area_header, parent, false);
    }

}
