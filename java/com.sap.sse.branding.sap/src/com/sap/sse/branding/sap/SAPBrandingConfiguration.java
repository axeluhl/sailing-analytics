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
    
    @Override
    public String getSailingAnalyticsSailing(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l->Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "sailingAnalyticsSailing");
    }

    @Override
    public String getFooterCopyright() {
        return "@ 2011-2025 SAP Sailing Analytics";
    }

    @Override
    public String getFooterLegalLink() {
        return "/gwt/Home.html#/imprint/:";
    }

    @Override
    public String getFooterPrivacyLink() {
        return "https://www.sap.com/about/legal/privacy.html?campaigncode=CRM-XH21-OSP-Sailing";
    }

    @Override
    public String getFooterJobsLink() {
        return "https://jobs.sapsailing.com";
    }

    @Override
    public String getFooterSupportLink() {
        return "https://support.sapsailing.com";
    }

    @Override
    public String getFooterWhatsNewLink() {
        return "/gwt/Home.html#WhatsNewPlace:navigationTab=SailingAnalytics";
    }

    @Override
    public String getSportsOn(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l->Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "sportsOn");
    }

    @Override
    public String getFollowSports(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l->Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "followSports");
    }

    @Override
    public String getFacebookLink() {
        return "https://www.facebook.com/SAP";
    }

    @Override
    public String getxLink() {
        return "https://x.com/sap";
    }

    @Override
    public String getInstagramLink() {
        return "https://www.instagram.com/sap/";
    }
    @Override
    public String getWelcomeToSailingAnalytics(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l->Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "welcomeToSailingAnalytics");
    }

    @Override
    public String getWelcomeToSailingAnalyticsBody(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l->Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "welcomeToSailingAnalyticsBody");
    }

    @Override
    public String getEventBaseURL(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l->Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "eventBaseURL");
    }
    
    @Override
    public String getSolutions1Headline(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l -> Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "solutions1Headline");
    }

    @Override
    public String getSolutions2Headline(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l -> Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "solutions2Headline");
    }

    @Override
    public String getSolutions3Headline(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l -> Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "solutions3Headline");
    }

    @Override
    public String getSolutions4Headline(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l -> Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "solutions4Headline");
    }

    @Override
    public String getSolutions5Headline(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l -> Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "solutions5Headline");
    }

    @Override
    public String getSolutions6Headline(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l -> Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "solutions6Headline");
    }

    @Override
    public String getSolutions1Title(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l -> Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "solutions1Title");
    }

    @Override
    public String getContentSolutions11(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l -> Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "contentSolutions11");
    }

    @Override
    public String getContentSolutions12(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l -> Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "contentSolutions12");
    }

    @Override
    public String getContentSolutions13(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l -> Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "contentSolutions13");
    }

    @Override
    public String getContentSolutions14(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l -> Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "contentSolutions14");
    }

    @Override
    public String getContentSolutions15(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l -> Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "contentSolutions15");
    }

    @Override
    public String getContentSolutions17(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l -> Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "contentSolutions17");
    }

    @Override
    public String getContentSolutions18(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l -> Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "contentSolutions18");
    }

    @Override
    public String getContentSolutions19(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l -> Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "contentSolutions19");
    }

    @Override
    public String getContentSolutions110(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l -> Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "contentSolutions110");
    }

    @Override
    public String getContentSolutions111(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l -> Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "contentSolutions111");
    }

    @Override
    public String getContentSolutions112(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l -> Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "contentSolutions112");
    }

    @Override
    public String getContentSolutions113(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l -> Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "contentSolutions113");
    }

    @Override
    public String getSolutions2Title(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l -> Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "solutions2Title");
    }

    @Override
    public String getContentSolutions21(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l -> Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "contentSolutions21");
    }

    @Override
    public String getContentSolutions22(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l -> Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "contentSolutions22");
    }

    @Override
    public String getSolutions3Title(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l -> Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "solutions3Title");
    }

    @Override
    public String getContentSolutions3(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l -> Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "contentSolutions3");
    }

    @Override
    public String getSolutions3ReadMore(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l -> Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "solutions3ReadMore");
    }

    @Override
    public String getSolutions4Title(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l -> Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "solutions4Title");
    }

    @Override
    public String getContentSolutions4(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l -> Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "contentSolutions4");
    }

    @Override
    public String getSolutions4ReadMore(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l -> Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "solutions4ReadMore");
    }

    @Override
    public String getSolutions5Title(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l -> Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "solutions5Title");
    }

    @Override
    public String getContentSolutions5(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l -> Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "contentSolutions5");
    }

    @Override
    public String getSolutions5ReadMore(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l -> Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "solutions5ReadMore");
    }

    @Override
    public String getSolutions6Title(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l -> Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "solutions6Title");
    }

    @Override
    public String getContentSolutions6(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l -> Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "contentSolutions6");
    }

    @Override
    public String getSolutions6ReadMore(Optional<String> locale) {
        return sailingServerStringMessages.get(locale.map(l -> Locale.forLanguageTag(l)).orElse(Locale.ENGLISH), "solutions6ReadMore");
    }
    
    @Override
    public String getSolutions3PlayStoreURL() {
        return "https://play.google.com/store/apps/details?id=com.sap.sailing.racecommittee.app&pli=1";
    }

    @Override
    public String getSolutions4AppStoreURL() {
        return "https://apps.apple.com/us/app/sail-insight-powered-by-sap/id1495355086";
    }

    @Override
    public String getSolutions4PlayStoreURL() {
        return "https://play.google.com/store/apps/details?id=org.sailyachtresearch.sailinsight";
    }

    @Override
    public String getSolutions5PlayStoreURL() {
        return "https://play.google.com/store/apps/details?id=com.sap.sailing.android.buoy.positioning.app";
    }

    @Override
    public String getSolution2ReadMoreLink() {
        return "/gwt/Home.html#WhatsNewPlace:navigationTab=SailingAnalytics";
    }

    @Override
    public String getSolutions3ReadMoreLink() {
        return "/gwt/Home.html#WhatsNewPlace:navigationTab=RaceManagerApp";
    }

    @Override
    public String getSolutions4ReadMoreLink() {
        return "https://sail-insight.com/";
    }

    @Override
    public String getSolutions5ReadMoreLink() {
        return "/gwt/Home.html#WhatsNewPlace:navigationTab=BuoyPingerApp";
    }

    @Override
    public String getSolutions6ReadMoreLink() {
        return "/gwt/Home.html#WhatsNewPlace:navigationTab=SailingSimulator";
    }
    
    @Override
    public String getMoreLoginInformationNotificationsURL() {
        return "/sap-branding/images/notifications.png";
    }
    
    @Override
    public String getMoreLoginInformationSettingsURL() {
        return "/sap-branding/images/settings.png";
    }
    
    @Override
    public String getMoreLoginInformationSailorProfilesURL() {
        return " /sap-branding/images/sailorprofiles.png";
    }
    
    @Override
    public String getMoreLoginInformationSimulatorURL() {
        return "/sap-branding/images/simulator.png";
    }
}
