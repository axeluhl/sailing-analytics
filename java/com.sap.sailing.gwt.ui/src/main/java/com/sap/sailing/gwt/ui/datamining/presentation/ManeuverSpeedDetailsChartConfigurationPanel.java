package com.sap.sailing.gwt.ui.datamining.presentation;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.gwt.ui.client.StringMessages;

/**
 * A control panel for maneuver speed details charts.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverSpeedDetailsChartConfigurationPanel extends HorizontalPanel {

    private IntegerBox minCountDataIntegerBox = new IntegerBox();
    private DoubleBox minValueDoubleBox = new DoubleBox();
    private DoubleBox maxValueDoubleBox = new DoubleBox();
    private CheckBox zeroTo360AxisLabelingCheckBox = new CheckBox();

    private static final String NUMBER_BOX_WIDTH = "40px";

    public ManeuverSpeedDetailsChartConfigurationPanel(Runnable applyConfigurationCallback,
            StringMessages stringMessages) {
        setHorizontalAlignment(ALIGN_LEFT);
        setVerticalAlignment(ALIGN_MIDDLE);
        setSpacing(10);
        getElement().getStyle().setMarginTop(-10, Unit.PX);
        getElement().getStyle().setMarginBottom(-10, Unit.PX);

        add(new Label(stringMessages.minDataCount() + ":"));
        minCountDataIntegerBox.setWidth(NUMBER_BOX_WIDTH);
        add(minCountDataIntegerBox);

        add(new Label(stringMessages.minValue() + ":"));
        minValueDoubleBox.setWidth(NUMBER_BOX_WIDTH);
        add(minValueDoubleBox);

        add(new Label(stringMessages.maxValue() + ":"));
        maxValueDoubleBox.setWidth(NUMBER_BOX_WIDTH);
        add(maxValueDoubleBox);

        add(new Label(stringMessages.zeroTo360AxisLabeling() + ":"));
        add(zeroTo360AxisLabelingCheckBox);

        Button applyButton = new Button(stringMessages.apply(), (ClickEvent e) -> applyConfigurationCallback.run());
        add(applyButton);
    }

    public Double getMinValue() {
        return minValueDoubleBox.getValue();
    }

    public Double getMaxValue() {
        return maxValueDoubleBox.getValue();
    }

    public Integer getMinDataCount() {
        return minCountDataIntegerBox.getValue();
    }

    public void setMinValue(Double minValue) {
        minValueDoubleBox.setValue(minValue);
    }

    public void setMaxValue(Double maxValue) {
        maxValueDoubleBox.setValue(maxValue);
    }

    public void setMinDataCount(Integer minCount) {
        minCountDataIntegerBox.setValue(minCount);
    }

    public boolean isZeroTo360AxisLabeling() {
        return zeroTo360AxisLabelingCheckBox.getValue();
    }

    public void setZeroTo360AxisLabeling(boolean zeroTo360AxisLabeling) {
        zeroTo360AxisLabelingCheckBox.setValue(zeroTo360AxisLabeling);
    }

}
