package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.domain.common.racelog.tracking.DeviceMappingConstants;
import com.sap.sailing.domain.common.racelog.tracking.QRCodeURLCreationException;
import com.sap.sailing.gwt.ui.client.GwtUrlHelper;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceIdentifierDTO;
import com.sap.sailing.gwt.ui.shared.DeviceMappingDTO;
import com.sap.sse.gwt.client.ErrorReporter;

public class RaceLogAddDeviceMappingDialog extends AbstractLogAddDeviceMappingDialog{
    private final String leaderboardName;
    private final String fleetName;
    private final String raceColumnName;

    public RaceLogAddDeviceMappingDialog(SailingServiceAsync sailingService, final ErrorReporter errorReporter,
            final StringMessages stringMessages, final String leaderboardName, final String raceColumnName,
            final String fleetName, DialogCallback<DeviceMappingDTO> callback, final DeviceMappingDTO mapping) {
        super(sailingService, errorReporter, stringMessages, callback, mapping);
        this.leaderboardName = leaderboardName;
        this.fleetName = fleetName;
        this.raceColumnName = raceColumnName;
        
        loadCompetitorsAndMarks();
    }
    
    @Override
    protected DeviceMappingDTO getResult() {
        String deviceTypeS = deviceType.getSelectedIndex() < 0 ? null : deviceType.getValue(deviceType
                .getSelectedIndex());
        DeviceIdentifierDTO deviceIdentifier = new DeviceIdentifierDTO(deviceTypeS, deviceId.getValue());
        return new DeviceMappingDTO(deviceIdentifier, from.getValue(), to.getValue(), selectedItem, null);
    }

    @Override
    protected DeviceMappingQRCodeWidget setupQRCodeWidget() {
        return new DeviceMappingQRCodeWidget(stringMessages, new DeviceMappingQRCodeWidget.URLFactory() {
            @SuppressWarnings("deprecation")
            @Override
            public String createURL(String baseUrlWithoutTrailingSlash, String mappedItemType, String mappedItemId)
                    throws QRCodeURLCreationException {
                if (from.getValue() == null && to.getValue() == null) {
                    throw new QRCodeURLCreationException(stringMessages.atMostOneEndOfTheTimeRangeMayBeOpen());
                }
                
                Long fromMillis = null;
                if (from.getValue() != null){
                    fromMillis = from.getValue().getTime();
                }
                
                Long toMillis = null;
                if (to.getValue() != null) {
                    toMillis = to.getValue().getTime();
                }
                
                return DeviceMappingConstants.getDeviceMappingForRaceLogUrl(baseUrlWithoutTrailingSlash,
                        leaderboardName, raceColumnName, fleetName, mappedItemType, mappedItemId, fromMillis, toMillis,
                        GwtUrlHelper.INSTANCE);
            }
        });
    }

    @Override
    protected void loadCompetitorsAndMarks() {
        sailingService.getCompetitorRegistrationsFromLogHierarchy(leaderboardName,
                itemSelectionPanel.getSetCompetitorsCallback());

        sailingService.getMarksInRaceLog(leaderboardName, raceColumnName, fleetName,
                itemSelectionPanel.getSetMarksCallback());
    }

}