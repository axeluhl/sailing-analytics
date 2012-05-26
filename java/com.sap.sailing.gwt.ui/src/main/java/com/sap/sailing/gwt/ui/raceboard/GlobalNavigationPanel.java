package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.URLFactory;

public class GlobalNavigationPanel extends FlowPanel {

    private final String debugParam;

    interface AnchorTemplates extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<a href=\"{0}\">{1}</a>")
        SafeHtml anchor(String url, String displayName);
    }
    
    public static final AnchorTemplates ANCHORTEMPLATE = GWT.create(AnchorTemplates.class);
    public static final String STYLE_NAME_PREFIX = "globalNavigation-";

    public GlobalNavigationPanel(StringMessages stringMessages, boolean showHomeNavigation, String leaderboardName, String leaderboardGroupName) {
        super();
        
        setStyleName("globalNavigation");
        
        debugParam = Window.Location.getParameter("gwt.codesvr");

        String spectatorViewLink = "/gwt/Spectator.html";
        String leaderboardViewLink = "/gwt/Leaderboard.html";
    
        String homeLink = spectatorViewLink;
        
        if(showHomeNavigation) {
            if (leaderboardGroupName != null && !leaderboardGroupName.isEmpty()) {
                String leaderBoardGroupLink = spectatorViewLink + "?leaderboardGroupName=" + leaderboardGroupName; 
                addNavigationLink(leaderboardGroupName, leaderBoardGroupLink, "leaderBoardGroup");
            } else {
                addNavigationLink(stringMessages.home(), homeLink, "home");
            }
        }
        
        if (leaderboardName != null && !leaderboardName.isEmpty()) {
            String leaderBoardLink = leaderboardViewLink + "?name=" + leaderboardName;
            if (leaderboardGroupName != null && !leaderboardGroupName.isEmpty()) {
                leaderBoardLink += "&leaderboardGroupName=" + leaderboardGroupName;
            }
            addNavigationLink(leaderboardName, leaderBoardLink, "leaderBoard");
        }        
    }

    private void addNavigationLink(String linkName, String linkUrl, String styleNameExtension) {
        String url = linkUrl;
        if(debugParam != null && !debugParam.isEmpty()) {
            url += url.contains("?") ? "&" : "?";
            url += "gwt.codesvr=" + debugParam;
        }
        
        HTML linkHtml = new HTML(ANCHORTEMPLATE.anchor(URLFactory.INSTANCE.encode(url), linkName));
        linkHtml.addStyleName(STYLE_NAME_PREFIX + styleNameExtension);
        add(linkHtml);
    }
}
