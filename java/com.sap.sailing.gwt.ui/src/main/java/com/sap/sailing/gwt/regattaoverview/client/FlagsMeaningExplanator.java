package com.sap.sailing.gwt.regattaoverview.client;

import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class FlagsMeaningExplanator {

    public static String getFlagsMeaning(StringMessages stringMessages, Flags upperFlag, Flags lowerFlag, boolean displayed) {
        if (upperFlag.equals(Flags.CLASS) && lowerFlag.equals(Flags.GOLF) && displayed) {
            return stringMessages.classAndGolfUp();
        } else if (upperFlag.equals(Flags.CLASS) && displayed) {
            return stringMessages.classDisplayed();
        } else if (upperFlag.equals(Flags.PAPA) && displayed) {
            return stringMessages.papaDisplayed();
        } else if (upperFlag.equals(Flags.BLACK) && displayed) {
            return stringMessages.blackDisplayed();
        } else if ((upperFlag.equals(Flags.ZULU) || upperFlag.equals(Flags.INDIA)) && displayed) {
            return stringMessages.zuluIndiaDisplayed();
        } else if (upperFlag.equals(Flags.PAPA) && !displayed) {
            return stringMessages.papaRemoved();
        } else if (upperFlag.equals(Flags.BLACK) && !displayed) {
            return stringMessages.blackRemoved();
        } else if ((upperFlag.equals(Flags.ZULU) || upperFlag.equals(Flags.INDIA)) && !displayed) {
            return stringMessages.zuluIndiaRemoved();
        } else if (upperFlag.equals(Flags.CLASS) && !displayed) {
            return stringMessages.classFlagRemoved();
        } else if (upperFlag.equals(Flags.XRAY)) {
            return stringMessages.xray();
        } else if (upperFlag.equals(Flags.FIRSTSUBSTITUTE)) {
            return stringMessages.firstSubstitute();
        } else if (upperFlag.equals(Flags.BLUE) && displayed) {
            return stringMessages.blueFlagDisplayed();
        } else if (upperFlag.equals(Flags.BLUE) && !displayed) {
            return stringMessages.blueFlagRemoved();
        } else if (upperFlag.equals(Flags.GOLF) && !displayed) {
            return stringMessages.golfRemoved();
        } else if (upperFlag.equals(Flags.AP) && lowerFlag.equals(Flags.ALPHA) && displayed) {
            return stringMessages.answeringPennantOverAlphaDisplayed();
        } else if (upperFlag.equals(Flags.AP) && lowerFlag.equals(Flags.HOTEL) && displayed) {
            return stringMessages.answeringPennantOverHotelDisplayed();
        } else if (upperFlag.equals(Flags.AP) && displayed) {
            return stringMessages.answeringPennantDisplayed();
        } else if (upperFlag.equals(Flags.AP) && !displayed) {
            return stringMessages.answeringPennantRemoved();
        } else if (upperFlag.equals(Flags.NOVEMBER) && lowerFlag.equals(Flags.ALPHA) && displayed) {
            return stringMessages.novemberOverAlphaDisplayed();
        } else if (upperFlag.equals(Flags.NOVEMBER) && lowerFlag.equals(Flags.HOTEL) && displayed) {
            return stringMessages.novemberOverHotelDisplayed();
        } else if (upperFlag.equals(Flags.NOVEMBER) && displayed) {
            return stringMessages.novemberDisplayed();
        } else if (upperFlag.equals(Flags.BRAVO) && displayed) {
            return stringMessages.bravoDisplayed();
        } else if (upperFlag.equals(Flags.BRAVO) && !displayed) {
            return stringMessages.bravoRemoved();
        } else if (upperFlag.equals(Flags.ESSTHREE) && displayed) {
            return stringMessages.essThreeDisplayed();
        } else if (upperFlag.equals(Flags.ESSTWO) && displayed) {
            return stringMessages.essTwoDisplayed();
        } else if (upperFlag.equals(Flags.ESSONE) && displayed) {
            return stringMessages.essOneDisplayed();
        } else if (upperFlag.equals(Flags.ESSONE) && !displayed) {
            return stringMessages.essOneRemoved();
        }
        return "";
    }
    
}
