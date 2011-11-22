package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

public class LeaderboardPage extends AbstractEntryPoint {
    @Override
    public void onModuleLoad() {
        super.onModuleLoad();
        sailingService.getLeaderboardNames(new AsyncCallback<List<String>>() {

            @Override
            public void onSuccess(List<String> leaderboardNames) {

                String leaderboardName = Window.Location.getParameter("name");

                if (leaderboardNames.contains(leaderboardName)) {

                    LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(stringConstants);
                    logoAndTitlePanel.addStyleName("LogoAndTitlePanel");

                    LeaderboardPanel leaderboardPanel = new LeaderboardPanel(sailingService, leaderboardName,
                            LeaderboardPage.this, stringConstants);

                    String padding = Window.Location.getParameter("padding");

                    if (padding != null && Boolean.valueOf(padding)) {
                        leaderboardPanel.addStyleName("leftPaddedPanel");
                    }

                    RootPanel.get().add(logoAndTitlePanel);
                    RootPanel.get().add(leaderboardPanel);

                } else {
                    RootPanel.get().add(new Label(stringConstants.noSuchLeaderboard()));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                reportError("Error trying to obtain list of leaderboard names: " + t.getMessage());
            }
        });
    }

}
