package com.sap.sailing.gwt.ui.leaderboardedit;

import java.util.List;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sailing.gwt.ui.actions.AsyncActionsExecutor;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;

public class LeaderboardEditPage extends AbstractEntryPoint {
    @Override
    public void onModuleLoad() {
        super.onModuleLoad();
        sailingService.getLeaderboardNames(new AsyncCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> leaderboardNames) {
                String leaderboardName = Window.Location.getParameter("name");
                if (leaderboardNames.contains(leaderboardName)) {
                    LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(stringMessages.editScores(), stringMessages, isSmallWidth());
                    logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
                    EditableLeaderboardPanel leaderboardPanel = new EditableLeaderboardPanel(sailingService, new AsyncActionsExecutor(), leaderboardName, null,
                            LeaderboardEditPage.this, stringMessages, userAgent);
                    RootPanel.get().add(logoAndTitlePanel);
                    RootPanel.get().add(leaderboardPanel);
                } else {
                    RootPanel.get().add(new Label(stringMessages.noSuchLeaderboard()));
                }
            }
            @Override
            public void onFailure(Throwable t) {
                reportError("Error trying to obtain list of leaderboard names: "+t.getMessage());
            }
        });
    }

}
