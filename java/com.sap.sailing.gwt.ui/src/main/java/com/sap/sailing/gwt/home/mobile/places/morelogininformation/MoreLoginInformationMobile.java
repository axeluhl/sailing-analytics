package com.sap.sailing.gwt.home.mobile.places.morelogininformation;

import java.util.Optional;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.places.morelogininformation.AbstractMoreLoginInformation;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.shared.ClientConfiguration;

/**
 * Mobile page that shows the benefits of logging in on sapsailing.com.
 */
public class MoreLoginInformationMobile extends AbstractMoreLoginInformation {
    private static final StringMessages I18N = StringMessages.INSTANCE;

    interface Binder extends UiBinder<Widget, MoreLoginInformationMobile> {}
    private static final Binder BINDER = GWT.create(Binder.class);


    interface MoreLoginInformationUiBinder extends UiBinder<Widget, AbstractMoreLoginInformation> {
    }
    @UiField HeadingElement  headline;
    @UiField ParagraphElement intro;
    
    @UiField MoreLoginInformationContentMobile sailorProfiles;
    @UiField MoreLoginInformationContentMobile userSettings;
    @UiField MoreLoginInformationContentMobile strategySimulator;
    @UiField MoreLoginInformationContentMobile userNotifications;

    public MoreLoginInformationMobile(Runnable registerCallback) {
        super(BINDER, registerCallback);
        if (ClientConfiguration.getInstance().isBrandingActive() ) {
            headline.setInnerText(I18N.moreLoginInformationHeadline(ClientConfiguration.getInstance().getBrandTitle(Optional.empty()) + " "));
            intro.setInnerText(I18N.moreLoginInformationIntroduction(ClientConfiguration.getInstance().getBrandTitle(Optional.empty()) + " "));
            strategySimulator.setContent(I18N.moreLoginInformationSectionStrategySimulatorDescription(ClientConfiguration.getInstance().getBrandTitle(Optional.empty()) + " "));
            userSettings.configureImage(ClientConfiguration.getInstance().getMoreLoginInformationSettingsURL());
            sailorProfiles.configureImage(ClientConfiguration.getInstance().getMoreLoginInformationSailorProfilesURL());
            strategySimulator.configureImage(ClientConfiguration.getInstance().getMoreLoginInformationSimulatorURL());
            userNotifications.configureImage(ClientConfiguration.getInstance().getMoreLoginInformationNotificationsURL());
        } else {
            headline.setInnerText(I18N.moreLoginInformationHeadline(""));
            intro.setInnerText(I18N.moreLoginInformationIntroduction(""));
            strategySimulator.setContent(I18N.moreLoginInformationSectionStrategySimulatorDescription(""));
        }
    }
}
