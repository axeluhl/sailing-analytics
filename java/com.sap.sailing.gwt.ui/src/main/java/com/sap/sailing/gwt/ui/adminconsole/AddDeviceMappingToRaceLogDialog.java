package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Date;
import java.util.List;

import com.github.gwtbootstrap.datetimepicker.client.ui.base.HasViewMode.ViewMode;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.racelog.RaceLogServletConstants;
import com.sap.sailing.domain.common.racelog.tracking.DeviceMappingConstants;
import com.sap.sailing.domain.common.racelog.tracking.MappableToDevice;
import com.sap.sailing.gwt.ui.adminconsole.DeviceMappingQRCodeWidget.QRCodeURLCreationException;
import com.sap.sailing.gwt.ui.adminconsole.ItemToMapToDeviceSelectionPanel.SelectionChangedHandler;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.BetterDateTimeBox;
import com.sap.sailing.gwt.ui.shared.DeviceIdentifierDTO;
import com.sap.sailing.gwt.ui.shared.DeviceMappingDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class AddDeviceMappingToRaceLogDialog extends DataEntryDialog<DeviceMappingDTO> {
    private final BetterDateTimeBox from;
    private final BetterDateTimeBox to;
    private final ListBox deviceType;
    private final TextBox deviceId;
    private final DeviceMappingQRCodeWidget qrWidget;
    private final StringMessages stringMessages;
    private MappableToDevice selectedItem;
    private final ItemToMapToDeviceSelectionPanel itemSelectionPanel;

    public AddDeviceMappingToRaceLogDialog(SailingServiceAsync sailingService, final ErrorReporter errorReporter,
            final StringMessages stringMessages, final String leaderboardName, final String raceColumnName,
            final String fleetName, DialogCallback<DeviceMappingDTO> callback, final DeviceMappingDTO mapping) {
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

        from = initTimeBox();
        from.setValue(null);
        to = initTimeBox();
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
        
        itemSelectionPanel = new ItemToMapToDeviceSelectionPanel(sailingService, stringMessages, errorReporter,
                new SelectionChangedHandler() {
                    @Override
                    public void onSelectionChange(MarkDTO mark) {
                        qrWidget.setMappedItem(DeviceMappingConstants.URL_MARK_ID_AS_STRING, mark.getIdAsString());
                        validate();
                    }

                    @Override
                    public void onSelectionChange(CompetitorDTO competitor) {
                        qrWidget.setMappedItem(DeviceMappingConstants.URL_COMPETITOR_ID_AS_STRING,
                                competitor.getIdAsString());
                        validate();
                    }
                }, mapping != null ? mapping.mappedTo : null);

        //load table content
        sailingService.getCompetitorRegistrations(leaderboardName, raceColumnName, fleetName,
                itemSelectionPanel.getSetCompetitorsCallback());

        sailingService.getMarksInRaceLog(leaderboardName, raceColumnName, fleetName,
                itemSelectionPanel.getSetMarksCallback());

        if (mapping != null) {
            deviceId.setValue(mapping.deviceIdentifier.deviceId);
            from.setValue(mapping.from);
            to.setValue(mapping.to);
        }

        qrWidget = new DeviceMappingQRCodeWidget(stringMessages, new DeviceMappingQRCodeWidget.URLFactory() {
            @SuppressWarnings("deprecation")
            @Override
            public String createURL(String baseUrlWithoutTrailingSlash, String mappedItemQueryParam)
                    throws QRCodeURLCreationException {
                if (from.getValue() == null) {
                    throw new QRCodeURLCreationException("from not set");
                }
                if (to.getValue() == null) {
                    throw new QRCodeURLCreationException("to not set");
                }
                long fromMillis = from.getValue().getTime();
                long toMillis = to.getValue().getTime();
                if (fromMillis > toMillis) {
                    throw new QRCodeURLCreationException("from cannt lie after to");
                }
                return baseUrlWithoutTrailingSlash + DeviceMappingConstants.APK_PATH + "?"
                        + RaceLogServletConstants.PARAMS_LEADERBOARD_NAME + "=" + DeviceMappingQRCodeWidget.encode(leaderboardName) + "&"
                        + RaceLogServletConstants.PARAMS_RACE_COLUMN_NAME + "=" + DeviceMappingQRCodeWidget.encode(raceColumnName)
                        + "&" + RaceLogServletConstants.PARAMS_RACE_FLEET_NAME + "=" + DeviceMappingQRCodeWidget.encode(fleetName)
                        + "&" + mappedItemQueryParam + "&" + DeviceMappingConstants.URL_FROM_MILLIS + "=" + fromMillis
                        + "&" + DeviceMappingConstants.URL_TO_MILLIS + "=" + toMillis;
            }
        });
        qrWidget.generateQRCode();
    }

    @Override
    protected Widget getAdditionalWidget() {
        HorizontalPanel panel = new HorizontalPanel();
        VerticalPanel leftSidePanel = new VerticalPanel();
        Grid entryGrid = new Grid(4, 2);
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

        return panel;
    }

    private BetterDateTimeBox initTimeBox() {
        final BetterDateTimeBox timeBox = new BetterDateTimeBox();
        timeBox.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                validate();
            }
        });
        timeBox.addAttachHandler(new Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if (event.isAttached()) {
                    addAutoHidePartner(timeBox.getPicker());
                }
            }
        });
        timeBox.setAutoClose(true);
        timeBox.setStartView(ViewMode.HOUR);
        timeBox.setFormat("dd/mm/yyyy hh:ii");
        return timeBox;
    }

    @Override
    protected DeviceMappingDTO getResult() {
        String deviceTypeS = deviceType.getSelectedIndex() < 0 ? null : deviceType.getValue(deviceType
                .getSelectedIndex());
        DeviceIdentifierDTO deviceIdentifier = new DeviceIdentifierDTO(deviceTypeS, deviceId.getValue());
        return new DeviceMappingDTO(deviceIdentifier, from.getValue(), to.getValue(), selectedItem, null);
    }

}
