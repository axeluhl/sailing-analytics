package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.StringMessages;

/**
 * Can be used in {@link AbstractEventManagementPanel}s to enable some sort of time-delayed race simulation.
 * Provides a checkbox (see {@link #isSimulate()}) that enables simulation and an offset in minutes (see
 * {@link #getOffsetToStartTimeInMinutes()}).
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class SimulationPanel extends VerticalPanel {
    private static final String ZERO_AS_STRING = "0";
    final CheckBox simulateWithStartTimeNowCheckBox;
    final TextBox offsetToStartTimeOfSimulatedRaceTextBox;

    public SimulationPanel(StringMessages stringMessages) {
        offsetToStartTimeOfSimulatedRaceTextBox = new TextBox();
        offsetToStartTimeOfSimulatedRaceTextBox.setWidth("40px");
        offsetToStartTimeOfSimulatedRaceTextBox.setEnabled(false);
        offsetToStartTimeOfSimulatedRaceTextBox.setValue(ZERO_AS_STRING);
        simulateWithStartTimeNowCheckBox = new CheckBox(stringMessages.simulateAsLiveRace());
        simulateWithStartTimeNowCheckBox.ensureDebugId("SimulateWithStartTimeNowCheckBox");
        simulateWithStartTimeNowCheckBox.setWordWrap(false);
        simulateWithStartTimeNowCheckBox.setValue(Boolean.FALSE);
        simulateWithStartTimeNowCheckBox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                offsetToStartTimeOfSimulatedRaceTextBox.setEnabled(simulateWithStartTimeNowCheckBox.getValue());
                offsetToStartTimeOfSimulatedRaceTextBox.setFocus(simulateWithStartTimeNowCheckBox.getValue());
            }
        });

        final FlowPanel simulateAsLiveRacePanel = new FlowPanel();
        simulateAsLiveRacePanel.add(simulateWithStartTimeNowCheckBox);
        final Label offsetToStartLabel = new Label(stringMessages.simulateWithOffset());
        final HorizontalPanel simulateWithOffsetPanel = new HorizontalPanel();
        simulateWithOffsetPanel.add(offsetToStartLabel);
        simulateWithOffsetPanel.add(offsetToStartTimeOfSimulatedRaceTextBox);
        add(simulateAsLiveRacePanel);
        add(simulateWithOffsetPanel);
    }
    
    public boolean isSimulate() {
        return simulateWithStartTimeNowCheckBox.getValue();
    }
    
    public String getOffsetToStartTimeInMinutes() {
        return offsetToStartTimeOfSimulatedRaceTextBox.getValue();
    }
}
