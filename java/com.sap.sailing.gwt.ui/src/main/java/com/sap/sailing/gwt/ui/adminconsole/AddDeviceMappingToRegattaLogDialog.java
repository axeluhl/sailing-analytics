package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.racelog.tracking.DeviceMappingConstants;
import com.sap.sailing.domain.common.racelog.tracking.QRCodeURLCreationException;
import com.sap.sailing.gwt.ui.adminconsole.ItemToMapToDeviceSelectionPanel.SelectionChangedHandler;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.controls.GenericListBox;
import com.sap.sse.gwt.client.controls.GenericListBox.ValueBuilder;

public class AddDeviceMappingToRegattaLogDialog extends AbstractCancelableDialog {
    private DeviceMappingQRCodeWidget qrWidget;
    private ItemToMapToDeviceSelectionPanel itemSelectionPanel;
    private final String leaderboardName;

    public AddDeviceMappingToRegattaLogDialog(SailingServiceAsync sailingService, final ErrorReporter errorReporter,
            final StringMessages stringMessages, final String leaderboardName) {
        super(sailingService, stringMessages, errorReporter);
        this.leaderboardName = leaderboardName;
        setupUi();
        center();
    }

    @Override
    protected void addMainContent(Panel mainPanel) {
        super.addMainContent(mainPanel);

        CaptionPanel inputPanel = new CaptionPanel();
        Grid inputGrid = new Grid(1, 2);
        inputPanel.add(inputGrid);
        inputGrid.setWidget(0, 0, new Label(stringMessages.event()));
        final GenericListBox<EventDTO> events = new GenericListBox<EventDTO>(new ValueBuilder<EventDTO>() {
            @Override
            public String getValue(EventDTO item) {
                return item.getName();
            }
        });
        inputGrid.setWidget(0, 1, events);
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

        itemSelectionPanel = new ItemToMapToDeviceSelectionPanel(sailingService, stringMessages, errorReporter,
                new SelectionChangedHandler() {
                    @Override
                    public void onSelectionChange(MarkDTO mark) {
                        qrWidget.setMappedItem(DeviceMappingConstants.URL_MARK_ID_AS_STRING, mark.getIdAsString());
                        qrWidget.generateQRCode();
                    }

                    @Override
                    public void onSelectionChange(CompetitorDTO competitor) {
                        qrWidget.setMappedItem(DeviceMappingConstants.URL_COMPETITOR_ID_AS_STRING,
                                competitor.getIdAsString());
                        qrWidget.generateQRCode();
                    }
                }, null);

        // load table content
        sailingService.getCompetitorRegistrations(leaderboardName, itemSelectionPanel.getSetCompetitorsCallback());
        // TODO marks from RegattaLog also?

        qrWidget = new DeviceMappingQRCodeWidget(stringMessages, new DeviceMappingQRCodeWidget.URLFactory() {
            @Override
            public String createURL(String baseUrlWithoutTrailingSlash, String mappedItemType, String mappedItemId)
                    throws QRCodeURLCreationException {
                if (events.getValue() == null) {
                    throw new QRCodeURLCreationException("no event selected");
                }
                String eventIdAsString = events.getValue().id.toString();
                return DeviceMappingConstants.getDeviceMappingForRegattaLogUrl(baseUrlWithoutTrailingSlash, eventIdAsString,
                        leaderboardName, mappedItemType, mappedItemId);
            }
        });
        qrWidget.generateQRCode();

        VerticalPanel leftSidePanel = new VerticalPanel();
        leftSidePanel.add(inputPanel);

        CaptionPanel qrContentPanel = new CaptionPanel();
        leftSidePanel.add(qrContentPanel);
        qrContentPanel.add(qrWidget);

        HorizontalPanel panel = new HorizontalPanel();
        mainPanel.add(panel);
        panel.add(leftSidePanel);
        panel.add(itemSelectionPanel);
    }
}