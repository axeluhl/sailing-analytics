package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.http.client.URL;
import com.sap.sailing.domain.common.racelog.tracking.DeviceMappingConstants;
import com.sap.sailing.domain.common.racelog.tracking.QRCodeURLCreationException;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.QRCodeWrapper;

public class DeviceMappingQRCodeWidget extends BaseQRIdentifierWidget {
    
    private static final int qrCodeSize = 320;
    private static final int errorCorrectionLevel = QRCodeWrapper.ERROR_CORRECTION_LEVEL_L;
    
    private String mappedItemId;
    private String mappedItemType;
    
    static interface URLFactory {
        String createURL(String baseUrlWithoutTrailingSlash, String mappedItemType, String mappedItemId)
            throws QRCodeURLCreationException;
    }
    
    private final URLFactory urlFactory;

    public DeviceMappingQRCodeWidget(StringMessages stringMessages,
            URLFactory urlFactory) {
        super(qrCodeSize, stringMessages, errorCorrectionLevel);
        this.urlFactory = urlFactory;
    }
    
    /**
     * @param mappedItemType one of the param names in {@link DeviceMappingConstants}
     */
    public void setMappedItem(String mappedItemType, String mappedItemIdAsString) {
        this.mappedItemId = URL.encode(mappedItemIdAsString);
        this.mappedItemType = mappedItemType;
        generateQRCode();
    }
    
    @Override
    protected String generateEncodedQRCodeContent() throws QRCodeURLCreationException {
        if (!isServerUrlValid()){
            super.url.setText("");
            throw new QRCodeURLCreationException(stringMessages.serverURLInvalid());
        }
        
        String serverUrl = getServerUrlWithoutFinalSlash();
        if (serverUrl.isEmpty()) {
            throw new QRCodeURLCreationException(stringMessages.serverURLEmpty());
        }
        if (mappedItemId == null) {
            throw new QRCodeURLCreationException(stringMessages.pleaseSelectAnItemToMapTo());
        }
        
        return urlFactory.createURL(serverUrl, mappedItemType, mappedItemId);
    }
}
