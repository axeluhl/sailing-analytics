package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.gwt.ui.client.DataEntryDialogWithDateTimeBox;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RaceLogSetStartTimeAndProcedureDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.controls.datetime.DateAndTimeInput;
import com.sap.sse.gwt.client.controls.datetime.DateTimeInput.Accuracy;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class SetStartTimeDialog extends DataEntryDialogWithDateTimeBox<RaceLogSetStartTimeAndProcedureDTO> {

    private static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);
    
    private final SailingServiceAsync service;
    private final ErrorReporter errorReporter;
    private final String leaderboardName;
    private final String raceColumnName;
    private final String fleetName;
    private final StringMessages stringMessages;
    
    private int currentPassId = -1;
    private Label currentStartTimeLabel;
    private Label currentPassIdBox;
    private DateAndTimeInput timeBox;
    private TextBox authorNameBox;
    private com.sap.sse.gwt.client.controls.IntegerBox authorPriorityBox;
    private ListBox racingProcedureSelection;
    private CheckBox advancePassIdCheckbox;
    
    public SetStartTimeDialog(SailingServiceAsync service, ErrorReporter errorReporter, String leaderboardName, 
            String raceColumnName, String fleetName, StringMessages stringMessages, 
            DataEntryDialog.DialogCallback<RaceLogSetStartTimeAndProcedureDTO> callback) {
        super(stringMessages.setStartTime(), stringMessages.setStartTimeDescription(), stringMessages.setStartTime(), 
                stringMessages.cancel(), new StartTimeValidator(stringMessages), callback);
        this.service = service;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        this.leaderboardName = leaderboardName;
        this.raceColumnName = raceColumnName;
        this.fleetName = fleetName;
        this.ensureDebugId("SetStartTimeDialog");
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        VerticalPanel content = new VerticalPanel();
        content.add(createCurrentPanel());
        content.add(createInputPanel());
        refreshCurrentStartTime();
        return content;
    }

    private Widget createCurrentPanel() {
        CaptionPanel current = new CaptionPanel(stringMessages.liveData());
        HorizontalPanel currentPanel = new HorizontalPanel();
        currentPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        Grid grid = new Grid(2,2);
        currentStartTimeLabel = new Label("");
        grid.setWidget(0, 0, createLabel(stringMessages.startTime()));
        grid.setWidget(0, 1, currentStartTimeLabel);
        
        currentPassIdBox = new Label("");
        grid.setWidget(1, 0, createLabel(stringMessages.currentPass()));
        grid.setWidget(1, 1, currentPassIdBox);
        currentPanel.add(grid);
        
        PushButton refreshButton = new PushButton(new Image(resources.reloadIcon()));
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                refreshCurrentStartTime();
            }
        });
        currentPanel.add(refreshButton);
        
        current.add(currentPanel);
        return current;
    }
    
    private Widget createInputPanel() {
        Grid content = new Grid(5, 2);
        timeBox = createDateTimeBox(new Date(), Accuracy.SECONDS);
        timeBox.ensureDebugId("StartTimeTimeBox");
        content.setWidget(0, 0, createLabel(stringMessages.startTime()));
        content.setWidget(0, 1, timeBox);
        
        
        authorNameBox = createTextBox("Shore");
        authorNameBox.ensureDebugId("AuthorNameTextBox");
        content.setWidget(1, 0, createLabel(stringMessages.authorName()));
        content.setWidget(1, 1, authorNameBox);
        authorPriorityBox = createIntegerBox(4, 2);
        authorPriorityBox.ensureDebugId("AuthorPriorityIntegerBox");
        content.setWidget(2, 0, createLabel(stringMessages.authorPriority()));
        content.setWidget(2, 1, authorPriorityBox);
        
        racingProcedureSelection = createListBox(false);
        ListBoxUtils.setupRacingProcedureTypeListBox(racingProcedureSelection, RacingProcedureType.RRS26, stringMessages.no());
        int racingCounter = 0;
        for (RacingProcedureType racingType : RacingProcedureType.values()) {
            racingProcedureSelection.setValue(racingCounter++, racingType.name());
        }
        racingProcedureSelection.ensureDebugId("RacingProcedureListBox");
        content.setWidget(3, 0, createLabel(stringMessages.racingProcedure()));
        content.setWidget(3, 1, racingProcedureSelection);
        
        advancePassIdCheckbox = createCheckbox(stringMessages.advancePassId());
        advancePassIdCheckbox.setValue(false);
        advancePassIdCheckbox.ensureDebugId("AnvancePassIdCheckBox");
        content.setWidget(4, 1, advancePassIdCheckbox);
        return content;
    }

    @Override
    protected RaceLogSetStartTimeAndProcedureDTO getResult() {
        RaceLogSetStartTimeAndProcedureDTO dto = new RaceLogSetStartTimeAndProcedureDTO();
        dto.leaderboardName = leaderboardName;
        dto.raceColumnName = raceColumnName;
        dto.fleetName = fleetName;
        dto.authorName = authorNameBox.getValue();
        dto.authorPriority = authorPriorityBox.getValue();
        dto.logicalTimePoint = new Date();
        dto.startTime = timeBox.getValue();
        dto.passId = advancePassIdCheckbox.getValue() ? currentPassId+1 : currentPassId;
        dto.racingProcedure = RacingProcedureType.values()[racingProcedureSelection.getSelectedIndex()];
        return dto;
    }

    private void refreshCurrentStartTime() {
        service.getStartTimeAndProcedure(leaderboardName, raceColumnName, fleetName, new AsyncCallback<com.sap.sse.common.Util.Triple<Date,Integer, RacingProcedureType>>() {
            
            @Override
            public void onSuccess(com.sap.sse.common.Util.Triple<Date, Integer, RacingProcedureType> result) {
                if (result == null) {
                    currentStartTimeLabel.setText(stringMessages.notAvailable());
                    currentPassIdBox.setText(stringMessages.notAvailable());
                } else {
                    Date startTime = result.getA();
                    if (startTime == null) {
                        currentStartTimeLabel.setText(stringMessages.unknown());
                    } else {
                        currentStartTimeLabel.setText(DateTimeFormat.getFormat(
                                DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM).format(startTime));                    
                    }
                    currentPassId = result.getB().intValue();
                    currentPassIdBox.setText(result.getB().toString());
                    int racingProcedureIndex = 0;
                    for (RacingProcedureType racingProcedureSelect : RacingProcedureType.values()) {
                        if (racingProcedureSelect.equals(result.getC())) {
                            break;
                        }
                        racingProcedureIndex++;
                    }
                    racingProcedureSelection.setSelectedIndex(racingProcedureIndex);
                }
            }
            
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(caught.getMessage());
            }
        });
    }
    
    private static class StartTimeValidator implements Validator<RaceLogSetStartTimeAndProcedureDTO> {
        
        private final StringMessages stringMessages;
        
        public StartTimeValidator(StringMessages stringMessages) {
            this.stringMessages = stringMessages;
        }

        @Override
        public String getErrorMessage(RaceLogSetStartTimeAndProcedureDTO dto) {
            if (dto.authorName == null || dto.authorName.isEmpty() || 
                    dto.authorPriority == null || dto.startTime == null) {
                return stringMessages.pleaseEnterAValue();
            }
            return null;
        }
        
    }

}
