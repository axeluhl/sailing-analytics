package com.sap.sailing.gwt.ui.datamining.presentation;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class ManeuverSpeedDetailsChartConfigurationPanel extends HorizontalPanel {
    
    private DoubleBox minValueDoubleBox = new DoubleBox();
    private DoubleBox maxValueDoubleBox = new DoubleBox();
    private CheckBox flipWindDirectionCheckBox = new CheckBox();
    
    public ManeuverSpeedDetailsChartConfigurationPanel(ClickHandler applyButtonClickHandler, StringMessages stringMessages) {
        setHorizontalAlignment(ALIGN_LEFT);
        setVerticalAlignment(ALIGN_MIDDLE);
        setSpacing(10);
        Button applyButton = new Button(stringMessages.apply(), applyButtonClickHandler);
        add(new Label(stringMessages.minValue() + ":"));
        add(minValueDoubleBox);
        add(new Label(stringMessages.maxValue() + ":"));
        add(maxValueDoubleBox);
        add(new Label(stringMessages.flipWindDirection() + ":"));
        add(flipWindDirectionCheckBox);
        add(applyButton);
    }
    
    public Double getMinValue() {
        return minValueDoubleBox.getValue();
    }
    public Double getMaxValue() {
        return maxValueDoubleBox.getValue();
    }
    public void setMinValue(Double minValue) {
        minValueDoubleBox.setValue(minValue);
    }
    public void setMaxValue(Double maxValue) {
        maxValueDoubleBox.setValue(maxValue);
    }
    public boolean isFlipWindDirection() {
        return flipWindDirectionCheckBox.getValue();
    }
    public void setFlipWindDirection(boolean flipWindDirection) {
        flipWindDirectionCheckBox.setValue(flipWindDirection);
    }

}
