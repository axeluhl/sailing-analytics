package com.sap.sailing.gwt.ui.shared.racemap;

import java.util.ArrayList;
import java.util.List;

public class RaceMapHelpLinesSettings {
    
    /**
     * Types of help lines on the map
     */
    public enum HelpLineTypes {
        STARTLINE, FINISHLINE, ADVANTAGELINE, // COURSEMIDDLELINE ..not supported yet
    }
    
    private ArrayList<HelpLineTypes> visibleHelpLines;

    /**
     * Creates new RaceMapHelpLinesSettings with the {@link HelpLineTypes} <code>STARTLINE</code>, <code>FINISHLINE</code> and <code>ADVANTAGELINE</code>.<br />
     */
    public RaceMapHelpLinesSettings() {
        visibleHelpLines = new ArrayList<HelpLineTypes>();
        visibleHelpLines.add(HelpLineTypes.STARTLINE);
        visibleHelpLines.add(HelpLineTypes.FINISHLINE);
        visibleHelpLines.add(HelpLineTypes.ADVANTAGELINE);
    }
    
    public RaceMapHelpLinesSettings(ArrayList<HelpLineTypes> visibleHelpLines) {
        this.visibleHelpLines = visibleHelpLines;
    }

    public List<HelpLineTypes> getVisibleHelpLines() {
        return visibleHelpLines;
    }
    
    public void setVisibleHelpLines(List<HelpLineTypes> visibleHelpLines) {
        this.visibleHelpLines = new ArrayList<HelpLineTypes>(visibleHelpLines);
    }

    public boolean containsHelpLine(HelpLineTypes helpLineType) {
        return visibleHelpLines.contains(helpLineType);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((visibleHelpLines == null) ? 0 : visibleHelpLines.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RaceMapHelpLinesSettings other = (RaceMapHelpLinesSettings) obj;
        if (visibleHelpLines == null) {
            if (other.visibleHelpLines != null)
                return false;
        } else if (!visibleHelpLines.equals(other.visibleHelpLines))
            return false;
        return true;
    }
}