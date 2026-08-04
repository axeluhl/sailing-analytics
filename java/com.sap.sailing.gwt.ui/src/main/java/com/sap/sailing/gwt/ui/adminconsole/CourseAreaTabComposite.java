package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.dto.CourseAreaDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.landscape.common.SharedLandscapeConstants;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.controls.busyindicator.BusyIndicator;
import com.sap.sse.gwt.client.controls.busyindicator.SimpleBusyIndicator;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class CourseAreaTabComposite extends Composite {
    private final CourseAreaListInlineEditorComposite courseAreaList;

    public CourseAreaTabComposite(final Collection<EventDTO> existingEvents,
            final SailingServiceWriteAsync sailingServiceWrite, final StringMessages stringMessages) {
        courseAreaList = new CourseAreaListInlineEditorComposite(Collections.<CourseAreaDTO> emptyList(),
                new CourseAreaListInlineEditorComposite.ExpandedUi(stringMessages, IconResources.INSTANCE.removeIcon(),
                        SuggestedCourseAreaNames.suggestedCourseAreaNames, stringMessages.enterCourseAreaName(), 30));
        final VerticalPanel courseAreasPanel = new VerticalPanel();
        courseAreasPanel.setSpacing(5);
        final Map<String, EventDTO> eventsCache = new HashMap<>();
        for (final EventDTO event : existingEvents) {
            eventsCache.put(event.getId().toString(), event);
        }
        final ListBox eventDropDown = new ListBox();
        eventDropDown.addItem(stringMessages.pleaseSelectAnEvent());
        final List<EventDTO> sortedExistingEvents = new ArrayList<>(existingEvents);
        sortedExistingEvents.sort(Comparator.comparing(EventDTO::getName));
        for (final EventDTO event : sortedExistingEvents) {
            eventDropDown.addItem(event.getName(), event.getId().toString());
        }
        final Button appendButton = new Button(stringMessages.appendCourseAreas());
        appendButton.setEnabled(false);
        appendButton.addStyleName("btn-secondary");
        final Button clearAllButton = new Button(stringMessages.clearAllCourseAreas());
        clearAllButton.setEnabled(false);
        clearAllButton.addStyleName("btn-secondary");
        eventDropDown.addChangeHandler(e -> {
            final boolean hasSelection = eventDropDown.getSelectedIndex() > 0;
            appendButton.setEnabled(hasSelection);
            clearAllButton.setEnabled(hasSelection);
        });
        appendButton.addClickHandler(e -> {
            final EventDTO selected = eventsCache.get(eventDropDown.getValue(eventDropDown.getSelectedIndex()));
            if (selected != null) {
                new CourseAreaSelectionDialog(selected.getVenue().getCourseAreas(), stringMessages,
                        new DataEntryDialog.DialogCallback<List<CourseAreaDTO>>() {
                    @Override
                    public void ok(final List<CourseAreaDTO> result) {
                        final List<CourseAreaDTO> updated = new ArrayList<>(courseAreaList.getValue());
                        for (final CourseAreaDTO area : result) {
                            updated.add(new CourseAreaDTO(UUID.randomUUID(), area.getName(), area.getCenterPosition(), area.getRadius()));
                        }
                        courseAreaList.setValue(updated);
                    }
                    @Override
                    public void cancel() {}
                }).show();
            }
        });
        clearAllButton.addClickHandler(e -> courseAreaList.setValue(new ArrayList<>()));
        final TextBox remoteBaseUrlBox = new TextBox();
        remoteBaseUrlBox.setValue(SharedLandscapeConstants.DEFAULT_SAILING_SERVER_URL);
        remoteBaseUrlBox.setWidth("300px");
        remoteBaseUrlBox.getElement().setPropertyString("placeholder", stringMessages.remoteServerUrlPlaceholder());
        final TextBox bearerTokenOrNullBox = new TextBox();
        bearerTokenOrNullBox.setWidth("200px");
        bearerTokenOrNullBox.getElement().setPropertyString("placeholder", stringMessages.bearerTokenPlaceholder());
        final BusyIndicator remoteLoadBusyIndicator = new SimpleBusyIndicator();
        final Button loadRemoteEventsButton = new Button(stringMessages.loadRemoteEvents());
        loadRemoteEventsButton.addClickHandler(e -> {
            eventDropDown.clear();
            eventDropDown.addItem(stringMessages.pleaseSelectAnEvent());
            eventsCache.clear();
            final String baseUrl = remoteBaseUrlBox.getValue().trim();
            if (baseUrl.isEmpty()) {
                final List<EventDTO> sorted = new ArrayList<>(existingEvents);
                sorted.sort(Comparator.comparing(EventDTO::getName));
                for (final EventDTO event : sorted) {
                    eventsCache.put(event.getId().toString(), event);
                    eventDropDown.addItem(event.getName(), event.getId().toString());
                }
                eventDropDown.setEnabled(true);
            } else {
                remoteLoadBusyIndicator.setBusy(true);
                eventDropDown.setEnabled(false);
                final String bearerTokenOrNull = bearerTokenOrNullBox.getValue().trim().isEmpty() ? null : bearerTokenOrNullBox.getValue().trim();
                sailingServiceWrite.getRemoteEvents(baseUrl, bearerTokenOrNull,
                        new AsyncCallback<List<EventDTO>>() {
                    @Override
                    public void onSuccess(final List<EventDTO> result) {
                        remoteLoadBusyIndicator.setBusy(false);
                        result.sort(Comparator.comparing(EventDTO::getName));
                        for (final EventDTO event : result) {
                            eventsCache.put(event.getId().toString(), event);
                            eventDropDown.addItem(event.getName(), event.getId().toString());
                        }
                        eventDropDown.setEnabled(true);
                    }
                    @Override
                    public void onFailure(final Throwable caught) {
                        remoteLoadBusyIndicator.setBusy(false);
                        eventDropDown.clear();
                        eventDropDown.addItem(caught.getMessage());
                    }
                });
            }
        });
        final VerticalPanel copyPanel = new VerticalPanel();
        copyPanel.setWidth("100%");
        copyPanel.setSpacing(3);
        final Grid remoteInputGrid = new Grid(2, 3);
        remoteInputGrid.setWidget(0, 0, new Label(stringMessages.copyCourseAreasFromAnotherUrl()));
        remoteInputGrid.setWidget(0, 1, remoteBaseUrlBox);
        final HorizontalPanel loadRow = new HorizontalPanel();
        loadRow.setSpacing(3);
        loadRow.add(loadRemoteEventsButton);
        loadRow.add(remoteLoadBusyIndicator);
        remoteInputGrid.setWidget(0, 2, loadRow);
        remoteInputGrid.setWidget(1, 0, new Label(stringMessages.bearerTokenOrNullForRemoteEvents()));
        remoteInputGrid.setWidget(1, 1, bearerTokenOrNullBox);
        eventDropDown.setWidth("100%");
        final HorizontalPanel actionButtons = new HorizontalPanel();
        actionButtons.setSpacing(3);
        actionButtons.add(appendButton);
        actionButtons.add(clearAllButton);
        final HorizontalPanel eventDropDownRow = new HorizontalPanel();
        eventDropDownRow.setSpacing(3);
        eventDropDownRow.setWidth("100%");
        eventDropDownRow.add(new Label(stringMessages.event() + ":"));
        eventDropDownRow.add(eventDropDown);
        eventDropDownRow.add(actionButtons);
        copyPanel.add(remoteInputGrid);
        copyPanel.add(eventDropDownRow);
        courseAreasPanel.add(copyPanel);
        courseAreasPanel.add(courseAreaList);
        initWidget(courseAreasPanel);
    }

    public List<CourseAreaDTO> getValue() {
        return courseAreaList.getValue();
    }

    public void setValue(final List<CourseAreaDTO> value) {
        courseAreaList.setValue(value);
    }

    public void addValueChangeHandler(final ValueChangeHandler<Iterable<CourseAreaDTO>> handler) {
        courseAreaList.addValueChangeHandler(handler);
    }
}
