package com.sap.sailing.gwt.ui.shared.windpattern.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.gwt.ui.shared.WindPatternDTO;
import com.sap.sailing.gwt.ui.shared.windpattern.WindPattern;
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
        WindPatternSetting<Double> windSpeedSetting = new WindPatternSettingSliderBar("baseWindSpeed",
                "Base Speed (kn)", 2, 22, 12,10);
        display.addSetting(windSpeedSetting);

        WindPatternSetting<Double> blastProbability = new WindPatternSettingSliderBar("blastProbability",
                "Probability (%)", 0, 50, 25,10);
        display.addSetting(blastProbability);
        WindPatternSetting<Double> maxBlastSize = new WindPatternSettingSliderBar("maxBlastSize", "Gust Size", 1, 10,
                1,10);
        display.addSetting(maxBlastSize);
        WindPatternSetting<Double> blastWindSpeed = new WindPatternSettingSliderBar("blastWindSpeed",
                "Average Speed (%)", 0, 200, 120,10);
        display.addSetting(blastWindSpeed);
        WindPatternSetting<Double> blastWindSpeedVar = new WindPatternSettingSliderBar("blastWindSpeedVar",
                "Speed Variance (%)", 1e-4, 100, 10,10);
        display.addSetting(blastWindSpeedVar);
    }

    private void addOscillationParameters(WindPatternDisplay display) {
        WindPatternSetting<Double> windSpeedSetting = new WindPatternSettingSliderBar("baseWindSpeed",
                "Base Speed (kn)", 2, 22, 12,10);
        display.addSetting(windSpeedSetting);
        WindPatternSetting<Double> baseWindBearing = new WindPatternSettingSliderBar("baseWindBearing",
                "Average Direction (Degrees)", -20, 20, 0,10);
        display.addSetting(baseWindBearing);
        WindPatternSetting<Double> leftWindSpeed = new WindPatternSettingSliderBar("leftWindSpeed",
                "Speed Left Side (%)", 0, 200, 100,10);
        display.addSetting(leftWindSpeed);
        WindPatternSetting<Double> middleWindSpeed = new WindPatternSettingSliderBar("middleWindSpeed",
                "Speed Middle (%)", 0, 200, 100,10);
        display.addSetting(middleWindSpeed);
        WindPatternSetting<Double> rightWindSpeed = new WindPatternSettingSliderBar("rightWindSpeed",
                "Speed Right Side (%)", 0, 200, 100,10);
        display.addSetting(rightWindSpeed);
        WindPatternSetting<Double> frequency = new WindPatternSettingSliderBar("frequency",
                "Frequency (per hr)", 0, 20, 6,10);
        display.addSetting(frequency);
        WindPatternSetting<Double> amplitude = new WindPatternSettingSliderBar("amplitude",
                "Amplitude (Degrees)", -30, 30, 0, 10);
        display.addSetting(amplitude);
        WindPatternSetting<Double> curSpeed = new WindPatternSettingSliderBar("curSpeed",
                "Current Speed (kn)", 0, 2, 0, 10);
        display.addSetting(curSpeed);
        WindPatternSetting<Double> curBearing = new WindPatternSettingSliderBar("curBearing",
                "Current Bearing (Degrees)", 0, 360, 180, 36);
        display.addSetting(curBearing);
    }

}
