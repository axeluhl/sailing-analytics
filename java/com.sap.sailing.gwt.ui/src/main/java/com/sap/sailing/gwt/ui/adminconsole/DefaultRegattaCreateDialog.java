package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.EventAndRegattaDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;

public class DefaultRegattaCreateDialog extends
		AbstractRegattaWithSeriesAndFleetsDialog<EventAndRegattaDTO> {

	private static AdminConsoleResources resources = GWT
			.create(AdminConsoleResources.class);

	private final SailingServiceAsync sailingService;
	private final ErrorReporter errorReporter;
	private final Iterable<RegattaDTO> defaultRegattas;

	public DefaultRegattaCreateDialog(
			Iterable<RegattaDTO> selectedRegattas,
			SailingServiceAsync sailingService,
			ErrorReporter errorReporter,
			StringMessages stringConstants,
			com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback<EventAndRegattaDTO> callback) {
		super(new RegattaDTO(), new ArrayList<EventDTO>(),
				"Create default settings for all regattas...", stringConstants
						.ok(), stringConstants,
				null /* RegattaParameterValidator */, callback);
		this.sailingService = sailingService;
		this.errorReporter = errorReporter;
		this.defaultRegattas = selectedRegattas;
		setCourseAreaSelection();
		setSeriesEditor();
	}

	protected void setSeriesEditor() {
		List<SeriesDTO> series = new ArrayList<SeriesDTO>();
		for (RegattaDTO regattaDTO : defaultRegattas) {
			series.addAll(regattaDTO.series);
		}
		seriesEditor = new SeriesWithFleetsDefaultListEditor(
				series, stringMessages, resources.removeIcon(), /* enableFleetRemoval */
				true);
	}

	protected void setupAdditionalWidgetsOnPanel(final VerticalPanel panel) {
//		Button newEventBtn = new Button("Create New Event");
//		newEventBtn.addClickHandler(new ClickHandler() {
//
//			@Override
//			public void onClick(ClickEvent event) {
//				openEventCreateDialog();
//			}
//		});
//
//		Grid grid = (Grid) panel.getWidget(0);
//		Grid newGrid = new Grid(6, 3);
//		for (int i = 0; i < grid.getColumnCount(); i++) {
//			for (int j = 0; j < grid.getRowCount(); j++) {
//				newGrid.setWidget(j, i, grid.getWidget(j, i));
//			}
//		}
//
//		newGrid.setWidget(3, 2, newEventBtn);
//		panel.remove(0);
//		panel.add(newGrid);

		TabPanel tabPanel = new TabPanel();
		tabPanel.setWidth("100%");
		tabPanel.add(seriesEditor, stringMessages.series());
		tabPanel.selectTab(0);
		panel.add(tabPanel);
	}

	private void openEventCreateDialog() {
		EventCreateDialog dialog = new EventCreateDialog(existingEvents,
				stringMessages, new DialogCallback<EventDTO>() {
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
		sailingService.createEvent(newEvent.getName(), newEvent.startDate,
				newEvent.endDate, newEvent.venue.getName(), newEvent.isPublic,
				courseAreaNames, newEvent.getImageURLs(),
				newEvent.getVideoURLs(), newEvent.getSponsorImageURLs(),
				newEvent.getLogoImageURL(), newEvent.getOfficialWebsiteURL(),
				new AsyncCallback<EventDTO>() {

					@Override
					public void onFailure(Throwable t) {
						errorReporter
								.reportError("Error trying to create new event "
										+ newEvent.getName()
										+ ": "
										+ t.getMessage());
					}

					@Override
					public void onSuccess(EventDTO newEvent) {
						existingEvents.add(newEvent);
						sailingEventsListBox.addItem(newEvent.getName());
						sailingEventsListBox
								.setSelectedIndex(sailingEventsListBox
										.getItemCount() - 1);
						courseAreaListBox.clear();
						courseAreaListBox.addItem(stringMessages
								.selectCourseArea());
						for (CourseAreaDTO courseArea : newEvent.venue
								.getCourseAreas()) {
							courseAreaListBox.addItem(courseArea.getName());
						}
						courseAreaListBox.setItemSelected(1, true);
						courseAreaListBox.setEnabled(true);
					}
				});
	}

	@Override
	protected EventAndRegattaDTO getResult() {
		EventAndRegattaDTO eventAndRegatta = new EventAndRegattaDTO(
				super.getSelectedEvent(), super.getRegattaDTO());
		return eventAndRegatta;
	}

}
