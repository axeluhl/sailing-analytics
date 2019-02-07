package com.sap.sailing.gwt.ui.leaderboardedit;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.common.authentication.FixedSailingAuthentication;
import com.sap.sailing.gwt.common.authentication.SAPSailingHeaderWithAuthentication;
import com.sap.sailing.gwt.common.communication.routing.ProvidesLeaderboardRouting;
import com.sap.sailing.gwt.settings.client.leaderboardedit.LeaderboardEditContextDefinition;
import com.sap.sailing.gwt.ui.client.AbstractSailingEntryPoint;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardEntryPoint;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTOWithSecurity;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.settings.SettingsToUrlSerializer;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.ui.authentication.decorator.AuthorizedContentDecorator;
import com.sap.sse.security.ui.authentication.decorator.WidgetFactory;
import com.sap.sse.security.ui.authentication.generic.GenericAuthentication;
import com.sap.sse.security.ui.authentication.generic.GenericAuthorizedContentDecorator;
import com.sap.sse.security.ui.authentication.generic.sapheader.SAPHeaderWithAuthentication;

public class LeaderboardEditPage extends AbstractSailingEntryPoint implements ProvidesLeaderboardRouting {
    private static final Logger logger = Logger.getLogger(LeaderboardEntryPoint.class.getName());
    private String leaderboardName;
    
    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();
        
        final LeaderboardEditContextDefinition settings = new SettingsToUrlSerializer()
                .deserializeFromCurrentLocation(new LeaderboardEditContextDefinition());
        leaderboardName = settings.getLeaderboardName();
        getSailingService().getLeaderboardNames(new MarkedAsyncCallback<List<String>>(
                new AsyncCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> leaderboardNames) {
                if (leaderboardNames.contains(leaderboardName)) {
                    getSailingService().getAvailableDetailTypesForLeaderboard(leaderboardName, null, new AsyncCallback<Iterable<DetailType>>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            logger.log(Level.SEVERE, "Could not load detailtypes", caught);
                        }

                        @Override
                        public void onSuccess(Iterable<DetailType> result) {
                            SAPHeaderWithAuthentication header = initHeader();
                            GenericAuthentication genericSailingAuthentication = new FixedSailingAuthentication(getUserService(), header.getAuthenticationMenuView());
                            AuthorizedContentDecorator authorizedContentDecorator = new GenericAuthorizedContentDecorator(genericSailingAuthentication);
                                            getSailingService().getLeaderboardWithSecurity(leaderboardName,
                                                    new AsyncCallback<StrippedLeaderboardDTOWithSecurity>() {

                                                        @Override
                                                        public void onFailure(Throwable caught) {
                                                            reportError(
                                                                    "Error trying to obtain list leaderboard with security: "
                                                                            + caught.getMessage());
                                                        }

                                                        @Override
                                                        public void onSuccess(
                                                                StrippedLeaderboardDTOWithSecurity leaderboardWithSecurity) {
                                                            authorizedContentDecorator.setPermissionToCheck(
                                                                    leaderboardWithSecurity, DefaultActions.UPDATE);
                                                            authorizedContentDecorator
                                                                    .setContentWidgetFactory(new WidgetFactory() {
                                                                        @Override
                                                                        public Widget get() {
                                                                            EditableLeaderboardPanel leaderboardPanel = new EditableLeaderboardPanel(
                                                                                    getSailingService(),
                                                                                    new AsyncActionsExecutor(),
                                                                                    leaderboardName, null,
                                                                                    LeaderboardEditPage.this,
                                                                                    getStringMessages(), userAgent,
                                                                                    result);
                                                                            leaderboardPanel.ensureDebugId(
                                                                                    "EditableLeaderboardPanel");
                                                                            return leaderboardPanel;
                                                                        }
                                                                    });

                                                            DockLayoutPanel mainPanel = new DockLayoutPanel(Unit.PX);
                                                            RootLayoutPanel.get().add(mainPanel);
                                                            mainPanel.addNorth(header, 75);
                                                            mainPanel.add(new ScrollPanel(authorizedContentDecorator));
                                                        }
                                                    });
                        }
                    });
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

    @Override
    public String getLeaderboardName() {
        return leaderboardName;
    }
}
