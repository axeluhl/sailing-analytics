package com.sap.sailing.gwt.ui.regattaoverview;

import com.sap.sailing.domain.common.racelog.Flags;

public class FlagsMeaningExplanator {

    public static String getFlagsMeaning(Flags upperFlag, Flags lowerFlag, boolean displayed) {
        if (upperFlag.equals(Flags.CLASS) && lowerFlag.equals(Flags.GOLF) && displayed) {
            return "Flag displayed - five minutes till start - the startprocedure is a gate start";
        } else if (upperFlag.equals(Flags.CLASS) && displayed) {
            return "Flag displayed - five minutes till start";
        } else if (upperFlag.equals(Flags.PAPA) && displayed) {
            return "Start mode flag - four minutes till start";
        } else if (upperFlag.equals(Flags.BLACK) && displayed) {
            return "Start mode flag - disqualification for early starters - four minutes till start";
        } else if (upperFlag.equals(Flags.ZULU) && displayed) {
            return "Start mode flag - penalties for early starters - four minutes till start";
        } else if (upperFlag.equals(Flags.INDIA) && displayed) {
            return "Start mode flag - penalties for early starters - one minute till start";
        } else if (upperFlag.equals(Flags.PAPA) && !displayed) {
            return "Start mode flag - one minute till start";
        } else if (upperFlag.equals(Flags.BLACK) && !displayed) {
            return "Start mode flag - disqualification for early starters - one minute till start";
        } else if (upperFlag.equals(Flags.ZULU) && !displayed) {
            return "Start mode flag - start with penalties for early starters - one minute till start";
        } else if (upperFlag.equals(Flags.INDIA) && !displayed) {
            return "Start mode flag - start with penalties for early starters - one minute till start";
        } else if (upperFlag.equals(Flags.CLASS) && !displayed) {
            return "Flag removed - race starting signal";
        } else if (upperFlag.equals(Flags.XRAY)) {
            return "Individual recall - had early starters";
        } else if (upperFlag.equals(Flags.FIRSTSUBSTITUTE)) {
            return "General recall - had too many early starters - start procedure will be repeated";
        } else if (upperFlag.equals(Flags.BLUE) && displayed) {
            return "Flag displayed - first competitor passed the finishing line";
        } else if (upperFlag.equals(Flags.BLUE) && !displayed) {
            return "Flag removed - last competitor passed the finishing line or the finish time limit is passed";
        } else if (upperFlag.equals(Flags.GOLF) && !displayed) {
            return "Flag removed - gate is closed";
        } else if (upperFlag.equals(Flags.AP) && lowerFlag.equals(Flags.ALPHA) && displayed) {
            return "Flag displayed - start postponed - no more racing today";
        } else if (upperFlag.equals(Flags.AP) && lowerFlag.equals(Flags.HOTEL) && displayed) {
            return "Flag displayed - start postponed - further signals ashore";
        } else if (upperFlag.equals(Flags.AP) && displayed) {
            return "Flag displayed - start postponed";
        } else if (upperFlag.equals(Flags.AP) && !displayed) {
            return "Flag removed - start procedure starts in one minute";
        } else if (upperFlag.equals(Flags.NOVEMBER) && lowerFlag.equals(Flags.ALPHA) && displayed) {
            return "Flag displayed - race abandoned - no more racing today";
        } else if (upperFlag.equals(Flags.NOVEMBER) && lowerFlag.equals(Flags.HOTEL) && displayed) {
            return "Flag displayed - start abandoned - further signals ashore";
        } else if (upperFlag.equals(Flags.NOVEMBER) && displayed) {
            return "Flag displayed - start abandoned";
        } else if (upperFlag.equals(Flags.BRAVO) && displayed) {
            return "Protest time started";
        } else if (upperFlag.equals(Flags.BRAVO) && !displayed) {
            return "Protest time ended";
        } else if (upperFlag.equals(Flags.ESSTHREE) && displayed) {
            return "Three minutes till start";
        } else if (upperFlag.equals(Flags.ESSTWO) && displayed) {
            return "Two minutes till start";
        } else if (upperFlag.equals(Flags.ESSONE) && displayed) {
            return "One minute till start";
        } else if (upperFlag.equals(Flags.ESSONE) && !displayed) {
            return "Race starting signal";
        }
        return "";
    }
    
}
