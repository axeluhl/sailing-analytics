package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.http.client.URL;
import com.sap.sailing.domain.common.racelog.tracking.DeviceMappingConstants;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.QRCodeComposite;

public class DeviceMappingQRCodeWidget extends BaseQRIdentifierWidget {
    
    private static final int qrCodeSize = 320;
    private static final int errorCorrectionLevel = QRCodeComposite.ERROR_CORRECTION_LEVEL_L;
    
    private String mappedItemId;
    private String mappedItemType;
    
    static class QRCodeURLCreationException extends Exception {
        private static final long serialVersionUID = -8243995470804772458L;
        public QRCodeURLCreationException(String message) {
            super(message);
        }
    }
    
    static interface URLFactory {
        String createURL(String baseUrlWithoutTrailingSlash, String mappedItemQueryParam)
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
        String serverUrl = getServerUrlWithoutFinalSlash();
        if (serverUrl.isEmpty()) {
            throw new QRCodeURLCreationException("Server URL empty");
        }
        if (mappedItemId == null) {
            throw new QRCodeURLCreationException("No item selected for mapping");
        }
        
        return urlFactory.createURL(serverUrl, mappedItemType + "=" + mappedItemId);
    }
}
