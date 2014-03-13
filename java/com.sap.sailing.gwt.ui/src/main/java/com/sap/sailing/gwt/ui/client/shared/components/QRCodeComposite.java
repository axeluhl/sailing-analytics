package com.sap.sailing.gwt.ui.client.shared.components;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * When using this composite you need to include the following
 * 
 *      <script type="text/javascript" src="qrcode/qrcode.min.js"></script>
 */
public class QRCodeComposite extends Composite {
    
    private static final String qrCodeDivId = "qr-code";
    
    private int sizeInPixel;
    
    public QRCodeComposite(int sizeInPixel) {
        this.sizeInPixel = sizeInPixel;
        
        SimplePanel qrCodeContainer = new SimplePanel();
        qrCodeContainer.getElement().setId(qrCodeDivId);
        qrCodeContainer.setHeight(sizeInPixel + "px");
        qrCodeContainer.setWidth(sizeInPixel + "px");
        
        initWidget(qrCodeContainer);
    }
    
    @Override
    protected void onAttach() {
        super.onAttach();
        createQRCode(qrCodeDivId, sizeInPixel);
    }
    
    private native void createQRCode(String id, int size) /*-{
        $wnd.qrcode = new $wnd.QRCode(id, {
            text: "",
            width: size,
            height: size,
            colorDark : "#000000",
            colorLight : "#ffffff",
            correctLevel : $wnd.QRCode.CorrectLevel.H
        });
        $wnd.qrcode.clear();
    }-*/;
    
    public native void clearQRCode() /*-{
        $wnd.qrcode.clear();
    }-*/;
    
    public native void generateQRCode(String content) /*-{
        $wnd.qrcode.clear();
        $wnd.qrcode.makeCode(content);
    }-*/;

}
