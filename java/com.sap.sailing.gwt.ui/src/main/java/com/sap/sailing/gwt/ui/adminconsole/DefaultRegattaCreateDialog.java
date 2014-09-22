package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;

public class DefaultRegattaCreateDialog extends RegattaWithSeriesAndFleetsCreateDialog {

    private SailingServiceAsync sailingService;
    private ErrorReporter errorReporter;

    public DefaultRegattaCreateDialog(Collection<RegattaDTO> existingRegattas, List<EventDTO> existingEvents, SailingServiceAsync sailingService,
            ErrorReporter errorReporter, StringMessages stringConstants,
            com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback<RegattaDTO> callback) {
        super(existingRegattas, existingEvents, stringConstants, callback);
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;

        nameEntryField.setText("Default");
        nameEntryField.setReadOnly(true);
        boatClassEntryField.setText("DeOfault");
        boatClassEntryField.setReadOnly(true);
        setCourseAreaSelection();

    }

    protected void setSeriesEditor() {
        this.seriesEditor = new SeriesWithFleetsDefaultListEditor(Collections.<SeriesDTO> emptyList(), stringMessages,
                resources.removeIcon(), /* enableFleetRemoval */true);
    }

    protected void setupAdditionalWidgetsOnPanel(final VerticalPanel panel) {
        Button newEventBtn = new Button("Create New Event");
        newEventBtn.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                openEventCreateDialog();
            }
        });

        Grid grid = (Grid) panel.getWidget(0);
        Grid newGrid = new Grid(6, 3);
        for (int i = 0; i < grid.getColumnCount(); i++) {
            for (int j = 0; j < grid.getRowCount(); j++) {
                newGrid.setWidget(j, i, grid.getWidget(j, i));
            }

        }

        newGrid.setWidget(3, 2, newEventBtn);
        panel.remove(0);
        panel.add(newGrid);
    }

    private void openEventCreateDialog() {
        EventCreateDialog dialog = new EventCreateDialog(existingEvents, stringMessages,
                new DialogCallback<EventDTO>() {
                    @Override
                    public void cancel() {
                    }

                    @Override
                    public void ok(final EventDTO newEvent) {
                        createEvent(newEvent);
                    }
                });
        dialog.show();
    }

    private void createEvent(final EventDTO newEvent) {
        List<String> courseAreaNames = new ArrayList<String>();
        for (CourseAreaDTO courseAreaDTO : newEvent.venue.getCourseAreas()) {
            courseAreaNames.add(courseAreaDTO.getName());
        }
        sailingService.createEvent(newEvent.getName(), newEvent.startDate, newEvent.endDate, newEvent.venue.getName(),
                newEvent.isPublic, courseAreaNames, newEvent.getImageURLs(), newEvent.getVideoURLs(),
                newEvent.getSponsorImageURLs(), newEvent.getLogoImageURL(), newEvent.getOfficialWebsiteURL(),
                new AsyncCallback<EventDTO>() {

                    @Override
                    public void onFailure(Throwable t) {
                        errorReporter.reportError("Error trying to create new event " + newEvent.getName() + ": "
                                + t.getMessage());
                    }

                    @Override
                    public void onSuccess(EventDTO newEvent) {
                        sailingEventsListBox.setSelectedIndex(sailingEventsListBox.getItemCount() - 1);
                        for (CourseAreaDTO courseArea : newEvent.venue.getCourseAreas()) {
                            courseAreaListBox.addItem(courseArea.getName());
                        }
                        courseAreaListBox.setEnabled(true);
                    }
                });
    }

}
