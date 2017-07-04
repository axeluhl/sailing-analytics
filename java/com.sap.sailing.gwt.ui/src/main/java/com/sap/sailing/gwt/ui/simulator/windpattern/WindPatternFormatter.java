package com.sap.sailing.gwt.ui.simulator.windpattern;

import com.sap.sailing.gwt.ui.client.StringMessages;

public class WindPatternFormatter {
    
    private final StringMessages stringMessages;
    
    public WindPatternFormatter(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
    }

    public String formatPattern(WindPattern pattern) {
        switch (pattern) {
            case BLASTS: return stringMessages.gusts();
            case MEASURED: return stringMessages.measured();
            case OSCILLATIONS: return stringMessages.oscillations();
            case OSCILLATION_WITH_BLASTS: return stringMessages.oscillationWithGusts();
            case NONE: return stringMessages.chooseAWindPattern();
        }
        return null;
    }
    
    public String formatSetting(WindPatternSetting.SettingName settingName) {
        String unit = formatSettingUnit(settingName);
        return formatSettingName(settingName) + (unit == null ? "" : (" (" + unit + ")"));
    }
    
    private String formatSettingName(WindPatternSetting.SettingName settingName) {
        switch (settingName) {
            case BASE_BEARING_IN_DEGREES: return stringMessages.baseBearing();
            case RACE_COURSE_DIFF_IN_DEGREES: return stringMessages.raceCourseDiff();
            case BASE_SPEED_IN_KNOTS: return stringMessages.baseSpeed();
            case PROPABILITY_IN_PERCENT: return stringMessages.probability();
            case GUST_SIZE: return stringMessages.gustSize();
            case AVERAGE_SPEED_IN_PERCENT: return stringMessages.averageSpeed();
            case SPEED_VARIANCE_IN_PERCENT: return stringMessages.speedVariance();
            case AVERAGE_DIRECTION_IN_DEGREES: return stringMessages.averageDirection();
            case SPEED_LEFT_SIDE_IN_PERCENT: return stringMessages.speedLeftSide();
            case SPEED_MIDDLE_IN_PERCENT: return stringMessages.speedMiddle();
            case SPEED_RIGHT_SIDE_IN_PERECENT: return stringMessages.speedRightSide();
            case FREQUENCY_PER_HOURS: return stringMessages.frequency();
            case AMPLITUDE_IN_DEGREES: return stringMessages.amplitude();
            case CURRENT_SPEED_IN_KNOTS: return stringMessages.currentSpeed();
            case CURRENT_BEARING_IN_DEGREES: return stringMessages.currentBearing();
        }
        return null;
    }
    
    private String formatSettingUnit(WindPatternSetting.SettingName settingName) {
        switch (settingName) {
            case FREQUENCY_PER_HOURS:
                return stringMessages.perHours();
            case BASE_SPEED_IN_KNOTS:
            case CURRENT_SPEED_IN_KNOTS:
                return stringMessages.knotsUnit();
            case BASE_BEARING_IN_DEGREES:
            case RACE_COURSE_DIFF_IN_DEGREES:
            case AVERAGE_DIRECTION_IN_DEGREES:
            case AMPLITUDE_IN_DEGREES:
            case CURRENT_BEARING_IN_DEGREES:
                return stringMessages.degreesShort();
            case PROPABILITY_IN_PERCENT:
            case AVERAGE_SPEED_IN_PERCENT:
            case SPEED_VARIANCE_IN_PERCENT:
            case SPEED_LEFT_SIDE_IN_PERCENT:
            case SPEED_MIDDLE_IN_PERCENT:
            case SPEED_RIGHT_SIDE_IN_PERECENT:
                return stringMessages.percentUnit();
            case GUST_SIZE:
        }
        return null;
    }
}
