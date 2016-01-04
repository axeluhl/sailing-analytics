package com.sap.sailing.gwt.ui.leaderboardedit;

import java.util.List;

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.AbstractSailingEntryPoint;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.theme.client.component.sapheader2.SAPHeader2;

public class LeaderboardEditPage extends AbstractSailingEntryPoint {
    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();
        
        sailingService.getLeaderboardNames(new MarkedAsyncCallback<List<String>>(
                new AsyncCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> leaderboardNames) {
                String leaderboardName = Window.Location.getParameter("name");
                if (leaderboardNames.contains(leaderboardName)) {
                    EditableLeaderboardPanel leaderboardPanel = new EditableLeaderboardPanel(sailingService, new AsyncActionsExecutor(), leaderboardName, null,
                            LeaderboardEditPage.this, getStringMessages(), userAgent);
                    leaderboardPanel.ensureDebugId("EditableLeaderboardPanel");
                    RootPanel.get().add(initHeader());
                    RootPanel.get().add(leaderboardPanel);
                } else {
                    RootPanel.get().add(new Label(getStringMessages().noSuchLeaderboard()));
                }
            }
            @Override
            public void onFailure(Throwable t) {
                reportError("Error trying to obtain list of leaderboard names: "+t.getMessage());
            }
        }));
    }

    private Widget initHeader() {
        Label title = new Label(getStringMessages().editScores());
        title.getElement().getStyle().setColor("white");
        title.getElement().getStyle().setFontSize(20, Unit.PX);
        title.getElement().getStyle().setFontWeight(FontWeight.BOLD);
        title.getElement().getStyle().setMarginTop(16, Unit.PX);
        SAPHeader2 header = new SAPHeader2(getStringMessages().sapSailingAnalytics(), title, false);
        header.getElement().getStyle().setPosition(Position.FIXED);
        header.getElement().getStyle().setTop(0, Unit.PX);
        header.getElement().getStyle().setWidth(100, Unit.PCT);
        header.getElement().getStyle().setZIndex(19);
        return header;
    }
}
