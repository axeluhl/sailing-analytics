package com.sap.sailing.gwt.home.desktop.places.morelogininformation;

import java.util.Optional;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.places.morelogininformation.AbstractMoreLoginInformation;
import com.sap.sse.gwt.shared.ClientConfiguration;

/**
 * Desktop page that shows the benefits of logging in on sapsailing.com.
 */
public class MoreLoginInformationDesktop extends AbstractMoreLoginInformation {

    
    interface Binder extends UiBinder<Widget, MoreLoginInformationDesktop> {}
    private static final Binder BINDER = GWT.create(Binder.class);


    interface MoreLoginInformationUiBinder extends UiBinder<Widget, AbstractMoreLoginInformation> {
    }
    
    @UiField HeadingElement  headline;
    @UiField ParagraphElement intro;
    
    @UiField MoreLoginInformationContent sailorProfiles;
    @UiField MoreLoginInformationContent userSettings;
    @UiField MoreLoginInformationContent strategySimulator;
    @UiField MoreLoginInformationContent userNotifications;

    public MoreLoginInformationDesktop(Runnable registerCallback) {
        super(BINDER, registerCallback);
        if (ClientConfiguration.getInstance().isBrandingActive() ) {
            headline.setInnerText(applyBrandTitle(headline.getInnerText(), ClientConfiguration.getInstance().getBrandTitle(Optional.empty())));
            intro.setInnerText(applyBrandTitle(intro.getInnerText(), ClientConfiguration.getInstance().getBrandTitle(Optional.empty())));
            strategySimulator.setContent(applyBrandTitle(strategySimulator.getContent(), ClientConfiguration.getInstance().getBrandTitle(Optional.empty())));
            userSettings.configureImage(ClientConfiguration.getInstance().getMoreLoginInformationSettingsURL());
            sailorProfiles.configureImage(ClientConfiguration.getInstance().getMoreLoginInformationSailorProfilesURL());
            strategySimulator.configureImage(ClientConfiguration.getInstance().getMoreLoginInformationSimulatorURL());
            userNotifications.configureImage(ClientConfiguration.getInstance().getMoreLoginInformationNotificationsURL());
        }
    }
    
    private static String applyBrandTitle(String message, String brandTitle) {
        final String TOKEN = "Sailing Analytics";
        int idx = message.indexOf(TOKEN);
        String prefix = message.substring(0, idx);
        String already = brandTitle + " ";
        return prefix + already + message.substring(idx);
      }

}
