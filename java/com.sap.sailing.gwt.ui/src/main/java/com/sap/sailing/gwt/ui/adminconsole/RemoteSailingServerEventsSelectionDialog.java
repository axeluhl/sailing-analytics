package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;
import com.sap.sailing.gwt.ui.shared.RemoteSailingServerReferenceDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * A dialog to set up events inclusion for given remote sailing server instance. By default all events are loaded and
 * {@link notSelected} radio button is checked. There is a checkBox per event. If {@link include} radio is selected then
 * just selected events will be loaded. If {@link exclude} radio is selected then selected events will not be loaded.
 * {@link completeRemoteServerReference} constructor parameter is a remote server DTO with all events loaded. It's
 * loaded on the fly at the moment user enters this dialog. {@link referenceDTO} field is a remote server instance which
 * is persisted in database having the list of selected events and {@link include} flag persisted.
 * 
 * @author Dmitry Bilyk
 *
 */
public class RemoteSailingServerEventsSelectionDialog extends DataEntryDialog<RemoteSailingServerReferenceDTO> {
    protected StringMessages stringMessages;
    private Grid eventsSelectionGrid;
    private List<EventBaseDTO> allEvents;
    private List<EventBaseDTO> selectedEvents;
    private RemoteSailingServerReferenceDTO referenceDTO;
    private RadioButton notSelectedRadio;
    private RadioButton includeRadio;
    private RadioButton excludeRadio;

    public RemoteSailingServerEventsSelectionDialog(RemoteSailingServerReferenceDTO completeRemoteServerReference,
            RemoteSailingServerReferenceDTO referenceDTO, StringMessages stringMessages,
            DialogCallback<RemoteSailingServerReferenceDTO> callback) {
        super(stringMessages.remoteServerEventsSelection(), null, stringMessages.ok(), stringMessages.cancel(), null,
                callback);
        this.stringMessages = stringMessages;
        this.referenceDTO = referenceDTO;
        this.allEvents = StreamSupport.stream(completeRemoteServerReference.getEvents().spliterator(), false)
                .collect(Collectors.toList());
        this.selectedEvents = referenceDTO.getSelectedEvents();
    }

    @Override
    protected RemoteSailingServerReferenceDTO getResult() {
        List<EventBaseDTO> selectedEvents = new ArrayList<>();
        for (int i = 0; i < allEvents.size(); i++) {
            CheckBox selecteEventCheckBox = (CheckBox) eventsSelectionGrid.getWidget(i, 1);
            if (selecteEventCheckBox.getValue()) {
                selectedEvents.add(allEvents.get(i));
            }
        }

        if (notSelectedRadio.getValue()) {
            referenceDTO.setInclude(null);
        } else if (includeRadio.getValue()) {
            referenceDTO.setInclude(true);
        } else {
            referenceDTO.setInclude(false);
        }
        referenceDTO.updateSelectedEvents(selectedEvents);
        return referenceDTO;
    }

    @Override
    protected Widget getAdditionalWidget() {
        if (allEvents.isEmpty()) {
            getStatusLabel().setText(stringMessages.eventsListIsEmpty());
            return null;
        }
        eventsSelectionGrid = new Grid(allEvents.size(), 2);
        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setSpacing(10);
        createEventsInclusionTypePanel(mainPanel);
        setupEventsSelectionForm(mainPanel);
        return mainPanel;
    }

    private void setupEventsSelectionForm(final VerticalPanel mainPanel) {
        eventsSelectionGrid.setCellSpacing(5);
        for (int i = 0; i < allEvents.size(); i++) {
            EventBaseDTO eventBaseDTO = allEvents.get(i);
            eventsSelectionGrid.setWidget(i, 0, new Label(eventBaseDTO.getName()));
            CheckBox eventCheckBox = new CheckBox();
            eventsSelectionGrid.setWidget(i, 1, eventCheckBox);
            eventCheckBox.setValue(selectedEvents.contains(allEvents.get(i)));
        }
        mainPanel.add(eventsSelectionGrid);
    }

    private void createEventsInclusionTypePanel(final VerticalPanel mainPanel) {
        HorizontalPanel eventsSelectionPanel = new HorizontalPanel();
        String eventsSelectionRadioGroupName = "eventsInclusionRadioGroup";
        notSelectedRadio = new RadioButton(eventsSelectionRadioGroupName, stringMessages.notSelected());
        includeRadio = new RadioButton(eventsSelectionRadioGroupName, stringMessages.include());
        excludeRadio = new RadioButton(eventsSelectionRadioGroupName, stringMessages.exclude());

        addValueChangedHandlers();

        eventsSelectionPanel.add(notSelectedRadio);
        eventsSelectionPanel.add(includeRadio);
        eventsSelectionPanel.add(excludeRadio);
        updateInclusionSelection();
        mainPanel.add(eventsSelectionPanel);
    }

    private void updateInclusionSelection() {
        if (referenceDTO.getInclude() == null) {
            notSelectedRadio.setValue(true);
            setEventsSelectionGridEnabled(false);
        } else if (referenceDTO.getInclude()) {
            includeRadio.setValue(true);
            setEventsSelectionGridEnabled(true);
        } else {
            excludeRadio.setValue(true);
            setEventsSelectionGridEnabled(true);
        }
    }

    private void addValueChangedHandlers() {
        notSelectedRadio.addValueChangeHandler(event -> {
            if (event.getValue()) {
                setEventsSelectionGridEnabled(false);
            }
        });
        includeRadio.addValueChangeHandler(selectedValueChangeHandler());
        excludeRadio.addValueChangeHandler(selectedValueChangeHandler());
    }

    private ValueChangeHandler<Boolean> selectedValueChangeHandler() {
        return event -> {
            setEventsSelectionGridEnabled(event.getValue());
        };
    }

    private void setEventsSelectionGridEnabled(final boolean enabled) {
        Style gridStyle = eventsSelectionGrid.getElement().getStyle();
        gridStyle.setProperty("opacity", enabled ? "1" : "0.6");
        gridStyle.setProperty("pointerEvents", enabled ? "auto" : "none");
    }

}
