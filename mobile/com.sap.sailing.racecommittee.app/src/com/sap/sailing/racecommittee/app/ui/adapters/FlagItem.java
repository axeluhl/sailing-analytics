package com.sap.sailing.racecommittee.app.ui.adapters;

public abstract class FlagItem {

    public String first_line;
    public String second_line;
    public String flag;
    public Boolean touched = false;

    public FlagItem(String line1, String line2, String flag) {
        this.first_line = line1;
        this.second_line = line2;
        this.flag = flag;
    }
}
