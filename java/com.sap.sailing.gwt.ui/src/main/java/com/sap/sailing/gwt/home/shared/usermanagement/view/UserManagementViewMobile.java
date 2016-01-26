package com.sap.sailing.gwt.home.shared.usermanagement.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.mobile.partials.section.MobileSection;
import com.sap.sailing.gwt.home.mobile.partials.sectionHeader.SectionHeaderContent;
import com.sap.sse.security.ui.authentication.UserManagementResources;
import com.sap.sse.security.ui.authentication.UserManagementSharedResources;
import com.sap.sse.security.ui.authentication.UserManagementResources.LocalCss;

public class UserManagementViewMobile extends Composite implements UserManagementView {
    
    private static final LocalCss LOCAL_CSS = UserManagementResources.INSTANCE.css(); 
    
    interface UserManagementViewUiBinder extends UiBinder<MobileSection, UserManagementViewMobile> {
    }
    
    private static UserManagementViewUiBinder uiBinder = GWT.create(UserManagementViewUiBinder.class);
    
    @UiField SectionHeaderContent sectionHeaderUi;
    private final MobileSection contentContainerUi;
    
    @UiField(provided = true)
    UserManagementSharedResources res = SharedResources.INSTANCE;

    public UserManagementViewMobile() {
        LOCAL_CSS.ensureInjected();
        super.initWidget(contentContainerUi = uiBinder.createAndBindUi(this));
    }
    
    @Override
    public void setHeading(String heading) {
        sectionHeaderUi.setSectionTitle(heading);
        sectionHeaderUi.setVisible(heading != null && !heading.isEmpty());
    }
    
    @Override
    public void setWidget(IsWidget w) {
        contentContainerUi.clearContent();
        if (w != null) {
            Widget widget = w.asWidget();
            widget.getElement().getStyle().setMarginTop(1, Unit.EM);
            contentContainerUi.addContent(widget);
        }
    }
    
}
