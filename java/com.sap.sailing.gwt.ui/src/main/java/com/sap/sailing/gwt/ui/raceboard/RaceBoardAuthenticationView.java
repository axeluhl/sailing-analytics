package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.formfactor.DeviceDetector;
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
    
    @UiField(provided = true) RaceBoardResources res = RaceBoardResources.INSTANCE;

    private Presenter presenter;

    public RaceBoardAuthenticationView() {
        LOCAL_CSS.ensureInjected();
        popupPanel.addStyleName(LOCAL_CSS.flyover());
        super.initWidget(uiBinder.createAndBindUi(this));
        popupPanel.setWidget(this);
        popupPanel.setStyleName(res.mainCss().usermanagement_view());
        popupPanel.setStyleName(res.mainCss().usermanagement_mobile(), DeviceDetector.isMobile());
        
        popupPanel.addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> event) {
                presenter.onVisibilityChanged(false);
            }
        });
    }
    
    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
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
        popupPanel.show();
        presenter.onVisibilityChanged(true);
    }
    
    public void hide() {
        popupPanel.hide();
    }
    
    public boolean isShowing() {
        return popupPanel.isShowing();
    }

}
