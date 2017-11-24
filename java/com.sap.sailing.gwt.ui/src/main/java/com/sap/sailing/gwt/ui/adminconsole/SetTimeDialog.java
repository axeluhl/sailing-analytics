package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.DataEntryDialogWithDateTimeBox;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.controls.datetime.DateTimeInput;
import com.sap.sse.gwt.client.controls.datetime.DateTimeInput.Accuracy;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public abstract class SetTimeDialog<T> extends DataEntryDialogWithDateTimeBox<T> {

    private static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);

    protected final SailingServiceAsync service;
    protected final ErrorReporter errorReporter;
    protected final String leaderboardName;
    protected final String raceColumnName;
    protected final String fleetName;
    protected final StringMessages stringMessages;

    protected int currentPassId = -1;
    protected Label currentStartOrFinishingTimeLabel;
    protected Label currentPassIdBox;
    protected DateTimeInput timeBox;
    protected TextBox authorNameBox;
    protected com.sap.sse.gwt.client.controls.IntegerBox authorPriorityBox;

    public SetTimeDialog(SailingServiceAsync service, ErrorReporter errorReporter, String title, String message,
            String okButtonName, String cancelButtonName, String leaderboardName, String raceColumnName,
            String fleetName, StringMessages stringMessages,
            com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator<T> validator,
            DataEntryDialog.DialogCallback<T> callback) {
        super(title, message, okButtonName, cancelButtonName, validator, callback);
        this.service = service;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        this.leaderboardName = leaderboardName;
        this.raceColumnName = raceColumnName;
        this.fleetName = fleetName;
    }

    @Override
    protected Widget getAdditionalWidget() {
        VerticalPanel content = new VerticalPanel();
        content.add(createCurrentPanel());
        content.add(createInputPanel());
        refreshCurrentTime();
        return content;
    }

    private Widget createCurrentPanel() {
        CaptionPanel current = new CaptionPanel(stringMessages.liveData());
        HorizontalPanel currentPanel = new HorizontalPanel();
        currentPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        Grid grid = new Grid(3, 2);
        currentStartOrFinishingTimeLabel = new Label("");
        grid.setWidget(0, 0, createLabel(getTimeLabel()));
        grid.setWidget(0, 1, currentStartOrFinishingTimeLabel);
        
        grid.setText(1, 0, "");
        grid.setText(1, 1, "");
        
        additionalCurrentTimeLabel(grid);

        currentPassIdBox = new Label("");
        grid.setWidget(2, 0, createLabel(stringMessages.currentPass()));
        grid.setWidget(2, 1, currentPassIdBox);
        currentPanel.add(grid);

        PushButton refreshButton = new PushButton(new Image(resources.reloadIcon()));
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                refreshCurrentTime();
            }
        });
        currentPanel.add(refreshButton);

        current.add(currentPanel);
        return current;
    }

    protected abstract String getTimeLabel();

    private Widget createInputPanel() {
        Grid content = new Grid(7, 2);
        timeBox = createDateTimeBox(new Date(), Accuracy.SECONDS);
        content.setWidget(0, 0, createLabel(getTimeLabel()));
        content.setWidget(0, 1, timeBox);
        
        content.setText(1, 0, "");
        content.setText(1, 1, "");
        
        additionalInput(content);

        authorNameBox = createTextBox("Shore");
        authorNameBox.ensureDebugId("AuthorNameTextBox");
        content.setWidget(2, 0, createLabel(stringMessages.authorName()));
        content.setWidget(2, 1, authorNameBox);
        content.setWidget(3, 0, createLabel(stringMessages.authorPriority()));
        authorPriorityBox = createIntegerBox(4, 2);
        authorPriorityBox.ensureDebugId("AuthorPriorityIntegerBox");
        content.setWidget(3, 1, authorPriorityBox);

        addAdditionalInput(content);
        
        return content;
    }
    
    protected void additionalCurrentTimeLabel(Grid content) {
    }

    protected abstract void additionalInput(Grid content);

    protected abstract void addAdditionalInput(Grid content);

    protected abstract void refreshCurrentTime();
}
