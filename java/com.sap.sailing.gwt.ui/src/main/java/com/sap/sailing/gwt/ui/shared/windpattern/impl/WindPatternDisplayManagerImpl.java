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
        WindPatternSetting<Double> windSpeedSetting = new WindPatternSettingSliderBar("baseWindSpeed",
                "Base Wind Speed (kn)", 0, 30, 0);
        display.addSetting(windSpeedSetting);

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
        WindPatternSetting<Double> blastProbability = new WindPatternSettingSliderBar("blastProbability",
                "Blast Probability (%)", 0, 50, 0);
        display.addSetting(blastProbability);
        WindPatternSetting<Double> maxBlastSize = new WindPatternSettingSliderBar("maxBlastSize", "Blast Size", 1, 10,
                1);
        display.addSetting(maxBlastSize);
        WindPatternSetting<Double> blastWindSpeed = new WindPatternSettingSliderBar("blastWindSpeed",
                "Average Blast Wind Speed (%)", 0, 100, 0);
        display.addSetting(blastWindSpeed);
        WindPatternSetting<Double> blastWindSpeedVar = new WindPatternSettingSliderBar("blastWindSpeedVar",
                "Blast Wind Speed Variance (%)", 0, 100, 0);
        display.addSetting(blastWindSpeedVar);
    }

    private void addOscillationParameters(WindPatternDisplay display) {
        WindPatternSetting<Double> baseWindBearing = new WindPatternSettingSliderBar("baseWindBearing",
                "Average Wind Direction (Degrees)", 0, 360, 0);
        display.addSetting(baseWindBearing);
        WindPatternSetting<Double> frequency = new WindPatternSettingSliderBar("frequency",
                "Oscillation Frequency (per hr)", 0, 60, 0);
        display.addSetting(frequency);
        WindPatternSetting<Double> amplitude = new WindPatternSettingSliderBar("amplitude",
                "Oscillation Amplitude (Degrees)", 0, 20, 0);
        display.addSetting(amplitude);
        WindPatternSetting<Double> leftWindSpeed = new WindPatternSettingSliderBar("leftWindSpeed",
                "Wind Speed Left Side (%)", 0, 200, 0);
        display.addSetting(leftWindSpeed);
        WindPatternSetting<Double> middleWindSpeed = new WindPatternSettingSliderBar("middleWindSpeed",
                "Wind Speed Middle (%)", 0, 200, 0);
        display.addSetting(middleWindSpeed);
        WindPatternSetting<Double> rightWindSpeed = new WindPatternSettingSliderBar("rightWindSpeed",
                "Wind Speed Right Side (%)", 0, 200, 0);
        display.addSetting(rightWindSpeed);
    }

}
