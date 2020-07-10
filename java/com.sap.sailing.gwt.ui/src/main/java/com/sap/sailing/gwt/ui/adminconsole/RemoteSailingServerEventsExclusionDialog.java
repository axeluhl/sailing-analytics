package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
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
 * A dialog to exclude events from the list of loaded events of remote sailing server instance. Events are included by
 * default on UI. If user excludes the event it will not be loaded from the remote sailing server and will not be visible
 * on the requesting server
 * 
 * @author Dmitry Bilyk
 *
 */
public class RemoteSailingServerEventsExclusionDialog extends DataEntryDialog<RemoteSailingServerReferenceDTO> {
    protected StringMessages stringMessages;
    private Grid formGrid;
    private List<EventBaseDTO> events;
    private RemoteSailingServerReferenceDTO referenceDTO;

    public RemoteSailingServerEventsExclusionDialog(RemoteSailingServerReferenceDTO referenceDTO,
            StringMessages stringMessages, DialogCallback<RemoteSailingServerReferenceDTO> callback) {
        super(stringMessages.remoteServerEventsExclusion(), null, stringMessages.ok(), stringMessages.cancel(), null,
                callback);
        this.stringMessages = stringMessages;
        this.referenceDTO = referenceDTO;
        events = StreamSupport.stream(referenceDTO.getEvents().spliterator(), false).collect(Collectors.toList());
    }

    @Override
    protected RemoteSailingServerReferenceDTO getResult() {
        List<EventBaseDTO> excludedEvents = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            RadioButton excludedRadio = (RadioButton) formGrid.getWidget(i + 1, 2);
            if (excludedRadio.getValue()) {
                excludedEvents.add(events.get(i));
            }
        }
        referenceDTO.addExcludedEvents(excludedEvents);
        return referenceDTO;
    }

    @Override
    protected Widget getAdditionalWidget() {
        if (events.isEmpty()) {
            FlowPanel flowPanel = new FlowPanel();
            flowPanel.getElement().getStyle().setMargin(20, Unit.PX);
            setDialogMessage(stringMessages.eventsListIsEmpty(), flowPanel);
            return flowPanel;
        }
        formGrid = new Grid(events.size() + 1, 3);
        formGrid.setCellSpacing(3);
        createGridHeader();
        for (int i = 0; i < events.size(); i++) {
            EventBaseDTO eventBaseDTO = events.get(i);
            int eventPosition = i + 1;
            formGrid.setWidget(eventPosition, 0, new Label(eventBaseDTO.getName()));
            String radioGroupName = eventBaseDTO.getId().toString();
            RadioButton includedRadio = new RadioButton(radioGroupName);
            RadioButton excludedRadio = new RadioButton(radioGroupName);
            includedRadio.setValue(true);
            formGrid.setWidget(eventPosition, 1, includedRadio);
            formGrid.setWidget(eventPosition, 2, excludedRadio);
        }
        return formGrid;
    }

    private void createGridHeader() {
        formGrid.setWidget(0, 0, new Label(stringMessages.eventName()));
        formGrid.setWidget(0, 1, new Label(stringMessages.included()));
        formGrid.setWidget(0, 2, new Label(stringMessages.excluded()));
    }

}
