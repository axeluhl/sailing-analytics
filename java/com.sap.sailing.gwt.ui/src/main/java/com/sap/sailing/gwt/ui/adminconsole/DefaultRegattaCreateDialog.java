package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
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
import com.sap.sailing.gwt.ui.shared.EventAndRegattaDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public class DefaultRegattaCreateDialog extends AbstractRegattaWithSeriesAndFleetsDialog<EventAndRegattaDTO> {

    private SailingServiceAsync sailingService;
    private ErrorReporter errorReporter;

    public DefaultRegattaCreateDialog(Collection<RegattaDTO> existingRegattas, List<EventDTO> existingEvents,
            SailingServiceAsync sailingService, ErrorReporter errorReporter, StringMessages stringConstants,
            com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback<EventAndRegattaDTO> callback) {
        super(new RegattaDTO(), existingEvents, stringConstants.addRegatta(), stringConstants.ok(), stringConstants,
                null /*RegattaParameterValidator*/, callback);
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        setCourseAreaSelection();

    }

//    protected void setSeriesEditor() {
//        this.seriesEditor = new SeriesWithFleetsDefaultListEditor(Collections.<SeriesDTO> emptyList(), stringMessages,
//                resources.removeIcon(), /* enableFleetRemoval */true);
//    }

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
                        existingEvents.add(newEvent);
                        sailingEventsListBox.addItem(newEvent.getName());
                        sailingEventsListBox.setSelectedIndex(sailingEventsListBox.getItemCount() - 1);
                        courseAreaListBox.clear();
                        courseAreaListBox.addItem(stringMessages.selectCourseArea());
                        for (CourseAreaDTO courseArea : newEvent.venue.getCourseAreas()) {
                            courseAreaListBox.addItem(courseArea.getName());
                        }
                        if(courseAreaListBox.getItemCount()>1){
                            courseAreaListBox.setItemSelected(1, true);
                        }
                        courseAreaListBox.setEnabled(true);
                    }
                });
    }
    
    @Override
    protected RegattaDTO getResult() {
        RegattaDTO dto = super.getResult();
        dto.event = getSelectedEvent();
        return dto;
    }

    @Override
    protected EventAndRegattaDTO getResult() {
        EventAndRegattaDTO eventAndRegatta = new EventAndRegattaDTO(super.getSelectedEvent(),super.getRegattaDTO());
        return eventAndRegatta;
    }
    

}
