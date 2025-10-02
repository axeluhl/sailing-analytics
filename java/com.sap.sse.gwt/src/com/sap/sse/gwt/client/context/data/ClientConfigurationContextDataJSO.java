package com.sap.sse.gwt.client.context.data;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Access custom information for GWT client from static browser page.
 * 
 * @see com.sap.sse.gwt.client.context.impl.ClientConfigurationContextDataFactoryImpl
 * @see com.sap.sse.gwt.shared.ClientConfiguration
 * @author Georg Herdt
 *
 */
public class ClientConfigurationContextDataJSO extends JavaScriptObject {
    protected ClientConfigurationContextDataJSO() {
    }

    public final native boolean isDebrandingActive() /*-{
        return this.debrandingActive;
    }-*/;

    public final native String getId() /*-{
        return this.id;
    }-*/;

    public final native String getBrandTitle() /*-{
        return this.brandTitle;
    }-*/;

    public final native String getDefaultBrandingLogoURL() /*-{
        return this.defaultBrandingLogoURL;
    }-*/;

    public final native String getGreyTransparentLogoURL() /*-{
        return this.greyTransparentLogoURL;
    }-*/;

    public final native String getSoutionsInSailingImageURL() /*-{
        return this.solutionsInSailingImageURL;
    }-*/;

    public final native String getSolutionsInSailingTrimmedImageURL() /*-{
        return this.solutionsInSailingTrimmedImageURL;
    }-*/;

    public final native String getSailingRaceManagerAppTrimmedImageURL() /*-{
        return this.sailingRaceManagerAppTrimmedImageURL;
    }-*/;

    public final native String getSailingSimulatorTrimmedImageURL() /*-{
        return this.sailingSimulatorTrimmedImageURL;
    }-*/;

    public final native String getSailInSightAppImageURL() /*-{
        return this.sailInSightAppImageURL;
    }-*/;

    public final native String getSailingRaceManagerAppImageURL() /*-{
        return this.sailingRaceManagerAppImageURL;
    }-*/;

    public final native String getSailingSimulatorImageURL() /*-{
        return this.sailingSimulatorImageURL;
    }-*/;

    public final native String getBuoyPingerAppImageURL() /*-{
        return this.buoyPingerAppImageURL;
    }-*/;

    public final native String getSailingAnalyticsImageURL() /*-{
        return this.sailingAnalyticsImageURL;
    }-*/;

    public final native String getSailingAnalyticsReadMoreText() /*-{
        return this.sailingAnalyticsReadMoreText;
    }-*/;
    public final native String getSailingAnalyticsSailing() /*-{
        return this.sailingAnalyticsSailing;
    }-*/;
    public final native String getFooterCopyright() /*-{
        return this.footerCopyright;
    }-*/;
    public final native String getFooterLegalLink() /*-{
        return this.footerLegalLink;
    }-*/;
    public final native String getFooterPrivacyLink() /*-{
        return this.footerPrivacyLink;
    }-*/;
    public final native String getFooterJobsLink() /*-{
        return this.footerJobsLink;
    }-*/;
    public final native String getFooterSupportLink() /*-{
        return this.footerSupportLink;
    }-*/;
    public final native String getFooterWhatsNewLink() /*-{
        return this.footerWhatsNewLink;
    }-*/;
    public final native String getSportsOn() /*-{
        return this.sportsOn;
    }-*/;
    public final native String getFollowSports() /*-{
        return this.followSports;
    }-*/;
    public final native String getFacebookLink() /*-{
        return this.facebookLink;
    }-*/;
    public final native String getxLink() /*-{
        return this.xLink;
    }-*/;
    public final native String getInstagramLink() /*-{
        return this.instagramLink;
    }-*/;
    public final native String getWelcomeToSailingAnalytics()/*-{
        return this.welcomeToSailingAnalytics;
    }-*/;
    public final native String getWelcomeToSailingAnalyticsBody()/*-{
        return this.welcomeToSailingAnalyticsBody;
    }-*/;
    public final native String getSolutions1Headline() /*-{
        return this.solutions1Headline;
    }-*/;
    public final native String getSolutions2Headline() /*-{
        return this.solutions2Headline;
    }-*/;
    public final native String getSolutions3Headline() /*-{
        return this.solutions3Headline;
    }-*/;
    public final native String getSolutions4Headline() /*-{
        return this.solutions4Headline;
    }-*/;
    public final native String getSolutions5Headline() /*-{
        return this.solutions5Headline;
    }-*/;
    public final native String getSolutions6Headline() /*-{
        return this.solutions6Headline;
    }-*/;
    public final native String getSolutions1Title() /*-{
        return this.solutions1Title;
    }-*/;
    public final native String getContentSolutions11() /*-{
        return this.contentSolutions11;
    }-*/;
    public final native String getContentSolutions12() /*-{
        return this.contentSolutions12;
    }-*/;
    public final native String getContentSolutions13() /*-{
        return this.contentSolutions13;
    }-*/;
    public final native String getContentSolutions14() /*-{
        return this.contentSolutions14;
    }-*/;
    public final native String getContentSolutions15() /*-{
        return this.contentSolutions15;
    }-*/;
    public final native String getContentSolutions17() /*-{
        return this.contentSolutions17;
    }-*/;
    public final native String getContentSolutions18() /*-{
        return this.contentSolutions18;
    }-*/;
    public final native String getContentSolutions19() /*-{
        return this.contentSolutions19;
    }-*/;
    public final native String getContentSolutions110() /*-{
        return this.contentSolutions110;
    }-*/;
    public final native String getContentSolutions111() /*-{
        return this.contentSolutions111;
    }-*/;
    public final native String getContentSolutions112() /*-{
        return this.contentSolutions112;
    }-*/;
    public final native String getContentSolutions113() /*-{
        return this.contentSolutions113;
    }-*/;
    public final native String getSolutions2Title() /*-{
        return this.solutions2Title;
    }-*/;
    public final native String getContentSolutions21() /*-{
        return this.contentSolutions21;
    }-*/;
    public final native String getContentSolutions22() /*-{
        return this.contentSolutions22;
    }-*/;
    public final native String getSolutions3Title() /*-{
        return this.solutions3Title;
    }-*/;
    public final native String getContentSolutions3() /*-{
        return this.contentSolutions3;
    }-*/;
    public final native String getSolutions3ReadMore() /*-{
        return this.solutions3ReadMore;
    }-*/;
    public final native String getSolutions4Title() /*-{
        return this.solutions4Title;
    }-*/;
    public final native String getContentSolutions4() /*-{
        return this.contentSolutions4;
    }-*/;
    public final native String getSolutions4ReadMore() /*-{
        return this.solutions4ReadMore;
    }-*/;
    public final native String getSolutions5Title() /*-{
        return this.solutions5Title;
    }-*/;
    public final native String getContentSolutions5() /*-{
        return this.contentSolutions5;
    }-*/;
    public final native String getSolutions5ReadMore() /*-{
        return this.solutions5ReadMore;
    }-*/;
    public final native String getSolutions6Title() /*-{
        return this.solutions6Title;
    }-*/;
    public final native String getContentSolutions6() /*-{
        return this.contentSolutions6;
    }-*/;
    public final native String getSolutions6ReadMore() /*-{
        return this.solutions6ReadMore;
    }-*/;
    public final native String getSolutions3PlayStoreURL() /*-{
        return this.solutions3PlayStoreURL;
    }-*/;
    public final native String getSolutions4AppStoreURL() /*-{
        return this.solutions4AppStoreURL;
    }-*/;  
    public final native String getSolutions4PlayStoreURL() /*-{
        return this.solutions4PlayStoreURL;
    }-*/;
    public final native String getSolutions5PlayStoreURL() /*-{
        return this.solutions5PlayStoreURL;
    }-*/;
    public final native String getSolution2ReadMoreLink() /*-{
        return this.solution2ReadMoreLink;
    }-*/;
    public final native String getSolutions3ReadMoreLink() /*-{
        return this.solutions3ReadMoreLink;
    }-*/;
    public final native String getSolutions4ReadMoreLink() /*-{
        return this.solutions4ReadMoreLink;
    }-*/;
    public final native String getSolutions5ReadMoreLink() /*-{
        return this.solutions5ReadMoreLink;
    }-*/;
    public final native String getSolutions6ReadMoreLink() /*-{
        return this.solutions6ReadMoreLink;
    }-*/;
    public final native String getMoreLoginInformationNotificationsURL() /*-{
        return this.moreLoginInformationNotificationsURL;
    }-*/;
    public final native String getMoreLoginInformationSettingsURL() /*-{
        return this.moreLoginInformationSettingsURL;
    }-*/;
    public final native String getMoreLoginInformationSailorProfilesURL() /*-{
        return this.moreLoginInformationSailorProfilesURL;
    }-*/;
    public final native String getMoreLoginInformationSimulatorURL() /*-{
        return this.moreLoginInformationSimulatorURL;
    }-*/;
}