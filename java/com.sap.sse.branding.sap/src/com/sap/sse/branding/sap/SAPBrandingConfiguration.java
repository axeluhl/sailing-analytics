package com.sap.sse.branding.sap;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;

import com.sap.sse.branding.shared.BrandingConfiguration;
import com.sap.sse.i18n.ResourceBundleStringMessages;

public class SAPBrandingConfiguration implements BrandingConfiguration {
    public static final String ID = "SAP";
    private String defaultBrandingLogoURL;
    private String greyTransparentLogoURL;
    private final ResourceBundleStringMessages sailingServerStringMessages;
    private static final String STRING_MESSAGES_BASE_NAME = "stringmessages/SAPBrandingStringMessages";

    public SAPBrandingConfiguration() {
        sailingServerStringMessages = ResourceBundleStringMessages.create(STRING_MESSAGES_BASE_NAME, getClass().getClassLoader(),
                StandardCharsets.UTF_8.name());
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDefaultBrandingLogoURL(Optional<String> locale) {
        return defaultBrandingLogoURL;
    }

    @Override
    public String getGreyTransparentLogoURL(Optional<String> locale) {
        return greyTransparentLogoURL;
    }

    @Override
    public String getBrandTitle(Optional<String> locale) {
        return "SAP";
    }
    
    @Override
    public String getSolutionsInSailingImageURL() {
        return "/sap-branding/images/solutions-sap-in-sailing.jpg";
    }

    @Override
    public String getSoutionsInSailingTrimmedImageURL() {
        return "/sap-branding/images/solutions-sap-trimmed.png";
    }

    @Override
    public String getSailingRaceManagerAppImageURL() {
        return "/sap-branding/images/solutions-sap-sailing-race-manager.png";
    }

    @Override
    public String getSailingRaceManagerAppTrimmedImageURL() {
        return "/sap-branding/images/solutions-race.png";
    }

    @Override
    public String getSailInSightAppImageURL() {
        return "/sap-branding/images/solutions-sap-sailing-insight.png";
    }
    
    @Override
    public String getSailingSimulatorImageURL() {
        return "/sap-branding/images/solutions-simulator.png";
    }

    @Override
    public String getSailingSimulatorTrimmedImageURL() {
        return "/sap-branding/images/solutions-simulator-trimmed.png";
    }

    @Override
    public String getBuoyPingerAppImageURL() {
        return "/sap-branding/images/solutions-sap-sailing-buoy-pinger.png";
    }

    @Override
    public String getSailingAnalyticsImageURL() {
        return "/sap-branding/images/solutions-sap.png";
    }

    @Override
    public String getSailingAnalyticsReadMoreText(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l->Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "sailingAnalyticsReadMore");
    }
}
