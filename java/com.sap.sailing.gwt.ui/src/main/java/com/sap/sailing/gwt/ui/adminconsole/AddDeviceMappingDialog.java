package com.sap.sailing.gwt.ui.adminconsole;

import java.io.Serializable;
import java.util.Collection;
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
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.racelog.tracking.DeviceMappingConstants;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.BetterDateTimeBox;
import com.sap.sailing.gwt.ui.shared.DeviceIdentifierDTO;
import com.sap.sailing.gwt.ui.shared.DeviceMappingDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class AddDeviceMappingDialog extends DataEntryDialog<DeviceMappingDTO> {
    private final BetterDateTimeBox from;
    private final BetterDateTimeBox to;
    private final ListBox deviceType;
    private final TextBox deviceId;
    private final DeviceMappingQRIdentifierWidget qrWidget;
    private final CompetitorTableWrapper<SingleSelectionModel<CompetitorDTO>> competitorTable;
    private final MarkTableWrapper<SingleSelectionModel<MarkDTO>> markTable;
    private final StringMessages stringMessages;
    
    private Serializable selectedItem;

    public AddDeviceMappingDialog(SailingServiceAsync sailingService, final ErrorReporter errorReporter,
            final StringMessages stringMessages, String leaderboardName, String raceColumnName, String fleetName,
            DialogCallback<DeviceMappingDTO> callback, final DeviceMappingDTO mapping) {
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
                        && (valueToValidate.to == null || valueToValidate.to.compareTo(new Date(Long.MAX_VALUE)) == 0)) {
                    return stringMessages.atMostOneEndOfTheTimeRangeMayBeOpen();
                }
                if (valueToValidate.from != null && valueToValidate.to != null &&
                        valueToValidate.to.before(valueToValidate.from)) {
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
        from.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                qrWidget.setFromMillis(event.getValue().getTime());
            }
        });
        to.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                qrWidget.setToMillis(event.getValue().getTime());
            }
        });
        
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
                errorReporter.reportError("Could not load deserializable device identifier types: " + caught.getMessage());
            }
        });
        
        deviceId = createTextBox("");
        
        final SingleSelectionModel<MarkDTO> markSelectionModel = new SingleSelectionModel<MarkDTO>();
        competitorTable = new CompetitorTableWrapper<>(sailingService, stringMessages, errorReporter,
                new SingleSelectionModel<CompetitorDTO>(), true);
        markTable = new MarkTableWrapper<SingleSelectionModel<MarkDTO>>(
                markSelectionModel, sailingService, stringMessages, errorReporter);
        
        competitorTable.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if (competitorTable.getSelectionModel().getSelectedSet().size() == 1) {
                    CompetitorDTO selectedCompetitor = competitorTable.getSelectionModel().
                            getSelectedSet().iterator().next();
                    selectedItem = selectedCompetitor;
                    deselectAll(markTable.getSelectionModel(), markTable.getDataProvider().getList());
                    qrWidget.setMappedItem(DeviceMappingConstants.COMPETITOR_ID_AS_STRING,
                            selectedCompetitor.getIdAsString());
                    validate();
                }
            }
        });
        
        markTable.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if (markSelectionModel.getSelectedSet().size() == 1) {
                    MarkDTO selectedMark = markSelectionModel.getSelectedSet().iterator().next();
                    selectedItem = selectedMark;
                    deselectAll(competitorTable.getSelectionModel(), competitorTable.getAllCompetitors());
                    qrWidget.setMappedItem(DeviceMappingConstants.MARK_ID_AS_STRING,
                            selectedMark.getIdAsString());
                    validate();
                }
            }
        });
        
        sailingService.getCompetitorRegistrations(leaderboardName, raceColumnName, fleetName, new AsyncCallback<Collection<CompetitorDTO>>() {
            @Override
            public void onSuccess(Collection<CompetitorDTO> result) {
                competitorTable.refreshCompetitorList(result);
                if (mapping != null && mapping.mappedTo instanceof CompetitorDTO) {
                    //got new DTOs with new object identities, so have to go through them one by one
                    CompetitorDTO old = (CompetitorDTO) mapping.mappedTo;
                    for (CompetitorDTO inList : competitorTable.getAllCompetitors()) {
                        if (old.getIdAsString().equals(inList.getIdAsString())) {
                            competitorTable.getSelectionModel().setSelected(inList, true);
                        }
                    }
                }
            }
            
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Could not load competitors: " + caught.getMessage());
            }
        });
        
        sailingService.getMarksInRaceLog(leaderboardName, raceColumnName, fleetName, new AsyncCallback<Collection<MarkDTO>>() {
            @Override
            public void onSuccess(Collection<MarkDTO> result) {
                markTable.refresh(result);
                if (mapping != null && mapping.mappedTo instanceof MarkDTO) {
                    //got new DTOs with new object identities, so have to go through them one by one
                    MarkDTO old = (MarkDTO) mapping.mappedTo;
                    for (MarkDTO inList : markTable.getDataProvider().getList()) {
                        if (old.getIdAsString().equals(inList.getIdAsString())) {
                            markTable.getSelectionModel().setSelected(inList, true);
                        }
                    }
                }
            }
            
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Could not load marks: " + caught.getMessage());
            }
        });
        
        if (mapping != null) {
            deviceId.setValue(mapping.deviceIdentifier.deviceId);
            from.setValue(mapping.from);
            to.setValue(mapping.to);
        }
        
        qrWidget = new DeviceMappingQRIdentifierWidget(leaderboardName, raceColumnName, fleetName, stringMessages);
        qrWidget.generateQRCode();
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        HorizontalPanel panel = new HorizontalPanel();
        VerticalPanel tablesPanel = new VerticalPanel();
        VerticalPanel leftSidePanel = new VerticalPanel();
        Grid entryGrid = new Grid(4, 2);
        CaptionPanel entryPanel = new CaptionPanel(stringMessages.mappingDetails());
        CaptionPanel qrPanel = new CaptionPanel(stringMessages.qrCode());
        CaptionPanel marksPanel = new CaptionPanel(stringMessages.mark());
        CaptionPanel competitorsPanel = new CaptionPanel(stringMessages.competitor());
        
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
        panel.add(tablesPanel);
        tablesPanel.add(marksPanel);
        tablesPanel.add(competitorsPanel);
        leftSidePanel.add(entryPanel);
        leftSidePanel.add(qrPanel);
        
        marksPanel.setContentWidget(markTable.asWidget());
        competitorsPanel.setContentWidget(competitorTable.asWidget());
        entryPanel.setContentWidget(entryGrid);
        qrPanel.setContentWidget(qrContentPanel);
        
        return panel;
    }
    
    private static <T> void deselectAll(SelectionModel<T> selectionModel, Iterable<T> list) {
        for (T t : list) {
            selectionModel.setSelected(t, false);
        }
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
        String deviceTypeS = deviceType.getSelectedIndex() < 0 ? null : deviceType.getValue(deviceType.getSelectedIndex());
        DeviceIdentifierDTO deviceIdentifier = new DeviceIdentifierDTO(deviceTypeS, deviceId.getValue());
        return new DeviceMappingDTO(deviceIdentifier, from.getValue(), to.getValue(),
                selectedItem, null);
    }

}
