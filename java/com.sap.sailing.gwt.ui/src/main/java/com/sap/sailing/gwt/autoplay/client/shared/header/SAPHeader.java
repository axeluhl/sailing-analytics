package com.sap.sailing.gwt.autoplay.client.shared.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.FullscreenUtil;

public class SAPHeader extends Composite {
    private static SAPHeaderUiBinder uiBinder = GWT.create(SAPHeaderUiBinder.class);

    interface SAPHeaderUiBinder extends UiBinder<Widget, SAPHeader> {
    }

    @UiField DivElement pageTitleDiv;
    @UiField Button startFullScreenButton;
    
    public SAPHeader(String pageTitle, boolean startInAutoScreenMode) {
        SAPHeaderResources.INSTANCE.css().ensureInjected();
        
        initWidget(uiBinder.createAndBindUi(this));
        
        pageTitleDiv.setInnerText(pageTitle);
        startFullScreenButton.setVisible(startInAutoScreenMode);

        // the 'fullscreen' button should disappear after some seconds (10)
        if(startInAutoScreenMode) {
            Scheduler.get().scheduleFixedPeriod(new RepeatingCommand() {
                public boolean execute () {
                    startFullScreenButton.setVisible(false);
                    return false;
                }
              }, 10000);
        }
    }
    
   @UiHandler("startFullScreenButton") 
   void startFullScreenClicked(ClickEvent e) {
       FullscreenUtil.requestFullscreen();
   }
}
