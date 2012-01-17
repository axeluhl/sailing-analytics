package com.sap.sailing.gwt.ui.raceboard;

import java.util.List;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;

public class RaceBoardEntryPoint extends AbstractEntryPoint {
    @Override
    public void onModuleLoad() {     
        super.onModuleLoad();
        sailingService.getLeaderboardNames(new AsyncCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> leaderboardNames) {
                String leaderboardName = Window.Location.getParameter("name");
                    LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(stringMessages);
                    logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
                    RaceBoardPanel raceBoardPanel = new RaceBoardPanel(sailingService, leaderboardName,
                            RaceBoardEntryPoint.this, stringMessages);
                    String padding = Window.Location.getParameter("padding");
                    if (padding != null && Boolean.valueOf(padding)) {
                        raceBoardPanel.addStyleName("leftPaddedPanel");
                    }
                    RootPanel.get().add(logoAndTitlePanel);
                    RootPanel.get().add(raceBoardPanel);
            }

            @Override
            public void onFailure(Throwable t) {
                reportError("Error trying to obtain list of leaderboard names: " + t.getMessage());
            }
        });
    }

}
