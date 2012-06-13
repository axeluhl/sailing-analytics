package com.sap.sailing.gwt.ui.shared.windpattern.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.gwt.ui.shared.WindPatternDTO;
import com.sap.sailing.gwt.ui.shared.windpattern.WindPatternDisplay;
import com.sap.sailing.gwt.ui.shared.windpattern.WindPatternDisplayManager;
import com.sap.sailing.gwt.ui.shared.windpattern.WindPatternSetting;

public class WindPatternDisplayManagerImpl implements WindPatternDisplayManager {

    public WindPatternDisplayManagerImpl() {

    }

    @Override
    public List<WindPatternDTO> getWindPatterns() {
        List<WindPatternDTO> list = new ArrayList<WindPatternDTO>();
        for (WindPattern w : WindPattern.values()) {
            list.add(new WindPatternDTO(w.name(), w.getDisplayName()));
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
        }
        return display;
    }

    private void addBlastParameters(WindPatternDisplay display) {
        WindPatternSetting<Double> windSpeedSetting = new WindPatternSettingSliderBar("baseWindSpeed",
                "Base Wind Speed (kn)", 2, 22, 12);
        display.addSetting(windSpeedSetting);

        WindPatternSetting<Double> blastProbability = new WindPatternSettingSliderBar("blastProbability",
                "Gust Probability (%)", 0, 50, 25);
        display.addSetting(blastProbability);
        WindPatternSetting<Double> maxBlastSize = new WindPatternSettingSliderBar("maxBlastSize", "Gust Size", 1, 10,
                1);
        display.addSetting(maxBlastSize);
        WindPatternSetting<Double> blastWindSpeed = new WindPatternSettingSliderBar("blastWindSpeed",
                "Average Gust Wind Speed (%)", 0, 200, 120);
        display.addSetting(blastWindSpeed);
        WindPatternSetting<Double> blastWindSpeedVar = new WindPatternSettingSliderBar("blastWindSpeedVar",
                "Gust Wind Speed Variance (%)", 1e-4, 100, 10);
        display.addSetting(blastWindSpeedVar);
    }

    private void addOscillationParameters(WindPatternDisplay display) {
        WindPatternSetting<Double> windSpeedSetting = new WindPatternSettingSliderBar("baseWindSpeed",
                "Base Wind Speed (kn)", 2, 22, 12);
        display.addSetting(windSpeedSetting);

        WindPatternSetting<Double> baseWindBearing = new WindPatternSettingSliderBar("baseWindBearing",
                "Average Wind Direction (Degrees)", -20, 20, 0);
        display.addSetting(baseWindBearing);
        WindPatternSetting<Double> frequency = new WindPatternSettingSliderBar("frequency",
                "Oscillation Frequency (per hr)", 0, 60, 30);
        display.addSetting(frequency);
        WindPatternSetting<Double> amplitude = new WindPatternSettingSliderBar("amplitude",
                "Oscillation Amplitude (Degrees)", 0, 30, 15);
        display.addSetting(amplitude);
        WindPatternSetting<Double> leftWindSpeed = new WindPatternSettingSliderBar("leftWindSpeed",
                "Wind Speed Left Side (%)", 0, 200, 100);
        display.addSetting(leftWindSpeed);
        WindPatternSetting<Double> middleWindSpeed = new WindPatternSettingSliderBar("middleWindSpeed",
                "Wind Speed Middle (%)", 0, 200, 100);
        display.addSetting(middleWindSpeed);
        WindPatternSetting<Double> rightWindSpeed = new WindPatternSettingSliderBar("rightWindSpeed",
                "Wind Speed Right Side (%)", 0, 200, 100);
        display.addSetting(rightWindSpeed);
    }

}
