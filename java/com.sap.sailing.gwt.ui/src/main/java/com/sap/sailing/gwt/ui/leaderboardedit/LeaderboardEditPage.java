package com.sap.sailing.gwt.ui.leaderboardedit;

import java.util.List;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sailing.gwt.ui.actions.AsyncActionsExecutor;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;
import com.sap.sailing.gwt.ui.client.MarkedAsyncCallback;

public class LeaderboardEditPage extends AbstractEntryPoint {
    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();
        sailingService.getLeaderboardNames(new MarkedAsyncCallback<List<String>>() {
            @Override
            public void handleSuccess(List<String> leaderboardNames) {
                String leaderboardName = Window.Location.getParameter("name");
                if (leaderboardNames.contains(leaderboardName)) {
                    LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(stringMessages.editScores(), stringMessages, LeaderboardEditPage.this);
                    logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
                    // TODO: Here happens something async. We have to use the semaphore for ui tests.
                    EditableLeaderboardPanel leaderboardPanel = new EditableLeaderboardPanel(sailingService, new AsyncActionsExecutor(), leaderboardName, null,
                            LeaderboardEditPage.this, stringMessages, userAgent);
                    leaderboardPanel.ensureDebugId("EditableLeaderboardPanel");
                    RootPanel.get().add(logoAndTitlePanel);
                    RootPanel.get().add(leaderboardPanel);
                } else {
                    RootPanel.get().add(new Label(stringMessages.noSuchLeaderboard()));
                }
            }
            @Override
            public void handleFailure(Throwable t) {
                reportError("Error trying to obtain list of leaderboard names: "+t.getMessage());
            }
        });
    }

}
