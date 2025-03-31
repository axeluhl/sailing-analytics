package com.sap.sailing.gwt.ui.simulator.windpattern.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.gwt.ui.shared.WindPatternDTO;
import com.sap.sailing.gwt.ui.simulator.windpattern.WindPattern;
import com.sap.sailing.gwt.ui.simulator.windpattern.WindPatternDisplay;
import com.sap.sailing.gwt.ui.simulator.windpattern.WindPatternDisplayManager;
import com.sap.sailing.gwt.ui.simulator.windpattern.WindPatternSetting;
import com.sap.sailing.gwt.ui.simulator.windpattern.WindPatternSetting.SettingName;
import com.sap.sailing.simulator.util.SailingSimulatorConstants;

public class WindPatternDisplayManagerImpl implements WindPatternDisplayManager {

	char mode = 'f';
	
    public WindPatternDisplayManagerImpl() {
    };
    
    @Override
    public void setMode(char mode) {
    	this.mode = mode;    	
    }

    @Override
    public List<WindPatternDTO> getWindPatterns(char mode) {
    	this.setMode(mode);
        List<WindPatternDTO> list = new ArrayList<WindPatternDTO>();
        for (WindPattern w : WindPattern.values()) {
        	if (((w == WindPattern.MEASURED)||(w == WindPattern.NONE))&&(mode == SailingSimulatorConstants.ModeEvent)) {
        		continue;
        	}
            list.add(new WindPatternDTO(w));
        }
        return list;
    }

    @Override
    public WindPatternDisplay getDisplay(WindPattern windPattern) {
        WindPatternDisplay display = new WindPatternDisplayImpl(windPattern);

        switch (windPattern) {
        case BLASTS:
            addBlastParameters(display);
            break;
        case OSCILLATIONS:
            addOscillationParameters(display);
            break;
        case OSCILLATION_WITH_BLASTS:
            addOscillationParameters(display);
            break;
        case MEASURED:
            break;
        case NONE:
            break;
        default:
            break;
        }
        return display;
    }

    private void addBlastParameters(WindPatternDisplay display) {
    	if (mode == SailingSimulatorConstants.ModeEvent) {
    		WindPatternSetting<Double> windBearingSetting = new WindPatternSettingSliderBar("windBearing",
    				SettingName.BASE_BEARING_IN_DEGREES, 0, 360, 5, 0, 36);
    		display.addSetting(windBearingSetting);
    		WindPatternSetting<Double> baseWindBearing = new WindPatternSettingSliderBar("baseWindBearing",
    				SettingName.RACE_COURSE_DIFF_IN_DEGREES, -20, 20, 1, 0, 10);
    		display.addSetting(baseWindBearing);
    	}
        WindPatternSetting<Double> windSpeedSetting = new WindPatternSettingSliderBar("baseWindSpeed",
                SettingName.BASE_SPEED_IN_KNOTS, 2, 22, 0.5, 12, 10);
        display.addSetting(windSpeedSetting);

        WindPatternSetting<Double> blastProbability = new WindPatternSettingSliderBar("blastProbability",
                SettingName.PROPABILITY_IN_PERCENT, 0, 50, 5, 25, 10);
        display.addSetting(blastProbability);
        WindPatternSetting<Double> maxBlastSize = new WindPatternSettingSliderBar("maxBlastSize", 
                SettingName.GUST_SIZE, 1, 10, 1, 1, 10);
        display.addSetting(maxBlastSize);
        WindPatternSetting<Double> blastWindSpeed = new WindPatternSettingSliderBar("blastWindSpeed",
                SettingName.AVERAGE_SPEED_IN_PERCENT, 0, 200, 5, 120, 10);
        display.addSetting(blastWindSpeed);
        WindPatternSetting<Double> blastWindSpeedVar = new WindPatternSettingSliderBar("blastWindSpeedVar",
                SettingName.SPEED_VARIANCE_IN_PERCENT, 1e-4, 100, 5, 10, 10);
        display.addSetting(blastWindSpeedVar);
    }

    private void addOscillationParameters(WindPatternDisplay display) {
    	if (mode == SailingSimulatorConstants.ModeEvent) {
    		WindPatternSetting<Double> windBearingSetting = new WindPatternSettingSliderBar("windBearing",
    				SettingName.BASE_BEARING_IN_DEGREES, 0, 360, 5, 0, 36);
    		display.addSetting(windBearingSetting);
    		WindPatternSetting<Double> baseWindBearing = new WindPatternSettingSliderBar("baseWindBearing",
    				SettingName.RACE_COURSE_DIFF_IN_DEGREES, -20, 20, 1, 0, 10);
    		display.addSetting(baseWindBearing);
    		WindPatternSetting<Double> windSpeedSetting = new WindPatternSettingSliderBar("baseWindSpeed",
    				SettingName.BASE_SPEED_IN_KNOTS, 2, 22, 1, 12, 10);    
    		display.addSetting(windSpeedSetting);
    	} else {
    		WindPatternSetting<Double> windSpeedSetting = new WindPatternSettingSliderBar("baseWindSpeed",
    				SettingName.BASE_SPEED_IN_KNOTS, 2, 22, 1, 12, 10);    
    		display.addSetting(windSpeedSetting);
    		WindPatternSetting<Double> baseWindBearing = new WindPatternSettingSliderBar("baseWindBearing",
    				SettingName.AVERAGE_DIRECTION_IN_DEGREES, -20, 20, 1, 0, 10);
    		display.addSetting(baseWindBearing);
    	}
    	WindPatternSetting<Double> leftWindSpeed = new WindPatternSettingSliderBar("leftWindSpeed",
    			SettingName.SPEED_LEFT_SIDE_IN_PERCENT, 0, 200, 5, 100, 10);
    	display.addSetting(leftWindSpeed);
    	WindPatternSetting<Double> middleWindSpeed = new WindPatternSettingSliderBar("middleWindSpeed",
    			SettingName.SPEED_MIDDLE_IN_PERCENT, 0, 200, 5, 100, 10);
        display.addSetting(middleWindSpeed);
        WindPatternSetting<Double> rightWindSpeed = new WindPatternSettingSliderBar("rightWindSpeed",
                SettingName.SPEED_RIGHT_SIDE_IN_PERECENT, 0, 200, 5, 100, 10);
        display.addSetting(rightWindSpeed);
        WindPatternSetting<Double> frequency = new WindPatternSettingSliderBar("frequency",
                SettingName.FREQUENCY_PER_HOURS, 0, 20, 0.5, 6, 10);
        display.addSetting(frequency);
        WindPatternSetting<Double> amplitude = new WindPatternSettingSliderBar("amplitude",
                SettingName.AMPLITUDE_IN_DEGREES, -30, 30, 1, 0, 10);
        display.addSetting(amplitude);
        WindPatternSetting<Double> curSpeed = new WindPatternSettingSliderBar("curSpeed",
                SettingName.CURRENT_SPEED_IN_KNOTS, 0, 2, 0.1, 0, 10);
        display.addSetting(curSpeed);
        WindPatternSetting<Double> curBearing = new WindPatternSettingSliderBar("curBearing",
                SettingName.CURRENT_BEARING_IN_DEGREES, 0, 360, 5, 180, 36);
        display.addSetting(curBearing);
    }

}
