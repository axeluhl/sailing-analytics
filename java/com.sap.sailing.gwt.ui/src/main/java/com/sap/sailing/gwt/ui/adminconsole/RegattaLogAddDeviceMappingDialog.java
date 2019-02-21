package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.BranchIOConstants;
import com.sap.sailing.domain.common.MailInvitationType;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.racelog.tracking.DeviceMappingConstants;
import com.sap.sailing.domain.common.racelog.tracking.MappableToDevice;
import com.sap.sailing.domain.common.racelog.tracking.QRCodeURLCreationException;
import com.sap.sailing.gwt.ui.adminconsole.ItemToMapToDeviceSelectionPanel.SelectionChangedHandler;
import com.sap.sailing.gwt.ui.client.DataEntryDialogWithDateTimeBox;
import com.sap.sailing.gwt.ui.client.GwtUrlHelper;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceIdentifierDTO;
import com.sap.sailing.gwt.ui.shared.DeviceMappingDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.controls.GenericListBox;
import com.sap.sse.gwt.client.controls.GenericListBox.ValueBuilder;
import com.sap.sse.gwt.client.controls.datetime.DateAndTimeInput;
import com.sap.sse.gwt.client.controls.datetime.DateTimeInput.Accuracy;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.ui.client.UserService;

public class RegattaLogAddDeviceMappingDialog extends DataEntryDialogWithDateTimeBox<DeviceMappingDTO> {
    private final String leaderboardName;
    private final GenericListBox<EventDTO> events; 

    protected final DateAndTimeInput from;
    protected final DateAndTimeInput to;
    protected final ListBox deviceType;
    protected final TextBox deviceId;
    protected final DeviceMappingQRCodeWidget qrWidget;
    protected final StringMessages stringMessages;
    protected MappableToDevice selectedItem;
    protected final ItemToMapToDeviceSelectionPanel itemSelectionPanel;
    protected final SailingServiceAsync sailingService;
    protected Grid entryGrid;
    private String regattaRegisterSecret;

    public RegattaLogAddDeviceMappingDialog(SailingServiceAsync sailingService, final UserService userService, final ErrorReporter errorReporter,
            final StringMessages stringMessages, String leaderboardName, String regattaRegisterSecret, final MailInvitationType mailInvitationType,
            DialogCallback<DeviceMappingDTO> callback,
            final DeviceMappingDTO mapping) {
        super(stringMessages.add(stringMessages.deviceMappings()), stringMessages.add(stringMessages.deviceMappings()),
                stringMessages.add(), stringMessages.cancel(), new DataEntryDialog.Validator<DeviceMappingDTO>() {
                    @Override
                    public String getErrorMessage(DeviceMappingDTO valueToValidate) {
                        String deviceType = valueToValidate.deviceIdentifier.deviceType;
                        String deviceId = valueToValidate.deviceIdentifier.deviceId;
                        if (deviceType == null || deviceType.isEmpty()) {
                            return stringMessages.pleaseEnterA(stringMessages.deviceType());
                        }
                        if (deviceId == null || deviceId.isEmpty()) {
                            return stringMessages.pleaseEnterA(stringMessages.deviceId());
                        }
                        if ((valueToValidate.from == null || valueToValidate.from.compareTo(new Date(Long.MIN_VALUE)) == 0)
                                && (valueToValidate.to == null || valueToValidate.to
                                        .compareTo(new Date(Long.MAX_VALUE)) == 0)) {
                            return stringMessages.atMostOneEndOfTheTimeRangeMayBeOpen();
                        }
                        if (valueToValidate.from != null && valueToValidate.to != null
                                && valueToValidate.to.before(valueToValidate.from)) {
                            return stringMessages.startOfTimeRangeMustLieBeforeEnd();
                        }
                        
                        if (valueToValidate.mappedTo == null) {
                            return stringMessages.pleaseSelectAnItemToMapTo();
                        }
                        return null;
                    }
                }, true, callback);
        this.stringMessages = stringMessages;
        this.sailingService = sailingService;
        this.regattaRegisterSecret = regattaRegisterSecret;
        from = createDateTimeBox(new Date(), Accuracy.SECONDS);
        from.setValue(null);
        to = createDateTimeBox(new Date(), Accuracy.SECONDS);
        to.setValue(null);

        deviceType = createListBox(false);
        sailingService.getDeserializableDeviceIdentifierTypes(new AsyncCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> result) {
                String typeToPreselect = mapping != null ? mapping.deviceIdentifier.deviceType : null;
                int i = 0;
                for (String type : result) {
                    deviceType.addItem(type);
                    if (type.equals(typeToPreselect)) {
                        deviceType.setSelectedIndex(i);
                    }
                    i++;
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Could not load deserializable device identifier types: "
                        + caught.getMessage());
            }
        });
        deviceId = createTextBox("");
        itemSelectionPanel = new ItemToMapToDeviceSelectionPanel(sailingService, userService, stringMessages, errorReporter,
                new SelectionChangedHandler() {
                    @Override
                    public void onSelectionChange(MarkDTO mark) {
                        selectedItem = mark;
                        qrWidget.setMappedItem(DeviceMappingConstants.URL_MARK_ID_AS_STRING, mark.getIdAsString());
                        validateAndUpdate();
                    }

                    @Override
                    public void onSelectionChange(CompetitorDTO competitor) {
                        selectedItem = competitor;
                        qrWidget.setMappedItem(DeviceMappingConstants.URL_COMPETITOR_ID_AS_STRING,
                                competitor.getIdAsString());
                        validateAndUpdate();
                    }
                    
                    @Override
                    public void onSelectionChange(BoatDTO boat) {
                        selectedItem = boat;
                        qrWidget.setMappedItem(DeviceMappingConstants.URL_BOAT_ID_AS_STRING,
                                boat.getIdAsString());
                        validateAndUpdate();
                    }
                }, mapping != null ? mapping.mappedTo : null);
        if (mapping != null) {
            deviceId.setValue(mapping.deviceIdentifier.deviceId);
            from.setValue(mapping.from);
            to.setValue(mapping.to);
        }
        qrWidget = setupQRCodeWidget(mailInvitationType);
        qrWidget.generateQRCode();
        this.leaderboardName = leaderboardName;
        loadCompetitorsBoatsAndMarks();
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
        HorizontalPanel panel = new HorizontalPanel();
        VerticalPanel leftSidePanel = new VerticalPanel();
        entryGrid = new Grid(4, 2);
        CaptionPanel entryPanel = new CaptionPanel(stringMessages.mappingDetails());
        CaptionPanel qrPanel = new CaptionPanel(stringMessages.qrCode());
        entryGrid.setWidget(0, 0, new Label(stringMessages.deviceType()));
        entryGrid.setWidget(0, 1, deviceType);
        entryGrid.setWidget(1, 0, new Label(stringMessages.deviceId()));
        entryGrid.setWidget(1, 1, deviceId);
        entryGrid.setWidget(2, 0, new Label(stringMessages.from()));
        entryGrid.setWidget(2, 1, from);
        entryGrid.setWidget(3, 0, new Label(stringMessages.to()));
        entryGrid.setWidget(3, 1, to);
        VerticalPanel qrContentPanel = new VerticalPanel();
        Label explanation = new Label(stringMessages.deviceMappingQrCodeExplanation());
        explanation.setWidth("400px");
        qrContentPanel.add(explanation);
        qrContentPanel.add(qrWidget);
        panel.add(leftSidePanel);
        panel.add(itemSelectionPanel);
        leftSidePanel.add(entryPanel);
        leftSidePanel.add(qrPanel);
        entryPanel.setContentWidget(entryGrid);
        qrPanel.setContentWidget(qrContentPanel);
        entryGrid.insertRow(0);
        entryGrid.setWidget(0, 0, new Label(stringMessages.event()));
        entryGrid.setWidget(0, 1, events);
        return panel;
    }

    private DeviceMappingQRCodeWidget setupQRCodeWidget(final MailInvitationType mailInvitationType) {
        return new DeviceMappingQRCodeWidget(stringMessages, new DeviceMappingQRCodeWidget.URLFactory() {
            @Override
            public String createURL(String baseUrlWithoutTrailingSlash, String mappedItemType, String mappedItemId)
                    throws QRCodeURLCreationException {
                if (events.getValue() == null) {
                    throw new QRCodeURLCreationException(stringMessages.noEventSelected());
                }

                final String eventIdAsString = events.getValue().id.toString();
                String url = DeviceMappingConstants.getDeviceMappingForRegattaLogUrl(baseUrlWithoutTrailingSlash,
                        eventIdAsString, leaderboardName, mappedItemType, mappedItemId, regattaRegisterSecret,
                        GwtUrlHelper.INSTANCE);
                if (mailInvitationType == MailInvitationType.LEGACY) {
                    // URL is already legacy
                } else if (mailInvitationType == MailInvitationType.SailInsight1) {
                    url = BranchIOConstants.SAILINSIGHT_APP_BRANCHIO + "?"
                            + BranchIOConstants.SAILINSIGHT_APP_BRANCHIO_PATH + "="
                            + GwtUrlHelper.INSTANCE.encodeQueryString(url);
                } else if (mailInvitationType == MailInvitationType.SailInsight2) {
                    url = BranchIOConstants.SAILINSIGHT_2_APP_BRANCHIO + "?"
                            + BranchIOConstants.OPEN_REGATTA_2_APP_BRANCHIO_PATH + "="
                            + GwtUrlHelper.INSTANCE.encodeQueryString(url);
                }
                else {
                    throw new QRCodeURLCreationException("Unknown mail invitation type: " + mailInvitationType);
                }

                return url;
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

    private void loadCompetitorsBoatsAndMarks() {
        sailingService.getCompetitorRegistrationsForLeaderboard(leaderboardName, itemSelectionPanel.getSetCompetitorsCallback());
        sailingService.getBoatRegistrationsForLeaderboard(leaderboardName, itemSelectionPanel.getSetBoatsCallback());
        sailingService.getMarksInRegattaLog(leaderboardName, itemSelectionPanel.getSetMarksCallback());
    }
}
