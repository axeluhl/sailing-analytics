package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
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
 * A dialog to set up events inclusion for given remote sailing server instance. There is a checkBox per event. If
 * <code>includeRadio</code> is selected then just selected events will be loaded. If <code>excludeRadio</code> is
 * selected then selected events will not be loaded. For existing remote server instances by default
 * <code>excludeRadio</code> is selected with an empty list of selected events. For newly created instances
 * <code>include</code> flag is set to <code>true</code> by default with an empty selection list of events.
 * {@link completeRemoteServerReference} constructor parameter is a remote server DTO with all events loaded. It's
 * loaded on the fly at the moment user enters this dialog. {@link referenceDTO} field is a remote server instance which
 * is persisted in database having the list of selected events and {@link include} flag.
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
        final List<EventBaseDTO> selectedEvents = new ArrayList<>();
        for (int i = 0; i < allEvents.size(); i++) {
            CheckBox selectEventCheckBox = (CheckBox) eventsSelectionGrid.getWidget(i, 1);
            if (selectEventCheckBox.getValue()) {
                selectedEvents.add(allEvents.get(i));
            }
        }

        referenceDTO.setInclude(includeRadio.getValue());
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
        final VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setSpacing(10);
        createEventsInclusionTypePanel(mainPanel);
        addMultiSelectionPanel(mainPanel);
        setupEventsSelectionForm(mainPanel);
        return mainPanel;
    }

    private void addMultiSelectionPanel(final VerticalPanel mainPanel) {
        final HorizontalPanel multiSelectionPanel = new HorizontalPanel();
        multiSelectionPanel.setSpacing(3);
        final Button selectAllButton = new Button(stringMessages.selectAll());
        selectAllButton.addClickHandler(createMultiSelectionHandler(true));
        final Button deselectAllButton = new Button(stringMessages.deselectAll());
        deselectAllButton.addClickHandler(createMultiSelectionHandler(false));
        multiSelectionPanel.add(selectAllButton);
        multiSelectionPanel.add(deselectAllButton);
        mainPanel.add(multiSelectionPanel);
    }

    private ClickHandler createMultiSelectionHandler(final boolean checked) {
        return new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                for (int i = 0; i < eventsSelectionGrid.getRowCount(); i++) {
                    CheckBox selectEventCheckBox = (CheckBox) eventsSelectionGrid.getWidget(i, 1);
                    selectEventCheckBox.setValue(checked);
                }
            }
        };
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
        includeRadio = new RadioButton(eventsSelectionRadioGroupName, stringMessages.include());
        excludeRadio = new RadioButton(eventsSelectionRadioGroupName, stringMessages.exclude());

        eventsSelectionPanel.add(includeRadio);
        eventsSelectionPanel.add(excludeRadio);
        includeRadio.setValue(referenceDTO.isInclude());
        excludeRadio.setValue(!referenceDTO.isInclude());
        mainPanel.add(eventsSelectionPanel);
    }

}
