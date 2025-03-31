package com.sap.sailing.gwt.ui.client.shared.controls;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * When using this wrapper you need to include the following
 * 
 * <script type="text/javascript" src="qrcode/qrcode.min.js"></script>
 * 
 * See: https://github.com/davidshimjs/qrcodejs/blob/master/qrcode.js#L120
 */
public class QRCodeWrapper {

    private final JavaScriptObject qrCodeObj;
    private String qrCodeContent = "";

    // L:1,M:0,Q:3,H:2
    public static final int ERROR_CORRECTION_LEVEL_M = 0;
    public static final int ERROR_CORRECTION_LEVEL_L = 1;
    public static final int ERROR_CORRECTION_LEVEL_H = 2;
    public static final int ERROR_CORRECTION_LEVEL_Q = 3;

    // https://github.com/davidshimjs/qrcodejs/blob/master/qrcode.js#L120

    public QRCodeWrapper(JavaScriptObject qrCodeObj) {
        this.qrCodeObj = qrCodeObj;
    }

    public static QRCodeWrapper wrap(IsWidget isWidget, int sizeInPixel, int errorCorrectionLevel) {
        return wrap(isWidget.asWidget().getElement(), sizeInPixel, errorCorrectionLevel);
    }

    public static QRCodeWrapper wrap(Element element, int sizeInPixel, int errorCorrectionLevel) {
        JavaScriptObject qrCodeObj = createQRCode(element, sizeInPixel, errorCorrectionLevel);
        return new QRCodeWrapper(qrCodeObj);
    }

    public void setQrCodeContent(String qrCodeContent) {
        this.qrCodeContent = qrCodeContent;
        updateQrCode(qrCodeObj, qrCodeContent);
    }

    public void clearQRCode() {
        this.clearQRCode(qrCodeObj);
    }

    private static native JavaScriptObject createQRCode(Element el, int size, int errorCorrectionLevel) /*-{
	var qrCode = new $wnd.QRCode(el, {
	    width : size,
	    height : size,
	    colorDark : "#000000",
	    colorLight : "#ffffff",
	    correctLevel : errorCorrectionLevel
	});
	return qrCode;
    }-*/;

    private native void updateQrCode(JavaScriptObject qrCode, String content) /*-{
	qrCode.clear();
	qrCode.makeCode(content);
    }-*/;

    private native void clearQRCode(JavaScriptObject qrCode) /*-{
	qrCode.clear();
    }-*/;
}