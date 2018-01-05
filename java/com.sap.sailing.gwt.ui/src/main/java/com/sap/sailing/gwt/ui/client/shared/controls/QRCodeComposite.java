package com.sap.sailing.gwt.ui.client.shared.controls;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * When using this composite you need to include the following
 * 
 * <script type="text/javascript" src="qrcode/qrcode.min.js"></script>
 */
public class QRCodeComposite extends Composite {
    private final static String qrCodeDivId = "qr-code";
    private final QRCodeWrapper qrCodeWrapper;

    public QRCodeComposite(int sizeInPixel) {
        this(sizeInPixel, QRCodeWrapper.ERROR_CORRECTION_LEVEL_H);
    }

    public QRCodeComposite(int sizeInPixel, int errorCorrectionLevel) {
        SimplePanel qrCodeContainer = new SimplePanel();
        qrCodeContainer.getElement().setId(qrCodeDivId);
        qrCodeContainer.setHeight(sizeInPixel + "px");
        qrCodeContainer.setWidth(sizeInPixel + "px");
        //
        initWidget(qrCodeContainer);
        qrCodeWrapper = QRCodeWrapper.wrap(qrCodeContainer, sizeInPixel, errorCorrectionLevel);
    }

    public void clearQRCode() {
        qrCodeWrapper.clearQRCode();
    }

    public void setQrCodeContent(String qrCodeContent) {
        qrCodeWrapper.setQrCodeContent(qrCodeContent);
    }
}
