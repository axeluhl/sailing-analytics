package com.sap.sailing.gwt.ui.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;

public class GlobalNavigationPanel extends FlowPanel {

    interface AnchorTemplates extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<a href=\"{0}\">{1}</a>")
        SafeHtml anchor(String url, String displayName);
    }
    
    public static final AnchorTemplates ANCHORTEMPLATE = GWT.create(AnchorTemplates.class);
    public static final String STYLE_NAME_PREFIX = "globalNavigation-";

    public GlobalNavigationPanel(StringMessages stringMessages, boolean showHomeNavigation, String leaderboardName, String leaderboardGroupName) {
        super();
        setStyleName("globalNavigation");
        if (showHomeNavigation) {
            if (leaderboardGroupName != null && !leaderboardGroupName.isEmpty()) {
                Map<String, String> leaderboardGroupLinkParameters = new HashMap<String, String>();
                leaderboardGroupLinkParameters.put("showRaceDetails", "true");
                leaderboardGroupLinkParameters.put("leaderboardGroupName", leaderboardGroupName);
                String leaderBoardGroupLink = EntryPointLinkFactory.createLeaderboardGroupLink(leaderboardGroupLinkParameters);
                addNavigationLink(leaderboardGroupName, leaderBoardGroupLink, "leaderBoardGroup", "Go to the Event overview.");
            } else {
                addNavigationLink(stringMessages.home(), "/", "home", "Go to the Event overview");
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
            addNavigationLink(leaderboardName, leaderBoardLink, "leaderBoard", "Go to the overview and see all Races in one Leaderboard");
        }        
    }

    private void addNavigationLink(String linkName, String linkUrl, String styleNameExtension, String htmlTitle) {
    	String setHtmlTitle = htmlTitle;
        HTML linkHtml = new HTML(ANCHORTEMPLATE.anchor(linkUrl, linkName));
        linkHtml.addStyleName("globalNavigationLink");
        linkHtml.addStyleName(STYLE_NAME_PREFIX + styleNameExtension);
        linkHtml.setTitle(setHtmlTitle);
        add(linkHtml);
    }
}
