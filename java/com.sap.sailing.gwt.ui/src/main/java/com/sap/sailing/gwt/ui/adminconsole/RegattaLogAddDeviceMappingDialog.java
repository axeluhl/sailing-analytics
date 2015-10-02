package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.racelog.tracking.DeviceMappingConstants;
import com.sap.sailing.domain.common.racelog.tracking.QRCodeURLCreationException;
import com.sap.sailing.gwt.ui.client.GwtUrlHelper;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceIdentifierDTO;
import com.sap.sailing.gwt.ui.shared.DeviceMappingDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.controls.GenericListBox;
import com.sap.sse.gwt.client.controls.GenericListBox.ValueBuilder;

public class RegattaLogAddDeviceMappingDialog extends AbstractLogAddDeviceMappingDialog {
    private final String leaderboardName;
    private final GenericListBox<EventDTO> events; 

    public RegattaLogAddDeviceMappingDialog(SailingServiceAsync sailingService, final ErrorReporter errorReporter,
            final StringMessages stringMessages, final String leaderboardName, DialogCallback<DeviceMappingDTO> callback, final DeviceMappingDTO mapping) {
        super(sailingService, errorReporter, stringMessages, callback, mapping);
        this.leaderboardName = leaderboardName;
        
        loadCompetitorsAndMarks();
        
        events = new GenericListBox<EventDTO>(new ValueBuilder<EventDTO>() {
            @Override
            public String getValue(EventDTO item) {
                return item.getName();
            }
        });
        
        events.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                qrWidget.generateQRCode();
            }
        });
        
        sailingService.getEventsForLeaderboard(leaderboardName, new AsyncCallback<Collection<EventDTO>>() {
            @Override
            public void onSuccess(Collection<EventDTO> result) {
                events.addItems(result);
            }

            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Could not load events: " + caught.getMessage());
            }
        });
    }

    @Override
    protected Widget getAdditionalWidget() {
        Widget panel = super.getAdditionalWidget();
        
        entryGrid.insertRow(0);
        entryGrid.setWidget(0, 0, new Label(stringMessages.event()));
        entryGrid.setWidget(0, 1, events);
        
        return panel;
    }

    @Override
    protected DeviceMappingQRCodeWidget setupQRCodeWidget() {
        return new DeviceMappingQRCodeWidget(stringMessages, new DeviceMappingQRCodeWidget.URLFactory() {
            @Override
            public String createURL(String baseUrlWithoutTrailingSlash, String mappedItemType, String mappedItemId)
                    throws QRCodeURLCreationException {
                if (events.getValue() == null) {
                    throw new QRCodeURLCreationException(stringMessages.noEventSelected());
                }
                String eventIdAsString = events.getValue().id.toString();
                return DeviceMappingConstants.getDeviceMappingForRegattaLogUrl(baseUrlWithoutTrailingSlash, eventIdAsString,
                        leaderboardName, mappedItemType, mappedItemId, GwtUrlHelper.INSTANCE);
            }
        });
    }

    @Override
    protected DeviceMappingDTO getResult() {
        String deviceTypeS = deviceType.getSelectedIndex() < 0 ? null : deviceType.getValue(deviceType
                .getSelectedIndex());
        DeviceIdentifierDTO deviceIdentifier = new DeviceIdentifierDTO(deviceTypeS, deviceId.getValue());
        return new DeviceMappingDTO(deviceIdentifier, from.getValue(), to.getValue(), selectedItem, null);
    }

    @Override
    protected void loadCompetitorsAndMarks() {
        sailingService.getCompetitorRegistrationsFromLogHierarchy(leaderboardName, itemSelectionPanel.getSetCompetitorsCallback());
        // fetching marks definitions from the race logs; note that currently there are no mark *definitions*
        // on regatta logs although there can be mark/device assignments on a regatta log which then gives the
        // mark a position that applies across all races in the regatta-like thing
        sailingService.getMarksInRaceLogsAndTrackedRaces(leaderboardName, itemSelectionPanel.getSetMarksCallback());
    }
}