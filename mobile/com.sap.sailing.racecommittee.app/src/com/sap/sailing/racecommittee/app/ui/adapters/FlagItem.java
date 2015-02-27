package com.sap.sailing.racecommittee.app.ui.adapters;

import com.sap.sailing.domain.common.racelog.Flags;

public abstract class FlagItem {

    public String first_line;
    public String second_line;
    public String file_name;
    public Flags flag;
    public Boolean touched = false;

    public FlagItem(String line1, String line2, String fileName, Flags flag) {
        this.first_line = line1;
        this.second_line = line2;
        this.file_name = fileName;
        this.flag = flag;
    }
}
