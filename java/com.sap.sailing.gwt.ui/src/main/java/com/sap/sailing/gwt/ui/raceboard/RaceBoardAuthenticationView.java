package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.common.CommonSharedResources;
import com.sap.sse.security.ui.authentication.UserManagementResources;
import com.sap.sse.security.ui.authentication.UserManagementResources.LocalCss;
import com.sap.sse.security.ui.authentication.view.FlyoutAuthenticationView;

public class RaceBoardAuthenticationView extends Composite implements FlyoutAuthenticationView {
    
    private static final LocalCss LOCAL_CSS = UserManagementResources.INSTANCE.css(); 
    
    interface RaceBoardAuthenticationViewUiBinder extends UiBinder<Widget, RaceBoardAuthenticationView> {
    }
    
    private static RaceBoardAuthenticationViewUiBinder uiBinder = GWT.create(RaceBoardAuthenticationViewUiBinder.class);
    
    private final PopupPanel popupPanel = new PopupPanel(true, false);
    
    @UiField DivElement headingUi;
    @UiField SimplePanel contentContainerUi;
    
    @UiField(provided = true) CommonSharedResources res = RaceBoardResources.INSTANCE;

    private HandlerRegistration windowResizeHandler;

    public RaceBoardAuthenticationView() {
        LOCAL_CSS.ensureInjected();
        popupPanel.addStyleName(LOCAL_CSS.flyover());
        super.initWidget(uiBinder.createAndBindUi(this));
        popupPanel.setWidget(this);
    }
    
    @Override
    public void setAutoHidePartner(IsWidget autoHidePartner) {
        popupPanel.addAutoHidePartner(autoHidePartner.asWidget().getElement());
    }
    
    @Override
    public void setHeading(String heading) {
        headingUi.setInnerText(heading);
        UIObject.setVisible(headingUi, heading != null && !heading.isEmpty());
    }
    
    @Override
    public void setWidget(IsWidget w) {
        contentContainerUi.setWidget(w);
    }
    
    public void show() {
        popupPanel.setPopupPositionAndShow(new PositionCallback() {
            @Override
            public void setPosition(int offsetWidth, int offsetHeight) {
                Element anchor = Document.get().getElementById("usrMngmtFlyover");
                if (anchor != null) {
                    int left = anchor.getAbsoluteLeft() + anchor.getOffsetWidth() - offsetWidth + 10;
                    popupPanel.setPopupPosition(left, anchor.getAbsoluteTop() + 20);
                }
            }
        });
        if (windowResizeHandler == null) {
            windowResizeHandler = Window.addResizeHandler(new ResizeHandler() {
                @Override
                public void onResize(ResizeEvent event) {
                    RaceBoardAuthenticationView.this.show();
                }
            });
        }
    }
    
    public void hide() {
        popupPanel.hide();
        if (windowResizeHandler != null) {
            windowResizeHandler.removeHandler();
        }
    }
    
    public boolean isShowing() {
        return popupPanel.isShowing();
    }

}
