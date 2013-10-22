package com.sap.sailing.gwt.ui.polarsheets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;

/**
 * Shows radio buttons allowing the user to arrange the data displayed in the histogram chart of the polar sheets panel
 * 
 * @author d054528 Frederik Petersen
 * 
 */
public class PolarSheetsHistogramDataArrangeButtonBar extends HorizontalPanel{

    private final RadioButton arrangeByNothingButton;

    public PolarSheetsHistogramDataArrangeButtonBar(final PolarSheetsHistogramPanel parentPanel) {
        this.setHeight("100%");
        this.add(new Label("Arrange by..."));
        arrangeByNothingButton = createArrangeByNothingButton(parentPanel);
        this.add(arrangeByNothingButton);
        RadioButton arrangeByGaugesIdsButton = createArrangeByGaugesIdButton(parentPanel);
        this.add(arrangeByGaugesIdsButton);
        RadioButton arrangeByDayButton = createArrangeByDayButton(parentPanel);
        this.add(arrangeByDayButton);
        RadioButton arrangeByDayAndWindGaugeButton = createArrangeByDayAndWindGaugeButton(parentPanel);
        this.add(arrangeByDayAndWindGaugeButton);
    }

    private RadioButton createArrangeByNothingButton(final PolarSheetsHistogramPanel parentPanel) {
        RadioButton arrangeByNothingButton = new RadioButton("Arrange","... Nothing");
        arrangeByNothingButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                if (parentPanel.hasData()) {
                    parentPanel.arrangeByNothing();
                }
            }
        });
        arrangeByNothingButton.setValue(true, false);
        return arrangeByNothingButton;
    }

    private RadioButton createArrangeByGaugesIdButton(final PolarSheetsHistogramPanel parentPanel) {
        RadioButton arrangeByGaugesIdsButton = new RadioButton("Arrange","... Wind Gauge Ids");
        arrangeByGaugesIdsButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                if (parentPanel.hasData()) {
                    parentPanel.arrangeByWindGaugeIds();
                }
            }
        });
        return arrangeByGaugesIdsButton;
    }
    
    private RadioButton createArrangeByDayButton(final PolarSheetsHistogramPanel parentPanel) {
        RadioButton arrangeByDayButton = new RadioButton("Arrange","... Day");
        arrangeByDayButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                if (parentPanel.hasData()) {
                    parentPanel.arrangeByDay();
                }
            }
        });
        return arrangeByDayButton;
    }
    
    private RadioButton createArrangeByDayAndWindGaugeButton(final PolarSheetsHistogramPanel parentPanel) {
        RadioButton arrangeByDayAndWindGaugeButton = new RadioButton("Arrange","... Day And Gauge Ids");
        arrangeByDayAndWindGaugeButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                if (parentPanel.hasData()) {
                    parentPanel.arrangeByDayAndGaugeIds();
                }
            }
        });
        return arrangeByDayAndWindGaugeButton;
    }
    
    /**
     * Resets the histogram chart to the default (no filter)
     */
    public void reset() {
        arrangeByNothingButton.setValue(true);
    }

}
