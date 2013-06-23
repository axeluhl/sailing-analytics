package com.sap.sailing.gwt.ui.regattaoverview;

import java.util.Date;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaOverviewEntryDTO;

public class RaceDetailPanel extends SimplePanel {

    private final DateTimeFormat timeFormatter = DateTimeFormat.getFormat("HH:mm:ss");
    private final DateTimeFormat dateFormatter = DateTimeFormat.getFormat("dd.MM.yyyy");
    private final DateTimeFormat durationFormatter = DateTimeFormat.getFormat("H'h' mm'min' ss'sec'");
    private StringMessages stringMessages;

    private RegattaOverviewEntryDTO shownEntry;
    private Button closeButton;
    private Label raceLabel;
    private Label startTimeLabel;
    private Label startDateLabel;
    private Label finishTimeLabel;
    private Label finishDurationLabel;
    private Label protestTimeLabel;

    public RaceDetailPanel(final StringMessages stringMessages, final ClickHandler closeButtonHandler) {
        this.stringMessages = stringMessages;

        closeButton = new Button(stringMessages.close());
        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                shownEntry = null;
                if (closeButtonHandler != null) {
                    closeButton.addClickHandler(closeButtonHandler);
                }
            }
        });
        raceLabel = new Label();
        startTimeLabel = new Label();
        startDateLabel = new Label();
        finishTimeLabel = new Label();
        finishDurationLabel = new Label();
        protestTimeLabel = new Label();

        this.addStyleName("RaceDetailPanel");
        Grid grid = new Grid(3, 3);
        grid.setCellSpacing(6);
        grid.setWidget(0, 0, new Label(stringMessages.startAt()));
        grid.setWidget(1, 0, new Label(stringMessages.finishAt()));
        grid.setWidget(2, 0, new Label(stringMessages.protestEndsAt()));
        grid.setWidget(0, 1, startTimeLabel);
        grid.setWidget(1, 1, finishTimeLabel);
        grid.setWidget(2, 1, protestTimeLabel);
        grid.setWidget(0, 2, startDateLabel);
        grid.setWidget(1, 2, finishDurationLabel);
        

        HorizontalPanel headerPanel = new HorizontalPanel();
        headerPanel.addStyleName("RaceDetailPanel-header");
        headerPanel.setWidth("100%");
        headerPanel.add(raceLabel);
        headerPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        headerPanel.add(closeButton);

        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setWidth("100%");
        mainPanel.add(headerPanel);
        mainPanel.add(grid);
        setWidget(mainPanel);
    }

    public void show(RegattaOverviewEntryDTO entry) {
        shownEntry = entry;
        updateUi();
    }

    private void updateUi() {
        if (shownEntry == null) {
            return;
        }
        String raceName = shownEntry.regattaDisplayName + " " + shownEntry.raceInfo.fleetName + " "
                + shownEntry.raceInfo.raceName;
        raceLabel.setText(stringMessages.showingDetailsOfRace(raceName));
        
        String startTimeLabelText = shownEntry.raceInfo.startTime == null ? "-" : timeFormatter.format(shownEntry.raceInfo.startTime); 
        startTimeLabel.setText(startTimeLabelText);
        
        String startDateLabelText = shownEntry.raceInfo.startTime == null ? "" : dateFormatter.format(shownEntry.raceInfo.startTime);
        startDateLabel.setText(startDateLabelText);
        
        String finishTimeText = shownEntry.raceInfo.finishedTime == null ? "-" : timeFormatter.format(shownEntry.raceInfo.finishedTime);
        finishTimeLabel.setText(finishTimeText);
        
        String finishDurationText = "";
        if (shownEntry.raceInfo.finishedTime != null && shownEntry.raceInfo.startTime != null) {
            Date raceDuration = new Date(shownEntry.raceInfo.finishedTime.getTime() - shownEntry.raceInfo.startTime.getTime());
            finishDurationText = durationFormatter.format(raceDuration);
        }
        finishDurationLabel.setText("(" + finishDurationText + ")");
        
        String protestTimeText = shownEntry.raceInfo.protestFinishTime == null ? "-" : timeFormatter.format(shownEntry.raceInfo.protestFinishTime);
        protestTimeLabel.setText(protestTimeText);
    }

    /**
     * Announces an update to a {@link RegattaOverviewEntryDTO}. Updates the UI of this {@link RaceDetailPanel} if
     * passed entry represents the same race as currently shown entry.
     * 
     * @param updated
     *            entry
     * @return <code>true</code> if UI is updated; otherwise <code>false</code>.
     */
    public boolean update(RegattaOverviewEntryDTO entry) {
        if (areOfSameRace(shownEntry, entry)) {
            show(entry);
            return true;
        }
        return false;
    }

    private static boolean areOfSameRace(RegattaOverviewEntryDTO left, RegattaOverviewEntryDTO right) {
        if (left == null || right == null) {
            return false;
        }

        return bothNullOrEquals(left.courseAreaIdAsString, right.courseAreaIdAsString)
                && bothNullOrEquals(left.regattaName, right.regattaName)
                && bothNullOrEquals(left.raceInfo.seriesName, right.raceInfo.seriesName)
                && bothNullOrEquals(left.raceInfo.fleetName, right.raceInfo.fleetName)
                && bothNullOrEquals(left.raceInfo.raceName, right.raceInfo.raceName);
    }

    private static boolean bothNullOrEquals(Object left, Object right) {
        return left == null ? right == null : left.equals(right);
    }

}