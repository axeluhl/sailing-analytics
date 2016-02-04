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
import com.sap.sailing.gwt.common.authentication.FixedSailingAuthentication;
import com.sap.sailing.gwt.common.authentication.SAPHeaderWithAuthentication;
import com.sap.sailing.gwt.ui.client.AbstractSailingEntryPoint;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.security.ui.authentication.decorator.AuthorizedContentDecorator;
import com.sap.sse.security.ui.authentication.decorator.WidgetFactory;
import com.sap.sse.security.ui.authentication.generic.GenericAuthentication;
import com.sap.sse.security.ui.authentication.generic.GenericAuthorizedContentDecorator;

public class LeaderboardEditPage extends AbstractSailingEntryPoint {
    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();
        
        sailingService.getLeaderboardNames(new MarkedAsyncCallback<List<String>>(
                new AsyncCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> leaderboardNames) {
                final String leaderboardName = Window.Location.getParameter("name");
                if (leaderboardNames.contains(leaderboardName)) {
                    
                    SAPHeaderWithAuthentication header = initHeader();
                    GenericAuthentication genericSailingAuthentication = new FixedSailingAuthentication(getUserService(), header.getAuthenticationMenuView());
                    AuthorizedContentDecorator authorizedContentDecorator = new GenericAuthorizedContentDecorator(genericSailingAuthentication);
                    authorizedContentDecorator.setContentWidgetFactory(new WidgetFactory() {
                        @Override
                        public Widget get() {
                            EditableLeaderboardPanel leaderboardPanel = new EditableLeaderboardPanel(sailingService, new AsyncActionsExecutor(), leaderboardName, null,
                                    LeaderboardEditPage.this, getStringMessages(), userAgent);
                            leaderboardPanel.ensureDebugId("EditableLeaderboardPanel");
                            return leaderboardPanel;
                        }
                    });
                    
                    RootPanel.get().add(authorizedContentDecorator);
                    RootPanel.get().add(header);
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

    private SAPHeaderWithAuthentication initHeader() {
        Label title = new Label(getStringMessages().editScores());
        title.getElement().getStyle().setColor("white");
        title.getElement().getStyle().setFontSize(20, Unit.PX);
        title.getElement().getStyle().setFontWeight(FontWeight.BOLD);
        title.getElement().getStyle().setMarginTop(16, Unit.PX);
        
        SAPHeaderWithAuthentication header = new SAPHeaderWithAuthentication(getStringMessages().sapSailingAnalytics(), title);
        
        header.getElement().getStyle().setPosition(Position.FIXED);
        header.getElement().getStyle().setTop(0, Unit.PX);
        header.getElement().getStyle().setWidth(100, Unit.PCT);
        return header;
    }
}
