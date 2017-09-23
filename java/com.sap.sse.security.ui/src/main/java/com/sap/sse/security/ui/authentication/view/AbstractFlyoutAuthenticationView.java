package com.sap.sse.security.ui.authentication.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.common.CommonSharedResources;
import com.sap.sse.security.ui.authentication.UserManagementResources;
import com.sap.sse.security.ui.authentication.UserManagementResources.LocalCss;

/**
 * Abstract implementation of {@link FlyoutAuthenticationView} providing default functionality and styles. 
 */
public abstract class AbstractFlyoutAuthenticationView implements FlyoutAuthenticationView {
    
    private static final LocalCss LOCAL_CSS = UserManagementResources.INSTANCE.css(); 
    
    interface AuthenticationViewUiBinder extends UiBinder<Widget, AbstractFlyoutAuthenticationView> {
    }
    
    private static AuthenticationViewUiBinder uiBinder = GWT.create(AuthenticationViewUiBinder.class);
    
    protected final PopupPanel popupPanel = new PopupPanel(true, false);
    
    @UiField DivElement headingUi;
    @UiField protected DivElement flyoverContentUi;
    @UiField SimplePanel contentContainerUi;
    
    private Presenter presenter;

    /**
     * Creates a new {@link AbstractFlyoutAuthenticationView} with the given style resources.
     * 
     * @param res the {@link CommonSharedResources} to use
     */
    public AbstractFlyoutAuthenticationView(CommonSharedResources res) {
        LOCAL_CSS.ensureInjected();
        popupPanel.ensureDebugId("authenticationView");
        popupPanel.addStyleName(LOCAL_CSS.flyover());
        popupPanel.setWidget(uiBinder.createAndBindUi(this));
        
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
    
    protected Presenter getPresenter() {
        return presenter;
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
    
    @Override
    public void hide() {
        popupPanel.hide();
    }
    
    @Override
    public boolean isShowing() {
        return popupPanel.isShowing();
    }

    @Override
    public Widget asWidget() {
        return popupPanel.getWidget();
    }
}
