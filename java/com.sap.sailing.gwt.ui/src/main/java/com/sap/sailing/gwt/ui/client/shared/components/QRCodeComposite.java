package com.sap.sailing.gwt.ui.client.shared.components;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * When using this composite you need to include the following
 * 
 *      <script type="text/javascript" src="qrcode/qrcode.min.js"></script>
 */
public class QRCodeComposite extends Composite {
    //https://github.com/davidshimjs/qrcodejs/blob/master/qrcode.js#L120
    //L:1,M:0,Q:3,H:2
    public static final int ERROR_CORRECTION_LEVEL_L = 1;
    public static final int ERROR_CORRECTION_LEVEL_M = 0;
    public static final int ERROR_CORRECTION_LEVEL_Q = 3;
    public static final int ERROR_CORRECTION_LEVEL_H = 2;
    
    private static final String qrCodeDivId = "qr-code";
    
    private int sizeInPixel;
    private int errorCorrectionLevel;
    
    public QRCodeComposite(int sizeInPixel) {
        this(sizeInPixel, ERROR_CORRECTION_LEVEL_H);
    }
    
    public QRCodeComposite(int sizeInPixel, int errorCorrectionLevel) {
        this.sizeInPixel = sizeInPixel;
        this.errorCorrectionLevel = errorCorrectionLevel;
        
        SimplePanel qrCodeContainer = new SimplePanel();
        qrCodeContainer.getElement().setId(qrCodeDivId);
        qrCodeContainer.setHeight(sizeInPixel + "px");
        qrCodeContainer.setWidth(sizeInPixel + "px");
        
        initWidget(qrCodeContainer);
    }
    
    @Override
    protected void onAttach() {
        super.onAttach();
        createQRCode(qrCodeDivId, sizeInPixel, errorCorrectionLevel);
    }
    
    private native void createQRCode(String id, int size, int errorCorrectionLevel) /*-{
        $wnd.qrcode = new $wnd.QRCode(id, {
            text: "",
            width: size,
            height: size,
            colorDark : "#000000",
            colorLight : "#ffffff",
            correctLevel : errorCorrectionLevel
        });
        $wnd.qrcode.clear();
    }-*/;
    
    public native void clearQRCode() /*-{
        if ($wnd.qrcode != null){ //if exists
            $wnd.qrcode.clear();
        }
    }-*/;
    
    public native void generateQRCode(String content) /*-{
        $wnd.qrcode.clear();
        $wnd.qrcode.makeCode(content);
    }-*/;

}
