package com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.wrapper;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.SailorProfile;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.domain.SailorProfileEntry;
import com.sap.sailing.gwt.home.shared.usermanagement.decorator.AuthorizedContentDecoratorDesktop;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sse.gwt.resources.CommonControlsCSS;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;

public class SailorProfileWrapperViewImpl extends Composite implements SailorProfileWrapperView {

    interface MyBinder extends UiBinder<Widget, SailorProfileWrapperViewImpl> {
    }

    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    @UiField(provided = true) AuthorizedContentDecoratorDesktop decoratorUi;
    @UiField(provided = true) SailorProfile sailorProfileUi;
    
    private final FlagImageResolver flagImageResolver;

    public SailorProfileWrapperViewImpl(FlagImageResolver flagImageResolver) {
        this.flagImageResolver = flagImageResolver;
    }

    @Override
    public void setPresenter(Presenter presenter) {
        decoratorUi = new AuthorizedContentDecoratorDesktop(presenter);
        sailorProfileUi = new SailorProfile(flagImageResolver);
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    @Override
    protected void onLoad() {
        super.onLoad();
        CommonControlsCSS.ensureInjected();
    }
    
    @Override
    public NeedsAuthenticationContext getDecorator() {
        return decoratorUi;
    }

    @Override
    public void setProfileList(List<SailorProfileEntry> entries) {
        sailorProfileUi.setProfileList(entries);

    }
    
}
