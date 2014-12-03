package com.sap.sailing.gwt.ui.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class GlobalNavigationPanel extends FlowPanel {

    interface AnchorTemplates extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<a href=\"{0}\">{1}</a>")
        SafeHtml anchor(String url, String displayName);
    }
    
    public static final AnchorTemplates ANCHORTEMPLATE = GWT.create(AnchorTemplates.class);
    
    private final String styleName;

    /**
     * @param event an optional event; may be <code>null</code>.
     * @param styleNamePrefix an optional prefix for the style name; may be null - if not null the prefix
     *         is prepended to the style settings.
     */
    public GlobalNavigationPanel(StringMessages stringMessages, boolean showHomeNavigation, String leaderboardName,
            String leaderboardGroupName, EventDTO event, String styleNamePrefix) {
        super();
        if (styleNamePrefix != null && !styleNamePrefix.equalsIgnoreCase("")) {
            this.styleName = styleNamePrefix+"-globalNavigation";
        } else {
            this.styleName = "globalNavigation";
        }
        setStyleName(this.styleName);
        if (event != null  && event.getName() != null) {
            String eventLink = EntryPointLinkFactory.createEventLink(new HashMap<String, String>(), event.id.toString());
            addNavigationLink(event.getName(), eventLink, "event", stringMessages.goToEventOverview());
        } else {
            if (showHomeNavigation) {
                if (leaderboardGroupName != null && !leaderboardGroupName.isEmpty()) {
                    Map<String, String> leaderboardGroupLinkParameters = new HashMap<String, String>();
                    leaderboardGroupLinkParameters.put("showRaceDetails", "true");
                    leaderboardGroupLinkParameters.put("leaderboardGroupName", leaderboardGroupName);
                    String leaderBoardGroupLink = EntryPointLinkFactory.createLeaderboardGroupLink(leaderboardGroupLinkParameters);
                    addNavigationLink(leaderboardGroupName, leaderBoardGroupLink, "leaderBoardGroup", stringMessages.goToEventOverview());
                } else {
                    addNavigationLink(stringMessages.home(), "/", "home", stringMessages.goToEventOverview());
                }
            }
        }
        
        if (leaderboardName != null && !leaderboardName.isEmpty()) {
            Map<String, String> leaderboardLinkParameters = new HashMap<String, String>();
            leaderboardLinkParameters.put("showRaceDetails", "true");
            leaderboardLinkParameters.put("name", leaderboardName);
            if (leaderboardGroupName != null && !leaderboardGroupName.isEmpty()) {
                leaderboardLinkParameters.put("leaderboardGroupName", leaderboardGroupName);
            }
            String leaderBoardLink = EntryPointLinkFactory.createLeaderboardLink(leaderboardLinkParameters);
            addNavigationLink(leaderboardName, leaderBoardLink, "leaderBoard", stringMessages.goToOverviewAndSeeLeaderboard());
        }
        addLoginPanel();
    }

    private void addNavigationLink(String linkName, String linkUrl, String styleNameExtension, String htmlTitle) {
    	String setHtmlTitle = htmlTitle;
        HTML linkHtml = new HTML(ANCHORTEMPLATE.anchor(linkUrl, linkName));
        linkHtml.addStyleName(styleName+"-"+"Link");
        linkHtml.addStyleName(styleName+"-"+styleNameExtension);
        linkHtml.setTitle(setHtmlTitle);
        add(linkHtml);
    }
    
    private void addLoginPanel(){
        
    }
}
