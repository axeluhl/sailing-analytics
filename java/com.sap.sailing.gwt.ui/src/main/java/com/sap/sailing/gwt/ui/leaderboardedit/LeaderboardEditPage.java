package com.sap.sailing.gwt.ui.leaderboardedit;

import java.util.List;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.security.Permission;
import com.sap.sailing.gwt.common.authentication.FixedSailingAuthentication;
import com.sap.sailing.gwt.common.authentication.SAPSailingHeaderWithAuthentication;
import com.sap.sailing.gwt.settings.client.leaderboardedit.LeaderboardEditContextDefinition;
import com.sap.sailing.gwt.ui.client.AbstractSailingEntryPoint;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.settings.SettingsToUrlSerializer;
import com.sap.sse.security.ui.authentication.decorator.AuthorizedContentDecorator;
import com.sap.sse.security.ui.authentication.decorator.WidgetFactory;
import com.sap.sse.security.ui.authentication.generic.GenericAuthentication;
import com.sap.sse.security.ui.authentication.generic.GenericAuthorizedContentDecorator;
import com.sap.sse.security.ui.authentication.generic.sapheader.SAPHeaderWithAuthentication;

public class LeaderboardEditPage extends AbstractSailingEntryPoint {
    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();
        
        sailingService.getLeaderboardNames(new MarkedAsyncCallback<List<String>>(
                new AsyncCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> leaderboardNames) {
                final LeaderboardEditContextDefinition settings = new SettingsToUrlSerializer()
                        .deserializeFromCurrentLocation(new LeaderboardEditContextDefinition());
                final String leaderboardName = settings.getLeaderboardName();
                if (leaderboardNames.contains(leaderboardName)) {
                    
                    SAPHeaderWithAuthentication header = initHeader();
                    GenericAuthentication genericSailingAuthentication = new FixedSailingAuthentication(getUserService(), header.getAuthenticationMenuView());
                    AuthorizedContentDecorator authorizedContentDecorator = new GenericAuthorizedContentDecorator(genericSailingAuthentication);
                    authorizedContentDecorator.setPermissionToCheck(Permission.MANAGE_LEADERBOARD_RESULTS);
                    authorizedContentDecorator.setContentWidgetFactory(new WidgetFactory() {
                        @Override
                        public Widget get() {
                            EditableLeaderboardPanel leaderboardPanel = new EditableLeaderboardPanel(sailingService, new AsyncActionsExecutor(), leaderboardName, null,
                                    LeaderboardEditPage.this, getStringMessages(), userAgent);
                            leaderboardPanel.ensureDebugId("EditableLeaderboardPanel");
                            return leaderboardPanel;
                        }
                    });
                    
                    DockLayoutPanel mainPanel = new DockLayoutPanel(Unit.PX);
                    RootLayoutPanel.get().add(mainPanel);
                    mainPanel.addNorth(header, 75);
                    mainPanel.add(new ScrollPanel(authorizedContentDecorator));
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
        SAPSailingHeaderWithAuthentication header = new SAPSailingHeaderWithAuthentication(getStringMessages().editScores());
//        header.getElement().getStyle().setPosition(Position.FIXED);
//        header.getElement().getStyle().setTop(0, Unit.PX);
        header.getElement().getStyle().setWidth(100, Unit.PCT);
        return header;
    }
}
