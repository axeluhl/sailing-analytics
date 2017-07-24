package com.sap.sailing.gwt.home.desktop.partials.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.PopupPanel;
import com.sap.sse.security.ui.authentication.UserManagementResources;
import com.sap.sse.security.ui.authentication.UserManagementResources.LocalCss;

public class LoginPopup extends PopupPanel {
    private static final LocalCss LOCAL_CSS = UserManagementResources.INSTANCE.css();
    private static final LoginPopupViewResources.CSS CSS = LoginPopupViewResources.INSTANCE.css();
    private LoginPopupContent lpop;

    public interface LoginPopupViewResources extends ClientBundle {
        public static final LoginPopupViewResources INSTANCE = GWT.create(LoginPopupViewResources.class);

        @Source("LoginPopup.gss")
        CSS css();

        public interface CSS extends CssResource {
            String mainLayout();

            String mobileLayout();
        }
    }

    public LoginPopup(boolean desktop) {
        LOCAL_CSS.ensureInjected();
        CSS.ensureInjected();
        this.addStyleName(LOCAL_CSS.flyover());
        if(desktop){
            this.addStyleName(CSS.mainLayout());
        }else{
            this.addStyleName(CSS.mobileLayout());
        }
        ensureDebugId("loginpopupNewUser");
        setAutoHideEnabled(true);
        lpop = new LoginPopupContent();

        add(lpop);
    }

    public void doShow(Runnable onDispose, Runnable onMoreInfo) {
        show();
        lpop.getDismiss().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                hide();
                onDispose.run();
            }
        });
        lpop.getMoreInfo().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                hide();
                onMoreInfo.run();
            }
        });
    }
}
