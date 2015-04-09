package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.racelog.tracking.QRCodeURLCreationException;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.QRCodeComposite;

public abstract class BaseQRIdentifierWidget implements IsWidget {
    
    protected final TextBox serverBox;
    protected final Grid inputGrid;
    private final QRCodeComposite qrCodeComposite;
    private final Widget baseWidget;
    private final Label error;
    private final Label url;

    public BaseQRIdentifierWidget(int qrCodeSize, StringMessages stringMessages) {
        this(qrCodeSize, stringMessages, QRCodeComposite.ERROR_CORRECTION_LEVEL_H);
    }
    
    public BaseQRIdentifierWidget(int qrCodeSize, StringMessages stringMessages, int errorCorrectionLevel) {
        serverBox = new TextBox();
        serverBox.setVisibleLength(40);
        serverBox.setValue(Window.Location.getProtocol() + "//" + Window.Location.getHost());
        serverBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                generateQRCode();
            }
        });
        serverBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                generateQRCode();
            }
        });
        
        inputGrid = new Grid(1, 2);
        inputGrid.setWidget(0, 0, new Label(stringMessages.serverUrl()+":"));
        inputGrid.setWidget(0, 1, serverBox);
        
        qrCodeComposite = new QRCodeComposite(qrCodeSize, errorCorrectionLevel);
        
        error = new Label();
        error.setStyleName("errorLabel");
        
        url = new Label();
        
        VerticalPanel panel = new VerticalPanel();
        panel.add(inputGrid);
        panel.add(qrCodeComposite);
        panel.setCellHorizontalAlignment(qrCodeComposite, HasHorizontalAlignment.ALIGN_CENTER);
        panel.add(error);
        panel.add(url);
        
        baseWidget = panel;
    }
    
    @Override
    public Widget asWidget() {
        return baseWidget;
    }

    protected abstract String generateEncodedQRCodeContent() throws QRCodeURLCreationException;
    
    public void generateQRCode() {
        try {
            String qrCodeUrl = generateEncodedQRCodeContent();
            qrCodeComposite.generateQRCode(qrCodeUrl);
            error.setText("");
            url.setText(qrCodeUrl);
        } catch (QRCodeURLCreationException e) {
            setError(e.getMessage());
        }
    }
    
    protected String getServerUrlWithoutFinalSlash() {
        String serverUrl = serverBox.getValue();
        if (serverUrl.endsWith("/")) {
            return serverUrl.substring(0, serverUrl.length() - 1);
        }
        return serverUrl;
    }
    
    protected void clear() {
        qrCodeComposite.clearQRCode();
        url.setText("");
    }
    
    protected void setError(String text) {
        error.setText(text);
        qrCodeComposite.clearQRCode();
    }
}
